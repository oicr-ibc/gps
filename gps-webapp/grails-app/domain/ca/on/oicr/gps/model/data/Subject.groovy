package ca.on.oicr.gps.model.data

import java.io.StringWriter;
import java.util.Comparator;
import java.util.Date;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import ca.on.oicr.gps.model.knowledge.KnownMutation;
import ca.on.oicr.gps.model.reporting.Report;
import ca.on.oicr.gps.util.ObservedMutationComparator;
import ca.on.oicr.gps.util.SampleComparator;

import groovy.xml.MarkupBuilder

class Subject {
	
	static final String SEX_FEMALE = 'F'
	static final String SEX_MALE = 'M'
	static final String SEX_UNSPECIFIED = '-'
	
	static hasMany = [ samples: Sample, reports: Report, decisions: Decision ]

    String patientId
	String gender
	
	Summary summary
	
	Date lastUpdated
	
	/**
	 * The set of reports, sorted in descending order by the date they are generated,
	 * so the first in the set is always the most recent. This is, virtually all the 
	 * time, the only one we are interested in. 
	 */
	SortedSet<Report> reports
	
	static constraints = {
		patientId(nullable: false, maxSize: 20, blank:false, unique:true)
		gender(nullable: false, inList:[SEX_MALE, SEX_FEMALE, SEX_UNSPECIFIED])
		lastUpdated(nullable: true)
	}
	
	static audit = [ 'patientId', 'gender' ]

	static mapping = {
		samples cascade: "all"
	}
	
	static transients = [ 
						"allReportable", 
		                "allSamples", 
						"subjectMutationReportData",
						"mutations",
						"mutationsList",
						"observedMutationsList",
						"observations",
						"runsCompleted"
					]

	
	static Integer countAll() {
		return Subject.count()
	}
	
	static Integer countActive() {
		def criteria = Subject.createCriteria()
		return criteria.count {
			summary {
				isNull('medidataUploadDate')
			}
		}
	}
	
	/**
	 * Returns a list of the currently reportable subjects. These are the ones with samples, submissions,
	 * and whose data has not yet been uploaded to Medidata Rave. 
	 * 
	 * @return
	 */
	static List<Object> getAllReportable(params) {
		
		// Groovy multiline strings do not work in GORM HQL (!)
		// Statement to find subjects with a summary of the output
		
		def statement = "\
select new map(s as subject, \
               summary.biopsyDate as biopsyDate, \
               summary.consentDate as consentDate, \
               summary.medidataUploadDate as medidataUploadDate, \
               (select max(rs1.process.date) from RunSample as rs1 where rs1.sample.subject = s and rs1.process.panel.technology = 'Sequenom') as sequenomDate, \
               (select max(rs2.process.date) from RunSample as rs2 where rs2.sample.subject = s and rs2.process.panel.technology = 'PacBio') as pacBioDate, \
               (select max(rs3.process.date) from RunSample as rs3 where rs3.sample.subject = s and rs3.process.panel.technology = 'ABI') as sangerDate) \
from Subject as s \
join s.summary as summary \
		";
		
		// We want to inject some details into the HQL, but these details are identifiers rather than
		// traditional values.
		
		def queryParameters = [:]
		if (params.patientId) {
			statement += "where s.patientId like :patientId "
			queryParameters.patientId = "%" + params.patientId + "%"
		}
		
		def sortOrder = (params.order == 'asc') ? 'asc' : 'desc';
		if (params.sort == 'patientId') {
			statement += "order by s.patientId $sortOrder "
		}
		
		def subjects = Subject.executeQuery(statement, queryParameters)
		
		return subjects
	}
	
	/**
	 * Returns a list of all samples for a given subject. 
	 * 
	 * @return a list of samples
	 */
	// This is somewhat trivial, as all needs to do is return the list of samples
	def getAllSamples() {
		return samples
	}
	
	/**
	 * Returns a list of all processes for a given subject.
	 * 
	 * @return a list of processes
	 */
	// This is much less trivial
	def getAllProcesses() {
		return Process.withCriteria {
			runSamples {
				sample {
					eq("subject", this)
				}
			}
		}
	}
	
	Map<KnownMutation, List<ObservedMutation>> getObservations() {
		SortedMap<KnownMutation, List> mutations = new TreeMap<KnownMutation, List>()
		
		// This looks like a lot of looping, but most of these will be empty or single entries.
		for(Sample sample : samples) {
			for(RunSample runSample : sample.runSamples) {
				for(ObservedMutation mutation : runSample.mutations) {
					KnownMutation kmut = mutation.knownMutation
					if (! mutations.containsKey(kmut)) {
						mutations.putAt(kmut, [] as List<ObservedMutation>)
					}
					mutations.getAt(kmut).push(mutation)
				}
			}
		}
		
		return mutations
	}
	
	Boolean getRunsCompleted() {
		def runsCompleted = false
		for(Sample sample : samples) {
			for(RunSample runSample : sample.runSamples) {
				return true
			}
		}
		return false
	}
	
	List<ObservedMutation> getObservedMutationsList() {
		def mutations = [] as SortedSet<ObservedMutation>

		// This looks like a lot of looping, but most of these will be empty or single entries.
		for(Sample sample : samples) {
			for(RunSample runSample : sample.runSamples) {
				for(ObservedMutation mutation : runSample.mutations) {
					mutations.add(mutation)
				}
			}
		}
		
		return mutations.asList()
	}
	
	List<String> getMutationsList() {
		return getObservedMutationsList().collect {
			it.knownMutation.gene + " " + it.knownMutation.mutation
		}
	}
	
	/**
	 * Returns a list of unique mutation names. 
	 * @return
	 */
	def getMutations() {
		
		List<String> mutations = getMutationsList()
		
		if (! mutations) {
			return getRunsCompleted() ? "None" : null;
		}
		
		// Fiddle with the list to remove duplicates
		Set<String> uniqueMutations = [] as Set<String>
		uniqueMutations.addAll(mutations)
		mutations.clear()
		mutations.addAll(uniqueMutations)
		
		return mutations.sort().join(", ")
	}
	
	/**
	 * Returns a list of observed mutations for a given subject. This can be filtered and sorted
	 * more easily than some of the other structures, and corresponds more directly to what is needed
	 * for reporting. 
	 * 
	 * @return a list of observed mutations
	 */
	def getSubjectMutationReportData() {
		
		Comparator sampleComparator = new SampleComparator();
		Comparator observedMutationComparator = new ObservedMutationComparator();
		
		SortedMap<String, SortedMap<Sample, List<ObservedMutation>>> data = new TreeMap<String, SortedMap<Sample, List<ObservedMutation>>>();
		
		// This looks like a lot of looping, but most of these will be empty or single entries.
		for(Sample sample : samples) {
			
			String sampleType = sample.type
			if (! data.containsKey(sampleType)) {
				data.put(sampleType, new TreeMap<Sample, List<ObservedMutation>>(sampleComparator));
			}
			
			SortedMap<Sample, List<ObservedMutation>> typeData = data.get(sampleType)
			
			def mutations = []
			for(RunSample runSample : sample.runSamples) {
				for(ObservedMutation mutation : runSample.mutations) {
					mutations.add(mutation)
				}
			}
			
			mutations = mutations.sort(observedMutationComparator);
			typeData.put(sample, mutations)
		}

		return data
	}
	
	Map<String, Object> toMap() {
		Map<String, Object> subjectMap = new HashMap<String, Object>();
		subjectMap.put("id", id)
		subjectMap.put("patientId", patientId)
		subjectMap.put("gender", gender)
		subjectMap.put("summary", summary.toMap())
		subjectMap.put("mutations", mutations)
		return subjectMap
	}
	
	String toString() {
		return "<subject " + patientId + ">";
	}
	
	/**
	 * Finds and returns the most recent decision of a given type. This will depend on the date
	 * and should (a) ignore all withdrawn decisions, even more recent ones, and (b) only include
	 * those of a given sample type. 
	 * @param sampleType
	 * @return
	 */
	Decision findDecision(String sampleType) {
		Decision result = null
		for(Decision next : decisions) {
			if(next.source == sampleType && next.decisionType != Decision.TYPE_WITHDRAWN) {
				result = next
			}
		}
		return result
	}

	/**
	 * Finds and returns the earliest decision of a given type. This will depend on the date
	 * and should (a) ignore all withdrawn decisions, even more recent ones, and (b) only include
	 * those of a given sample type. 
	 * @param sampleType
	 * @return
	 */
	Decision findFirstDecision(String sampleType) {
		Decision result = null
		for(Decision next : decisions) {
			if(next.source == sampleType && next.decisionType != Decision.TYPE_WITHDRAWN) {
				result = next
				break;
			}
		}
		return result
	}
}
