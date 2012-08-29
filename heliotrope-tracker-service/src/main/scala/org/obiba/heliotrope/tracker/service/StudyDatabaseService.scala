package org.obiba.heliotrope.tracker.service

import akka.actor.Actor
import akka.event.Logging

import org.obiba.heliotrope.tracker._
import org.obiba.heliotrope.util._

/**
 * This actor handles requests from the routing logic. At this stage, the resource request
 * has been transformed into an appropriate case object, and this actor services requests
 * using these encoded case objects, which also include a completer function which does
 * appropriate serialization for the response. 
 */
class StudyDatabaseService extends Actor {
  
  val log = Logging(context.system, this)
  
  def withRequestHandler(body: => AnyRef): Unit = { 
    try {
      val result = body
      sender ! result
    } catch {
      case e: Exception => 
        sender ! akka.actor.Status.Failure(e)
        throw e
    }
  }
  
  /**
   * The actor's receive method, listening on its request queue, and dispatching 
   * requests accordingly. 
   */
  // See: http://doc.akka.io/docs/akka/2.0/scala/actors.html
  def receive = {
    case msg: GetRequest => {
      log.debug("GET request: " + msg.resource.toString())
      withRequestHandler {
        msg.resource.evaluate()
      }
    }
    case msg: PutRequest => {
      log.debug("PUT request: " + msg.resource.toString() + " with value: " + msg.value.toString())
      withRequestHandler {
        msg.resource.update(msg.value) 
      }
    }
    case msg: PostRequest => {
      log.debug("POST request: " + msg.resource.toString() + " with value: " + msg.value.toString())
      withRequestHandler {
        msg.resource.store(msg.value) 
      }
    }
  }
}
