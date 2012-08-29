package ca.on.oicr.gps.service

import org.springframework.security.core.context.SecurityContextHolder

/**
 * A small service, primarily used to retrieve the current user name and other details as
 * needed. As a service, this can be wired into other services and controllers as required.
 * 
 * @author swatt
 */

class CurrentUserService {

    static transactional = false

	/**
	 * Returns the current user name, or null if none
	 * @return the user name
	 */
    def currentUserName() {
		def context = SecurityContextHolder.getContext()
		def authentication = context.getAuthentication()
		return authentication?.principal?.getUsername()
    }
}
