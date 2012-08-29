package ca.on.oicr.gps.controller

import static org.junit.Assert.*

import java.util.Map;

import ca.on.oicr.gps.model.reporting.Query;
import ca.on.oicr.gps.test.MetaClassTestCase;

import groovy.util.GroovyTestCase;
import grails.test.*

class QueryControllerIntegrationTests extends MetaClassTestCase {
	
	def queryId
	def queryController
	Map renderMap
	Map redirectMap

	public void setUp() {
		super.setUp()

		registerMetaClass(QueryController.class)
		QueryController.metaClass.render = {Map m ->
			renderMap = m
		}
		QueryController.metaClass.redirect = {Map m ->
			redirectMap = m
		}
		
		def newQuery = new Query()
		newQuery.name = "Second Query"
		newQuery.body = """
SELECT DISTINCT s.patient_id
FROM subject s
ORDER BY s.patient_id DESC
"""
		
		newQuery.save(failOnError:true)
		queryId = newQuery.id

		queryController = new QueryController()
	}
	
	public void tearDown() {
		super.tearDown()
	}
	
    void testCreateQuery() {
		
		queryController.params.name = "A New Query"
		queryController.params.body = """
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
		def model = queryController.save()		
		assertNotNull(redirectMap)
		assertTrue redirectMap.containsKey("action")
		assertEquals "show", redirectMap.action
		assertTrue redirectMap.containsKey("id")
    }
	
	/**
	 * Test a missing query - this should redirect to the list
	 */
	void testShowMissingQuery() {
		queryController.params.id = -1
		
		def model = queryController.show()
		assertNotNull(redirectMap)
		assertEquals "list", redirectMap.action
	}
	
	/**
	 * Test a full query - this should redirect to the list
	 */
	void testShowQuery() {
		queryController.params.id = queryId
		
		def model = queryController.show()
		assertNull(redirectMap)
		
		assertTrue(model.containsKey("queryInstance"))
		assertEquals(queryId, model.queryInstance.id)
	}
	
	/**
	 * Test a missing query - this should redirect to the list
	 */
	void testExportMissingQuery() {
		queryController.params.id = -1
		
		def model = queryController.export()
		assertNotNull(redirectMap)
		assertEquals "list", redirectMap.action
	}
	
	/**
	 * Test a full query - this should redirect to the list
	 */
	void testExportQuery() {
		queryController.params.id = queryId
		
		def model = queryController.export()
		assertNull(redirectMap)
		assertTrue(model.containsKey("queryInstance"))
		assertEquals(queryId, model.queryInstance.id)
	}
	
	/**
	 * Test a missing query - this should redirect to the list
	 */
	void testEditMissingQuery() {
		queryController.params.id = -1
		
		def model = queryController.edit()
		assertNotNull(redirectMap)
		assertEquals "list", redirectMap.action
	}
	
	/**
	 * Test a full query - this should redirect to the list
	 */
	void testEditQuery() {
		queryController.params.id = queryId
		
		def model = queryController.edit()
		assertNull(redirectMap)
		
		assertTrue(model.containsKey("queryInstance"))
		assertEquals(queryId, model.queryInstance.id)
	}
	
	/**
	 * Test a missing query - this should redirect to the list
	 */
	void testDeleteMissingQuery() {
		queryController.params.id = -1
		
		def model = queryController.delete()
		assertNotNull(redirectMap)
		assertEquals "list", redirectMap.action
	}
	
	/**
	 * Test a full query - this should redirect to the list
	 */
	void testDeleteQuery() {
		queryController.params.id = queryId
		
		def model = queryController.delete()
		assertNotNull(redirectMap)
		assertEquals "list", redirectMap.action
		
		// In theory, the query won't be in the DB now. In practice, Hibernate 
		// may still have it cached. So we need to be careful testing that. 
	}
}