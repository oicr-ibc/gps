package ca.on.oicr.gps.model.reporting

/**
 * Defines the body part of a document, which we store as a blob to allow us to archive things
 * sensibly, and keep the data out of the reports table directly. 
 * @author swatt
 *
 */

class ReportDocument {
	
	String type
	byte[] body

    static constraints = {
		type(nullable: false)
		body(maxSize: 1024 * 1024 * 16)
    }
}
