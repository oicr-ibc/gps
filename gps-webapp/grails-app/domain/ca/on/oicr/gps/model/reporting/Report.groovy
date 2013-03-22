package ca.on.oicr.gps.model.reporting

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.xml.sax.InputSource;

import groovy.util.XmlParser;
import ca.on.oicr.gps.model.data.ObservedMutation;
import ca.on.oicr.gps.model.data.Subject;

class Report implements Comparable {
	
	static hasMany = [mutations: ObservedMutation]
	
	static belongsTo = [ subject: Subject ]

	Date generated
	ReportDocument document

    static constraints = {
		generated(nullable: false)
		document(nullable: false)
		subject(nullable: false)
    }

	static mapping = {
		document cascade: "all"
	}
	
	static transients = [ "data" ]

	/**
	 * Returns a parsed version of the document body. 
	 * @return
	 */
	def getData() {
		InputStream input = new ByteArrayInputStream(document.body)
		return new XmlParser().parse(new InputSource(input))
	}

	/**
	 * Implements Comparable, so that we order by date, in a descending order
	 */
	public int compareTo(obj) {
        obj.generated.compareTo(generated)
    }
}
