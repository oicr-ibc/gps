package ca.on.oicr.gps.model.knowledge

class GeneCharacteristics {

	static belongsTo = [ gene: KnownGene ]

 	String fullName
	String somaticTumorTypes
	String germlineTumorTypes
	String cancerSyndrome
	String description
	
    static constraints = {
		fullName(nullable: true)
		somaticTumorTypes(nullable: true)
		germlineTumorTypes(nullable: true)
		cancerSyndrome(nullable: true)
		description(nullable: true, maxSize: 2048)
    }
}
