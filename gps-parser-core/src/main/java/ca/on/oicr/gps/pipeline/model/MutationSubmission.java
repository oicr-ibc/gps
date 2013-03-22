package ca.on.oicr.gps.pipeline.model;

import java.io.InputStream;
import java.util.Date;

public interface MutationSubmission {
 
	public String getType();

	public Date getSubmissionDate();

	public InputStream getSubmissionInputStream();

}
