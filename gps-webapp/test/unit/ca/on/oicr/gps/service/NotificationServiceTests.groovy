package ca.on.oicr.gps.service

import ca.on.oicr.gps.model.data.Attachment;
import ca.on.oicr.gps.service.AppConfigService;
import ca.on.oicr.gps.service.NotificationService;

import grails.plugin.mail.MailService;
import grails.test.*

class NotificationServiceTests extends GrailsUnitTestCase {
	
	def notifier
	def messages = []
	def attachments = []
	
    public void setUp() {
        super.setUp()
		
		mockDomain(Attachment, attachments)

		def mockCurrentUserService = mockFor(CurrentUserService, true)
		mockCurrentUserService.demand.currentUserName(0..1000) {-> return "wobble"}
		
		def mockMailService = mockFor(MailService, true)
		
		// Since the argument to sendMail is actually a closure(!) getting the data
		// out of it from a mock is pretty hard. This makes it hard to check the rendering, too. 
		// All of this just needs to happen at integration stage
		mockMailService.demand.sendMail() {}
		
		def mockAppConfigService = mockFor(AppConfigService, true)
		mockAppConfigService.demand.emailSubjectPrefix() {-> return "wibble"}
		mockAppConfigService.demand.createSampleEmail() {-> return ['test_create@gps.oicr.on.ca'] }
		mockAppConfigService.demand.receivedSampleEmail() {-> return ['test_received@gps.oicr.on.ca'] }
		mockAppConfigService.demand.reminderSampleEmail() {-> return ['test_reminder@gps.oicr.on.ca'] }
		mockAppConfigService.demand.informationEmail() {-> return ['test_infomation@gps.oicr.on.ca'] }
		mockAppConfigService.demand.updateEmail() {-> return ['test_update@gps.oicr.on.ca'] }
		mockAppConfigService.demand.newPatientEmail() {-> return ['test_new_patient@gps.oicr.on.ca'] }
		
		// See: http://hartsock.blogspot.com/2008/02/logging-in-unit-test-of-service-in.html
		// Logging in a unit test does not work by default
		
		mockLogging(NotificationService)
		
		notifier = new NotificationService()
		notifier.mailService = mockMailService.createMock()
		notifier.appConfigService = mockAppConfigService.createMock()
		notifier.currentUserService = mockCurrentUserService.createMock()
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Unit tests for mail notification
	 */
    void testCreateNotification() {
		
		def currentUserService = notifier.currentUserService
		assertEquals "wobble", currentUserService.currentUserName()
		assertEquals "wobble", currentUserService.currentUserName()
		
		notifier.sendNotification(
			notification: notifier.NOTIFY_CREATED, 
			view: 'create', 
			subject: "Created message", 
			model: []
		)
    }
	
	void testReceivedNotification() {
		notifier.sendNotification(
			notification: notifier.NOTIFY_RECEIVED, 
			view: 'received', 
			subject: "Received message", 
			model: []
		)
	}
	
	void testReminderNotification() {
		notifier.sendNotification(
			notification: notifier.NOTIFY_REMINDER, 
			view: 'reminder', 
			subject: "Reminder message", 
			model: []
		)
    }

	void testInformationNotification() {
		notifier.sendNotification(
			notification: notifier.NOTIFY_INFORMATION, 
			view: 'information', 
			subject: "Information message", 
			model: []
		)
    }

	void testUpdateNotification() {
		notifier.sendNotification(
			notification: notifier.NOTIFY_UPDATE, 
			view: 'update', 
			subject: "Update message", 
			model: []
		)
    }

	void testInformationSubmissionNotification() {
		notifier.sendNotification(
			notification: notifier.NOTIFY_INFORMATION, 
			view: 'submission', 
			subject: "Information message", 
			model: [],
			attachment: 'This is a simple text file attachment',
			attachmentFilename: 'foo.txt',
			attachmentContentType: 'text/plain'
		)
    }
}
