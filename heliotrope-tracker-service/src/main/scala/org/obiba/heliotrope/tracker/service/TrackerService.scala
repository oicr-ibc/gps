package org.obiba.heliotrope.tracker.service

import cc.spray._
import directives._
import org.obiba.heliotrope.tracker._
import akka.actor.ActorSystem
import cc.spray.typeconversion.SprayJsonSupport

/**
 * The final assembled service, which collects together all the RootNavigator traits and all the expected
 * marshalling components, and establishes an initial dispatch to the route handling for the RootResource.
 */
trait TrackerService extends RootNavigator 
	with StudyNavigator 
	with SubjectNavigator 
	with SampleNavigator 
	with ProtocolNavigator 
	with StepNavigator {
  
  def actorSystem: ActorSystem
  
//  /**
//   * Implicit definition of the required marshalling components.
//   */
  
  /**
   * Starts the initial route dispatch process. 
   */
  def trackerService: Route = {
    route(RootResource())
  }
}
