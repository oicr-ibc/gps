package ca.on.oicr.gps.pipeline.mock;

import ca.on.oicr.gps.pipeline.domain.DomainKnownMutation;

public class DomainKnownMutationImpl implements DomainKnownMutation {
	
	private String name = null;
	public String getName() {
		return name;
	}

	private String criteriaString = null;

	public String getCriteriaString() {
		return criteriaString;
	}

	public DomainKnownMutationImpl(String initName, String initCriteriaString) {
		name = initName;
		criteriaString = initCriteriaString;
	}

	public DomainKnownMutationImpl(String initName) {
		name = initName;
	}
}
