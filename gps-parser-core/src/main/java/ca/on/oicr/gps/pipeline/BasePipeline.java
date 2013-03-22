package ca.on.oicr.gps.pipeline;

import ca.on.oicr.gps.pipeline.model.MutationSubmission;

abstract public class BasePipeline {
	
	abstract public String getTypeKey();
	
	public boolean canHandleSubmission(MutationSubmission submission) {
		if (! submission.getType().equals(getTypeKey())) {
			return false;
		}
		return true;
	}
}
