package ca.on.oicr.gps.model.knowledge

class TumourTypeCharacteristics {

	static belongsTo = [ tumourType: KnownTumourType ]

 	Boolean malignant = true
	
    static constraints = {
		malignant(nullable: false)
    }	
}
