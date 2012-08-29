package ca.on.oicr.gps.model.knowledge

class KnownTumourType {

	static hasMany = [ 
		frequencies: KnownMutationFrequency,
		significance: ClinicalSignificance
	]
	
	static hasOne =  [ characteristics: TumourTypeCharacteristics ]

	String name

    static constraints = {
		name(nullable: false, maxSize: 128)
    }
}
