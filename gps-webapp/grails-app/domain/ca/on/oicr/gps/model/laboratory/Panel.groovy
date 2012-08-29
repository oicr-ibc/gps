package ca.on.oicr.gps.model.laboratory

import ca.on.oicr.gps.pipeline.domain.DomainTarget

class Panel implements Comparable<Panel> {
	
	static hasMany = [ targets: Target ]

	String name 
	String technology
	String versionString 
	
    static constraints = {
		name(nullable: false, blank: false, maxSize: 32)
		technology(nullable: true, blank: false, maxSize: 32)
		versionString(nullable: false, blank: false, maxSize: 10, unique: ['name', 'technology'])
    }
	
	public int compareTo(Panel o) {
		if (id == o.id) {
			return 0
		}
		Integer compare = technology.compareTo(o.technology)
		if (compare != 0) {
			return compare;
		}
		compare = name.compareTo(o.name)
		if (compare != 0) {
			return compare;
		}
		return versionString.compareTo(o.versionString)
	}
	
	public Set<DomainTarget> getDomainTargets() {
		return (Set<DomainTarget>) targets
	}
}
