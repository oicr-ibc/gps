package ca.on.oicr.gps.controller

import grails.plugins.springsecurity.Secured
import ca.on.oicr.gps.model.data.ObservedMutation
import ca.on.oicr.gps.model.knowledge.KnownMutation;

@Secured(['ROLE_GPS-USERS'])
class ObservedMutationController {

    def index = {
        redirect(action: "list", params: params)
    }

    @Secured(['ROLE_GPS-USERS'])
    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.sort = params.sort ?: 'mutation'
		params.order = params.order ?: 'asc'
		params.offset = params.offset ?: '0'

		def results = ObservedMutation.getMutationSummary(params)
		def resultsTotal = results.size()
		
        [observedMutationInstanceList: results, observedMutationInstanceTotal: resultsTotal]
    }
	
    @Secured(['ROLE_GPS-USERS'])
    def reportable = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.offset = params.offset ?: '0'

		def results = ObservedMutation.getObservedMutations(params)
		def resultsTotal = ObservedMutation.count()
		
        [observedMutationInstanceList: results, observedMutationInstanceTotal: resultsTotal]
    }
	
    @Secured(['ROLE_GPS-USERS'])
    def reported = {
        def observedMutationInstance = ObservedMutation.get(params.id)
		
        if (!observedMutationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observedMutation.label', default: 'Observed Mutation'), params.id])}"
            redirect(action: "reportable")
        }
        else {
            return [observedMutationInstance: observedMutationInstance]
        }
    }
	
    @Secured(['ROLE_GPS-USERS'])
	def updateReported = {
		def observedMutationInstance = ObservedMutation.get(params.id)
		
		log.info("Updating observed mutation: " + observedMutationInstance?.id)

		if (observedMutationInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (observedMutationInstance.version > version) {
					
					observedMutationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'observedMutation.label', default: 'Observed Mutation')] as Object[], "Another user has updated this Observed Mutation while you were editing")
					render(view: "reported", model: [observedMutationInstance: observedMutationInstance])
					return
				}
			}
			
			observedMutationInstance.properties = params
			if (! params.reported) {
				params.putAt("reported", null)
				observedMutationInstance.putAt("reported", null)
			}

			if (!observedMutationInstance.hasErrors() && observedMutationInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'observedMutation.label', default: 'Observed Mutation'), observedMutationInstance.id])}"
				redirect(action: "reportable")
			}
			else {
				render(view: "reported", model: [observedMutationInstance: observedMutationInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'obseredMutation.label', default: 'Observed Mutation'), params.id])}"
			redirect(action: "list")
		}

	}
	
    @Secured(['ROLE_GPS-USERS'])
    def show = {
		
		def mutation = KnownMutation.findMutationByLabel(params.mutation)
		assert mutation
		
		def model = [mutation: mutation]
		model.numberOfPatients = ObservedMutation.getPatientCount(mutation)
		model.numberOfSamples = ObservedMutation.getSampleCount(mutation)
		model.observations = ObservedMutation.getObservations(mutation)
		
		return model
		
    }

	private def getMutationModelFromLabel(String label) {
		def matcher = label =~ /(\w+)\s+(\w+)/
		if (matcher.matches()) {
			
			def genePart = matcher.group(1)
			def mutationPart = matcher.group(2)

			def criteria = ObservedMutation.createCriteria()
			def mutation = criteria.get {
				knownMutation {
					eq("mutation", mutationPart.trim())
					knownGene {
						eq("name", genePart.trim())
					}
				}
			}
			
			assert mutation

			[mutation: mutation]
		} else {
			throw new RuntimeException("Internal error: invalid mutation term")
		}
	}
	
	private def getMutationModel() {
		return getMutationModelFromLabel(params.mutation)
	}
	
}
