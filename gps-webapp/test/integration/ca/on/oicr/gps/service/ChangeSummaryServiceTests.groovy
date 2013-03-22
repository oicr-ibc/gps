package ca.on.oicr.gps.service

import grails.test.*

class ChangeSummaryServiceTests extends GroovyTestCase {
	
	def changeSummaryService
	
    public void setUp() {
        super.setUp()
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Tests the basic logic of the change summary service. This is used to generate
	 * the change summaries to be sent in email notifications, and in the pages used
	 * to provide the more detailed summary of recent actions.
	 */
    void testChangeSummaryService() {
		
		assertNotNull(changeSummaryService)
    }
}
