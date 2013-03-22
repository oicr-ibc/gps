package ca.on.oicr.gps.data

import java.io.File
import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.context.SecurityContextHolder

import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Summary;
import ca.on.oicr.gps.model.system.AuditRecord;

import grails.test.*
import groovy.mock.interceptor.MockFor;

class SubjectIntegrationTests extends GroovyTestCase {
	
	static transactional = true
	
	def authenticationManager
	def changeSummaryService
	def appDataService
	
    public void setUp() {
        super.setUp()
		
		// Just to be on the safe side, delete old stuff
		def sub = Subject.findByPatientId('INT-001')
		if (sub) {
			sub.delete(flush:true)
		}
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Tests simple create and delete of a new subject. So far, we do nothing with
	 * updates. At the appropriate stages, we can test for summary records to check
	 * that we have a log of what changed and when. 
	 */
    void testSaveUpdateDelete() {
		
		def sub1
		def sub2
		def sub3
		
		def authToken = new UsernamePasswordAuthenticationToken("user", "user")
        try {
            def auth = authenticationManager.authenticate(authToken)
            SecurityContextHolder.getContext().setAuthentication(auth)
        } catch(e) {
			def exception = e
            fail("Exception $e should not be thrown")
        }
		
		Subject.withTransaction {
			sub1 = new Subject()
			sub1.patientId = 'INT-001'
			sub1.gender = 'F'

			sub1.summary = new Summary()
			sub1.summary.subject = sub1
	
			sub1.save(flush:true, validate:true, failOnError: true)

			sub2 = new Subject()
			sub2.patientId = 'INT-002'
			sub2.gender = 'F'

			sub2.summary = new Summary()
			sub2.summary.subject = sub2
	
			sub2.save(flush:true, validate:true, failOnError: true)
		}
		
		assertFalse(sub1.hasErrors())
		assertTrue(sub1.summary.validate())
		assertFalse(sub1.summary.hasErrors())

		// Check we get an identifier
		assertNotNull(sub1.id)
		
		assertFalse(sub2.hasErrors())
		assertTrue(sub2.summary.validate())
		assertFalse(sub2.summary.hasErrors())

		// Check we get an identifier
		assertNotNull(sub2.id)
		
		// Check we get a last updated timestamp. This is enough for now
		//assertNotNull(sub.lastUpdated)
		
		//def oldLastUpdated = sub.summary.lastUpdated
		
		// Now we can wait a little, just a little, and then make a change, update
		// again, and check the last updated timestamp is different. 
		
		Date watershed = new Date()
		
		Thread.sleep(50)
		
		Subject.withTransaction {
			sub1.summary.primaryPhysician = "Dr Who"
			sub1.summary.consentDate = new Date()
			sub1.summary.biopsyDate = new Date()
			sub1.save(flush:true, validate:true, failOnError: true)

			sub3 = new Subject()
			sub3.patientId = 'INT-003'
			sub3.gender = 'F'

			sub3.summary = new Summary()
			sub3.summary.subject = sub3
	
			sub3.save(flush:true, validate:true, failOnError: true)
		}
		
		assertTrue(sub3.validate())
		assertFalse(sub3.hasErrors())
		assertTrue(sub3.summary.validate())
		assertFalse(sub3.summary.hasErrors())

		// Check we get a different timestamp
		//def newLastUpdated = sub.summary.lastUpdated
		//assertNotSame(newLastUpdated, oldLastUpdated)
		
		// So long as all is well here, we'll be OK
		Subject.withTransaction {
			sub2.delete(flush:true, failOnError: true)
		}
		
		// At this stage, we ought to have a decent collection of audit records. So let's check them 
		// out, shall we?
		
		def results = AuditRecord.findAllByTimestampGreaterThanEquals(watershed)
		assertTrue(results.size > 0)
		
		// Now to exercise the change summary service and related components. This ought to run
		// as far as building an email, but that might be a little more challenging as that
		// requires GSP to be available.
		
		FieldPosition p = new FieldPosition(0)
		StringBuffer buffer = new StringBuffer()
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
		sdf.format(watershed, buffer, p)
		String modifiedSince = buffer.toString()
		
		appDataService.setAttribute('ModifiedSince', modifiedSince)
		def changes = changeSummaryService.getChanges()
		assertNotNull(changes)
		assertTrue(changes.get('insert') == 1)
		assertTrue(changes.get('update') == 1)
		assertTrue(changes.get('delete') == 1)
    }
}
