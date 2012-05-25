package ca.on.oicr.gps.pipeline.mock;

import ca.on.oicr.gps.pipeline.domain.DomainAssay;

public class DomainAssayImpl implements DomainAssay {
	
	private String name = null;
	
	public DomainAssayImpl(String nameValue) {
		name = nameValue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
