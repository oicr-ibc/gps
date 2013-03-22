package ca.on.oicr.gps.model.knowledge

class AgentEffectiveness {

	static EFFECTIVENESS_UNKNOWN = 1
	static EFFECTIVENESS_EFFECTIVE = 2
	static EFFECTIVENESS_INEFFECTIVE = 3

	static belongsTo = [ mutation: KnownMutation ]
	
	String agents
	Integer agentsEffective

    static constraints = {
		agents(nullable: false)
    }
	
	static mapping = {
        agents type:"text"
    }
	
	static transients = ['effectivenessCode']
	
	String getEffectivenessCode() {
		
		String result = "error"
		switch (agentsEffective) {
			case EFFECTIVENESS_UNKNOWN:           result = "unknown"; break;
			case EFFECTIVENESS_EFFECTIVE:         result = "effective"; break;
			case EFFECTIVENESS_INEFFECTIVE:       result = "ineffective"; break;
		}
		return result
	}
	
	static Integer toAgentsEffective(String code) {
		Integer result = EFFECTIVENESS_UNKNOWN
		switch (code) {
			case "unknown":            result = EFFECTIVENESS_UNKNOWN; break;
			case "effective":          result = EFFECTIVENESS_EFFECTIVE; break;
			case "ineffective":        result = EFFECTIVENESS_INEFFECTIVE; break;
		}
		return result
	}
}
