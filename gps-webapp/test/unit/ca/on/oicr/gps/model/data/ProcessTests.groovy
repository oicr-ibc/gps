package ca.on.oicr.gps.model.data

import ca.on.oicr.gps.model.data.Process;
import ca.on.oicr.gps.model.data.Submission;
import ca.on.oicr.gps.model.laboratory.Panel;
import grails.test.*

class ProcessTests extends GrailsUnitTestCase {
	
	def seq
	
    public void setUp() {
        super.setUp()
		
		def panel = new Panel(name: "OncoCarta", versionString: "1.0", technology: "Sequenom")

		seq = new Process()
		seq.runId = '0123456789'
		seq.chipcode = 'ABCDEF0123456789'
		seq.panel = panel
		seq.date = new Date()
		seq.submission = new Submission()
		
		mockDomain(Process, [seq])
		mockDomain(Submission)
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks that the untouched sequence run passes validation
	 */
    void testValidation() {
		// Check that an complete subject will validate
		assertTrue seq.validate()
		assertFalse seq.hasErrors()
    }
	
    /**
	 * Checks that too long a run identifier will fail validation
	 */
	void testRunId() {
		def old = seq.runId
		
		seq.runId = '01234567890123456789012345678901234567890123456789012345678901234567890123456789'
		assertFalse seq.validate()
		assertTrue seq.hasErrors()
		
		seq.runId = old
    }

	/**
	 * Checks that too long a chip code will fail validation
	 */
	void testChipcode() {
		def old = seq.chipcode
		
		seq.chipcode = '01234567890123456789012345678901234567890123456789012345678901234567890123456789'
		assertFalse seq.validate()
		assertTrue seq.hasErrors()
		
		seq.chipcode = old
    }

	/**
	 * Checks that too long a panel will fail validation
	 */
	void testPanel() {
		def old = seq.panel
		
		shouldFail {
			seq.panel = 'This panel has far too long a name and should fail to validate accordingly'
		}
		
		seq.panel = old
    }

	/**
	 * Checks that a null date will not fail validation
	 */
	void testDate() {
		def old = seq.date
		
		seq.date = null
		assertTrue seq.validate()
		assertFalse seq.hasErrors()
		
		seq.date = old
    }
}
