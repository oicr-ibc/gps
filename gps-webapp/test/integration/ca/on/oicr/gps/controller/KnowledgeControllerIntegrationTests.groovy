package ca.on.oicr.gps.controller

import java.util.Map;

class KnowledgeControllerIntegrationTests extends GroovyTestCase {
	
	def knowledgeController
	
	public void setUp() {
		super.setUp()

		knowledgeController = new KnowledgeController()
	}

    void testMutation() {
		
		knowledgeController.params.mutation = "KRAS G12A"
		def model = knowledgeController.mutation()
		
		assertNotNull(model)
		
		assertEquals("KRAS", model.mutation.gene)
		assertEquals("KRAS", model.mutation.knownGene.name)
    }
	
	void testObservedMutation() {
		knowledgeController.params.mutation = "EGFR T751A"
		def model = knowledgeController.mutation()
		
		assertNotNull(model)
		assertEquals("EGFR", model.mutation.gene)
		assertEquals("EGFR", model.mutation.knownGene.name)
	}
	
	void testGene() {
		knowledgeController.params.gene = "PPAPDC3"
		def model = knowledgeController.gene()
		assertNotNull(model)
		
		assertTrue(model.containsKey("gene"))
		def gene = model.gene
		
		assertEquals("PPAPDC3", gene.name)
		
		def characteristics = gene.characteristics
		assertNotNull(characteristics)
	}

	/**
	 * Test the action which generates ajax gene list
	 */
	void testFindGene() {
		knowledgeController.params.gene = "AKT"
		knowledgeController.findGene()
		def renderedresponse = knowledgeController.response.contentAsString
		assertNotNull(renderedresponse)
		assertTrue(renderedresponse.contains("AKT1"))
		assertTrue(! renderedresponse.contains("ERBB2"))
	}

	/**
	 * Test the action which generates ajax gene list
	 */
	void testFindAllGenes() {
		knowledgeController.params.gene = ""
		knowledgeController.findGene()
		def renderedresponse = knowledgeController.response.contentAsString
		assertNotNull(renderedresponse)
		assertTrue(renderedresponse.contains("AKT1"))
		assertTrue(renderedresponse.contains("ERBB2"))
	}

	/**
	 * Test the action which generates ajax mutation list
	 */
	void testQueryMutationHtml() {
		knowledgeController.params.term = ""
		knowledgeController.queryMutationHtml()
		def renderedresponse = knowledgeController.response.contentAsString
		assertNotNull(renderedresponse)
	}

	/**
	 * Test the generated page data for a mutation's frequencies
	 */
	void testMutationFrequencies_BRAF_V600E() {
		knowledgeController.params.mutation = "BRAF V600E"
		def model = knowledgeController.setMutation()
		def renderedresponse = knowledgeController.response.contentAsString
		def responseWithoutJS = (renderedresponse =~ /(?s)<script[^>]*>.*?<\/script>/).replaceAll("")
		def textString = (responseWithoutJS =~ /(?s)<[^>]+>/).replaceAll("")
		
		// Make sure we only have 10 tumour types in the frequency section
		assertFalse((renderedresponse =~ /<b>11\. Tumour<\/b>/).find())
	}
	
	/**
	 * Test the generated page data for a mutation's frequencies
	 */
    void testMutationStatus_BRAF_V600E() {
	    knowledgeController.params.mutation = "BRAF V600E"
	    def model = knowledgeController.setMutation()
	    def renderedresponse = knowledgeController.response.contentAsString
	    def responseWithoutJS = (renderedresponse =~ /(?s)<script[^>]*>.*?<\/script>/).replaceAll("")
	    def textString = (responseWithoutJS =~ /(?s)<[^>]+>/).replaceAll("")
	   
	    // Make sure we only have 10 tumour types in the frequency section
	    assertTrue((textString =~ /(?i)Status:\s+not\s+confirmed/).find())
    }
	
	/**
	 * Test the generated page report for a mutation
	 */
	void testMutationReportXML_BRAF_V600E() {
		knowledgeController.params.mutation = "BRAF V600E"
		def model = knowledgeController.getMutationModel()
		def renderedResponse = knowledgeController.getReportXML(model)
		
		assertTrue(renderedResponse.contains("BRAF"))
	}
	
	/**
	 * Test exporting - this test does take a while, and it would perhaps be better
	 * done at unit test stage with mocking. 
	 */
	void testExport() {
		def model = knowledgeController.export()
		def renderedResponse = knowledgeController.response.contentAsString
		
		assertTrue(renderedResponse.contains("BRAF"))
		assertTrue(renderedResponse.contains("V600E"))
	}
	
	/**
	 * Tests the setGene action
	 */
	void testSetGene() {
		knowledgeController.params.gene = "BRAF"
		def model = knowledgeController.setGene()
		def renderedResponse = knowledgeController.response.contentAsString
		
		assertNotNull(renderedResponse)
		assertTrue(renderedResponse.contains("BRAF"))
	}

	/**
	 * Tests the setMutation action
	 */
	void testSetMutation() {
		
		knowledgeController.params.mutation = "BRAF V600E"
		def model = knowledgeController.setMutation()
		def renderedResponse = knowledgeController.response.contentAsString
		
		assertNotNull(renderedResponse)
		assertTrue(renderedResponse.contains("BRAF"))
		assertTrue(renderedResponse.contains("V600E"))
	}

	/**
	 * Test the action which generates ajax gene list
	 */
	void testFindMutation() {
		knowledgeController.params.mutation = "BRAF V600E"
		knowledgeController.findMutation()
		def renderedResponse = knowledgeController.response.contentAsString
		
		assertNotNull(renderedResponse)
		assertTrue(renderedResponse.contains("BRAF"))
		assertTrue(renderedResponse.contains("V600E"))
	}
	
	/**
	 * Tests that the genomic information is properly visible
	 */
	// TODO: GPS-111
	void testGeneGenomics() {
		knowledgeController.params.gene = "BRAF"
		def model = knowledgeController.gene()
		assertNotNull(model)
		
		assertTrue(model.containsKey("gene"))
		def gene = model.gene
		
		assertEquals("7", gene.chromosome)
		assertEquals(140424943, gene.start)
		assertEquals(140624564, gene.stop)
		assertEquals(199622, gene.geneSize)
	}
}
