package ca.on.oicr.gps.model.data

import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Summary;
import grails.test.*

class SubjectTests extends GrailsUnitTestCase {
	
	def sub
	
	/**
	 * Set up the tests
	 */
    public void setUp() {
        super.setUp()
		
		def decision1 = new Decision(source: Sample.SOURCE_STUDY_SAMPLE, date: new Date(112, 0, 15), decisionType: Decision.TYPE_INTERIM);
		def decision2 = new Decision(source: Sample.SOURCE_STUDY_SAMPLE, date: new Date(112, 0, 16), decisionType: Decision.TYPE_WITHDRAWN);
		def decision3 = new Decision(source: Sample.SOURCE_ARCHIVAL_SAMPLE, date: new Date(112, 0, 17), decisionType: Decision.TYPE_WITHDRAWN);
		def decision4 = new Decision(source: Sample.SOURCE_ARCHIVAL_SAMPLE, date: new Date(112, 0, 18), decisionType: Decision.TYPE_FINAL);
		def decision5 = new Decision(source: Sample.SOURCE_ARCHIVAL_SAMPLE, date: new Date(112, 0, 19), decisionType: Decision.TYPE_WITHDRAWN);
		mockDomain(Decision, [decision1, decision2, decision3, decision4, decision5])
		
		mockDomain(Subject)
		sub = new Subject()

		sub.patientId = '0123456789'
		sub.gender = 'F'

		sub.summary = new Summary()
		
		sub.addToDecisions(decision1)
		sub.addToDecisions(decision2)
		sub.addToDecisions(decision3)
		sub.addToDecisions(decision4)
		sub.addToDecisions(decision5)
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks that the untouched submission passes validation
	 */
    void testValidation() {
		// Check that an complete subject will validate
		def result = sub.validate()
		sub.errors.allErrors.each { log.info it.toString() }
		assertTrue result
		assertFalse sub.hasErrors()
    }
	
	/**
	 * Tests that a null and a very long patient identifier both
	 * cause validation errors
	 */
    void testPatientId() {
		def old = sub.patientId
		
		sub.patientId = null
		assertFalse sub.validate()
		assertTrue sub.hasErrors()

		sub.patientId = '012345678901234567890123456789'
		assertFalse sub.validate()
		assertTrue sub.hasErrors()

		sub.patientId = old
    }

	/**
	 * Tests that a null and invalid gender cause validation errors
	 */
	void testGender() {
		def old = sub.gender
		
		sub.gender = null
		assertFalse sub.validate()
		assertTrue sub.hasErrors()

		sub.gender = 'A'
		assertFalse sub.validate()
		assertTrue sub.hasErrors()

		sub.gender = old
    }
	
	/**
	 * Finds a recent study sample decision
	 */
	void testFindDecision1() {
		def found = sub.findDecision(Sample.SOURCE_STUDY_SAMPLE)
		assertTrue found != null
		
		assertEquals new Date(112, 0, 15), found.date
	}

	/**
	 * Finds a recent archival sample decision
	 */
	void testFindDecision2() {
		def found = sub.findDecision(Sample.SOURCE_ARCHIVAL_SAMPLE)
		assertTrue found != null
		
		assertEquals new Date(112, 0, 18), found.date
	}
}
