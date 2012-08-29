package ca.on.oicr.gps.service

import org.springframework.security.core.context.SecurityContextHolder;

import ca.on.oicr.gps.model.data.Attachment;

import grails.util.Environment;

/**
 * NotificationService is a general service that can be used to send notifications
 * from GPS. Typically it sends emails, but it can be refactored or intercepted
 * in ways that allow other kinds of notifications to be sent. 
 * 
 * @author swatt
 */
class NotificationService {

    static transactional = true

	def mailService
	def appConfigService
	def currentUserService
	
	/**
	 * Notification type to specify a new sample notification
	 */
	static NOTIFY_CREATED = 'create'
	
	/**
	 * Notification type to specify a received sample notification
	 */
	static NOTIFY_RECEIVED = 'received'
	
	/**
	 * Notification type to specify a sample reminder
	 */
	static NOTIFY_REMINDER = 'reminder'

	/**
	 * Notification type to specify an information message
	 */
	static NOTIFY_INFORMATION = 'information'

	/**
	 * Notification type to specify an updates message
	 */
	static NOTIFY_UPDATE = 'update'

	/**
	 * This method is used to send a notification. This encapsulates the mailing
	 * logic, allowing it to be mocked more easily for testing, and also allowing
	 * it to be extended to use other kinds of notifications at a later stage.
	 * Growl, for example, might be an example; or sending an SMS. 
	 * 
	 * @param notifyType - the type of notification
	 * @param message - the message
	 * @param model - model to use when rendering message
	 */
    void sendNotification(Map args) {
		
		String notification = args.notification
		String view = args.view
		String subjectLine = args.subject
		Map model = args.model ?: [:]
		
		String userName = currentUserService.currentUserName()
		model.userName = userName ?: "unknown"

		log.info("Sending notification: type: " + notification + ", subject: " + subjectLine)
		
		def environment = Environment.getCurrent()
		def subjectPrefix = appConfigService.emailSubjectPrefix() ?: (environment == Environment.PRODUCTION ? "gps" : environment.getName() + " gps")
		def mailSubject = "[" + subjectPrefix + "] " + subjectLine
		
		def mailView = view
		def mailRecipients
		
		switch (notification) {
			case NOTIFY_CREATED:
				mailRecipients = appConfigService.createSampleEmail()
				break
			case NOTIFY_RECEIVED:
				mailRecipients = appConfigService.receivedSampleEmail()
				break
			case NOTIFY_REMINDER:
				mailRecipients = appConfigService.reminderSampleEmail()
				break
			case NOTIFY_INFORMATION:
				mailRecipients = appConfigService.informationEmail()
				break
			case NOTIFY_UPDATE:
				mailRecipients = appConfigService.updateEmail()
				break
		}
		
		// It might seem odd that we simply stick the view name onto the /sample/mail 
		// prefix. It is intentional, however. If we use other notification
		// systems, we can add parallel sample views with the same view keys
		
		if (! mailRecipients) {
			log.info("No recipients configured for this type of message")
			return
		}
		
		assert mailRecipients
		log.info("Sending email to: " + mailRecipients)
		
		if (args.containsKey("attachment")) {
			Attachment att = new Attachment(
				attachment: args.attachment,
				attachmentFileName: args.attachmentFileName,
				attachmentContentType: args.attachmentContentType
			)
			att.save()
			
			// Check we now have a unique identifier for the download
			assert att.uniqueIdentifier
			model.putAt("attachmentId", att.uniqueIdentifier)
		}

		mailService.sendMail {
			to mailRecipients
			from 'gps@oicr.on.ca'
			subject mailSubject
			body (view: '/mail/' + mailView, model: model)
		}
    }
}