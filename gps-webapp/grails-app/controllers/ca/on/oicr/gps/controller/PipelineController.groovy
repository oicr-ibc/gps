package ca.on.oicr.gps.controller

import grails.plugins.springsecurity.Secured
import ca.on.oicr.gps.model.data.Submission;
import ca.on.oicr.gps.pipeline.SubmissionSource
import ca.on.oicr.gps.pipeline.model.PipelineError;
import ca.on.oicr.gps.pipeline.model.PipelineRuntimeException;
import ca.on.oicr.gps.pipeline.model.PipelineState;

@Secured(['ROLE_GPS-CONTRIBUTORS'])
class PipelineController {
	
	def pipelineService

	def index = {
	}

	def run = {
		
		Submission s = Submission.get(params.id)
		if (!s) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'submission.label', default: 'Submission'), params.id])}"
			redirect(controllerName: 'submission', action: "list")
		}
		
		log.info("Running pipeline for submission: " + s.id +" of type: " + s.dataType)

		PipelineState state = pipelineService.getPipelineState(s)
		
		try {
			// Run the pipeline
			pipelineService.runPipeline(state)
		} catch (PipelineRuntimeException err) {
			// Ignore, as we have the state reporting the error already
		} catch (RuntimeException err) {
			log.error(err.getMessage())
			err.printStackTrace()
			
			// Just use an empty state, since the one we had will have been wiped by the exception
			state.error("java.error", err.class.getName(), err.getMessage())
		}
		
		for(PipelineError err in state.errors()) {
			log.warn("Error: " + err.key + ", " + err.args);
		}
		
		[submissionInstance: s, pipelineErrors: state.errors()]
	}
}
