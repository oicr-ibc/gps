package ca.on.oicr.gps.model.data

import ca.on.oicr.gps.model.data.Sample;
import ca.on.oicr.gps.model.data.Subject;
import grails.test.*

class SampleTests extends GrailsUnitTestCase {
	
	def sample
	
    public void setUp() {
        super.setUp()

		mockDomain(Sample)
		sample = new Sample()

		sample.barcode = 'FGFR3_6'
		sample.name = 'FGFR3'
		sample.type = 'FFPE'
		sample.dnaConcentration = 0.0
		sample.dnaQuality = 'xxx'
		sample.sequenomNum = 'xxx'
		sample.dateReceived = new Date()
		sample.dateCreated = new Date()
		sample.lastUpdated = new Date()
		
		sample.subject = new Subject()
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks that leaving the owning components null fails validation
	 */
    void testOwners() {
		def old = sample.subject
		
		sample.subject = null
		assertFalse sample.validate()
		assertTrue sample.hasErrors()
		
		sample.subject = old
    }

	/**
	 * Checks that the untouched mutation object passes validation
	 */
    void testValidation() {
		def result = sample.validate()
		sample.errors.allErrors.each { log.info it.toString() }
		assertTrue result
		assertFalse sample.hasErrors()
    }
	
	/**
	 * Checks that long barcodes fail validation
	 */
	void testBarcode() {
		def old = sample.barcode
		
		sample.barcode = '989899800000000000000000000'
		assertFalse sample.validate()
		assertTrue sample.hasErrors()
		
		sample.barcode = old
	}

	/**
	 * Checks that long names fail validation
	 */
	void testName() {
		def old = sample.name
		
		sample.name = '989899800000000000000000000'
		assertFalse sample.validate()
		assertTrue sample.hasErrors()
		
		sample.name = old
	}

	/**
	 * Checks that invalid sample types fail validation
	 */
	void testType() {
		def old = sample.type
		
		sample.type = 'FFPD'
		assertFalse sample.validate()
		assertTrue sample.hasErrors()
		
		sample.type = old
	}

	/**
	 * Checks that invalid sample types fail validation
	 */
	void testDnaConcentration() {
		def old = sample.dnaConcentration
		
		shouldFail { sample.dnaConcentration = '0.3' }
		
		sample.dnaConcentration = old
	}

	/**
	 * Checks that invalid time types fail validation
	 */
	void testDateReceived() {
		def old = sample.dateReceived
		
		shouldFail { sample.dateReceived = '20110104' }
		
		sample.dateReceived = old
	}

	/**
	 * Checks that invalid time types fail validation
	 */
	void testDateCreated() {
		def old = sample.dateCreated
		
		shouldFail { sample.dateCreated = '20110104' }
		
		sample.dateCreated = null
		assertFalse sample.validate()
		assertTrue sample.hasErrors()

		sample.dateCreated = old
	}

	/**
	 * Checks that invalid time types fail validation
	 */
	void testLastUpdated() {
		def old = sample.lastUpdated
		
		shouldFail { sample.lastUpdated = '20110104' }
		
		sample.lastUpdated = old
	}

}
