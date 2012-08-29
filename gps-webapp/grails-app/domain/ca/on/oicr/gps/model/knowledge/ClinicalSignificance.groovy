package ca.on.oicr.gps.model.knowledge

class ClinicalSignificance {

	static Integer SIGNIFICANCE_UNKNOWN = 0
	static Integer SIGNIFICANCE_PROSPECTIVE = 1
	static Integer SIGNIFICANCE_RETROSPECTIVE = 2
	static Integer SIGNIFICANCE_PRECLINICAL = 3
	static Integer SIGNIFICANCE_CASE = 4
    static Integer SIGNIFICANCE_OBSERVATIONAL = 5

	static belongsTo = [ mutation: KnownMutation, tumourType: KnownTumourType ]
	
	Integer significance
	String significanceComment
	String significanceReference
	String significanceEvidence

    static constraints = {
		significanceComment(nullable: true)
		significanceReference(nullable: true)
		significanceEvidence(nullable: true, maxSize: 8)
    }

	static mapping = {
        significanceComment type:"text"
    }
	
	static transients = ['significanceCode']

	String getSignificanceCode() {
		String result = "error"
		switch (significance) {
			case SIGNIFICANCE_UNKNOWN:         result = "unknown"; break;
			case SIGNIFICANCE_PROSPECTIVE:     result = "prospective"; break;
			case SIGNIFICANCE_RETROSPECTIVE:   result = "retrospective"; break;
			case SIGNIFICANCE_PRECLINICAL:     result = "preclinical"; break;
			case SIGNIFICANCE_CASE:            result = "case"; break;
			case SIGNIFICANCE_OBSERVATIONAL:   result = "observational"; break;
		}
		return result
	}

	static Integer toSignificance(String code) {
		Integer result = SIGNIFICANCE_UNKNOWN
		switch (code) {
			case "unknown":         result = SIGNIFICANCE_UNKNOWN; break;
			case "prospective":     result = SIGNIFICANCE_PROSPECTIVE; break;
			case "retrospective":   result = SIGNIFICANCE_RETROSPECTIVE; break;
			case "preclinical":     result = SIGNIFICANCE_PRECLINICAL; break;
			case "case":            result = SIGNIFICANCE_CASE; break;
			case "observational":   result = SIGNIFICANCE_OBSERVATIONAL; break;
		}
		return result
	}
}
