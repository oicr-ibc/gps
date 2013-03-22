package ca.on.oicr.gps.pages

import grails.test.*
import org.htmlparser.Parser
import org.htmlparser.filters.TagNameFilter

import ca.on.oicr.gps.controller.QueryController;
import ca.on.oicr.gps.model.reporting.Query;

class QueryGroovyPagesTests extends GroovyPagesTestCase {

	def query1Id
	def query2Id
	
	public void setUp() {
		super.setUp()

		def newQuery1 = new Query()
		newQuery1.name = "A New Query"
		newQuery1.body = """
SELECT DISTINCT s.id, s.patient_id, 
                sam.barcode, 
                sam.source, 
                sam.dna_concentration, 
                sam.dna_quality, 
                CONCAT(km.gene, ' ', km.mutation) as mutation, 
                km.chromosome, 
                km.start, 
                km.stop, 
                pa.technology, 
                om.frequency, 
                om.confidence 
FROM observed_mutation om 
JOIN run_sample rs ON rs.id = om.run_sample_id 
JOIN sample sam ON rs.sample_id = sam.id 
JOIN subject s ON s.id = sam.subject_id 
JOIN known_mutation km ON om.known_mutation_id = km.id 
JOIN process p ON p.id = rs.process_id 
JOIN panel pa ON pa.id = p.panel_id 
ORDER BY s.id ASC, sam.barcode ASC
"""

		newQuery1.save(failOnError:true)
		query1Id = newQuery1.id
		
		def newQuery2 = new Query()
		newQuery2.name = "Second Query"
		newQuery2.body = """
SELECT DISTINCT s.patient_id
FROM subject s
ORDER BY s.patient_id DESC
"""
		
		newQuery2.save(failOnError:true)
		query2Id = newQuery2.id
	}

	public void tearDown() {
		super.tearDown()
	}

	/**
	 * Test the query controller's ability to list the queries
	 */
	void testQueryList() {
		def file = new File(System.properties['base.dir'], "grails-app/views/query/list.gsp")
		
		def subController = new QueryController()
		def model = subController.list()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
		
		assertTrue textString.contains("A New Query")
	}

	/**
	 * Test the query controller's ability to run a query. 
	 */
	void testQuery1Run() {
		def file = new File(System.properties['base.dir'], "grails-app/views/query/run.gsp")
		
		def subController = new QueryController()
		subController.params.id = query1Id
		def model = subController.run()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
		
		assertNotNull(textString)
	}

	/**
	 * Test the query controller's ability to run a query, this time with actual output 
	 */
	void testQuery2Run() {
		def file = new File(System.properties['base.dir'], "grails-app/views/query/run.gsp")
		
		def subController = new QueryController()
		subController.params.id = query2Id
		def model = subController.run()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
		
		assertNotNull(textString)
		assertTrue(textString.contains("TST001"))
	}

	/**
	 * Test the query controller's ability to export a query, this time with actual output 
	 */
	void testQuery2Export() {
		def file = new File(System.properties['base.dir'], "grails-app/views/query/export.gsp")
		
		def subController = new QueryController()
		subController.params.id = query2Id
		def model = subController.export()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
		
		assertNotNull(textString)
		assertTrue(textString.contains("TST001"))
	}
}
