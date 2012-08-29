package ca.on.oicr.gps.model.data

import ca.on.oicr.gps.model.data.Submission;
import grails.test.*

/**
 * Unit tests for a submission, principally validation. 
 * 
 * @author swatt
 */

class SubmissionTests extends GrailsUnitTestCase {
	
	def sub
	
    public void setUp() {
        super.setUp()
		mockDomain(Submission)
		sub = new Submission()

		sub.dataType = 'ABI'
		sub.userName = 'user'
		sub.dateSubmitted = new Date()
		sub.fileName = 'filename.txt'
		sub.fileContents = "String data".getBytes()
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks that the untouched submission passes validation
	 */
    void testValidation() {
		// Check that an complete submission will validate
		assertTrue sub.validate()
		assertFalse sub.hasErrors()
    }
	
	/**
	 * Checks that a null or unexpected type will fail validation
	 */
	void testDataType() {
		def old = sub.dataType
		
		sub.dataType = null
		assertFalse sub.validate()
		assertTrue sub.hasErrors()

		sub.dataType = old
	}

	/**
	 * Checks that a null or empty user name will fail validation
	 */
	void testUserName () {
		def old = sub.userName
		
		sub.userName = null
		assertFalse sub.validate()
		assertTrue sub.hasErrors()

		sub.userName = ''
		assertFalse sub.validate()
		assertTrue sub.hasErrors()
		
		sub.userName = old
	}

	/**
	 * Checks that a null date will fail validation
	 */
	void testDateSubmitted () {
		def old = sub.dateSubmitted
		
		sub.dateSubmitted = null
		assertFalse sub.validate()
		assertTrue sub.hasErrors()
		
		sub.dateSubmitted = old
	}

	/**
	 * Checks that a bad file name will fail validation
	 */
	void testFileName () {
		def old = sub.fileName
		
		sub.fileName = null
		assertFalse sub.validate()
		assertTrue sub.hasErrors()
		
		sub.fileName = old
	}

	/**
	 * Checks that bad/empty file contents will fail validation
	 */
	void testFileContents () {
		def old = sub.fileContents
		
		sub.fileContents = null
		assertFalse sub.validate()
		assertTrue sub.hasErrors()
		
		sub.fileContents = old
	}
}
