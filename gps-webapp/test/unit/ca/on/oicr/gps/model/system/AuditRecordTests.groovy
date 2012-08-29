package ca.on.oicr.gps.model.system

import ca.on.oicr.gps.model.system.AuditRecord;
import grails.test.*

class AuditRecordTests extends GrailsUnitTestCase {
	
	def ar 
	
    public void setUp() {
        super.setUp()

		mockDomain(AuditRecord)
		ar = new AuditRecord(patientId: 'TST-001', 
			                 className: 'java.util.Date', 
			                 type: AuditRecord.TYPE_INSERT, 
							 propertyName: 'dummy',
							 newValue: 'Hello', 
			                 timestamp: new Date())
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks that the untouched mutation object passes validation
	 */
    void testValidation() {
		// Check default
		def result = ar.validate()
		ar.errors.allErrors.each { log.info it.toString() }
		assertTrue result
		assertFalse ar.hasErrors()
    }
	
	/**
	 * Checks that long barcodes fail validation
	 */
	void testNewValue() {
		def old = ar.newValue
		
		ar.newValue = null
		assertTrue ar.validate()
		assertFalse ar.hasErrors()
		
		ar.newValue = old
	}

}
