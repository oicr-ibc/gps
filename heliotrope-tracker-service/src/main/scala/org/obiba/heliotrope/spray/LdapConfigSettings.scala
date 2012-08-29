package org.obiba.heliotrope.spray

import com.typesafe.config.{ConfigFactory, Config}
import cc.spray.authentication.LdapAuthConfig
import org.obiba.heliotrope.tracker.User
import javax.naming.directory.SearchControls
import cc.spray.authentication.LdapQueryResult
import org.slf4j.{Logger, LoggerFactory}
import java.text.MessageFormat


object LdapConfigSettings {
  
  private val log = LoggerFactory.getLogger(getClass)
  
  private[this] val c: Config = {
    val c = ConfigFactory.load()
    c.checkValid(ConfigFactory.defaultReference(), "spray.servlet.ldap")
    c.getConfig("spray.servlet.ldap")
  }
  
  val Config = new LdapAuthConfig[User] {
    
    def configureSearchControls (searchControls: SearchControls, user: String): Unit = {
      searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE)
      searchControls.setReturningAttributes(Array("uid", "givenName", "sn"))
    }
    
    def contextEnv (user: String, pass: String): Seq[(String, String)] = {
      Seq(javax.naming.Context.PROVIDER_URL -> c.getString("server"))
    }
    
    def createUserObject(queryResult: LdapQueryResult): Option[User] = {
      log.debug("Query result: " + queryResult.toString())
      val user = User(identifier = queryResult.attrs("uid").value)
      log.debug("Result: " + user.toString())
      Some(user)
    }
    
    def searchBase(user: String): String = c.getString("search-base")
    
    def searchCredentials : (String, String) = (c.getString("search-user"), c.getString("search-pass"))
    
    def searchFilter (user: String): String = MessageFormat.format(c.getString("search-filter"), user)
  }  
}