package ca.on.oicr.gps.model.data

import java.util.Set;

class Sample {
	
	static belongsTo = [ subject: Subject ]
	static hasMany = [ runSamples: RunSample ]
	
	public static final String SOURCE_STUDY_SAMPLE = 'sample'
	public static final String SOURCE_ARCHIVAL_SAMPLE = 'archive'
	public static final String SOURCE_BLOOD_SAMPLE = 'blood'
	
	String barcode
	String name
	String type
	String site
	String source
	Float dnaConcentration
	String dnaQuality
	String sequenomNum
	Date dateReceived
	Date dateCreated
	Date lastUpdated
	Date dateCollected
	Boolean requiresCollection = true
	
    static constraints = {
		barcode(nullable: false, blank:false, maxSize:20, unique:true)
		name(nullable: true, maxSize:20)
		type(nullable: false, inList:['FFPE','Frozen','Fluid','Blood','FNA'])
		site(nullable: true, inList:['primary','metastases'])
		source(nullable: true, inList:[SOURCE_STUDY_SAMPLE, SOURCE_ARCHIVAL_SAMPLE, SOURCE_BLOOD_SAMPLE])
		dnaConcentration(nullable: true, blank:false)
		dnaQuality(nullable: true, blank:false)
		sequenomNum(nullable: true, maxSize: 15)
		dateReceived(nullable: true)
		dateCreated(nullable: false)
		lastUpdated(nullable: true)
		dateCollected(nullable: true)
		requiresCollection(nullable: false)
    }
	
	static mapping = {
		runSamples cascade: "all"
	}

	String toString() {
		return "<sample " + barcode + ">";
	}
}
