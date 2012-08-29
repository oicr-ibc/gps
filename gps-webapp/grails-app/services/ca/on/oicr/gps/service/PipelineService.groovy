package ca.on.oicr.gps.service

import org.hibernate.Session;
import org.hibernate.FlushMode;
import org.hibernate.Transaction;
 
import ca.on.oicr.gps.model.data.Process;
import ca.on.oicr.gps.model.data.RunSample;
import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Submission;
import ca.on.oicr.gps.pipeline.DomainFacadeImpl;
import ca.on.oicr.gps.pipeline.Pipeline
import ca.on.oicr.gps.pipeline.PipelineRunner
import ca.on.oicr.gps.pipeline.domain.DomainFacade;
import ca.on.oicr.gps.pipeline.model.MutationSubmission;
import ca.on.oicr.gps.pipeline.model.PipelineRuntimeException;
import ca.on.oicr.gps.pipeline.model.PipelineState

import ca.on.oicr.gps.pipeline.SubmissionSource

class PipelineService {
	
	def mutationKnowledgeService
	def pipelineRegistry
	def reportingService

    static transactional = true
	
    def runPipeline(PipelineState state) {

		PipelineRunner runner = new PipelineRunner(state)
		runner.run()
		
		// Throw a RuntimeException, which is the approved manner of notifying a service that
		// you want to roll back a transaction
		if (state.hasFailed()) {
			log.error("Error: " + state.errors)
			throw new PipelineRuntimeException(state)
		}
		
		SubmissionSource subSource = state.get(MutationSubmission.class)
		Submission sub = subSource.domainSubmission
		log.trace("Handling reporting submission for: " + sub)
		for(Subject subject in sub.subjects) {
			log.trace("Updating summary dates")
			reportingService.updateSummaryDates(subject)
			
			log.trace("Report update needed for: " + subject.patientId)
			reportingService.saveSubjectReport(subject) 
		}
		
		return state
    }
	
	def getPipelineState(Submission submission) {
		
		SubmissionSource source = new SubmissionSource(domainSubmission: submission)
		
		Pipeline pipeline = pipelineRegistry.pipelines.find { it.canHandleSubmission(source) }
		if (pipeline == null) {
			throw new RuntimeException("Can't handle submission of type: " + submission.dataType)
		}
		
		DomainFacade domain = new DomainFacadeImpl(source, mutationKnowledgeService)
		PipelineState state = pipeline.newState(source, domain)
		return state		
	}
}
