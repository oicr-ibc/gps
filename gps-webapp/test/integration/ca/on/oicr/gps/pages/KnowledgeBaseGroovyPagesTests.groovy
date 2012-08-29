package ca.on.oicr.gps.pages

import grails.test.*
import org.htmlparser.Parser
import org.htmlparser.filters.TagNameFilter

import ca.on.oicr.gps.controller.KnowledgeController;

/**
 * @author swatt
 * 
 * This test class does some round-trip testing between the data and the results
 * of the GSP rendering. The actual tests are fairly limited, as most of the actual 
 * test - apart from the data - can be mangled by internationalization. 
 */
class KnowledgeBaseGroovyPagesTests extends GroovyPagesTestCase {
	
	def knowledgeController
	
	public void setUp() {
		super.setUp()
		knowledgeController = new KnowledgeController()
	}
	
	public void tearDown() {
		super.tearDown()
	}
	
	/**
	 * Test the submission controller's ability to set up to create a new
	 * submission.
	 */
    void testMutation_KRAS_G12A() {
		
		knowledgeController.params.mutation = "KRAS G12A"
		def model = knowledgeController.mutation()
		
		def file = new File("grails-app/views/knowledge/mutation.gsp")
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
    }

	/**
	 * Test the page for a mutation
	 */
    void testMutation_BRAF_V600E() {
		
		knowledgeController.params.mutation = "BRAF V600E"
		def model = knowledgeController.mutation()
		
		def file = new File("grails-app/views/knowledge/mutation.gsp")
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
    }

	/**
	 * Test the page for a gene
	 */
    void testGene_PDGFRA() {
		
		knowledgeController.params.gene = "PDGFRA"
		def model = knowledgeController.gene()
		
		def file = new File("grails-app/views/knowledge/gene.gsp")
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
		
		assertTrue(textString.contains("PDGFRA I843_S847>T"))
		assertFalse(textString.contains("PDGFRA A1014_S1016delADS"))
    }

	/**
	 * A more complex mutation, with different frequencies and clinical information
	 * that deserve more complete validation. 
	 */
	void testMutation_NRAS_Q61K() {

		knowledgeController.params.mutation = "NRAS Q61K"
		def model = knowledgeController.mutation()
		
		def file = new File("grails-app/views/knowledge/mutation.gsp")
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
	}

	/**
	 * Test the page for a mutation's status
	 */
    void testMutationStatus_BRAF_V600E() {
		
		knowledgeController.params.mutation = "BRAF V600E"
		def model = knowledgeController.mutation()
		
		def file = new File("grails-app/views/knowledge/mutation.gsp")
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
		
		assertTrue((textString =~ /(?i)Status:\s+not\s+confirmed/).find())
    }
}