package ca.on.oicr.gps.job

import java.lang.StringBuffer;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

class ChangeSummaryJob {
	
	static final Logger log = Logger.getLogger(this)

	def appDataService
	def notificationService
	def changeSummaryService

	/**
	 * Specifies timing, cron-style, as that is exactly what we need. 	
	 */
    static triggers = {
		cron name: 'dailyEmailTrigger', cronExpression: "0 0 7 * * ?"
//		cron name: 'dailyEmailTrigger', cronExpression: "0 * * * * ?"
	}
	
	// Beware - according to the log I just ran, Quartz jobs can start before Bootstrap.groovy
	// has script has run. That can make stuff a little interesting. 

    def execute() {
		log.info("Running daily email job")
		
		Date newModifiedSince = new Date()
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
		
		def lastModifiedSince = appDataService.getAttribute('ModifiedSince')
		if (lastModifiedSince) {
			ParsePosition p = new ParsePosition(0);
			lastModifiedSince = sdf.parse(lastModifiedSince, p)
			log.info("Looking for changes since: " + lastModifiedSince)
		} else {
			log.info("Looking for all changes");
		}
		
		def changes = changeSummaryService.getChanges()
		if (changes) {
			notificationService.sendNotification(
				notification: notificationService.NOTIFY_UPDATE,
				view: 'update',
				subject: "Daily update",
				model: [changes:changes, modifiedSince: lastModifiedSince]
			);
		}

		// Now we set the ModifiedSince property to be a value that is, basically, now. Well, get
		// the time from the start of the job just in case of #paranoia
		
		FieldPosition fp = new FieldPosition(0)
		StringBuffer buffer = new StringBuffer()
		sdf.format(newModifiedSince, buffer, fp)
		appDataService.setAttribute('ModifiedSince', buffer.toString())
	}
}
