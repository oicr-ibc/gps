package ca.on.oicr.gps.controller

import java.util.Date;

import javax.swing.SpringLayout.Constraints;

import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import ca.on.oicr.gps.model.data.Process;
import ca.on.oicr.gps.model.data.Submission;
import ca.on.oicr.gps.pipeline.model.PipelineRuntimeException;
import ca.on.oicr.gps.pipeline.model.PipelineState;

import grails.plugins.springsecurity.Secured

@Secured(['ROLE_GPS-USERS'])
class SubmissionController {
	
	def pipelineService
	def pipelineRegistry
	def notificationService

    static allowedMethods = []

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.sort = params.sort ?: 'dateSubmitted'
		params.order = params.order ?: 'desc'
		
		def submissions = Submission.list(params)
		def submissionCount = Submission.count()
		
        [submissionInstanceList: submissions, submissionInstanceTotal: submissionCount]
    }

    @Secured(['ROLE_GPS-CONTRIBUTORS'])
	def create = {
        def submissionInstance = new Submission()

        submissionInstance.properties = params
		log.trace(pipelineRegistry.pipelines)
        return [submissionInstance: submissionInstance, pipelines: pipelineRegistry.pipelines]
    }

    @Secured(['ROLE_GPS-CONTRIBUTORS'])
	def save = { SubmissionCommand sc ->
		
		def submissionInstance = new Submission()

		// Inject the values from the command object, rather than directly from the parameters.
		// This makes testing a good bit easier, but also allows validation, not that this is
		// implemented yet. 
				
		def fileName = sc.dataFile.getOriginalFilename()
		def fileContents = sc.dataFile.getBytes()
		submissionInstance.fileName = fileName
		submissionInstance.fileContents = fileContents
		submissionInstance.dataType = sc.dataType
		submissionInstance.dateSubmitted = sc.dateSubmitted
		submissionInstance.userName = sc.userName
		
		// When handling submissions, we ought to run the pipeline, as this will do additional
		// validation, and maybe even generate the additional objects that we need. Any pipeline
		// errors also need to make their way into the flash messages. We can also refuse all 
		// creation of the submission because of these errors, which provides feedback to the
		// person submitting when they upload the submission. 

        if (!submissionInstance.hasErrors() && submissionInstance.save(flush: true)) {
			
			PipelineState state = pipelineService.getPipelineState(submissionInstance)
			
			try {
				pipelineService.runPipeline(state)
			} catch (PipelineRuntimeException error) {
			
				// If we get here, we have encountered an error, and ought to remove the submission. However, 
				// we do want the model to use for rendering the page again.
				
				// ERRORS: deleted object would be re-saved by cascade 
			
				//submissionInstance.delete(flush: true)
				//submissionInstance.discard()
				
				state = error.state
				flash.message = "${message(code: 'default.pipelineErrors.message', args: [message(code: 'submission.label', default: 'Submission'), submissionInstance.id])}"
				flash.pipelineErrors = state.errors
				
				render(view: "create", model: [submissionInstance: submissionInstance, pipelines: pipelineRegistry.pipelines])
				return
			} catch (Exception error) {
			
				// See GPS-74. This is where we end up if we hit a non pipeline error, and even 
				// then we should not save the submission. This is similar in style, but with 
				// different error rendering, since these are Java errors. 
				//
				// This is kind of defensive coding, so we should engineer an exception of some
				// kind that we can use to test this case. The ideal way would be to stub with
				// an exception throwing code fragment. 
			
				log.error(error.getLocalizedMessage())
				error.printStackTrace()
				
				submissionInstance.delete(flush: true)
				submissionInstance.discard()
				
				flash.message = "${message(code: 'default.pipelineErrors.message', args: [message(code: 'submission.label', default: 'Submission'), submissionInstance.id])}"
				flash.pipelineErrors = [error]
				
				render(view: "create", model: [submissionInstance: submissionInstance, pipelines: pipelineRegistry.pipelines])
				return
			}
			
			// And send an email!
			notificationService.sendNotification(
				notification: notificationService.NOTIFY_INFORMATION, 
				view: 'submission',
				subject: "Submission Received: " + submissionInstance.fileName,
				model: [submissionInstance: submissionInstance],
				attachment: sc.dataFile.getBytes(),
				attachmentFileName: sc.dataFile.getOriginalFilename(),
				attachmentContentType: sc.dataFile.getContentType()
			)
			
			// If we get here, we are good to continue, and the status is OK
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'submission.label', default: 'Submission'), submissionInstance.id])}"
            redirect(action: "show", id: submissionInstance.id)
        }
        else {
            render(view: "create", model: [submissionInstance: submissionInstance])
        }
    }

    def show = {
        def submissionInstance = Submission.get(params.id)
        if (!submissionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'submission.label', default: 'Submission'), params.id])}"
            redirect(action: "list")
        }
        else {
            [submissionInstance: submissionInstance]
        }
    }

    @Secured(['ROLE_GPS-CONTRIBUTORS'])
	def edit = {
        def submissionInstance = Submission.get(params.id)
        if (!submissionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'submission.label', default: 'Submission'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [submissionInstance: submissionInstance]
        }
    }

    @Secured(['ROLE_GPS-CONTRIBUTORS'])
	def update = {
        def submissionInstance = Submission.get(params.id)
        if (submissionInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (submissionInstance.version > version) {
                    
                    submissionInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'submission.label', default: 'Submission')] as Object[], "Another user has updated this Submission while you were editing")
                    render(view: "edit", model: [submissionInstance: submissionInstance])
                    return
                }
            }
            submissionInstance.properties = params
            if (!submissionInstance.hasErrors() && submissionInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'submission.label', default: 'Submission'), submissionInstance.id])}"
                redirect(action: "show", id: submissionInstance.id)
            }
            else {
                render(view: "edit", model: [submissionInstance: submissionInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'submission.label', default: 'Submission'), params.id])}"
            redirect(action: "list")
        }
    }

    @Secured(['ROLE_GPS-CONTRIBUTORS'])
	def delete = {
        def submissionInstance = Submission.get(params.id)
        if (submissionInstance) {
            try {
                submissionInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'submission.label', default: 'Submission'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'submission.label', default: 'Submission'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'submission.label', default: 'Submission'), params.id])}"
            redirect(action: "list")
        }
    }

    @Secured(['ROLE_GPS-CONTRIBUTORS'])
	def download = {
        def submissionInstance = Submission.get(params.id)
		
		if (request.method == 'PUT') {
			InputStream	inputStream = request.getInputStream()
			byte[] data = IOUtils.toByteArray(inputStream)
			submissionInstance.fileContents = data
			render ""
			return
		}

        if (submissionInstance) {
            response.setContentType("application/octet-stream")
            response.setHeader("Content-disposition", "attachment;filename=${submissionInstance.fileName}")

            response.outputStream << submissionInstance.fileContents // Performing a binary stream copy
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'submission.label', default: 'Submission'), params.id])}"
            redirect(action: "list")
        }
    }
}

final class SubmissionCommand {
	String dataType
	String userName
	Date dateSubmitted
	MultipartFile dataFile
	
	static constraints = {}
}