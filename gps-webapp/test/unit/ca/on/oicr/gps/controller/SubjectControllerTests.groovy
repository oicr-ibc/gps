package ca.on.oicr.gps.controller

import grails.test.*

/**
 * Unit testing for the SubjectController. 
 * 
 * @author swatt
 */

class SubjectControllerTests extends ControllerUnitTestCase {
    public void setUp() {
        super.setUp()
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Check that the /index action redirects to /list
	 */
    void testIndex() {
		controller.index()
		assertEquals "list", controller.redirectArgs.action
    }
}
