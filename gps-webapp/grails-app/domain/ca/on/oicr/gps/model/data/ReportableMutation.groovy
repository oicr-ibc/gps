package ca.on.oicr.gps.model.data

import ca.on.oicr.gps.model.laboratory.Panel;

class ReportableMutation {
	
	static final String LOE_LEVEL_1 = "Level I"
	static final String LOE_LEVEL_2 = "Level II"
	static final String LOE_LEVEL_3 = "Level III"
	static final String LOE_LEVEL_4 = "Level IV"
	static final String LOE_LEVEL_5 = "Level V"
	
	static belongsTo = [ decision: Decision ]
	static hasMany = [observedMutations: ObservedMutation]
	
	String levelOfEvidence
	String levelOfEvidenceGene
	String comment = ""
	String justification = ""
	Boolean actionable = false
	Boolean reportable = false
	
    static constraints = {
		levelOfEvidence(nullable: true, blank:true, inList: [LOE_LEVEL_1, LOE_LEVEL_2, LOE_LEVEL_3, LOE_LEVEL_4, LOE_LEVEL_5])
		levelOfEvidenceGene(nullable: true, blank:true, inList: [LOE_LEVEL_1, LOE_LEVEL_2, LOE_LEVEL_3, LOE_LEVEL_4, LOE_LEVEL_5])
		comment(nullable: false)
		actionable(nullable: false)
		reportable(nullable: false)
		justification(nullable: false)
    }

	static transients = [ "panels" ]

	public Set<Panel> getPanels() {
		return observedMutations.collect ([] as Set<Panel>) { it.getPanel() }
	}
}
