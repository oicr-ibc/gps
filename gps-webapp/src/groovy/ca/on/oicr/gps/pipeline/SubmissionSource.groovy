package ca.on.oicr.gps.pipeline

import java.io.InputStream;
import java.util.Date;

import ca.on.oicr.gps.model.data.Submission;
import ca.on.oicr.gps.pipeline.model.MutationSubmission;

/**
 * SubmissionSource is a Groovy class that acts as the source for 
 * submission information to the Java-based pipeline system. It does
 * this by implementing the MutationSubmission interface, and providing
 * that as an adaptor to the Groovy/Grails domain class Submission. 
 * This separates the Java pipeline properly from the Groovy domain
 * model. 
 * 
 * @author swatt
 *
 */

class SubmissionSource implements MutationSubmission {
	
	Submission domainSubmission

	Date getSubmissionDate() {
		return domainSubmission.dateSubmitted
	}
	
	String getType() {
		return domainSubmission.dataType;
	}
	
	InputStream getSubmissionInputStream() {
		return (InputStream) new ByteArrayInputStream(domainSubmission.fileContents)
	}
}
