package ca.on.oicr.gps.data

import ca.on.oicr.gps.model.data.Process;
import ca.on.oicr.gps.model.data.Sample;
import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Submission;
import ca.on.oicr.gps.model.data.Summary;
import ca.on.oicr.gps.pipeline.SubmissionSource;
import ca.on.oicr.gps.test.PipelineTestCase;
import grails.test.*

class ReportingDataTests extends PipelineTestCase {
	
	def sub1
	def sub2
	
    public void setUp() {
        super.setUp()
		
		// Load some submissions - we can now report on them
		sub1 = processSubmission('PacBioV2', "test/data/pacbio_test_07.xls");
		sub2 = processSubmission('Sequenom', "test/data/sequenom_test_01.xls");
    }

    void testGenerateReportSubjectList() {
		
		def subjects = Subject.getAllReportable([:])
		
		assertNotNull(subjects)
    }
	
    void testGenerateReportData() {
		
		def sj3 = Subject.findByPatientId("TST-002")
		assertNotNull(sj3)
		def data = sj3.getSubjectMutationReportData()
		
		assertNotNull(data.getAt('FFPE'))
		assertEquals(2, data.getAt('FFPE').size())

		assertNull(data.getAt('Frozen'))
		
		assertNotNull(data.getAt('FFPE').keySet().toList().getAt(0))
		assertEquals("TST002BIOXFOR1", data.getAt('FFPE').keySet().toList().getAt(0).barcode)
		
		def mutation = data.getAt('FFPE').values().toList().getAt(0).get(0)
		assertTrue(mutation.knownMutation.gene == "PIK3CA")
		assertTrue(mutation.knownMutation.mutation == "E542K")
    }

    void testSubmissionSubjects() {
		
		def pacBioSubmission = sub1
		assertNotNull(pacBioSubmission)
		
		def sequenomSubmission = sub2
		assertNotNull(sequenomSubmission)
		
		def pacBioSubjects = pacBioSubmission.subjects
		assertEquals(1, pacBioSubjects.size())
		assertEquals("TST-002", pacBioSubjects.toList().getAt(0).patientId)
		
		def sequenomSubjects = sequenomSubmission.subjects
		assertEquals(2, sequenomSubjects.size())
    }
}
