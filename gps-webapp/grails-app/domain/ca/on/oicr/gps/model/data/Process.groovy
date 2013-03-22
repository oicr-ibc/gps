package ca.on.oicr.gps.model.data

import java.util.Set;

import ca.on.oicr.gps.model.laboratory.Panel;
import ca.on.oicr.gps.pipeline.domain.DomainProcess;

class Process implements DomainProcess {
	
	static belongsTo = [ Submission ]
	static hasMany = [ runSamples: RunSample ]
	
	Submission submission
	String runId
	String chipcode
	Panel panel
	
	Date date
	
    static constraints = {
		runId(nullable: false, maxSize: 40, blank: false, unique: true)
		chipcode(nullable: true, maxSize: 40)
		date(nullable:true)
    }

	static mapping = {
		runSamples cascade: "all"
	}
}
