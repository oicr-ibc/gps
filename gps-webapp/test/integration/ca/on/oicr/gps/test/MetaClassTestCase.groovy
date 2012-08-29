package ca.on.oicr.gps.test;

import groovy.lang.ExpandoMetaClass;
import groovy.util.GroovyTestCase;

import java.util.Map;

/**
 * A class which provides the registerMetaClass method from GrailsUnitTestCase
 * but without the other crap that causes GrailsUnitTestCase to break everyone's
 * integration tests. This makes MetaClassTestCase a suitable base for
 * integration test, and it's also handy if that's all you need from 
 * GrailsUnitTestCase.
 * 
 * @author swatt
 */

public class MetaClassTestCase extends GroovyTestCase {
	
	Map savedMetaClasses
	
	/**
	 * Initializes the saved metaclass map
	 */
	protected void setUp() {
		super.setUp()
		
		savedMetaClasses = [:]
	}
	
	/**
	 * Restores all the registered metaclasses
	 */
	protected void tearDown() {
		super.tearDown()

		// Restore all the saved meta classes.
		savedMetaClasses.each { clazz, metaClass ->
			GroovySystem.metaClassRegistry.setMetaClass(clazz, metaClass)
		}
	}

	/**
	 * Registers a metaclass
	 * @param clazz
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
