package ca.on.oicr.gps.model.knowledge

class KnownMutationFrequency {

	static belongsTo = [ mutation: KnownMutation, tumourType: KnownTumourType ]
	
	Integer samples
	Float frequency
	Float relevance
	
    static constraints = {
		samples(nullable: false)
		frequency(nullable: false)
		relevance(nullable: false)
    }
	
	static transients = ['affected']
	
	Integer getAffected() {
		return Math.round(frequency * samples);
	}
}
