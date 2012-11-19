package ca.on.oicr.gps.controller

import grails.plugins.springsecurity.Secured

import ca.on.oicr.gps.model.data.Decision;
import ca.on.oicr.gps.model.data.ObservedMutation;
import ca.on.oicr.gps.model.data.ReportableMutation;
import ca.on.oicr.gps.model.data.Subject;

@Secured(['ROLE_GPS-CONTRIBUTORS'])
class DecisionController {

    def index = { }
	
	/**
	 * Cancels a decision. Redirects to the summary. 
	 * 
	 * @return nothing important
	 */
	def cancel() {
		def subjectId = params._subject
		redirect(controller: "summary", action: "show", params: [id: subjectId], fragment: "decisions")
	}
	
	
	@Secured(['ROLE_GPS-USERS'])
	def show = { 
		def decisionId = params.id
		def decision = Decision.get(decisionId)
		
		if (! decision) {
			throw new RuntimeException("Can't find decision: " + decisionId)
		}
		
		[decisionInstance: decision]
	}

	@Secured(['ROLE_GPS-CONTRIBUTORS'])
    def create = {
        def decisionInstance = new Decision()
		def subjectId = params._subject
		
		def subject = Subject.get(subjectId)
		if (! subject) {
			throw new RuntimeException("Can't find subject: " + subjectId)
		}
		
        decisionInstance.properties = params
		decisionInstance.subject = subject
		
		subject.getObservations().entrySet().collect {
			def rep = new ReportableMutation()
			rep.observedMutations = it.value
			decisionInstance.addToReportableMutations(rep)
		}
		
        return [decisionInstance: decisionInstance]
    }
		
	@Secured(['ROLE_GPS-CONTRIBUTORS'])
	def withdraw = {
		def decisionId = params._decision
		def decision = Decision.get(decisionId)
		assert decision
		
		decision.decisionType = Decision.TYPE_WITHDRAWN
		decision.save()
		
		redirect(action: "show", params: [id: decision.id])
	}

	@Secured(['ROLE_GPS-CONTRIBUTORS'])
    def save = {
		// First locate the subject
		def subjectId = params._subject
		log.info("Locating patientId: " + subjectId)
		
		def subject = Subject.get(subjectId)
		if (! subject) {
			throw new RuntimeException("Can't find subject: " + subjectId)
		}
		assert subject
		
		params.put('date', new Date().parse("dd/MM/yyyy", params.remove('date')))
		
        def decisionInstance = new Decision(params)
		
		// All previous decisions should be marked as withdrawn, assuming they are
		// of the same source type. 
		for(Decision decision : subject.decisions) {
			if (decision.source == decisionInstance.source) {
				decision.decisionType = Decision.TYPE_WITHDRAWN
				decision.save()
			}
		}
		
		// Now, we have a whole bunch of other properties that are likely to be attached
		// to mutations in the report, rather than to the whole object. 
		
		// We may need to merge multiple observed mutations which refer to the same
		// known mutation. This is important, as this how we can determine which
		// technologies are implicated in each mutation. This is something of an 
		// important challenge. 

		List reportableMutations = [] as List
		Integer i = 0
		for(String mutation in params.list('_mutationIds')) {
			ReportableMutation mut = new ReportableMutation()
			mut.reportable = params.list('reportable').find { Integer.valueOf(it) == i } != null
			mut.actionable = params.list('actionable').find { Integer.valueOf(it) == i } != null
			mut.comment = params.list('comment').getAt(i)
			mut.justification = params.list('justification').getAt(i)
			mut.levelOfEvidence = params.list('levelOfEvidence').getAt(i)
			mut.levelOfEvidenceGene = params.list('levelOfEvidenceGene').getAt(i)
			mutation.split(',').collect {
				mut.addToObservedMutations(ObservedMutation.get(Integer.decode(it)))
			}
			decisionInstance.addToReportableMutations(mut)
			i++
		}
		
		decisionInstance.subject = subject
		//decisionInstance.date = new Date()
		subject.addToDecisions(decisionInstance)
		subject.save()
		
		log.info("Creating new decision: " + subject.patientId)
        if (decisionInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'decision.label', default: 'Decision'), decisionInstance.id])}"
            redirect(controller: "summary", action: "show", id: subject.id, fragment: 'decisions')
        }
        else {
			log.info("Failed to create new decision: returning to dialog")
            render(view: "create", model: [decisionInstance: decisionInstance])
        }
    }
}
