package ca.on.oicr.gps.model.laboratory

import grails.test.*

class TargetTests extends GrailsUnitTestCase {

	def target

    public void setUp() {
        super.setUp()

		mockDomain(Target)
		mockDomain(Panel)
		target = new Target()
		target.chromosome = '7'
		target.gene = "EFGR"
		target.start = 55249010
		target.stop = 55249015
		target.panel = new Panel(name: "OncoCarta", versionString: "1.0")
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks that the untouched assay object passes validation
	 */
    void testValidation() {
		// Check that an complete summary will validate
		def result = target.validate()
		target.errors.allErrors.each { log.info it.toString() }
		assertTrue result
		assertFalse target.hasErrors()
    }
}
