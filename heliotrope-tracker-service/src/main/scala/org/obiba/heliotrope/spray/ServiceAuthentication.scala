package org.obiba.heliotrope.spray

import cc.spray._
import akka.event.Logging
import akka.dispatch.{Future, ExecutionContext}
import akka.actor.{Props, ActorSystem}
import org.obiba.heliotrope.tracker.User

trait ServiceAuthentication {

  implicit def actorSystem: ActorSystem
    
  val authenticator: GeneralAuthenticator[User]
   
  val authorizor: RequestContext => Boolean
}