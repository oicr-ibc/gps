package ca.on.oicr.gps.service

import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.springframework.context.ApplicationContext;

import ca.on.oicr.gps.model.knowledge.KnownMutation;

import grails.test.*

class MutationKnowledgeServiceTests extends GroovyTestCase {
	
	def mutationKnowledgeService
	
    public void setUp() {
        super.setUp()
    }

    public void tearDown() {
        super.tearDown()
    }

    void testMutationKnowledgeService() {

		// Once we have initialized, we can find a few mutations and get their public identifiers.
		// We need to check these are consistent. 
		
		def foundAll1 = mutationKnowledgeService.findAllBy(gene: "ABL1", chromosome: "9", start: 133738349, stop: 133738349, varAllele: "A")
		assertEquals(1, foundAll1.size())
		
		// Now let's find an example
		def found1 = mutationKnowledgeService.findBy(gene: "ABL1", chromosome: "9", start: 133738349, stop: 133738349, varAllele: "A")
		def found2 = mutationKnowledgeService.findBy(gene: "RET", chromosome: "10", start: 43617416, stop: 43617416, varAllele: "C")
		
		assertNotNull(found1)
		assertNotNull(found2)
		
		def found1PublicId = found1.publicId;
		def found2PublicId = found2.publicId;
		
		// Check we have a public identifier
		assertNotNull(found1.publicId)
		assertNotNull(found2.publicId)
    }
}
