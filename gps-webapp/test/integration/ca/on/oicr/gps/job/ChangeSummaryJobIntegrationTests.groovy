package ca.on.oicr.gps.job

import org.gmock.WithGMock;

import ca.on.oicr.gps.service.NotificationService;
import grails.plugin.mail.MailService;
import grails.test.*
import org.gmock.WithGMock;

@WithGMock
class ChangeSummaryJobIntegrationTests extends GroovyTestCase {
	
	def notificationService
	def appDataService
	def appConfigService
	def changeSummaryService
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
		def job = new ChangeSummaryJob()
		job.notificationService = notificationService
		job.changeSummaryService = changeSummaryService
		job.appDataService = appDataService
		job.execute()
    }
}
