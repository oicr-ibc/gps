package ca.on.oicr.gps.pages

import grails.test.*
import org.htmlparser.Parser
import org.htmlparser.filters.TagNameFilter

import ca.on.oicr.gps.controller.PanelController;
import ca.on.oicr.gps.model.laboratory.Panel;

/**
 * @author swatt
 * 
 * This test class does some round-trip testing between the data and the results
 * of the GSP rendering. The actual tests are fairly limited, as most of the actual 
 * test - apart from the data - can be mangled by internationalization. 
 */
class PanelGroovyPagesTests extends GroovyPagesTestCase {
	
	def panelController
	
	public void setUp() {
		super.setUp()
		panelController = new PanelController()
	}
	
	public void tearDown() {
		super.tearDown()
	}
	
	/**
	 * Test the submission controller's ability to set up to create a new
	 * submission.
	 */
    void testShowSolidPanel() {
		
		def panel = Panel.findByNameAndVersionString("SolidTumor", "1.0.0")
		panelController.params.id = panel.id
		def model = panelController.show()
		
		def file = new File("grails-app/views/panel/show.gsp")
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
    }
}