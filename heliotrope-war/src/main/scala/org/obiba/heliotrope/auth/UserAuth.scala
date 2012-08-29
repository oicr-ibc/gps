package org.obiba.heliotrope.auth

import net.liftweb.common._
import net.liftweb.util.Props
import net.liftweb.http.S
import scala.xml._
import net.liftweb.http.LiftRules
import net.liftweb.http.TemplateFinder
import net.liftweb.util.FieldError
import net.liftweb.ldap._
import org.obiba.heliotrope.domain.User
import net.liftweb.http.Req
import javax.naming.directory.{Attributes, Attribute => Attr}
import net.liftweb.http.PostRequest
import org.obiba.heliotrope.domain.FamilyNameUserOption
import org.obiba.heliotrope.domain.GivenNameUserOption
import org.obiba.heliotrope.domain.EmailUserOption

trait FieldSet {
    def toForm: NodeSeq
    def validate: List[FieldError]
    def save(user: User): Unit
}

trait AuthModule extends Logger {
    def loginPresentation: Box[NodeSeq]
    def signupPresentation: Box[NodeSeq] = Empty
    def moduleName: String
    def performInit(): Unit
    def isDefault = false
    def createHolder(): FieldSet
}

trait LDAPBase {
	this : AuthModule =>
	  
	val rolesToCheck = Props.get("role_list") match {
        case Full(s) => s.split(',').toList
        case _ => Nil
    }
	
	var currentRole : String = _
	
	object myLdapVendor extends LDAPVendor
	
	def constructDistinguishedName(who : String, isGroup : Boolean = false) = {
        val base = Props.get( if(isGroup) {"ldap.groupBase"} else {"ldap.userBase"} )  openOr ""
        val dn = "%s,%s".format(constructNameWithPrefix(who, isGroup), base)
        dn
    }
	
	def constructNameWithPrefix(username: String, isGroup: Boolean = false) = {
        val prefix = if(isGroup) {"cn"} else {Props.get("ldap.uidPrefix") openOr ""}
        val nameWithPrefix = "%s=%s".format(prefix, username)
        nameWithPrefix
    }
	
	def logInUser(who: User) {
        User.logUserIn(who)
        who.setRole(currentRole)
        S.notice(S.?("base_user_msg_welcome", who.niceName))
    }
	
	def logOutUser() {
	    val who = User.loggedInUser()
	    val niceName = who.map { _.niceName }
        User.logUserOut()
        S.notice(S.?("base_user_msg_goodbye", niceName))
    }
	
	def myLdap : LDAPVendor = {
        val ldapSrvHost = Props.get("ldap.server.host") openOr ""
        debug("LDAP server host: %s".format(ldapSrvHost))
        val ldapSrvPort = Props.get("ldap.server.port") openOr ""
        debug("LDAP server port: %s".format(ldapSrvPort))
        val ldapSrvBase = Props.get("ldap.server.base") openOr ""
        debug("LDAP server base: %s".format(ldapSrvBase))
        val ldapSrvUsrName = Props.get("ldap.server.userName") openOr ""
        debug("LDAP server username: %s".format(ldapSrvUsrName))
        val ldapSrvPwd = Props.get("ldap.server.password") openOr ""
        debug("LDAP server password: %s".format(ldapSrvPwd))
        val ldapSrvAuthType = Props.get("ldap.server.authType") openOr ""
        debug("LDAP server authentication type: %s".format(ldapSrvAuthType))
        val ldapSrvReferral= Props.get("ldap.server.referral") openOr ""
        debug("LDAP server referral: %s".format(ldapSrvReferral))
        val ldapSrvCtxFactory = Props.get("ldap.server.initial_context_factory") openOr ""
        debug("LDAP server initial context factory class: %s".format(ldapSrvCtxFactory))


        myLdapVendor.configure(Map("ldap.url" -> "ldap://%s:%s".format(ldapSrvHost, ldapSrvPort),
        						   "ldap.base" -> ldapSrvBase,
        						   "ldap.userName" -> ldapSrvUsrName,
        						   "ldap.password" -> ldapSrvPwd,
        						   "ldap.authType" -> ldapSrvAuthType,
        						   "referral" -> ldapSrvReferral,
        						   "ldap.initial_context_factory" -> ldapSrvCtxFactory))
        myLdapVendor
    }
	
	def getAttrs(dn : String) : Map[String, List[String]] = {
	    var attrsMap = Map.empty[String, List[String]]
	    val attrs : Attributes = myLdap.attributesFromDn(dn)
	    if (attrs != null) {
	        val allAttrs = attrs.getAll();
	        if (allAttrs != null) {
	            while(allAttrs.hasMore()) {
	            	val attribute = allAttrs.next().asInstanceOf[Attr];
	            	debug("Attribute name: '%s', has following values:".format(attribute.getID()))
	            	var attrValues = List.empty[String]
	            	for(i <- 0 until attribute.size()) {
	            		debug("Attribute value: '%s'".format(attribute.get(i)))
	            		attrValues ::= attribute.get(i).toString
	            	}
	            	attrsMap += (attribute.getID() -> attrValues)
	            }
	        }
	    }
	    attrsMap
	}
}
	
object LDAPAuthModule extends AuthModule with LDAPBase with Logger {
  
    override def isDefault = false
    
    def loginPresentation: Box[NodeSeq] = TemplateFinder.findAnyTemplate("templates-hidden" :: "ldap_login_form" :: Nil)
    
    def moduleName: String = "ldap"
      
    def createHolder(): FieldSet = new FieldSet {
        def toForm: NodeSeq = NodeSeq.Empty
        def validate: List[FieldError] = Nil
        def save(user: User): Unit = {}
    }
    
    def performInit(): Unit = {
        LiftRules.dispatch.append {
            case Req("ldap" :: "login" :: Nil, _, PostRequest) => {
                val from = S.referer openOr "/"
                
                val name = S.param("username").map(_.trim.toLowerCase) openOr ""
                val pwd = S.param("password").map(_.trim) openOr ""
                info("Found username and password");
            
                val ldapEnabled = Props.getBool("ldap.enabled") openOr false
                if(ldapEnabled) {
            	    info("LDAP is enabled: attempting to log in with LDAP");
                    def _getUserAttributes(dn: String) = myLdap.attributesFromDn(dn)
                    val ldapUserSearch = Props.get("ldap.user.search") openOr "(uid=%s)"
                    val ldapUserSearchExpanded = ldapUserSearch.format(name)
                
                    info("Base is: " + myLdap.ldapBaseDn.vend)
                    info("Searching for dn: " + ldapUserSearchExpanded)
                    val users = myLdap.search(ldapUserSearchExpanded)  
                
                    if (users.size >= 1) {  
                	    val userDn = users(0)
                	    info("Binding to user: " + userDn)
                	    if (myLdap.bindUser(userDn, pwd)) {  
                		    val completeDn = userDn + "," + myLdap.parameters().get("ldap.base").getOrElse("")  

   	                        info("LDAP check passed OK: " + completeDn);
                		    val ldapAttrs = getAttrs(completeDn)
	                        info(ldapAttrs)
                		
                		    val usr = User.findOrCreateUser(name, 
                		        new FamilyNameUserOption(ldapAttrs("sn").head),
                		        new GivenNameUserOption(ldapAttrs("givenName").head),
                		        new EmailUserOption(ldapAttrs("mail").head))
                		
                		    usr.map { User.logUserIn(_) }
                	    } else {
                	        info("LDAP bind failed");
                	        S.error(S.?("base_user_err_unknown_creds"))
                	    }
                    } else {
                	    info("LDAP user not found");
                	    S.error(S.?("base_user_err_unknown_creds"))
                    }
                }
               
                if (User.loggedInUser().isEmpty) {
            	    info("LDAP log in failed: checking user's password field");
                    val usr = User.findUser(name)
                    if (usr.exists { _.validPassword_?(pwd) }) {
                        usr.map { User.logUserIn(_) }
                    } else {
                        warn("Password incorrect for user: " + usr);
                	    S.error(S.?("base_user_err_unknown_creds"))
                    }
                }
 
                S.redirectTo(from)
            }
        }
    }
    
//    def checkRoles(who : String) : Boolean = {
//    	for (role <-rolesToCheck) {
//    		val ldapAttrs = getAttrs(constructDistinguishedName(role, true))
//    		val uniqueMember = ldapAttrs("uniqueMember").head
//    		if(who == uniqueMember) {
//    			currentRole = role
//    			return true
//    		}
//    	}
//    	return false;
//    }
//
}

class UserAuth {
  
  def getSingleton = UserAuth

}

object UserAuth extends UserAuth with Logger {

  private var modules: Map[String, AuthModule] = Map()

  def register(module: AuthModule) {
    modules += (module.moduleName -> module)
    if (module.isDefault && defAuth.isEmpty) defAuth = Full(module)
    info("Calling UserAuth.performInit")
    module.performInit()
  }

  def loginPresentation: List[NodeSeq] =
    modules.values.toList.flatMap(_.loginPresentation)
  
  private var defAuth: Box[AuthModule] = Empty

  def defaultAuthModule: AuthModule = defAuth.open_!
}