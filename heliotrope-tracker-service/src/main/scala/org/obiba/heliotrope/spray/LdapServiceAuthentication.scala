package org.obiba.heliotrope.spray

/*
 * Copyright (C) 2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modified from the initial spray sources, to handle optional use of password digests
 * stored in the MongoDB structures. This allows quick and easy setup, as an initial
 * admin user can be created with a naive password, which can then be changed and used
 * independently of the authentication by LDAP. This is necessary so that in the long
 * run configuration can be set independently of the configuration files, and that in
 * the immediate term, people can use the system without LDAP. 
 */

import org.obiba.heliotrope.tracker.User
import cc.spray.authentication._
import cc.spray.UserPassAuthenticator
import cc.spray.RequestContext
import cc.spray.GeneralAuthenticator
import java.util.Hashtable
import javax.naming.{Context, NamingException, NamingEnumeration}
import javax.naming.ldap.InitialLdapContext
import javax.naming.directory.{SearchControls, SearchResult, Attribute}
import collection.JavaConverters._
import akka.dispatch.{Promise, Future}
import akka.actor.ActorSystem
import org.obiba.heliotrope.tracker.service.DAO
import com.mongodb.casbah.commons.MongoDBObject

/**
 * The LdapAuthenticator faciliates user/password authentication against an LDAP server.
 * It delegates the application specific parts of the LDAP configuration to the given LdapAuthConfig instance,
 * which is also responsible for creating the object representing the application-specific user context.
 *
 * Authentication against an LDAP server is done in two separate steps:
 * First, some "search credentials" are used to log into the LDAP server and perform a search for the directory entry
 * matching a given user name. If exactly one user entry is found another LDAP bind operation is performed using the
 * principal DN of the found user entry to validate the password.
 */

class LdapMongoRecordAuthenticator(config: LdapAuthConfig[User])(implicit system: ActorSystem) extends UserPassAuthenticator[User] {
  def log = system.log

  def apply(userPass: Option[(String, String)]) = {
    
    def auth3(entry: LdapQueryResult, pass: String): Option[User] = {
      log.debug("Called auth3 '{}'/'{}'", entry, pass)
      ldapContext(entry.fullName, pass) match {
        case Right(authContext) =>
          authContext.close()
          config.createUserObject(entry)
        case Left(ex) =>
          log.info("Could not authenticate credentials '{}'/'{}': {}", entry.fullName, pass, ex)
          None
      }
    }

    def auth2(searchContext: InitialLdapContext, user: String, pass: String): Option[User] = {
      log.debug("Called auth2 '{}'/'{}'", user, pass)
      log.debug("Search context: " + searchContext.toString())
      try {
        query(searchContext, user) match {
          case entry :: Nil => 
            log.debug("Result: " + entry.toString())
            auth3(entry, pass)
          case Nil =>
            log.warning("User '{}' not found (search filter '{}' and search base '{}'", user, config.searchFilter(user),
              config.searchBase(user))
            None
          case entries =>
            log.warning("Expected exactly one search result for search filter '{}' and search base '{}', but got {}",
              config.searchFilter(user), config.searchBase(user), entries.size)
            None
        }
      } catch {
        case e => 
          log.debug("Caught an exception! " + e)
          e.printStackTrace()
          None
      }
    }
    
    def authMongo(user: String, pass: String): Option[User] = {
      log.debug("Called authMongo '{}'/'{}'", user, pass)
      
      val md = java.security.MessageDigest.getInstance("SHA-1")
      val ha = new sun.misc.BASE64Encoder()
      val encoded = ha.encode(md.digest(pass.getBytes()))
      
      val found = DAO.userDAO.findOne(MongoDBObject("identifier" -> user))
      
      log.debug("Password received: " + pass)
      log.debug("Encoded password: " + encoded)
      log.debug("Look up user: " + found)
      
      val matched = for(
          foundUser <- found;
          foundPassword <- foundUser.passwordDigest;
          matchedUser <- Some(foundUser) if (foundPassword == encoded)
      ) yield (matchedUser)

      log.debug("matched: '{}'", matched)
      matched.orElse(auth1(user, pass))
    }

    def auth1(user: String, pass: String) = {
      log.debug("Called auth1 '{}'/'{}'", user, pass)
      val (searchUser, searchPass) = config.searchCredentials
      ldapContext(searchUser, searchPass) match {
        case Right(searchContext) =>
          val result = auth2(searchContext, user, pass)
          searchContext.close()
          log.debug("Done auth1 '{}'", result)
          result
        case Left(ex) =>
          log.warning("Could not authenticate with search credentials '{}'/'{}': {}", searchUser, searchPass, ex)
          None
      }
    }

    userPass match {
      case Some((user, pass)) => Future(authMongo(user, pass))
      case None =>
        log.warning("LdapAuthenticator.apply called with empty userPass, authentication not possible")
        Promise.successful(None)
    }
  }

  def ldapContext(user: String, pass: String): Either[Throwable, InitialLdapContext] = {
    log.debug("Called ldapContext '{}'/'{}'", user, pass)
    scala.util.control.Exception.catching(classOf[NamingException]).either {
      val env = new Hashtable[AnyRef, AnyRef]
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
      env.put(Context.SECURITY_PRINCIPAL, user)
      env.put(Context.SECURITY_CREDENTIALS, pass)
      env.put(Context.SECURITY_AUTHENTICATION, "simple")
      for ((key, value) <- config.contextEnv(user, pass)) env.put(key, value)
      log.debug("Env: " + env.toString())
      new InitialLdapContext(env, null)
    }
  }

  def query(ldapContext: InitialLdapContext, user: String): List[LdapQueryResult] = {
    val results: NamingEnumeration[SearchResult] = ldapContext.search(
      config.searchBase(user),
      config.searchFilter(user),
      searchControls(user)
    )
    results.asScala.toList.map(searchResult2LdapQueryResult)
  }

  def searchControls(user: String) = {
    val searchControls = new SearchControls
    config.configureSearchControls(searchControls, user)
    searchControls
  }

  def searchResult2LdapQueryResult(searchResult: SearchResult): LdapQueryResult = {
    import searchResult._
    LdapQueryResult(
      name = getName,
      fullName = getNameInNamespace,
      className = getClassName,
      relative = isRelative,
      obj = getObject,
      attrs = getAttributes.getAll.asScala.toSeq.map(a => a.getID -> attribute2LdapAttribute(a)) (collection.breakOut)
    )
  }

  def attribute2LdapAttribute(attr: Attribute): LdapAttribute = {
    LdapAttribute(
      id = attr.getID,
      ordered = attr.isOrdered,
      values = attr.getAll.asScala.toSeq.map(v => if (v != null) v.toString else "")
    )
  }
}

object LdapMongoRecordAuthenticator {
  def apply(config: LdapAuthConfig[User])(implicit system: ActorSystem) = new LdapMongoRecordAuthenticator(config)
}

trait LdapServiceAuthentication extends ServiceAuthentication {
  
  val authenticator: GeneralAuthenticator[User] = new BasicHttpAuthenticator[User]("Heliotrope", LdapMongoRecordAuthenticator.apply(LdapConfigSettings.Config))
  
  val authorizor: RequestContext => Boolean = { ctx =>
    true
  }
}