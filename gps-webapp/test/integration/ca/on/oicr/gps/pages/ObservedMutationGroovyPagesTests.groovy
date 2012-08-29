package ca.on.oicr.gps.pages

import grails.test.*
import org.htmlparser.Parser
import org.htmlparser.filters.TagNameFilter

import ca.on.oicr.gps.controller.ObservedMutationController;

/**
 * @author swatt
 * 
 * This test class does some round-trip testing between the data and the results
 * of the GSP rendering. The actual tests are fairly limited, as most of the actual 
 * test - apart from the data - can be mangled by internationalization. 
 */
class ObservedMutationGroovyPagesTests extends GroovyPagesTestCase {
	
	def observedMutationController
	
	public void setUp() {
		super.setUp()
		observedMutationController = new ObservedMutationController()
	}
	
	public void tearDown() {
		super.tearDown()
	}
	
	/**
	 * Test the submission controller's ability to set up to create a new
	 * submission.
	 */
    void testObservedMutationList() {
		
		def model = observedMutationController.list()
		
		def file = new File("grails-app/views/observedMutation/list.gsp")
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
    }
}
