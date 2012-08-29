package ca.on.oicr.gps.model.system

class SecUser {

	String username
	String password
	boolean enabled = true
	boolean accountExpired = false
	boolean accountLocked = false
	boolean passwordExpired = false

	static constraints = {
		username blank: false, unique: true
		password blank: false
	}

	static mapping = {
		password column: '`password`'
	}

	Set<SecRole> getAuthorities() {
		SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
	}

	Set<SecRole> getSecRoles() {
		SecUserSecRole.findAllBySecUser(this) as Set
	}
}
