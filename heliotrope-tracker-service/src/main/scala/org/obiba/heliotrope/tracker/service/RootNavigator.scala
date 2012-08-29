package org.obiba.heliotrope.tracker.service

import cc.spray._
import directives._

/**
 * A simple navigator for the root resource /, which simply handles the top level of
 * the routing framework. Since almost everything is scoped to a study, access at
 * this level is limited. 
 */
trait RootNavigator extends Navigator {
  
  def route(resource: StudyResource): Route

  /**
   * Define the route for the root resource. 
   */
  def route(resource: RootResource): Route = {
    path("") {
      get {
        _.complete("Say / to Spray!")
      }
    } ~
    pathPrefix("study") {
      route(StudyResource())
    }
  }
}