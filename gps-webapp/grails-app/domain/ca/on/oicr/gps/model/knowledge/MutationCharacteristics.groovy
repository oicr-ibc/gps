package ca.on.oicr.gps.model.knowledge

class MutationCharacteristics {
	
	static ACTION_UNKNOWN = 0
	static ACTION_ACTIVATING = 1
	static ACTION_INACTIVATING = 2
	static ACTION_OTHER = 3
	static ACTION_NONE = 4
	
	static AVAILABLE_NO = 0
	static AVAILABLE_YES = 1

	static belongsTo = [ mutation: KnownMutation ]
	
	Integer action
	String actionReference
	String actionComment
	Integer agentsAvailable

    static constraints = {
		actionReference(nullable: true)
		actionComment(nullable: true)
		agentsAvailable(nullable: true)
    }

	static mapping = {
        actionComment type:"text"
    }
	
	static transients = ['actionCode', 'agentsAvailableCode']

	String getActionCode() {
		String result = "error"
		switch (action) {
			case ACTION_UNKNOWN:        result = "unknown"; break;
			case ACTION_ACTIVATING:     result = "activating"; break;
			case ACTION_INACTIVATING:   result = "inactivating"; break;
			case ACTION_NONE:           result = "none"; break;
			case ACTION_OTHER:          result = "other"; break;
		}
		return result
	}
	
	static Integer toAction(String code) {
		Integer result = ACTION_UNKNOWN
		switch (code) {
			case "unknown":             result = ACTION_UNKNOWN; break;
			case "activating":          result = ACTION_ACTIVATING; break;
			case "inactivating":        result = ACTION_INACTIVATING; break;
			case "none":                result = ACTION_NONE; break;
			case "other":               result = ACTION_OTHER; break;
		}
		return result
	}

	String getAgentsAvailableCode() {
		String result = "unknown"
		switch (agentsAvailable) {
			case AVAILABLE_NO:          result = "not available"; break;
			case AVAILABLE_YES:         result = "available"; break;
		}
		return result
	}

	static Integer toAgentsAvailable(String code) {
		Integer result = AVAILABLE_NO
		switch (code) {
			case "not available":       result = AVAILABLE_NO; break;
			case "available":           result = AVAILABLE_YES; break;
		}
		return result
	}
}
