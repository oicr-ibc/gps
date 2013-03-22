package ca.on.oicr.gps.model.data

import org.joda.time.DateTime;

import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Summary;

import grails.test.*
import groovy.mock.interceptor.MockFor;

class SummaryTests extends GrailsUnitTestCase {

	def summary
    
	public void setUp() {
        super.setUp()

		mockDomain(Summary)
		summary = new Summary()

		summary.subject = new Subject()
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks that the untouched submission passes validation
	 */
    void testValidation() {
		// Check that an complete summary will validate
		def result = summary.validate()
		summary.errors.allErrors.each { log.info it.toString() }
		assertTrue result
		assertFalse summary.hasErrors()
    }
	
	void testConsentDate() {
		def old = summary.consentDate
		
		shouldFail { summary.consentDate = "2011/03/14" }
		
		summary.consentDate = new Date()
		assertTrue summary.validate()
		assertFalse summary.hasErrors()

		summary.consentDate = old
	}

	void testBiopsyDate() {
		def old = summary.biopsyDate
		
		shouldFail { summary.biopsyDate = "2011/03/15" }
		
		summary.biopsyDate = new Date()
		assertTrue summary.validate()
		assertFalse summary.hasErrors()

		summary.biopsyDate = old
	}

	void testPathologyArrivalDate() {
		def old = summary.pathologyArrivalDate
		
		shouldFail { summary.pathologyArrivalDate = "2011/03/15" }
		
		summary.pathologyArrivalDate = new Date()
		assertTrue summary.validate()
		assertFalse summary.hasErrors()

		summary.pathologyArrivalDate = old
	}

	void testSequenomArrivalDate() {
		def old = summary.sequenomArrivalDate
		
		shouldFail { summary.sequenomArrivalDate = "2011/03/15" }
		
		summary.sequenomArrivalDate = new Date()
		assertTrue summary.validate()
		assertFalse summary.hasErrors()

		summary.sequenomArrivalDate = old
	}

	void testPacbioArrivalDate() {
		def old = summary.pacbioArrivalDate
		
		shouldFail { summary.pacbioArrivalDate = "2011/03/15" }
		
		summary.pacbioArrivalDate = new Date()
		assertTrue summary.validate()
		assertFalse summary.hasErrors()

		summary.pacbioArrivalDate = old
	}

	void testMedidataUploadDate() {
		def old = summary.medidataUploadDate
		
		shouldFail { summary.medidataUploadDate = "2011/03/15" }
		
		summary.medidataUploadDate = new Date()
		assertTrue summary.validate()
		assertFalse summary.hasErrors()

		summary.medidataUploadDate = old
	}

	void testArchivalArrivalDate() {
		def old = summary.archivalArrivalDate
		
		shouldFail { summary.archivalArrivalDate = "2011/03/15" }
		
		summary.archivalArrivalDate = new Date()
		assertTrue summary.validate()
		assertFalse summary.hasErrors()

		summary.archivalArrivalDate = old
	}

	void testExpertPanelDecisionDate() {
		def old = summary.expertPanelDecisionDate
		
		shouldFail { summary.expertPanelDecisionDate = "2011/04/01" }
		
		summary.expertPanelDecisionDate = new Date()
		assertTrue summary.validate()
		assertFalse summary.hasErrors()

		summary.expertPanelDecisionDate = old
	}

	void testElapsedWorkingDays() {
		summary.biopsyDate = new Date(2011, 03, 15)
		summary.expertPanelDecisionDate = new Date(2011, 04, 01)
		
		assertEquals 10, summary.getElapsedWorkingDays()
		assertEquals 10, summary.elapsedWorkingDays
		
		summary.consentDate = new Date(2011, 03, 14)

		assertEquals 11, summary.getElapsedWorkingDays()
		assertEquals 11, summary.elapsedWorkingDays
	}

	void testPsychosocial() {
		def old = summary.psychosocial
		
		summary.psychosocial = null
		assertFalse summary.validate()
		assertTrue summary.hasErrors()
		
		summary.psychosocial = summary.PSYCHOSOCIAL_AGREED
		assertTrue summary.validate()
		assertFalse summary.hasErrors()

		summary.psychosocial = old
	}
}
