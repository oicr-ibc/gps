package ca.on.oicr.gps.system

import java.util.Set;

import ca.on.oicr.gps.model.system.SecRole;
import ca.on.oicr.gps.model.system.SecUser;

/**
 * Sort of a domain class, except that it is not really persisted. This is derived from a
 * different data source, from LDAP. 
 * 
 * @author Stuart Watt
 */
class User {
	String userName
	String givenName
	String familyName
	String email
	
	/**
	 * Returns a nicely formatted name
	 * @return the name
	 */
	String getName() {
		return givenName + " " + familyName
	}
	
	String getEmailURL() {
		URL emailURL = null
		try {
			emailURL = new URL("mailto:" + email);
		} catch (MalformedURLException mue){
			// Do nothing
		}
		return emailURL
	}
	
	SecUser getSecUser() {
		return SecUser.findByUsername(userName)
	}
	
	Set<String> getAuthorities() {
		SecUser secUser = getSecUser()
		if (! secUser) {
			return [] as Set<String>
		}
		return secUser.getAuthorities().collect { it.authority }
	}
}
