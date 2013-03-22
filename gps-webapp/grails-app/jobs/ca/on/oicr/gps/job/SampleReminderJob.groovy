package ca.on.oicr.gps.job

import ca.on.oicr.gps.model.data.Sample

class SampleReminderJob {
	def mailService
	def appConfigService
	def notificationService
	
    static triggers = {
		// 1 min delay on start, 5 min listening interval
		simple name:'firstReminder', startDelay:10000, repeatInterval:  5 * 60000, repeatCount: -1
	}

    def execute() {
		def sampleList = Sample.withCriteria {
			Date now = new Date()
			Integer currentHour = now.format('HH') as int
			Integer currentMinute = now.format('mm') as int
			Date nowMinusDelay = now.updated(minute:currentMinute - appConfigService?.delay())
			Date nowMinusDelayAndInterval = nowMinusDelay.updated(minute:currentMinute - 5)

			isNull('dateReceived')
			isNotNull('dateCreated')
			eq('requiresCollection', true)
			between('dateCreated', nowMinusDelayAndInterval, nowMinusDelay)
		}
		if (sampleList) {
			notificationService.sendNotification(
				notification: notificationService.NOTIFY_REMINDER, 
				view: 'reminder',
				subject: "Sample Reminder",
				model: [sampleList:sampleList]
			)
		}
    }
}
