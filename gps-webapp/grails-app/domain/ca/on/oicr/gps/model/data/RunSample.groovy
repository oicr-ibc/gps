package ca.on.oicr.gps.model.data

import java.util.List;
import ca.on.oicr.gps.pipeline.domain.DomainRunSample

class RunSample implements DomainRunSample {

	static belongsTo = [ Process, Sample ]

	static hasMany = [ mutations: ObservedMutation ]
	
	Process process
	Sample sample
	
    static constraints = {
		
    }

	static mapping = {
		mutations cascade: "all"
	}
	
	static transients = [ "sortedRunSamples" ]
	
	static List<RunSample> getSortedRunSamples(params) {
		def criteria = RunSample.createCriteria()
		def sampleList = criteria.list { }
		sampleList = sampleList.sort { a, b -> patientIdKey(a).compareTo(patientIdKey(b)) }
		if (params.order == 'desc') {
			sampleList = sampleList.reverse()
		}
		sampleList
	}

	private static final patientIdKey(RunSample a) {
		String pid = a.sample.subject.patientId
		Integer division = pid.length() - 2
		return pid.substring(division) + pid.substring(0, division)
	}
}
