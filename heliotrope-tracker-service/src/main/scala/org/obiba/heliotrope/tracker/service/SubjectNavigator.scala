package org.obiba.heliotrope.tracker.service

import cc.spray._
import directives._
import org.obiba.heliotrope.tracker.Subject
import net.liftweb.json.JsonAST.JObject

/**
 * Route navigation trait for a subject. This handles routing logic associated 
 * with the .../subject/... components of a URL. These are applied to studies. 
 */
trait SubjectNavigator extends Navigator {
    
  /**
   * Regular expression for a subject name. 
   */
  val subjectNameMatcher = new SimpleRegexMatcher("""[\w-]+""".r)
    
  def route(resource: SampleResource): Route
  def route(resource: StepResource): Route

  /**
   * Define the route for all subjects. 
   */
  def route(resource: SubjectResource): Route = {
    pathEnd {
      get {
        handleRequest[List[Subject]](resource)
      } ~
      post {
        content(as[JObject]) { value =>
          handlePostRequest[Subject](value, resource)
        }
      }
    } ~
    pathPrefix(subjectNameMatcher) { string =>
      val labelledResource = SubjectResource(resource.resource, id = LabelIdentifier(string))
      
      pathEnd {
        get {
          handleRequest[Subject](labelledResource)
        } ~
        put {
          content(as[JObject]) { value =>
            handlePutRequest[Subject](value, labelledResource)
          }
        }
      } ~
      pathPrefix("sample") {
        route(SampleResource(resource = labelledResource))
      } ~
      pathPrefix("step") {
        route(StepResource(resource = labelledResource))
      }
    }
  }
}
