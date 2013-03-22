package ca.on.oicr.gps.model.knowledge

import grails.test.*


class KnowledgeBaseTests extends GroovyTestCase {
	
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
		assertEquals(findCDK4.name, "CDK4")
    }
	
	private def getMutation() {
		def criteria = KnownMutation.createCriteria()
		def foundMutation = criteria.get {
			eq("mutation", "G12A")
			knownGene {
				eq("name", "KRAS")
				
			}
		}
		return foundMutation
	}
	
	/**
	 * Simple integration test to check we can find a mutation. 
	 */
    void testFindMutation() {
		
		def foundMutation = getMutation()		
		assertNotNull(foundMutation)
    }
	
	/** 
	 * Given this same mutation, repeat the location but then check the property
	 * details
	 */
    void testFindMutationProperties() {
		
		KnownMutation mut = getMutation()
		
		assertNotNull(mut.characteristics)
		assertNotNull(mut.confirmations)
		assertNotNull(mut.sensitivity)
		assertNotNull(mut.effectiveness)

		assertEquals(3, mut.confirmations.size())
		assertEquals(2, mut.sensitivity.size())
		assertEquals(1, mut.effectiveness.size())
    }
}
