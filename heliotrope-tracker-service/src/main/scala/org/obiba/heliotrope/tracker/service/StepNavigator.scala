package org.obiba.heliotrope.tracker.service

import cc.spray._
import directives._
import org.obiba.heliotrope.tracker.Step

/**
 * Route navigation trait for a subject. This handles routing logic associated 
 * with the .../subject/... components of a URL. These are applied to studies. 
 */
trait StepNavigator extends Navigator {
    
  /**
   * Regular expression for a step name - this is the name of the protocol, so
   * we refer to that. 
   */
  val protocolNameMatcher: SimpleRegexMatcher
    
  /**
   * Define the route for all subjects. 
   */
  def route(resource: StepResource): Route = {
    pathEnd {
      get {
        handleRequest[List[Step]](resource)
      }
    } ~
    pathPrefix(protocolNameMatcher) { string =>
      val labelledResource = StepResource(resource.resource, id = LabelIdentifier(string))
      
      pathEnd {
        get {
          handleRequest[Step](labelledResource)
        }
      }
    }
  }
}



//package org.obiba.heliotrope.tracker.service
//
//import cc.spray._
//import directives._
//import org.obiba.heliotrope.tracker.{StepProcess, StepProtocol}
//
///**
// * Route navigation trait for a steps. This handles routing logic associated 
// * with the .../step/... components of a URL. 
// */
//trait StepNavigator extends Navigator {
//    
//  /**
//   * Regular expression for a step name. 
//   */
//  val stepNameMatcher = new SimpleRegexMatcher("""[\w-]+""".r)
//  
//  /**
//   * Handles routes for an identified step, either in a study or on something else,
//   * e.g., a sample or a subject. 
//   */
//  def route(resource: IdentifiedStepResource): Route = {
//    path("") {
//      get {
//        resource.resource match {
//          case inner: IdentifiedStudyResource =>
//          	handleRequest[StepProtocol](resource)
//          case _ =>
//            handleRequest[StepProcess](resource)
//        }
//      }
//    }
//  }
//
//  /**
//   * Handles routes for all steps, either in a study or on something else,
//   * e.g., a sample or a subject. 
//   */
//  def route(resource: AllStepsResource): Route = {
//    path("") {
//      get {
//        resource.resource match {
//          case inner: IdentifiedStudyResource =>
//          	handleRequest[List[StepProcess]](resource)
//          case _ =>
//            handleRequest[List[StepProcess]](resource)
//        }
//      }
//    } ~
//    pathPrefix(stepNameMatcher) { string =>
//      route(IdentifiedStepResource(resource.resource, Identifier(string)))
//    }
//  }
//}
