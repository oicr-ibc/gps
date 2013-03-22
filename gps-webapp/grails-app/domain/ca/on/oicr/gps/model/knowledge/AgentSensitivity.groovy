package ca.on.oicr.gps.model.knowledge

class AgentSensitivity {
	
	static AGENT_SENSITIVITY = 1
	static AGENT_RESISTANCE = 2
	static AGENT_MAYBE_SENSITIVITY = 3
	static AGENT_MAYBE_RESISTANCE = 4

	static belongsTo = [ mutation: KnownMutation ]
	
	String agentName
	Integer sensitivityType

    static constraints = {
		agentName(nullable: false, maxSize: 255)
    }

	static transients = ['sensitivityCode']

	String getSensitivityCode() {
		String result = "error"
		switch (sensitivityType) {
			case AGENT_SENSITIVITY:        result = "sensitivity"; break;
			case AGENT_RESISTANCE:         result = "resistance"; break;
			case AGENT_MAYBE_SENSITIVITY:  result = "maybe_sensitivity"; break;
			case AGENT_MAYBE_RESISTANCE:   result = "maybe_resistance"; break;
		}
		return result
	}

	static Integer toSensitivity(String code) {
		Integer result = null
		switch (code) {
			case "sensitivity":            result = AGENT_SENSITIVITY; break;
			case "resistance":             result = AGENT_RESISTANCE; break;
			case "maybe_sensitivity":      result = AGENT_MAYBE_SENSITIVITY; break;
			case "maybe_resistance":       result = AGENT_MAYBE_RESISTANCE; break;
		}
		return result
	}
}
