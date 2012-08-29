package ca.on.oicr.gps.model.data

import java.util.Date;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.Weeks;

import ca.on.oicr.gps.util.TimeUtilities;


class Summary {
	
	public static final String PSYCHOSOCIAL_UNKNOWN = "unknown"
	public static final String PSYCHOSOCIAL_AGREED = "agreed"
	public static final String PSYCHOSOCIAL_REFUSED = "refused"
	public static final String PSYCHOSOCIAL_INELIGIBLE = "ineligible"
	public static final String PSYCHOSOCIAL_COMPLETED = "completed"
	
	def timeService
	
	static belongsTo = [ subject: Subject ]
	
	String institution
	
	String primaryPhysician
	String primaryTumorSite
	
	//TODO map these into a workflow/process model
	Date consentDate
	Date biopsyDate
	String biopsySite
	Integer biopsyCores = null
	
	Date pathologyArrivalDate
	Date sequenomArrivalDate
	Date pacbioArrivalDate
	Date medidataUploadDate
	Date archivalArrivalDate
	String comment
	
	Date expertPanelInterimDecisionDate
	Date expertPanelDecisionDate
	String expertPanelDecision
	String reportedMutations
	
	String psychosocial = PSYCHOSOCIAL_UNKNOWN
	
   	static constraints = {
		institution(nullable: true, maxSize: 32)
		primaryPhysician(nullable: true, maxSize: 64)
		primaryTumorSite(nullable: true, maxSize: 256)
		consentDate(nullable: true)
		biopsyDate(nullable: true)
		biopsySite(nullable: true, maxSize: 256)
		biopsyCores(nullable: true)
		pathologyArrivalDate(nullable: true)
		sequenomArrivalDate(nullable: true)
		pacbioArrivalDate(nullable: true)
		medidataUploadDate(nullable: true)
		archivalArrivalDate(nullable: true)
		expertPanelInterimDecisionDate(nullable: true)
		expertPanelDecisionDate(nullable: true)
		expertPanelDecision(nullable: true)
		reportedMutations(nullable: true)
		psychosocial(nullable: false, inList:[
			PSYCHOSOCIAL_UNKNOWN, 
			PSYCHOSOCIAL_AGREED,
			PSYCHOSOCIAL_REFUSED,
			PSYCHOSOCIAL_INELIGIBLE,
			PSYCHOSOCIAL_COMPLETED
		])
		comment(nullable: true, maxSize: 1024)
	}

	static audit = [ 'institution', 
		             'primaryPhysician',
					 'primaryTumorSite',
					 'consentDate',
					 'biopsyDate',
					 'biopsySite',
					 'biopsyCores',
					 'pathologyArrivalDate', 
					 'sequenomArrivalDate',
					 'pacbioArrivalDate',
					 'medidataUploadDate',
					 'archivalArrivalDate',
					 'expertPanelInterimDecisionDate',
					 'expertPanelDecisionDate',
					 'expertPanelDecision',
					 'reportedMutations',
					 'psychosocial',
					 'comment'
					 ]

	static transients = [ 
		"elapsedWorkingDays", 
		"computedMedidataUploadDate", 
		"psychosocialCode"
	]
	
	/**
	 * Returns the number of elapsed working days. 
	 * @return
	 */
	Integer getElapsedWorkingDays() {
		
		def startDate = consentDate == null ? biopsyDate : consentDate
		
		// If this isn't valid, return null
		if (! startDate) {
			return null
		}
		
		if (expertPanelDecisionDate) {
			return TimeUtilities.calculateWorkingDays(new DateTime(startDate), new DateTime(expertPanelDecisionDate))
		} else {
			return TimeUtilities.calculateWorkingDays(new DateTime(startDate), timeService.now())
		}
	}

	Map<String, String> toMap() {
		Map<String, Object> summaryMap = new HashMap<String, Object>()
		summaryMap.put("institution", institution)
		summaryMap.put("elapsedWorkingDays", getElapsedWorkingDays())
		summaryMap.put("primaryPhysician", primaryPhysician)
		summaryMap.put("primaryTumorSite", primaryTumorSite)
		summaryMap.put("consentDate", consentDate)
		summaryMap.put("biopsyDate", biopsyDate)
		summaryMap.put("biopsySite", biopsySite)
		summaryMap.put("biopsyCores", biopsyCores)
		summaryMap.put("pathologyArrivalDate", pathologyArrivalDate)
		summaryMap.put("sequenomArrivalDate", sequenomArrivalDate)
		summaryMap.put("pacbioArrivalDate", pacbioArrivalDate)
		summaryMap.put("archivalArrivalDate", archivalArrivalDate)
		summaryMap.put("expertPanelInterimDecisionDate", expertPanelDecisionDate)
		summaryMap.put("expertPanelDecisionDate", expertPanelDecisionDate)
		summaryMap.put("expertPanelDecision", expertPanelDecision)
		summaryMap.put("reportedMutations", reportedMutations)
		summaryMap.put("psychosocial", psychosocial)
		summaryMap.put("comment", comment)

		summaryMap.put("medidataUploadDate", getComputedMedidataUploadDate())

		return summaryMap
	}
	
	/**
	 * Returns a computed version of the Medidata upload date. This will be null if any have not been
	 * uploaded, otherwise the most recent. 
	 * @return computed version of the Medidata upload date
	 */
	Date getComputedMedidataUploadDate() {
		
		def statement = "\
		select new map(max(ob.reported) as reported, \
                       count(*) as mutationCount, \
                       count(ob.reported) as reportedCount \
					   ) \
		from ObservedMutation as ob \
        join ob.runSample as rs \
        join rs.sample as s \
        join s.subject as sub \
		where sub.id = :subjectId \
		";
				
		def queryParameters = [subjectId: this.subject.id]
		def counts = Summary.executeQuery(statement, queryParameters)
		
		assert counts.size() == 1
		counts = counts[0]
		
		if (counts.mutationCount > 0 && counts.mutationCount == counts.reportedCount) {
			return counts.reported
		} else {
			return null
		}
	}

	String getPsychosocialCode() {
		
		String result = "unknown"
		switch (psychosocial) {
			case PSYCHOSOCIAL_UNKNOWN:           result = "unknown"; break;
			case PSYCHOSOCIAL_AGREED:            result = "agreed"; break;
			case PSYCHOSOCIAL_REFUSED:           result = "refused"; break;
			case PSYCHOSOCIAL_INELIGIBLE:        result = "ineligible"; break;
			case PSYCHOSOCIAL_COMPLETED:         result = "completed"; break;
		}
		return result
	}

	static Integer toPsychosocial(String code) {
		Integer result = PSYCHOSOCIAL_UNKNOWN
		switch (code) {
			case "unknown":             result = PSYCHOSOCIAL_UNKNOWN; break;
			case "agreed":              result = PSYCHOSOCIAL_AGREED; break;
			case "refused":             result = PSYCHOSOCIAL_REFUSED; break;
			case "ineligible":          result = PSYCHOSOCIAL_INELIGIBLE; break;
			case "completed":           result = PSYCHOSOCIAL_COMPLETED; break;
		}
		return result
	}
}
