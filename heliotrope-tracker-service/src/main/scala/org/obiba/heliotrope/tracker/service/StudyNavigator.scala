package org.obiba.heliotrope.tracker.service

import cc.spray._
import directives._
import org.obiba.heliotrope.tracker._
import cc.spray.authentication.LdapAuthenticator
import cc.spray.authentication.BasicHttpAuthenticator
import akka.dispatch.Future
import cc.spray.authentication.FromConfigUserPassAuthenticator
import net.liftweb.json.JsonAST.JObject

/**
 * Route navigation trait for a study. This handles routing logic associated 
 * with the /study/... components of a URL. 
 */
trait StudyNavigator extends Navigator {
  
  /**
   * Regular expression for a study name. 
   */
  val studyNameMatcher = new SimpleRegexMatcher("""[\w-]+""".r)
  
  def route(resource: ProtocolResource): Route
  def route(resource: SubjectResource): Route
  def route(resource: SampleResource): Route
  
//  def route(resource: AllSamplesResource): Route
  
  /**
   * Handles routes for an identified study.
   */
    
  /**
   * Handles routes for all studies. 
   */
  def route(resource: StudyResource): Route = {
    authenticate(authenticator) { user =>
      authorize(authorizor) {
        pathEnd {
          get {
            handleRequest[List[Study]](resource)
          } ~
          post {
            content(as[JObject]) { value =>
              handlePostRequest[Study](value, resource)
            }
          }
        } ~
        pathPrefix(studyNameMatcher) { string =>
          val labelledResource = StudyResource(LabelIdentifier(string))
          pathEnd {
            get {
              handleRequest[Study](labelledResource)
            } ~
            put {
              content(as[JObject]) { value =>
                handlePutRequest[Study](value, labelledResource)
              }
            }
          } ~
          pathPrefix("subject") {
            route(SubjectResource(resource = labelledResource))
          } ~
          pathPrefix("sample") {
            route(SampleResource(resource = labelledResource))
          } ~
          pathPrefix("protocol") {
            route(ProtocolResource(resource = labelledResource))
          }
        }
      }
    }
  }

  /**
   * Handles routes for all protocols in a study. 
   */
//  def route(resource: AllProtocolsResource): Route = {
//    _.reject()
//    path("") {
//      get {
//        resource.resource match {
//          case inner: IdentifiedStudyResource =>
//          	handleRequest[List[StepProtocol]](resource)
//          case _ =>
//            handleRequest[List[StepProtocol]](resource)
//        }
//      }
//    } ~
//    pathPrefix(protocolNameMatcher) { string =>
//      route(IdentifiedProtocolResource(resource.resource, Identifier(string)))
//    }
//  }

  /**
   * Handles routes for an identified protocol in a study . 
   */
//  def route(resource: IdentifiedProtocolResource): Route = {
//    _.reject()
//    path("") {
//      get {
//        resource.resource match {
//          case inner: IdentifiedStudyResource =>
//          	handleRequest[StepProtocol](resource)
//          case _ =>
//            handleRequest[StepProtocol](resource)
//        }
//      }
//    }
//  }

}