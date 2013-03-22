package ca.on.oicr.gps.controller

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import ca.on.oicr.gps.model.system.SecRole;
import ca.on.oicr.gps.model.system.SecUser;
import ca.on.oicr.gps.model.system.SecUserSecRole;
import ca.on.oicr.gps.system.User;
import ca.on.oicr.gps.util.UserComparator;
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_GPS-ADMINS'])
class UserController {
	
	def userListService
	def userDetailsService

    def index = {

        redirect(action: "list", params: params)
    }

    def list = {
		
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.sort = params.sort ?: 'userName'
		params.order = params.order ?: 'asc'

		
		List<User> userList = userListService.getUsers()
		
		if (params.sort == 'userName') {
			def uc = [compare:{ a,b -> a.userName.compareTo(b.userName) }] as Comparator
			userList = userList.sort(uc);
		} else if (params.sort == 'name') {
			Comparator userComparator = new UserComparator();
			userList = userList.sort(userComparator);
		} else if (params.sort == 'email') {
			def ec = [compare:{ a,b -> a.email.compareTo(b.email) }] as Comparator
			userList = userList.sort(ec);
		}
		
		if (params.order == 'desc') {
			userList = userList.reverse()
		}
		
		int userInstanceTotal = userList.size()
		
		int start = params.offset ? Integer.parseInt(params.offset) : 0
		int end = start + params.max
		if (end > userInstanceTotal - 1) {
			end = userInstanceTotal - 1
		}
		
		userList = userList[start..end]
		
		return [userList: userList, userInstanceTotal: userInstanceTotal]
	}
	
	def edit = {
		
		log.trace("Request user/edit: " + params.id)
		
		def userInstance = userListService.getUser(params.id)
		
		def secUser = SecUser.findByUsername(userInstance.userName)
		def allRoles = secUser ? secUser.getAuthorities().collect { it.authority } : [] as Set<String>
		
		def allPossibleRoles = SecRole.getAll().collect { it.authority }.sort() as List<String>
		
		log.trace("Found user: " + userInstance)
		log.trace("Found roles: " + allRoles)
		log.trace("Found possible roles: " + allPossibleRoles)
		
		if (!userInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect(action: "list")
		}
		else {
			[userInstance: userInstance, userRoles: allRoles, availableRoles: allPossibleRoles]
		}
	}
	
	def show = {

		log.trace("Request user/show: " + params.id)
		
		def userInstance = userListService.getUser(params.id)
		
		def secUser = SecUser.findByUsername(userInstance.userName)
		def allRoles = secUser ? secUser.getAuthorities().collect { it.authority } : [] as Set<String>
		
		def allPossibleRoles = SecRole.getAll().collect { it.authority }.sort() as List<String>
		
		log.trace("Found user: " + userInstance)
		log.trace("Found roles: " + allRoles)
		log.trace("Found possible roles: " + allPossibleRoles)
		
		if (!userInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect(action: "list")
		}
		else {
			[userInstance: userInstance, userRoles: allRoles, availableRoles: allPossibleRoles]
		}
	}
	
	def update = {

		log.trace("Request user/edit: " + params.id)

		def userInstance = userListService.getUser(params.id)
		
		def secUser = SecUser.findByUsername(userInstance.userName)
		log.trace("Found user instance: " + secUser)
		
		// It is entirely possible that we don't actually have this user yet, at least not as a SecUser
		// instance. In this case, we should silently instantiate it. 
		
		if (! secUser) {
			log.trace("Creating new instance for: " + userInstance.userName)
			secUser = new SecUser(username: userInstance.userName, password: "N/A")
			secUser.save(flush: true, failOnError: true)
		}
		
		def allRoles = secUser ? secUser.getAuthorities().collect { it.authority } : [] as Set<String>
		
		// Optimistic locking? This needs to have a version in the object, but the persistence we are looking
		// for is all in the SecUser instance. 
		
		if (userInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (secUser.version > version) {
					
					userInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'user.label', default: 'User')] as Object[], "Another user has updated this User while you were editing")
					render(view: "edit", model: [userInstance: userInstance, userRoles: allRoles])
					return
				}
			}
			
			// This is where we need to update the user. Only roles are really an issue at this stage. We
			// need to remember to delete roles which are no longer included, as well as adding those 
			// which are.
			
			Set<SecRole> currentRoles = secUser.getAuthorities()
			for(SecRole role in SecRole.getAll()) {

				boolean foundNewRole = params.containsKey(role.authority) 
				boolean foundOldRole = currentRoles.contains(role)
				
				if (foundNewRole != foundOldRole) {
					if (foundOldRole) {
						SecUserSecRole.remove(secUser, role)
					} else {
						SecUserSecRole.create(secUser, role)
					}
				}
			}
			
			if (!secUser.hasErrors() && secUser.save(flush: true)) {
				
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.userName])}"
				redirect(action: "show", id: userInstance.userName)
				
			} else {
			
				render(view: "edit", model: [userInstance: userInstance, userRoles: allRoles])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect(action: "list")
		}
	}

}
