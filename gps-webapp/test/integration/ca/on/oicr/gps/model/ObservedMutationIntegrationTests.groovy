package ca.on.oicr.gps.model

import ca.on.oicr.gps.model.data.ObservedMutation;
import grails.test.*

class ObservedMutationIntegrationTests extends GroovyTestCase {
    public void setUp() {
        super.setUp()
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Simple integration test to check we can find a gene. 
	 */
    void testGetMutationSummary() {
		
		def parameters = [:]
		def mutations = ObservedMutation.getMutationSummary(parameters)
		
		assertNotNull(mutations)
		assertEquals(0, mutations.size())
    }
}
