package ca.on.oicr.gps.data

import java.io.File

import ca.on.oicr.gps.model.data.Submission;
import grails.test.*

class SubmissionIntegrationTests extends GroovyTestCase {
	
    public void setUp() {
        super.setUp()
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Check basic save/delete operations on a submission. The data source for this ought
	 * really to be a proper file embedded for test purposes. 
	 */
    void testSaveAndDelete() {
		def sub = new Submission()
		
		def testFileName = 'test/data/pacbio_test_07.xls'
		def testFile = new File(testFileName)
		
		// Check we can find the test file, to be safe
		assertTrue testFile.exists()
		
		sub.dataType = 'ABI'
		sub.userName = 'user'
		sub.dateSubmitted = new Date()
		sub.fileName = testFile.path
		sub.fileContents = testFile.getBytes()
		
		// This is the save; check we get allocated an identifier
		assertNotNull sub.save()
		assertNotNull sub.id
		
		// We now ought to be able to locate and remove this record
		def testFileId = sub.id
		sub = null
		
		// First, let's look for the submission by identifier
		def foundSub = Submission.get(testFileId)
		assertNotNull foundSub
		
		// Now let's delete (and flush)
		foundSub.delete(flush:true)
		foundSub = null
		
		// Now look again, this time we should fail to find it
		foundSub = Submission.get(testFileId)
		this.assertNull foundSub
    }
}
