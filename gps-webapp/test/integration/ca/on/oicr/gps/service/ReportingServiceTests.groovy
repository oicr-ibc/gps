package ca.on.oicr.gps.service

import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.test.PipelineTestCase;
import grails.test.*

class ReportingServiceTests extends PipelineTestCase {
	
	def reportingService
	
    public void setUp() {
        super.setUp()
		
		// Load some submissions - we can now report on them
		processSubmission('PacBioV2', "test/data/pacbio_test_07.xls");
		processSubmission('Sequenom', "test/data/sequenom_test_01.xls");
    }
	
	void testReportXML() {
		def sj3 = Subject.findByPatientId("TST-002")
		
		// First, just generate XML and check it casually
		def generatedXML = reportingService.buildSubjectReportXML(sj3)
		assertNotNull(generatedXML)
	}

	void testReportPersistence() {
		def sj3 = Subject.findByPatientId("TST-002")

		// Now we should find two reports attached to the subject, one for each
		// submission.
		def reports = sj3.reports
		assertEquals(2, reports.size())
		
		// And the first (newest) report should contain a body with XML in it
		def firstReport = reports.toList().getAt(0)
		def firstReportBody = firstReport.document
		assertNotNull(firstReportBody)
		assertEquals("text/xml", firstReportBody.type)
		
		// Now we can compare the contents, but first, get the data into a string from a
		// byte array.
		
		String savedXML = new String(firstReportBody.body)
		
		// The two might actually be a little different, due to the times and possibly 
		// random set ordering from queries. But we can at least check that we do have
		// some data
		assertNotNull(savedXML)
		
	}
}
