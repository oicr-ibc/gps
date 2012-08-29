package ca.on.oicr.gps.job

import ca.on.oicr.gps.service.NotificationService;
import grails.plugin.mail.MailService;
import grails.test.*
import org.gmock.WithGMock;

@WithGMock
class SampleReminderJobIntegrationTests extends GroovyTestCase {
	
	def notificationService
	def appConfigService
	def currentUserService
	
    public void setUp() {
        super.setUp()
		
		def mailServiceMocker = mock(MailService)
		mailServiceMocker.sendMail(
			match {
				it instanceof Closure
			}
		).returns().stub()
		
		notificationService = new NotificationService()
		notificationService.mailService = mailServiceMocker
		notificationService.appConfigService = appConfigService
		notificationService.currentUserService = currentUserService

    }

    public void tearDown() {
        super.tearDown()
    }

    void testJobExecution() {
		def job = new SampleReminderJob()
		job.notificationService = notificationService
		job.appConfigService = appConfigService
		job.mailService = notificationService.mailService
		job.execute()
    }
}
