package ca.on.oicr.gps.model.knowledge

import grails.test.*

class KnownMutationTests extends GrailsUnitTestCase {
	
	def mutation
	
    public void setUp() {
        super.setUp()

		mutation = new KnownMutation()

		mutation.build = 'hg19'
		mutation.gene = 'FGFR3'
		mutation.refAllele = 'G'
		mutation.varAllele = 'A'
		mutation.chromosome = '4'
		mutation.start = 1807894
		mutation.stop = 1807894
		mutation.mutation = 'T651T'

		mockDomain(KnownMutation, [mutation])
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks that too long a gene will fail validation
	 */
	void testGene () {
		def old = mutation.gene
		
		mutation.gene = 'abcd0123456789ABCDEFGHIJ'
		assertFalse mutation.validate()
		assertTrue mutation.hasErrors()
		
		mutation.gene = old
	}

	/**
	 * Checks that a null, blank, or too long a refAllege will fail validation
	 */
	void testRefAllele () {
		def old = mutation.refAllele
		
		mutation.refAllele = null
		assertFalse mutation.validate()
		assertTrue mutation.hasErrors()

		mutation.refAllele = ''
		assertFalse mutation.validate()
		assertTrue mutation.hasErrors()

		mutation.refAllele = 'abcdefghij0123456789ABCDEFGHIJ0123456789abcdefghij'
		assertFalse mutation.validate()
		assertTrue mutation.hasErrors()
		
		mutation.refAllele = old
	}

	/**
	* Checks that a null, blank, or too long an allege will fail validation
	*/
   void testAllele () {
	   def old = mutation.varAllele
	   
	   mutation.varAllele = null
	   assertFalse mutation.validate()
	   assertTrue mutation.hasErrors()

	   mutation.varAllele = ''
	   assertFalse mutation.validate()
	   assertTrue mutation.hasErrors()

	   mutation.varAllele = 'abcdefghij0123456789ABCDEFGHIJ0123456789abcdefghij'
	   assertFalse mutation.validate()
	   assertTrue mutation.hasErrors()
	   
	   mutation.varAllele = old
   }

   /**
	* Checks that a null, blank, or an integer chromosome will fail validation
	*/
   void testChromosome () {
	   def old = mutation.chromosome
	   
	   mutation.chromosome = null
	   assertFalse mutation.validate()
	   assertTrue mutation.hasErrors()

	   mutation.chromosome = old
   }

   /**
	* Checks that a null start will fail validation
	*/
   void testStart () {
	   def old = mutation.start
	   
	   shouldFail { mutation.start = null }
	   
	   mutation.start = old
   }

   /**
	* Checks that a null stop will fail validation
	*/
   void testStop () {
	   def old = mutation.stop
	   
	   shouldFail { mutation.stop = null }

	   mutation.stop = old
   }
}
