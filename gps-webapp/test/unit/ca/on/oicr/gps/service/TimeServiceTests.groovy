package ca.on.oicr.gps.service

import org.joda.time.DateTime;

import ca.on.oicr.gps.service.TimeService;

import grails.test.*

class TimeServiceTests extends GrailsUnitTestCase {

	public void setUp() {
        super.setUp()
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks the service returns now
	 */
	void testNow() {
		def timeService = new TimeService()
		
		def now = timeService.now()
		assertNotNull(now)
	}		
}
