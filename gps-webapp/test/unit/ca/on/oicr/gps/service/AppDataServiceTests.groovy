package ca.on.oicr.gps.service

import ca.on.oicr.gps.service.AppDataService;
import ca.on.oicr.gps.model.system.AppData;
import grails.test.*

class AppDataServiceTests extends GrailsUnitTestCase {
	
    public void setUp() {
        super.setUp()
		
		// Mock ye olde domain
		mockDomain(AppData)
    }

    public void tearDown() {
        super.tearDown()
    }

    void testSomething() {
		def appDataService = new AppDataService()
		
		// Check it is initially empty
		assertNull(appDataService.getAttribute('property'))
		
		appDataService.setAttribute('property', 'Value 1')
		
		// Check it is no longer empty
		assertNotNull(appDataService.getAttribute('property'))
		
		// Check we got the right value
		assertSame('Value 1', appDataService.getAttribute('property'))
		
		// Now let's change it, we know this requires an update or delete
		appDataService.setAttribute('property', 'Value 2')
		
		// Check it is no longer empty
		assertNotNull(appDataService.getAttribute('property'))
		
		// Check we got the right value
		assertSame('Value 2', appDataService.getAttribute('property'))
    }
}
