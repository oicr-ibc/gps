package ca.on.oicr.gps.pipeline.mock;

import ca.on.oicr.gps.pipeline.domain.DomainRunAssay;

public class DomainRunAssayImpl implements DomainRunAssay {
	
	private String status = null;

	public String getStatus() {
		return status;
	}

	public void setStatus(String value) {
		status = value;
	}
}
