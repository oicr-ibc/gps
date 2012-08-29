package org.obiba.heliotrope.tracker.service

import cc.spray._
import directives._
import org.obiba.heliotrope.tracker.Protocol

/**
 * Route navigation trait for a subject. This handles routing logic associated 
 * with the .../subject/... components of a URL. These are applied to studies. 
 */
trait ProtocolNavigator extends Navigator {
    
  /**
   * Regular expression for a subject name. 
   */
  val protocolNameMatcher = new SimpleRegexMatcher("""[\w-]+""".r)
    
  /**
   * Define the route for all subjects. 
   */
  def route(resource: ProtocolResource): Route = {
    pathEnd {
      get {
        handleRequest[List[Protocol]](resource)
      }
    } ~
    pathPrefix(protocolNameMatcher) { string =>
      val labelledResource = ProtocolResource(resource.resource, id = LabelIdentifier(string))
      
      pathEnd {
        get {
          handleRequest[Protocol](labelledResource)
        }
      }
    }
  }
}
