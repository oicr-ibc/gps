package ca.on.oicr.gps.model.data

import grails.test.*

class DecisionTests extends GrailsUnitTestCase {

	def decision

    public void setUp() {
        super.setUp()
		
		mockDomain(Subject)
		def sub = new Subject()
		
		mockDomain(Decision)
		decision = new Decision(subject: sub)
		
		decision.noTumour = false
		decision.insufficientMaterial = false
		decision.noMutationsFound = false
		decision.unanimous = true
		
		decision.date = new Date(112, 2, 1, 12, 1, 0)
		decision.decisionType = decision.TYPE_FINAL
		decision.decision = "Blah"
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks that the untouched decision passes validation
	 */
    void testValidation() {
		// Check that an complete summary will validate
		def result = decision.validate()
		decision.errors.allErrors.each { log.info it.toString() }
		assertTrue result
		assertFalse decision.hasErrors()
    }
	
	void testMissingNoTumour() {
		def old = decision.noTumour
		
		decision.noTumour = null
		assertFalse decision.validate()
		assertTrue decision.hasErrors()

		decision.noTumour = old
	}

	void testMissingInsufficientMaterial() {
		def old = decision.insufficientMaterial
		
		decision.insufficientMaterial = null
		assertFalse decision.validate()
		assertTrue decision.hasErrors()

		decision.insufficientMaterial = old
	}

	void testMissingNoMutationsFound() {
		def old = decision.noMutationsFound
		
		decision.noMutationsFound = null
		assertFalse decision.validate()
		assertTrue decision.hasErrors()

		decision.noMutationsFound = old
	}

	void testMissingUnanimous() {
		def old = decision.unanimous
		
		decision.unanimous = null
		assertFalse decision.validate()
		assertTrue decision.hasErrors()

		decision.unanimous = old
	}

	void testMissingDate() {
		def old = decision.date
		
		decision.date = null
		assertFalse decision.validate()
		assertTrue decision.hasErrors()

		decision.date = old
	}

	void testMissingDecisionType() {
		def old = decision.decisionType
		
		decision.decisionType = null
		assertFalse decision.validate()
		assertTrue decision.hasErrors()

		decision.decisionType = old
	}

	void testMissingDecision() {
		def old = decision.decision
		
		decision.decision = null
		assertFalse decision.validate()
		assertTrue decision.hasErrors()

		decision.decision = old
	}
	
	void testMissingSubject() {
		def old = decision.subject
		
		decision.subject = null
		assertFalse decision.validate()
		assertTrue decision.hasErrors()

		decision.subject = old
	}
	
	void testDate() {
		def summary = decision.summary
		assertEquals "Final report: 1 March 2012 12:01PM", summary
	}
}
