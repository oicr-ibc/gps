package ca.on.oicr.gps.model.system

class AuditRecord {
	
	final static Integer TYPE_INSERT = 0
	final static Integer TYPE_DELETE = 1
	final static Integer TYPE_UPDATE = 2
	
	String userName
	String patientId
	Integer type
	
	String propertyName
	String oldValue
	String newValue
	
	Date timestamp
	
	// Add a transient computed accessor for a string version of the type. This is kind of like
	// a storage-efficient enum
	String getTypeName() {
		final typeMap = [(TYPE_INSERT): 'insert',
						 (TYPE_DELETE): 'delete',
						 (TYPE_UPDATE): 'update']
		return typeMap[type]
	}

    static constraints = {
		userName(nullable: true)
		patientId(nullable: false, maxSize: 20, blank:false)
		timestamp(nullable: false)
		type(nullable: false)
		propertyName(nullable: false, maxSize: 32, blank:false)
		oldValue(nullable: true)
		newValue(nullable: true)
    }
	
	static transients = [ "typeName" ]
	
	static mapping = {
		timestamp index:'AuditRecord_timestamp'
		patientId  index:'AuditRecord_patientId'
	}
}
