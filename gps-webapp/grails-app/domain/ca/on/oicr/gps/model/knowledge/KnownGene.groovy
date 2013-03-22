package ca.on.oicr.gps.model.knowledge

import java.util.List;

class KnownGene {
	
	String name

	static hasMany = [ knownMutations: KnownMutation ]
	
	static hasOne =  [ characteristics: GeneCharacteristics ]
	
	String chromosome
	Integer start
	Integer stop
	Integer geneSize
	Boolean visible = false
	
	static mapping = {
		name index: 'name_idx'
		visible index: 'visible_idx'
	}
	
	static transients = [
		'visibleMutations'
	]

    static constraints = {
		name(nullable: false, maxSize: 32, unique: true)
		chromosome(nullable: true)
		start(nullable: true)
		stop(nullable: true)
		geneSize(nullable: true)
		visible(nullable: false)
    }

	static List<KnownGene> findGenes(String term) {
		def matcher = term =~ /(\w*)(?:\s+)?(\w+)?/
		
		if (! matcher.matches()) {
			return []
		} else {
			String geneName = matcher.group(1)
			
			return KnownGene.withCriteria {
				ilike("name", geneName + "%")
				eq("visible", true)
				order("name", "asc")
			}
		}
	}
	
	List<KnownMutation> getVisibleMutations() {
		return knownMutations.toList().findAll { it.visible }
	}
}
