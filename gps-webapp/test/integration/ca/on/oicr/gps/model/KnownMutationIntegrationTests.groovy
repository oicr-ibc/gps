package ca.on.oicr.gps.model

import ca.on.oicr.gps.model.knowledge.KnownGene;
import ca.on.oicr.gps.model.knowledge.KnownMutation;
import ca.on.oicr.gps.model.knowledge.KnownTumourType;
import grails.test.*

class KnownMutationIntegrationTests extends GroovyTestCase {
    public void setUp() {
        super.setUp()
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Simple integration test to check we can find a gene. 
	 */
    void testFindGeneCDK4() {
		
		def criteria = KnownGene.createCriteria()
		def findCDK4 = criteria.get {
			eq("name", "CDK4")
		}
		
		assertNotNull(findCDK4)
		assertEquals("CDK4", findCDK4.name)
    }

	/**
	 * Simple integration test to check we can find a tumour type. 
	 */
    void testFindTumourType() {
		
		def criteria = KnownTumourType.createCriteria()
		def findLiverCarcinoma = criteria.get {
			eq("name", "liver carcinoma")
		}
		
		assertNotNull(findLiverCarcinoma)
		assertEquals("liver carcinoma", findLiverCarcinoma.name)
    }

	/**
	 * Simple integration test to check we can find mutations associated with a particular gene.
	 */
    void testFindMutations() {
		
		def criteria = KnownMutation.createCriteria()
		def foundMutations = criteria.list {
			knownGene {
				eq("name", "CDK4")
			}
		}
		
		assertEquals(9, foundMutations.size)
    }
	
	void testFrequencies() {
		def criteria = KnownMutation.createCriteria()
		def foundMutation = criteria.get {
			knownGene {
				eq("name", "BRAF")
			}
			eq("mutation", "V600E")
		}
		
		assertNotNull(foundMutation)
		assertEquals(49, foundMutation.frequencies.size())
		
		// Now, choose one from the COSMIC file
		def frequency = foundMutation.frequencies.find {
			it.tumourType.name.equals("adrenal gland adrenal cortical adenoma")
		}
		assertNotNull(frequency)
		assertEquals(0.0196078, frequency.frequency, 0.00001)
		
		frequency = foundMutation.frequencies.find {
			it.tumourType.name.equals("autonomic ganglia neuroblastoma")
		}
		// Being nice, we will also choose a second
		assertNotNull(frequency)
		assertEquals(0.00819672, frequency.frequency, 0.00001)
	}
	
	/**
	 * Another integration test, to also find CDK4 mutations, but done a rather simpler way.
	 * Of course, the result is completely different, thanks Grails
	 */
    void testReadMutationsDirectly() {
		
		def gene = KnownGene.findByName("CDK4")
		assertNotNull(gene)
		
		def foundMutations = gene.knownMutations
		assertEquals(9, foundMutations.size())
    }
	
	/**
	 * Added a new test to validate the sort ordering of the lists returned
	 * from the domain static methods.
	 */
	void testGeneSortOrder() {
		def genes = KnownGene.findGenes("")
		def cdk4Position = genes.findIndexOf { it.name == "CDK4" }
		def kitPosition = genes.findIndexOf { it.name == "KIT" }
		def krasPosition = genes.findIndexOf { it.name == "KRAS" }
		
		assertTrue(cdk4Position < kitPosition)
		assertTrue(cdk4Position < krasPosition)
		assertTrue(kitPosition < krasPosition)
	}

	/**
	 * Added a new test to validate the sort ordering of the lists returned
	 * from the domain static methods.
	 */
	void testMutationSortOrder() {
		def muts = KnownMutation.findMutations("")
		def BRAFV400EPosition = muts.findIndexOf { it.mutation == "V600E" && it.knownGene.name == "BRAF" }
		def EGFRT790MPosition = muts.findIndexOf { it.mutation == "T790M" && it.knownGene.name == "EGFR" }
		def ABL1Y253FPosition = muts.findIndexOf { it.mutation == "Y253F" && it.knownGene.name == "ABL1" }
		
		assertTrue(BRAFV400EPosition < EGFRT790MPosition)
		assertTrue(ABL1Y253FPosition < BRAFV400EPosition)
		assertTrue(ABL1Y253FPosition < EGFRT790MPosition)
	}
	
	/**
	 * A mutation that cannot exist, really. 
	 */
	void testMissingMutation() {
		def muts = KnownMutation.findMutations("Zipo Bibrok 5x10^8")
		assertTrue(muts != null)
		assertEquals(0, muts.size())
	}

	/**
	 * A mutation that does exist, but which has never been reported and is 
	 * therefore invisible in the knowledge base.
	 */
	void testNovelMutation() {
		def muts = KnownMutation.findMutations("PIK3CA Q546L")
		assertTrue(muts != null)
		assertEquals(1, muts.size())
	}
}
