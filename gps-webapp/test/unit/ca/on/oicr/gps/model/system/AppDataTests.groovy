package ca.on.oicr.gps.model.system

import ca.on.oicr.gps.model.system.AppData;
import grails.test.*

class AppDataTests extends GrailsUnitTestCase {
	
	def ad
	
    public void setUp() {
        super.setUp()

		mockDomain(AppData)
		ad = new AppData()
		ad.dataKey = 'property'
		ad.dataValue = 'value'
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks that the untouched submission passes validation
	 */
    void testValidation() {
		// Check that a complete AppData value will validate
		def result = ad.validate()
		ad.errors.allErrors.each { log.info it.toString() }
		assertTrue result
		assertFalse ad.hasErrors()
    }

	/**
	 * Checks that a null or unexpected type will fail validation
	 */
	void testNullValue() {
		def old = ad.dataValue
		
		ad.dataValue = null
		assertFalse ad.validate()
		assertTrue ad.hasErrors()

		ad.dataValue = old
	}
}
