package ca.on.oicr.gps.service

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.on.oicr.gps.model.system.AuditRecord;

/**
 * The change summary service is used to generate and display change summaries. Basically, all
 * subjects and summaries that were updated in the last 24 hours or so will be displayed. 
 * For an email, we only need a count of the changes. For a view, we want to detail them
 * more precisely. The lastUpdated field is a useful base for this, as we can report all
 * that have been reported since then. Obviously, we need to persistently store the boundary
 * date so we only report events since the last window. 
 * 
 * @author swatt
 */

class ChangeSummaryService {

    static transactional = true
	
	def appDataService
	
    def getChanges() {
		def modifiedSinceString = appDataService.getAttribute('ModifiedSince')
		
		Date modifiedSince
		
		if (modifiedSinceString) {
			ParsePosition p = new ParsePosition(0);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			modifiedSince = sdf.parse(modifiedSinceString, p)
		} else {
			modifiedSince = new Date()
		}
		
		// Now we can check for stuff that has changed since this date. 
		List<AuditRecord> results = AuditRecord.findAllByTimestampGreaterThanEquals(modifiedSince)

		// The audit record isn't actually linked to the regular records. In an ideal
		// world, we would have used Hibernate envers to do the versioning, but the
		// Grails integration of this is still in an earl stage. We do have the record
		// primary keys and the properties, and that ought to be enough. In addition,
		// we have a summary and a subject record. We kept these as separate because
		// we were facing issues of migrating existing data. We should probably just
		// have resolved the migration manually, and made things nicer. Now we are
		// suffering from that technical debt. 
		
		// Output required:
		// Patient Id -> [Added/Updated/Deleted [when/who]...]
		//
		// Aggregated view:
		// Added/Updated/Deleted -> count
		//
		// Obviously the second can be generated from the first. 
		
		def resultsByType = [insert: [] as Set, delete: [] as Set, update: [] as Set]
		
		results.each {
			resultsByType[it.typeName] << it.patientId
		}
		
		// Remove the empty keys
		resultsByType = resultsByType.findAll { it.value.size() > 0 }
		
		// Build a new count table
		def countsByType = [:]
		resultsByType.each { 
			countsByType[it.key] = it.value.size() 
		}
		
		return countsByType
    }
}
