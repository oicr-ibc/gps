package ca.on.oicr.gps.model.data

import java.text.DateFormat;
import java.util.Date;

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

class Decision {
	
	static final String TYPE_INTERIM = "interim"
	static final String TYPE_FINAL = "final"
	static final String TYPE_WITHDRAWN = "withdrawn"
	
	static belongsTo = [ subject: Subject ]
	static hasMany = [reportableMutations: ReportableMutation ]
	
	String source = ''

	Boolean noTumour = false
	Boolean insufficientMaterial = false
	Boolean noMutationsFound = false
	Boolean unanimous = true

	Date date = new Date()
	String decision = ""
	String decisionType
	
	static transients = [ "summary", "decisionMutationNames", "text" ]
	
    static constraints = {
		source(nullable: true, blank:true, inList:[Sample.SOURCE_STUDY_SAMPLE, Sample.SOURCE_ARCHIVAL_SAMPLE, Sample.SOURCE_BLOOD_SAMPLE])
		noTumour(nullable: false)
		insufficientMaterial(nullable: false)
		noMutationsFound(nullable: false)
		unanimous(nullable: false)
		date(nullable: false)
		decision(nullable: false)
		decisionType(nullable: false, blank:false, size: 1..12, inList: [TYPE_INTERIM, TYPE_FINAL, TYPE_WITHDRAWN])
    }
	
	String getText() {
		return ""
	}
	
	String getSummary() {
		String type = decisionType
		type = type.substring(0, 1).toUpperCase() + type.substring(1)
		
 		final DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMMM yyyy");
		String dateString = fmt.print(new DateTime(date))
		String text = getText()

		return type + " report: " + dateString + ((text) ? ": ${text}" : "")
	}
}
