package org.obiba.heliotrope.tracker.service

import cc.spray._
import akka.event.Logging
import akka.dispatch.Future
import akka.actor.{Props, ActorSystem}
import org.obiba.heliotrope.spray.LdapConfigSettings
import org.obiba.heliotrope.tracker.User
import org.obiba.heliotrope.spray.LdapServiceAuthentication

/**
 * Updated Boot class, for spray 1.0 and, therefore, Akka 2. 
 */
class Boot(system: ActorSystem) {

//  MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost("127.0.0.1", 27017), "tracker"))
//  MongoDB.useCollection("study") { coll => }
  
  val log = Logging(system, classOf[Boot])
  
  val ldapConfig = LdapConfigSettings.Config
  log.info("LDAP config: " + ldapConfig.toString())

  val mainModule = new TrackerService with LdapServiceAuthentication {
    implicit def actorSystem = system
    // bake your module cake here
  }
  
  val service = system.actorOf(
    props = Props(new HttpService(mainModule.trackerService)),
    name = "service1"
  )
  val rootService = system.actorOf(
    props = Props(new RootService(service)),
    name = "spray-root-service" // must match the name in the config so the ConnectorServlet can find the actor
  )
  
  system.registerOnTermination {
    // put additional cleanup code here
    system.log.info("Application shut down")
  }

}