package ca.on.oicr.gps.service

import java.util.List;

import org.junit.Ignore;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import ca.on.oicr.gps.model.data.ObservedMutation;
import ca.on.oicr.gps.model.data.RunSample;
import ca.on.oicr.gps.model.data.Sample
import ca.on.oicr.gps.model.data.Process
import ca.on.oicr.gps.model.data.Subject
import ca.on.oicr.gps.model.data.Submission
import ca.on.oicr.gps.model.data.Summary
import ca.on.oicr.gps.model.knowledge.KnownMutation;
import ca.on.oicr.gps.model.laboratory.Panel;
import ca.on.oicr.gps.pipeline.SubmissionSource;
import ca.on.oicr.gps.pipeline.model.PipelineRuntimeException;
import ca.on.oicr.gps.test.PipelineTestCase;

class PipelineServiceTests extends PipelineTestCase {
	
	def pipelineService
	
	void checkState(state) {
		// Check we get a state
		assertNotNull(state)
		
		// Check we get no errors
		def errors = state.errors
		if (errors.size() > 0) {
			for(error in errors) {
				System.err.println("Pipeline error: " + error.key + " " +
					error.args.join(", "));
			}
		}
		
		assertEquals(0, errors.size())
	}

	/**
	 * Tests basic pipeline running. This is now refactored as a service so the
	 * pipeline details are no longer embedded in the controller. This makes testing
	 * very much easier, as we can do it through unit testing.
	 */
    void testPipelineServiceSequenom1() {

		def source = createSubmission('Sequenom', "test/data/sequenom_test_01.xls");
		def state = pipelineService.getPipelineState(source)
		pipelineService.runPipeline(state)
		
		// Check the state is OK
		checkState(state)
		
		// Now for a few additional validations
		def runs = Process.getAll()
		assertEquals(3, runs.size())
		
		// Three runs will be Sequenom, but let's simply choose one by chip code,
		// doesn;t much matter which, as they both contain one sample
		Process sr1 = Process.findByChipcode("G0691483")
		assertNotNull(sr1)
		assertEquals("Sequenom", sr1.panel.technology)
		assertEquals("G0691483", sr1.getChipcode())
		
		def runSamples = sr1.runSamples
		assertEquals(1, runSamples.size())
		
		for(RunSample runSample in runSamples) {
			assertNotNull(runSample)
			assertNotNull(runSample.sample)
			assertNotNull(runSample.sample.subject)
		}
		
		Process sr2 = Process.findByChipcode("G0698756")
		assertNotNull(sr2)
		assertEquals("Sequenom", sr2.panel.technology)
		assertEquals("G0698756", sr2.getChipcode())
		
		runSamples = sr2.runSamples
		assertEquals(1, runSamples.size())
		
		for(RunSample runSample in runSamples) {
			assertNotNull(runSample)
			assertNotNull(runSample.sample)
			assertNotNull(runSample.sample.subject)
		}

		// Unpack and check the date
		Calendar cal = Calendar.getInstance();
		cal.setTime(sr1.getDate())
		assertEquals(2011, cal.get(Calendar.YEAR))
		assertEquals(3, cal.get(Calendar.MONTH))
		assertEquals(20, cal.get(Calendar.DATE))

		assertEquals(1, Subject.findByPatientId("GEN-002").reports.size())
		assertEquals(1, Subject.findByPatientId("GEN-003").reports.size())

		// Sequenom, patient count will be more interesting
		List<Subject> patients = source.getPatients()
		assertEquals 2, patients.size()

		Integer patientCount = source.getPatientCount()
		assertEquals 2, patientCount
		
		List<Panel> panels = source.getPanels()
		assertEquals 1, panels.size()
		
		Panel panel = panels.get(0)
		assertEquals "Sequenom", panel.technology
    }

	/**
	 * Tests basic pipeline running. This is now refactored as a service so the
	 * pipeline details are no longer embedded in the controller. This makes testing
	 * very much easier, as we can do it through unit testing. 
	 * 
	 * This test also checks the handling of unknown assays, which depends on 
	 * some different logic to the standard Sequenom handling, as there ought
	 * to be another assay reported with an unknown status, but no observed
	 * mutations can really be reported. 
	 */
    void testPipelineServiceSequenom2() {

		def source = createSubmission('Sequenom', "test/data/sequenom_test_02.xls");
		def state = pipelineService.getPipelineState(source)
		pipelineService.runPipeline(state)
		
		// Check the state is OK
		checkState(state)
		
		// Now for a few additional validations
		def runs = Process.getAll()
		assertEquals(2, runs.size())
		
		// Both runs will be Sequenom, so let's simply choose one by chip code
		// Doesn't matter which we get
		Process sr1 = Process.findByChipcode("G0696400")
		assertNotNull(sr1)
		assertEquals("Sequenom", sr1.panel.technology)
		assertEquals("G0696400", sr1.getChipcode())
		
		def runSamples = sr1.runSamples
		assertEquals(1, runSamples.size())
		
		for(RunSample runSample in runSamples) {
			assertNotNull(runSample)
			assertNotNull(runSample.sample)
			assertNotNull(runSample.sample.subject)
		}
		
		// Unpack and check the date
		Calendar cal = Calendar.getInstance();
		cal.setTime(sr1.getDate())
		assertEquals(2011, cal.get(Calendar.YEAR))
		assertEquals(4, cal.get(Calendar.MONTH))
		assertEquals(19, cal.get(Calendar.DATE))

		assertEquals(0, Subject.findByPatientId("GEN-002").reports.size())
		assertEquals(1, Subject.findByPatientId("GEN-003").reports.size())
    }

	/**
	 * Tests basic pipeline running. This is now refactored as a service so the
	 * pipeline details are no longer embedded in the controller. This makes testing
	 * very much easier, as we can do it through unit testing.
	 */
    void testPipelineServiceSanger1() {

		def source = createSubmission('ABI', "test/data/sanger_test_02.xls");
		def state = pipelineService.getPipelineState(source)
		pipelineService.runPipeline(state)
		
		// Check the state is OK
		checkState(state)
		
		// Now for a few additional validations
		def runs = Process.getAll()
		assertEquals(3, runs.size())
		
		// Both runs will be Sanger, but let's simply choose one by runId. Since the
		// Sanger template doesn't define a runId, we cannot really use these in
		// testing. 
		Process sr1 = Process.findByRunId("PMH002BIOXFOR1")
		assertNotNull(sr1)
		assertEquals("ABI", sr1.panel.technology)
		assertEquals("PMH002BIOXFOR1", sr1.getRunId())
		
		def runSamples = sr1.runSamples
		assertEquals(1, runSamples.size())
		
		for(RunSample runSample in runSamples) {
			assertNotNull(runSample)
			assertNotNull(runSample.sample)
			assertNotNull(runSample.sample.subject)
		}
		
		Process sr2 = Process.findByRunId("PMH003BIOXFRZ1")
		assertNotNull(sr2)
		assertEquals("ABI", sr2.panel.technology)
		assertEquals("PMH003BIOXFRZ1", sr2.getRunId())
		
		runSamples = sr2.runSamples
		assertEquals(1, runSamples.size())
		
		for(RunSample runSample in runSamples) {
			assertNotNull(runSample)
			assertNotNull(runSample.sample)
			assertNotNull(runSample.sample.subject)
		}
		
		RunSample runSample = runSamples.iterator().next()
		assertEquals(1, runSample.mutations.size())
		
		ObservedMutation mut = runSample.mutations.iterator().next()
		assertEquals("PIK3CA", mut.knownMutation.gene)

		// Unpack and check the date
		Calendar cal = Calendar.getInstance();
		cal.setTime(sr1.getDate())
		assertEquals(2011, cal.get(Calendar.YEAR))
		assertEquals(3, cal.get(Calendar.MONTH))
		assertEquals(20, cal.get(Calendar.DATE))
    }

	/**
	 * Tests basic pipeline running. This is now refactored as a service so the
	 * pipeline details are no longer embedded in the controller. This makes testing
	 * very much easier, as we can do it through unit testing.
	 */
    void testPipelineServiceSanger2() {

		def source = createSubmission('ABI', "test/data/sanger_test_03.xls");
		def state = pipelineService.getPipelineState(source)
		pipelineService.runPipeline(state)
		
		// Check the state is OK
		checkState(state)
		
		// Now for a few additional validations
		def runs = Process.getAll()
		assertEquals(2, runs.size())
		
		// Both runs will be Sanger, but let's simply choose one by runId. Since the
		// Sanger template doesn't define a runId, we cannot really use these in
		// testing. 
		Process sr1 = Process.findByRunId("PMH002BIOXFOR1")
		assertNotNull(sr1)
		assertEquals("ABI", sr1.panel.technology)
		assertEquals("PMH002BIOXFOR1", sr1.getRunId())
		
		def runSamples = sr1.runSamples
		assertEquals(1, runSamples.size())
		
		for(RunSample runSample in runSamples) {
			assertNotNull(runSample)
			assertNotNull(runSample.sample)
			assertNotNull(runSample.sample.subject)
		}
		
		Process sr2 = Process.findByRunId("PMH003BIOXFOR1")
		assertNotNull(sr2)
		assertEquals("ABI", sr2.panel.technology)
		assertEquals("PMH003BIOXFOR1", sr2.getRunId())
		
		runSamples = sr2.runSamples
		assertEquals(1, runSamples.size())
		
		for(RunSample runSample in runSamples) {
			assertNotNull(runSample)
			assertNotNull(runSample.sample)
			assertNotNull(runSample.sample.subject)
		}

		// Unpack and check the date
		Calendar cal = Calendar.getInstance();
		cal.setTime(sr1.getDate())
		assertEquals(2011, cal.get(Calendar.YEAR))
		assertEquals(3, cal.get(Calendar.MONTH))
		assertEquals(20, cal.get(Calendar.DATE))
    }

    void testPipelineServicePacBio1() {
		
		def source = createSubmission('PacBioV2', "test/data/pacbio_test_07.xls")
		def state = pipelineService.getPipelineState(source)
		pipelineService.runPipeline(state)
		
		// Check the state is OK
		checkState(state)

		// Now for a few additional validations
		def runs = Process.getAll()
		assertEquals(1, runs.size())
		
		// Check that even when we have no mutations reported, we get an association between 
		// the run and the sample
		Process sr1 = runs[0]
		assertNotNull(sr1)

		def runSamples = sr1.runSamples
		assertEquals(1, runSamples.size())
		
		for(RunSample runSample in runSamples) {
			assertNotNull(runSample)
			assertNotNull(runSample.sample)
		}

		assertEquals(1, Subject.findByPatientId("GEN-003").reports.size())

		// Old style PacBio, the patient count must always be one
		Integer patientCount = source.getPatientCount()
		assertEquals 1, patientCount
		
		List<Subject> patients = source.getPatients()
		assertEquals 1, patients.size()
    }

    void testPipelineServicePacBio2() {
		
		def source = createSubmission('PacBioV2', "test/data/pacbio_test_04.xls")
		def state = pipelineService.getPipelineState(source)
		
		assertNotNull(state)
		
		// This file contains a non-existent mutation, and being PacBio, we test that it 
		// does not exist before the file is loaded, and that it does exist after. In fact,
		// it is a made-up mutation
		def found = KnownMutation.findByGeneAndMutation("PDGFRA", "D1074D")
		assertNull(found)
		
		pipelineService.runPipeline(state)

		// And afterwards, the mutation should exist
		found = KnownMutation.findByGeneAndMutation("PDGFRA", "D1074D")
		assertNotNull(found)
		assertTrue(found.visible)
    }

    void testPipelineServicePacBio3() {
		
		// Has an incorrect association between patients and samples
		def source = createSubmission('PacBioV2', "test/data/pacbio_test_05.xls")
		def state = pipelineService.getPipelineState(source)
		
		assertNotNull(state)
		
		shouldFail (PipelineRuntimeException) {
			pipelineService.runPipeline(state)
		}
		
		def errors = state.errors
		assertEquals(1, errors.size())

		// New style PacBio, patient count will be more interesting
		List<Subject> patients = source.getPatients()
		assertEquals 2, patients.size()

		Integer patientCount = source.getPatientCount()
		assertEquals 2, patientCount
		
		List<Panel> panels = source.getPanels()
		assertEquals 1, panels.size()
		
		Panel panel = panels.get(0)
		assertEquals "PacBio", panel.technology
    }

	/**
	 * Tests a novel mutation, in PIK3CA - well, not entirely novel in that it is in COSMIC, 
	 * and really we ought to pick up properly the association with the known mutation in
	 * COSMIC. The mutation to test is PIK3CA W1051* - which is a slightly interesting one
	 * compared to the usual stuff we have been testing with. 
	 */
    void testPipelineServicePacBio4() {
		
		// Has an incorrect association between patients and samples
		def source = createSubmission('PacBioV2', "test/data/pacbio_test_06.xls")
		def state = pipelineService.getPipelineState(source)
		
		assertNotNull(state)
		
		shouldFail (PipelineRuntimeException) {
			pipelineService.runPipeline(state)
		}
		
		def errors = state.errors
		assertEquals(1, errors.size())

		// New style PacBio, patient count will be more interesting
		List<Subject> patients = source.getPatients()
		assertEquals 2, patients.size()

		Integer patientCount = source.getPatientCount()
		assertEquals 2, patientCount
		
		List<Panel> panels = source.getPanels()
		assertEquals 1, panels.size()
		
		Panel panel = panels.get(0)
		assertEquals "PacBio", panel.technology
    }

	/**
	 * Tests that duplicating and loading an entire submission does not cause any problem.
	 * Obviously, we don't want to have to worry too much about duplicated sequencing runs.
	 * This requires data to actually be persisted between tests. 
	 */
    void testPipelineServicePacBioDuplicate() {

		def source1 = createSubmission('PacBioV2', "test/data/pacbio_test_07.xls")
		def state1 = pipelineService.getPipelineState(source1)
		pipelineService.runPipeline(state1)
		
		// Check the state is OK
		checkState(state1)
		
		def source2 = createSubmission('PacBioV2', "test/data/pacbio_test_07.xls")
		def state2 = pipelineService.getPipelineState(source2)
		pipelineService.runPipeline(state2)

		// Check the state is OK - if we fail to handle duplicates, this will break
		checkState(state2)
    }

	/**
	 * Tests that a PacBio file that refers to an unknown patient is flagged as an 
	 * error. 
	 */
    void testPipelineServicePacBioInvalidPatientId() {

		def source = createSubmission('PacBioV2', "test/data/pacbio_test_fail_01.xls")
		def state = pipelineService.getPipelineState(source)
		assertNotNull(state)
		
		shouldFail (PipelineRuntimeException) { 
			pipelineService.runPipeline(state)
		}
		
		def errors = state.errors
		assertEquals(2, errors.size())
		assertEquals("data.incorrect.patient", errors[0].key)
		assertEquals("data.incorrect.patient", errors[1].key)
    }
	
	/**
	 * Tests that a PacBio file that refers to an unknown sample is flagged as an 
	 * error. 
	 */
    void testPipelineServicePacBioInvalidSampleId() {

		def source = createSubmission('PacBioV2', "test/data/pacbio_test_fail_02.xls")
		def state = pipelineService.getPipelineState(source)
		assertNotNull(state)

		shouldFail (PipelineRuntimeException) { 
			pipelineService.runPipeline(state)
		}
		
		def errors = state.errors
		assertEquals(2, errors.size())
		assertEquals("data.missing.sample", errors[0].key)
		assertEquals("data.missing.sample", errors[1].key)
    }
	
	/**
	 * Tests that a PacBio file that refers to an unknown panel is flagged as an 
	 * error. 
	 */

    void testPipelineServicePacBioInvalidPanel1() {

		def source = createSubmission('PacBioV2', "test/data/pacbio_test_fail_04.xls")
		def state = pipelineService.getPipelineState(source)
		assertNotNull(state)

		shouldFail (PipelineRuntimeException) { 
			pipelineService.runPipeline(state)
		}
		
		def errors = state.errors
		
		def invalid = errors.find { it.key == "data.unknown.panel" }
		assertNotNull(invalid)
    }
	
	/**
	 * Tests that a Sequenom file that refers to an unknown patient is flagged as an 
	 * error. 
	 */

    void testPipelineServiceSequenomInvalidPatientId() {

		def source = createSubmission('Sequenom', "test/data/sequenom_test_fail_01.xls")
		def state = pipelineService.getPipelineState(source)
		assertNotNull(state)
		
		shouldFail (PipelineRuntimeException) {
			pipelineService.runPipeline(state)
		}
		
		def errors = state.errors
		assertEquals(1, errors.size())
		assertEquals("data.incorrect.patient", errors[0].key)
    }
	
	/**
	 * Tests that a Sequenom file that refers to an unknown sample is flagged as an 
	 * error. 
	 */

    void testPipelineServiceSequenomInvalidSampleId() {

		def source = createSubmission('Sequenom', "test/data/sequenom_test_fail_02.xls")
		def state = pipelineService.getPipelineState(source)
		assertNotNull(state)
		
		shouldFail (PipelineRuntimeException) {
			pipelineService.runPipeline(state)
		}
		
		def errors = state.errors
		assertEquals(1, errors.size())
		assertEquals("data.missing.sample", errors[0].key)
    }
	
	/**
	 * Tests that a Sequenom file that refers to an unknown mutation is flagged as an 
	 * error. 
	 */

	@Ignore
    void testPipelineServiceSequenomInvalidMutation() {

		def source = createSubmission('Sequenom', "test/data/sequenom_test_fail_03.xls")
		def state = pipelineService.getPipelineState(source)

		shouldFail (PipelineRuntimeException) {
			pipelineService.runPipeline(state)
		}

		assertNotNull(state)
		def errors = state.errors
		assertEquals(1, errors.size())
		assertEquals("data.unknown.mutation", errors[0].key)
    }
	
	/**
	 * Private method used to generate and persist a submission preparatory to testing
	 * a pipeline
	 * @return a new submission
	 */
	private Submission createSubmission(String type, String filename) {
		Submission sub = new Submission()
		
		File file = new File(filename);
		sub.dataType = type
		sub.userName = 'user'
		sub.dateSubmitted = new Date()
		sub.fileName = file.name
		sub.fileContents = file.getBytes()
		sub.save(failOnError:true)
		
		return sub
	}
}
