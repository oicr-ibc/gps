package ca.on.oicr.gps.model.system

class SecRole implements Comparable<SecRole> {

	String authority

	static mapping = {
		cache true
	}

	static constraints = {
		authority blank: false, unique: true
	}
	
	public boolean equals(Object other) {
		if (! other) {
			return false
		} else {
			return authority.equals(other.authority)
		}
	}
	
	public int compareTo(SecRole other) {
		if (! other) {
			return -1
		} else {
			return authority.compareTo(other.authority)
		}
	}
}
