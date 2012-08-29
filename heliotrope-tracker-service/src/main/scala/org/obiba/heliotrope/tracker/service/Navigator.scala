package org.obiba.heliotrope.tracker.service

import cc.spray._
import directives._
import org.obiba.heliotrope.tracker.{Study, User}
import akka.actor.Props
import cc.spray.typeconversion._
import cc.spray.http._
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.actor.ActorSystem
import akka.event.Logging
import cc.spray.http.HttpResponse
import MediaTypes._
import net.liftweb.json._
import org.obiba.heliotrope.spray.PrettyLiftJsonSupport

/**
 * Base trait for all navigation components. For a complex routing framework, a set of
 * these will be created, each setting up expectations on one another. They can then be
 * mixed together to create the final service. For convenience, we define a few useful 
 * service methods. 
 */

trait Navigator extends Directives with PrettyLiftJsonSupport {
  
  def actorSystem: ActorSystem
  
  implicit val formats = DefaultFormats
  
  val log = Logging(actorSystem, "Navigator")
    
  /**
   * Define the expected authenticator component
   */
  val authenticator: GeneralAuthenticator[User]
  
  /**
   * Define the expected authorization function
   */
  val authorizor: RequestContext => Boolean

  /**
   * A lazy reference to a StudyDatabaseService actor, which is where all requests
   * will be sent following the completion of the routing logic. 
   */
  lazy val studyDatabaseService = actorSystem.actorOf(Props[StudyDatabaseService])
  
  /**
   * A convenient directive, essentially a shortcut for path("") { ... } which makes
   * the application at the end of the route URL more explicit. 
   */
  def pathEnd(route: Route): Route = {
    path("") {
      route
    }
  }
  
  /**
   * Set up a timeout of five seconds for a request. This should be enough for all
   * requests. 
   */
  implicit val timeout = Timeout(5 seconds)
  
  private def transformResponse(parameters: Seq[ResponseParameter]): HttpResponse => HttpResponse = { response =>
    response
  }
  
  /**
   * Returns the appropriate status code based on the parameters passed. 
   */
  private def getStatusCode(parameters: Seq[ResponseParameter]): StatusCode = {
    parameters.collectFirst { 
      case p: ResponseParameterCreated => StatusCodes.Created
    }.getOrElse(StatusCodes.OK)
  }
  
  /**
   * An even more generic request handler, which passes any type of request for
   * execution outside the HttpService actor. This implements handleRequest,
   * handlePutRequest, and handlePostRequest. 
   */
  private def handleRequest[T <: AnyRef](ctx: RequestContext, request: Request) = {
    
    val future = ask(studyDatabaseService, request).mapTo[AnyRef]
    
    future onComplete {

      // This will be a Response. All response some with parameters, and we need to process these somehow. 
      case Right(result) => 
        result match {
          case ValueResponse(value, parameters) =>
            ctx.withResponseTransformed(transformResponse(parameters)).complete(getStatusCode(parameters), value)
          case RejectionResponse(rejection, parameters) =>
            ctx.reject(rejection)
          case MissingResourceResponse(resource, parameters) =>
            ctx.complete(StatusCodes.NotFound, resource.toString())
          case ExceptionResponse(exception, parameters) =>
            ctx.complete(StatusCodes.InternalServerError, exception.toString())
        }
        
      // And this should be an exception of some kind. We never want these, so we can
      // 500 the whole lot of them.
      case Left(failure) =>
        ctx.complete(StatusCodes.InternalServerError, failure.toString + " " + request.resource.toString())
        
    }
  }
  
  /**
   * A generic request handler for GET requests. This can be used instead of
   * _.complete within a route, but does considerably more. It sends the request
   * as a case object to an actor to handle the request, and uses a future with
   * an onComplete handler to wait for a result. Completion is called when that
   * result is received. The net result of this is to get the slow processing
   * out of the HttpService actor and allow it to happen elsewhere. 
   */
  def handleRequest[T <: AnyRef](resource: Resource): Route = { ctx =>
    val request = GetRequest(resource.withOptions(ctx.request.queryParams))
    handleRequest[T](ctx, request)
  }
  
  /**
   * A generic request handler for PUT requests. This is essentially the same as 
   * handleRequest, but also allows a value to be passed into the request. This
   * will typically have been deserialized from the request body. 
   */
  def handlePutRequest[T <: AnyRef](value: JObject, resource: Resource): Route = { ctx =>
    val request = PutRequest(value, resource.withOptions(ctx.request.queryParams))
    handleRequest[T](ctx, request)
  }

  /**
   * A generic request handler for POST requests. This is essentially the same as 
   * handleRequest, but also allows a value to be passed into the request. This
   * will typically have been deserialized from the request body. 
   */
  def handlePostRequest[T <: AnyRef](value: JObject, resource: Resource): Route = { ctx =>
    val request = PostRequest(value, resource.withOptions(ctx.request.queryParams))
    handleRequest[T](ctx, request)
  }
}