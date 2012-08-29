package ca.on.oicr.gps.model.data

import ca.on.oicr.gps.model.data.ObservedMutation;
import ca.on.oicr.gps.model.data.Sample;
import ca.on.oicr.gps.model.data.Process;
import ca.on.oicr.gps.model.knowledge.KnownMutation;
import grails.test.*

class ObservedMutationTests extends GrailsUnitTestCase {
	
	def mutation
	
    public void setUp() {
        super.setUp()
		
		def runSample = new RunSample()
		mockDomain(RunSample, [runSample])
		
		def knownMutation = new KnownMutation()
		knownMutation.build = 'hg19'
		knownMutation.gene = 'FGFR3'
		knownMutation.refAllele = 'G'
		knownMutation.varAllele = 'A'
		knownMutation.chromosome = 4
		knownMutation.start = 1807894
		knownMutation.stop = 1807894
		knownMutation.mutation = 'T651T'
		mockDomain(KnownMutation, [knownMutation])
		
		mutation = new ObservedMutation()
		mutation.knownMutation = knownMutation
		mutation.runSample = runSample
		mutation.frequency = 0.5
		mutation.status = ObservedMutation.MUTATION_STATUS_FOUND
		
		mockDomain(ObservedMutation, [mutation])
    }

    public void tearDown() {
        super.tearDown()
    }
	
	void testComparability() {
		assertEquals 0, mutation.compareTo(mutation)
		assertFalse mutation.compareTo(new ObservedMutation()) == 0
		
		shouldFail { mutation.compareTo(mutation.knownMutation) }
		shouldFail { mutation.compareTo(null) }
	}

	/**
	 * Checks that the untouched mutation object passes validation
	 */
    void testValidation() {
		def result = mutation.validate()
		mutation.errors.allErrors.each { log.info it.toString() }
		assertTrue result
		assertFalse mutation.hasErrors()
    }

	/**
	 * Checks that leaving the owning components null fails validation
	 */
    void testOwners() {
		def old = mutation.runSample
		
		mutation.runSample = null
		assertFalse mutation.validate()
		assertTrue mutation.hasErrors()
		
		shouldFail { sample.runSample = '' }

		shouldFail { sample.runSample = 'abcd0123456789ABCDEFGHIJ' }
		
		mutation.runSample = old
    }

	/**
	* Checks that a correct status will pass validation
	*/
	void testStatus () {
		def old = mutation.status
	   
		mutation.status = ObservedMutation.MUTATION_STATUS_UNKNOWN
		assertTrue mutation.validate()
		assertFalse mutation.hasErrors()

		mutation.status = old
	}

	/**
	* Checks that a correct frequency will pass validation
	*/
	void testFrequency () {
		def old = mutation.frequency
	   
		// Check a null frequency is OK
		mutation.frequency = null
		assertTrue mutation.validate()
		assertFalse mutation.hasErrors()

		mutation.frequency = old
	}

	/**
	* Checks that an incorrect status will fail validation
	*/
	void testConfidence () {
		def old = mutation.confidence
	   
		mutation.confidence = 'None'
		assertFalse mutation.validate()
		assertTrue mutation.hasErrors()

		mutation.confidence = old
	}
}
