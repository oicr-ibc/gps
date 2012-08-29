package org.obiba.heliotrope.spray

import org.specs2.mutable._
import scala.util.parsing.json._
import cc.spray._

/**
 * Tests the LDAP configuration.
 */
class LdapSpecification extends Specification {
  "LDAP configuration" should {
    lazy val config = LdapConfigSettings.Config
    lazy val user: String = "mungo"
    
    "have the correct search base" in {
      config.searchBase(user) must_== "dc=oicr,dc=on,dc=ca"
    }
    
    "have the right server" in {
      lazy val result = config.contextEnv(user, "fatcat")
      lazy val first = result(0)
      (first._1 must_== javax.naming.Context.PROVIDER_URL).and(first._2 must_== "ldap://ldap.oicr.on.ca/")
    }
  }
}