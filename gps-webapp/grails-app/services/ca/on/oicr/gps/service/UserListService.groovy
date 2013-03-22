package ca.on.oicr.gps.service

import java.util.Hashtable;
import java.util.List;

import java.net.URI;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import ca.on.oicr.gps.model.system.SecUser;
import ca.on.oicr.gps.system.User;

class UserListService {
	
	def grailsApplication
	
	DirContext ldapCtx

	Boolean ldapActive
	String ldapServer
	String groupSearchPath
	String groupFilter
	String groupMemberAttribute
	
	String userSearchPath
	String userFilter
	
	// Move to a lazy configuration system, because an eager configuration the 
	// constructor is totally impossible to unit test. 
	private void ensureConfiguration() {
		def config = grailsApplication.config
		
		ldapActive            = config.grails.plugins.springsecurity.ldap.active
		ldapServer            = config.grails.plugins.springsecurity.ldap.context.server
		groupSearchPath       = config.grails.plugins.springsecurity.ldap.authorities.groupSearchBase
		groupFilter           = config.gps.userListService?.groupFilter             ?: "(cn=gps-users)"
		groupMemberAttribute  = config.gps.userListService?.groupMemberAttribute    ?: "memberUid"
		
		userSearchPath        = config.grails.plugins.springsecurity.ldap.search.base
		userFilter            = config.gps.userListService?.userFilter              ?: "(uid={0})"
		
		if (ldapActive) {
			ldapCtx = new InitialDirContext();
		}
	}

	/**
	* Returns a list of user names associated with the LDAP criteria, by directly
	* querying the LDAP system.
	* @return
	*/
    public List<User> getUsers() {
	   
		ensureConfiguration()
		if (! ldapActive) {
			throw new RuntimeException("Can't get list of users without LDAP")
		}
	   
		URI searchUri = new URI(ldapServer)
		searchUri = searchUri.resolve(groupSearchPath)
		
	    NamingEnumeration answer = ldapCtx.search(searchUri.toString(), groupFilter, null)
	   
	    SearchResult group = answer.next()
	   
	    Attributes attributes = group.getAttributes()
	    Attribute value = attributes.get(groupMemberAttribute)
	   
	    List<User> userNames = new ArrayList<String>()
	   
	    for(String name in value.getAll()) {
		    userNames.add(getUser(name))
	    }
	   
	    return userNames
    }
   
    public User getUser(String uid) {
		
		ensureConfiguration()
		if (! ldapActive) {
			return new User(userName: uid)
		}
	   
		URI searchUri = new URI(ldapServer)
		searchUri = searchUri.resolve(userSearchPath)

	    NamingEnumeration personAnswer = ldapCtx.search(searchUri.toString(), userFilter, [uid] as Object[],
		    new SearchControls(SearchControls.SUBTREE_SCOPE, 1, 0, null, true, true))
	   
	    Attributes personAttributes = personAnswer.next().getAttributes()
	    String givenName = personAttributes.get("givenName").get()
	    String familyName = personAttributes.get("sn").get()
	    String email = personAttributes.get("mail").get()
	    
		return new User(userName: uid, givenName: givenName, familyName: familyName, email: email)
    }
}
