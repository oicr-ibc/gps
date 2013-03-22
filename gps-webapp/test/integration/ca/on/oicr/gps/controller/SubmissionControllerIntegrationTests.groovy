package ca.on.oicr.gps.controller

import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.gmock.WithGMock;

import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import ca.on.oicr.gps.controller.SubmissionCommand;
import ca.on.oicr.gps.controller.SubmissionController;
import ca.on.oicr.gps.model.data.Submission;
import ca.on.oicr.gps.pipeline.SubmissionSource;
import ca.on.oicr.gps.service.NotificationService;
import ca.on.oicr.gps.test.PipelineTestCase;

import grails.plugin.mail.MailService;
import grails.test.*

@WithGMock
class SubmissionControllerIntegrationTests extends PipelineTestCase {
	
	def controller
	
	def pipelineService
	def messageSource
	def appConfigService
	def currentUserService
	
	Map savedMetaClasses = [:]
	Map renderMap
	Map redirectMap
	
    public void setUp() {
        super.setUp()
		
		registerMetaClass(SubmissionController.class)
		SubmissionController.metaClass.render = {Map m ->
			renderMap = m
		}
		SubmissionController.metaClass.redirect = {Map m ->
			redirectMap = m
		}
		
		def mailServiceMocker = mock(MailService)
		mailServiceMocker.sendMail(
			match {
				it instanceof Closure
			}
		).returns().stub()
		
		def notificationService = new NotificationService()
		notificationService.mailService = mailServiceMocker
		notificationService.appConfigService = appConfigService
		notificationService.currentUserService = currentUserService

		controller = new SubmissionController()
		controller.pipelineService = pipelineService
		controller.notificationService = notificationService
    }
	
    public void tearDown() {
        super.tearDown()
		
		savedMetaClasses.each { clazz, metaClass ->
			GroovySystem.metaClassRegistry.setMetaClass(clazz, metaClass)
		}
    }
	
    void testSaveSubmissionSequenom() {
		
		def mockFile = mock(MultipartFile)
		mockFile.getOriginalFilename().returns("test/data/sequenom_test_01.xls").stub()
		mockFile.getBytes().returns(IOUtils.toByteArray(new FileInputStream(new File(System.properties['base.dir'], "test/data/sequenom_test_01.xls")))).stub()
		mockFile.getContentType().returns("application/vnd.ms-excel").stub()
		
		def sc = new SubmissionCommand()
		
		play {
			sc.dataFile = mockFile
			sc.userName = 'Morag'
			sc.dateSubmitted = new Date()
			sc.dataType = 'Sequenom'

			controller.save(sc)
		}
		
		// Check we dodn't get a render, but a redirect
		assertNull(renderMap)
		assertNotNull(redirectMap)
		
		def identifier = redirectMap.id
		Submission submissionInstance = Submission.get(identifier)
		
		assertNotNull(submissionInstance)
		
		assertEquals(sc.dataType, submissionInstance.dataType)
		assertEquals(sc.userName, submissionInstance.userName)
		
		// Now check we have a process for that submission
		assertEquals(3, submissionInstance.processes.size())
    }

    void testSaveSubmissionSanger() {
		
		def mockFile = mock(MultipartFile)
		mockFile.getOriginalFilename().returns("test/data/sanger_test_02.xls").stub()
		mockFile.getBytes().returns(IOUtils.toByteArray(new FileInputStream(new File(System.properties['base.dir'], "test/data/sanger_test_02.xls")))).stub()
		mockFile.getContentType().returns("application/vnd.ms-excel").stub()
		
		def sc = new SubmissionCommand()
		
		play {
			sc.dataFile = mockFile
			sc.userName = 'Morag'
			sc.dateSubmitted = new Date()
			sc.dataType = 'ABI'
		
			controller.save(sc)
		}
		
		// Check we dodn't get a render, but a redirect
		assertNull(renderMap)
		assertNotNull(redirectMap)
		
		def identifier = redirectMap.id
		Submission submissionInstance = Submission.get(identifier)
		
		assertNotNull(submissionInstance)
		
		assertEquals(sc.dataType, submissionInstance.dataType)
		assertEquals(sc.userName, submissionInstance.userName)
		
		// Now check we have a process for that submission
		assertEquals(3, submissionInstance.processes.size())
    }
	
    void testSaveSubmissionPacBio() {
		
		def mockFile = mock(MultipartFile)
		mockFile.getOriginalFilename().returns("test/data/pacbio_test_04.xls").stub()
		mockFile.getBytes().returns(IOUtils.toByteArray(new FileInputStream(new File(System.properties['base.dir'], "test/data/pacbio_test_04.xls")))).stub()
		mockFile.getContentType().returns("application/vnd.ms-excel").stub()

		def sc = new SubmissionCommand()
		
		play {
			sc.dataFile = mockFile
			sc.userName = 'Morag'
			sc.dateSubmitted = new Date()
			sc.dataType = 'PacBioV2'
		
			controller.save(sc)
		}
		
		// Check we dodn't get a render, but a redirect
		assertNull(renderMap)
		assertNotNull(redirectMap)
		
		def identifier = redirectMap.id
		Submission submissionInstance = Submission.get(identifier)
		
		assertNotNull(submissionInstance)
		
		assertEquals(sc.dataType, submissionInstance.dataType)
		assertEquals(sc.userName, submissionInstance.userName)
		
		// Now check we have a process for that submission
		assertEquals(3, submissionInstance.processes.size())
    }
	
	// Stolen from GrailsUnitTestCase
    /**
     * Use this method when you plan to perform some meta-programming
     * on a class. It ensures that any modifications you make will be
     * cleared at the end of the test.
     * @param clazz The class to register.
     */
    protected void registerMetaClass(Class clazz) {
        // If the class has already been registered, then there's
        // nothing to do.
        if (savedMetaClasses.containsKey(clazz)) return

        // Save the class's current meta class.
        savedMetaClasses[clazz] = clazz.metaClass

        // Create a new EMC for the class and attach it.
        def emc = new ExpandoMetaClass(clazz, true, true)
        emc.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(clazz, emc)
    }
}
