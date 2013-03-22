package ca.on.oicr.gps.pipeline.mock;

import ca.on.oicr.gps.pipeline.domain.DomainObservedMutation;

public class DomainObservedMutationImpl implements DomainObservedMutation {

	private Float frequency = null;
	private int status;
	private String confidence = null;

	public void setFrequency(Float value) {
		frequency = value;
	}

	public void setStatus(int value) {
		status = value;
	}

	public void setConfidence(String value) {
		confidence = value;
	}
	
	public Float getFrequency() {
		return frequency;
	}

	public int getStatus() {
		return status;
	}

	public String getConfidence() {
		return confidence;
	}
}
