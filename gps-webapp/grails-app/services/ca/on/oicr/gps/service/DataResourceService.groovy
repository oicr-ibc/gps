package ca.on.oicr.gps.service

import java.io.InputStreamReader;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This service allows a range of data input files to be read into basic data structures. This is a
 * useful component for allowing initialization of various data components, such as panels, 
 * and translation tables that don't really belong in a database. 
 * 
 * @author swatt
 */

class DataResourceService implements ApplicationContextAware {

    static transactional = false

	ApplicationContext applicationContext

    def readCSVResourceData(String resource) {
		InputStream is = applicationContext.getResource("/WEB-INF/" + resource).getInputStream()
		if (is == null) {
			throw new Exception("Internal error: Failed to find OncoCarta translation table");
		}
		
		CSVReader reader = new CSVReader(new InputStreamReader(is))
		
		// The first line ought to define headings
		def headers = reader.readNext()
		headers = headers.collect { it.trim().toLowerCase() }
		
		// Now we can load this into something we can use more sensibly. 
		// The main usage of this is to look up by a combined set of fields
		// and return the remaining data. This is a task that could be done
		// very nicely by HSQL. 

		// This is followed by the data
		def entries = [ ]
		List data
		while (data = reader.readNext()) {
			
			// Since we can only remove from end of a list, a reversed list
			// of headers gets the ordering right
			def entry = [:]
			def index = 0
			headers.each { entry.putAt(it, data.getAt(index++)) }
			entries.add(entry)
		}
		
		return entries
    }
}
