package org.obiba.heliotrope.tracker.service

import cc.spray._
import directives._
import org.obiba.heliotrope.tracker.Sample
import net.liftweb.json.JsonAST.JObject

/**
 * Route navigation trait for a subject. This handles routing logic associated 
 * with the .../subject/... components of a URL. These are applied to studies. 
 */
trait SampleNavigator extends Navigator {
    
  /**
   * Regular expression for a sample name. 
   */
  val sampleNameMatcher = new SimpleRegexMatcher("""[\w-]+""".r)
    
  def route(resource: StepResource): Route

  /**
   * Define the route for all samples. 
   */
  def route(resource: SampleResource): Route = {
    pathEnd {
      get {
        handleRequest[List[Sample]](resource)
      } ~
      post {
        content(as[JObject]) { value =>
          handlePostRequest[Sample](value, resource)
        }
      }
    } ~
    pathPrefix(sampleNameMatcher) { string =>
      val labelledResource = SampleResource(resource.resource, id = LabelIdentifier(string))
      
      pathEnd {
        get {
          handleRequest[Sample](labelledResource)
        } ~
        put {
          content(as[JObject]) { value =>
            handlePutRequest[Sample](value, labelledResource)
          }
        }
      } ~
      pathPrefix("step") {
        route(StepResource(resource = labelledResource))
      }
    }
  }
}

//package org.obiba.heliotrope.tracker.service
//
//import cc.spray._
//import directives._
//import org.obiba.heliotrope.tracker.Sample
//import cc.spray.http.HttpMethods
//
///**
// * Route navigation trait for a subject. This handles routing logic associated 
// * with the .../subject/... components of a URL. These are applied to studies. 
// */
//trait SampleNavigator extends Navigator {
//    
//  /**
//   * Regular expression for a sample name. 
//   */
//  val sampleNameMatcher = new SimpleRegexMatcher("""[\w-]+""".r)
//
//  /**
//   * Define the dependency on route handling for steps
//   */
//  def route(resource: AllStepsResource): Route
//
//  /**
//   * Define the route for all samples. 
//   */
//  def route(resource: AllSamplesResource): Route = {
//    pathEnd {
//      get {
//        handleRequest[List[Sample]](resource)
//      } ~
//      post {
//        resource.resource match {
//          case r: IdentifiedSubjectResource =>
//            content(as[Sample]) { sample =>
//              handlePostRequest[Sample](sample, resource)
//            }
//          case _ => reject(MethodRejection(HttpMethods.GET))
//        }      
//      }
//    } ~
//    pathPrefix(sampleNameMatcher) { string =>
//      route(IdentifiedSampleResource(resource.resource, Identifier(string)))
//    }
//  }
//  
//  /**
//   * Define the route for an identified sample. 
//   */
//  def route(resource: IdentifiedSampleResource): Route = {
//    pathEnd {
//      get {
//        handleRequest[Sample](resource)
//      } ~
//      put {
//        resource.resource match {
//          case r: IdentifiedSubjectResource =>
//            content(as[Sample]) { sample =>
//              handlePutRequest[Sample](sample, resource)
//            }
//          case _ => reject(MethodRejection(HttpMethods.GET))
//        }
//      }
//    } ~
//    pathPrefix("step") {
//      route(AllStepsResource(resource))
//    }
//  }
//}
