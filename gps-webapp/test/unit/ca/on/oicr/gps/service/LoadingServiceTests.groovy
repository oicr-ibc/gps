package ca.on.oicr.gps.service

import ca.on.oicr.gps.model.knowledge.KnownMutation;
import ca.on.oicr.gps.model.laboratory.Target;
import ca.on.oicr.gps.model.laboratory.Panel;
import grails.test.*

class LoadingServiceTests extends GrailsUnitTestCase {
	
	def loadingService
	
    public void setUp() {
        super.setUp()
		
		mockLogging(LoadingService)
		loadingService = new LoadingService()
    }

    public void tearDown() {
        super.tearDown()
    }

    void testLoadingAssay() {
		
		mockDomain(Target)
		mockDomain(Panel)

		// First, locate a file, and build up a reader
        // Only assumes run from parent app
		def file
		def location = "data/panels/oncocarta_v1.0/panel_targets.csv"
		if (new File("heliotrope-app").exists()) {
		    file = new File("heliotrope-app/${location}")
		} else {
			file = new File(location)
		}
		assertTrue(file.exists())
		
		loadingService.loadPanelAndTargets("OncoCarta", "1.0", "Sequenom", file)
		
		// Now we can use the mocked version of the domain to do tests
		def panels = Panel.list()
		assertEquals(1, panels.size())
		
		def assays = panels[0].targets
		assertEquals(293, assays.size())
    }
}
