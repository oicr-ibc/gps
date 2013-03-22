package ca.on.oicr.gps.controller

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.springframework.mock.web.MockMultipartFile;

class PanelControllerIntegrationTests extends GroovyTestCase {
	
	Map savedMetaClasses = [:]
	Map renderMap

	def panelController
	
	public void setUp() {
		super.setUp()

		registerMetaClass(PanelController.class)
		PanelController.metaClass.render = {Map m ->
			renderMap = m
		}

		panelController = new PanelController()
	}
	
	public void tearDown() {
		super.tearDown()
	}
	
    void testPanelList() {
		def model = panelController.list()
		assertNotNull(model)
		assertTrue(model.containsKey('panelInstanceList'))
		assertTrue(model.containsKey('panelInstanceTotal'))
		assertEquals(5, model.getAt('panelInstanceTotal'))
    }
	
	void testPanelCreate() {
		InputStream input = new FileInputStream(new File(System.properties['base.dir'], "test/data/hotspot_v2.bed"))
		MockMultipartFile dataFile = new MockMultipartFile("hotspot_v2.bed", "hotspot_v2.bed", "application/octet-stream", input)
		PanelCommand cmd = new PanelCommand(name: "HotSpot", technology: "HotSpot", versionString: "1.0.0", dataFile: dataFile)
		
		panelController.response.format = 'json'
		panelController.save(cmd)
		
		assertEquals("show", renderMap.getAt("view"))
		assertNotNull(renderMap.getAt("model"))
		assertNotNull(renderMap.getAt("model").getAt("panelInstance"))
		assertEquals(660, renderMap.getAt("model").getAt("panelInstance").targets.size())
	}
	
	// Stolen from GrailsUnitTestCase
	/**
	 * Use this method when you plan to perform some meta-programming
	 * on a class. It ensures that any modifications you make will be
	 * cleared at the end of the test.
	 * @param clazz The class to register.
	 */
	protected void registerMetaClass(Class clazz) {
		// If the class has already been registered, then there's
		// nothing to do.
		if (savedMetaClasses.containsKey(clazz)) return

		// Save the class's current meta class.
		savedMetaClasses[clazz] = clazz.metaClass

		// Create a new EMC for the class and attach it.
		def emc = new ExpandoMetaClass(clazz, true, true)
		emc.initialize()
		GroovySystem.metaClassRegistry.setMetaClass(clazz, emc)
	}
}
