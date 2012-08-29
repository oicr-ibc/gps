package org.obiba.heliotrope.spray

import cc.spray._
import akka.event.Logging
import akka.dispatch.{Future, ExecutionContext}
import akka.actor.{Props, ActorSystem}
import org.obiba.heliotrope.tracker.User

trait DummyServiceAuthentication extends ServiceAuthentication {
  
  implicit def actorSystem: ActorSystem
    
  val authenticator: GeneralAuthenticator[User] = { ctx: RequestContext =>
    Future[Either[Rejection, User]] {
      val user = User(identifier = "mungo")
//      user.identifier.setFromString("mungo")
      val result: Either[Rejection, User] = Right(user)
      result
    }
  }
   
  /**
   * Inject an authorization function which does nothing useful - ideal for testing
   */
  val authorizor: RequestContext => Boolean = { ctx => 
    true 
  }
}