package ca.on.oicr.gps.service

import org.codehaus.groovy.grails.commons.GrailsApplication;

import grails.test.*
import groovy.mock.interceptor.MockFor;

class UserListServiceTests extends GrailsUnitTestCase {
	
	def mockConfig
	
	def userListService
	
    public void setUp() {
        super.setUp()
		
		mockConfig = new MockFor(GrailsApplication)
		mockConfig.demand.config { ->
			return [
				grails: [
					plugins: [
						springsecurity: [
							ldap: [
								context: [server: "ldap://10.0.0.100/"],
								authorities: [groupSearchBase: "/ou=Groups,dc=oicr,dc=on,dc=ca"],
								search: [base: "dc=oicr,dc=on,dc=ca"]
							]
						]
					]
				],
				gps: [
					userListService: [
						groupFilter: "(cn=gps-users)",
						groupMemberAttribute: "memberUid",
						userFilter: "(uid={0})",
					]
				]
			]
		}		
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Tests the user list - this should fail now we have disabled LDAP for testing.
	 */
    void testUserList() {
		
		userListService = new UserListService()
		userListService.grailsApplication = mockConfig.proxyInstance()
		def results
		
		shouldFail {
			results = userListService.users
		}
    }
}
