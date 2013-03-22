package ca.on.oicr.gps.model.data

import grails.test.*

class ReportableMutationTests extends GrailsUnitTestCase {
	
	def repMut
	
    public void setUp() {
        super.setUp()

		mockDomain(Decision)
		def dec = new Decision()
		
		mockDomain(ReportableMutation)
		repMut = new ReportableMutation(decision: dec)
		
		repMut.comment = ""		
		repMut.justification = ""		
		repMut.actionable = ""		
		repMut.reportable = ""		
		repMut.levelOfEvidence = repMut.LOE_LEVEL_1
		repMut.levelOfEvidenceGene = repMut.LOE_LEVEL_1
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks that the untouched ReportableMutation passes validation
	 */
    void testValidation() {
		def result = repMut.validate()
		repMut.errors.allErrors.each { log.info it.toString() }
		assertTrue result
		assertFalse repMut.hasErrors()
    }
	
	void testMissingComment() {
		def old = repMut.comment
		
		repMut.comment = null
		assertFalse repMut.validate()
		assertTrue repMut.hasErrors()

		repMut.comment = old
	}

	void testMissingJustification() {
		def old = repMut.justification
		
		repMut.justification = null
		assertFalse repMut.validate()
		assertTrue repMut.hasErrors()

		repMut.justification = old
	}

	void testMissingActionable() {
		def old = repMut.actionable
		
		repMut.actionable = null
		assertFalse repMut.validate()
		assertTrue repMut.hasErrors()

		repMut.actionable = old
	}

	void testMissingReportable() {
		def old = repMut.reportable
		
		repMut.reportable = null
		assertFalse repMut.validate()
		assertTrue repMut.hasErrors()

		repMut.reportable = old
	}

	void testMissingLevelOfEvidence() {
		def old = repMut.levelOfEvidence
		
		repMut.levelOfEvidence = null
		assertTrue repMut.validate()
		assertFalse repMut.hasErrors()

		repMut.levelOfEvidence = old
	}

	// This is actually permitted!
	void testMissingLevelOfEvidenceGene() {
		def old = repMut.levelOfEvidenceGene
		
		repMut.levelOfEvidenceGene = null
		assertTrue repMut.validate()
		assertFalse repMut.hasErrors()

		repMut.levelOfEvidenceGene = old
	}
}
