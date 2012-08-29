package ca.on.oicr.gps.controller

import ca.on.oicr.gps.model.data.Sample;
import ca.on.oicr.gps.model.data.Subject;
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_GPS-USERS'])
class SampleController {

    static allowedMethods = []

	def appConfigService
	def notificationService
	
    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.sort = params.sort ?: 'dateCreated'
		params.order = params.order ?: 'desc'
		params.offset = params.offset ?: '0'
		
		def samples
		def sampleCount
		
		def criteria = Sample.createCriteria()
		
		def sampleList = criteria.list {
			order(params.sort, params.order)
			if (params.barcode) {
				like("barcode", "%" + params.barcode + "%")
			}
			maxResults(params.max)
			firstResult(Integer.parseInt(params.offset))
		}

		if (! params.barcode) {
			sampleCount = Sample.count()
		} else {
			sampleCount = Sample.countByBarcodeLike("%" + params.barcode + "%")
		}

        [sampleInstanceList: sampleList, sampleInstanceTotal: sampleCount]
    }

	@Secured(['ROLE_GPS-CONTRIBUTORS'])
    def create = {
        def sampleInstance = new Sample()
        sampleInstance.properties = params
        return [sampleInstance: sampleInstance]
    }

	@Secured(['ROLE_GPS-CONTRIBUTORS'])
    def save = {
		// First locate the subject
		log.info("Locating patientId: " + params.patientId)
		def subjectInstance = Subject.findByPatientId(params.patientId)
		assert subjectInstance
		
		params.remove('patientId')
		
        def sampleInstance = new Sample(params)
		sampleInstance.dateCreated = new Date()
		subjectInstance.addToSamples(sampleInstance)
		
		log.info("Creating new sample: barcode: " + sampleInstance.barcode)
        if (sampleInstance.save(flush: true)) {
			
			notificationService.sendNotification(
				notification: notificationService.NOTIFY_CREATED, 
				view: 'create',
				subject: "Sample Registered: " + sampleInstance.barcode,
				model: [sampleInstance:sampleInstance]
			)
			
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'sample.label', default: 'Sample'), sampleInstance.id])}"
            redirect(action: "show", id: sampleInstance.id)
        }
        else {
			log.info("Failed to create new sample: returning to dialog")
            render(view: "create", model: [sampleInstance: sampleInstance])
        }
    }

    def show = {
        def sampleInstance = Sample.get(params.id)
        if (!sampleInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
            redirect(action: "list")
        }
        else {
            [sampleInstance: sampleInstance]
        }
    }

	@Secured(['ROLE_GPS-CONTRIBUTORS'])
    def edit = {
        def sampleInstance = Sample.get(params.id)
        if (!sampleInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [sampleInstance: sampleInstance]
        }
    }
	
	@Secured(['ROLE_GPS-OICR'])
    def receive = {
        def sampleInstance = Sample.get(params.id)
        if (!sampleInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
            redirect(action: "list")
        }
        else {
			
            return [sampleInstance: sampleInstance]
        }
    }

	@Secured(['ROLE_GPS-OICR'])
	def received = {
		
		def sampleInstance = Sample.get(params.id)
		
		if (sampleInstance) {
			log.info("Marking sample as received: " + sampleInstance.barcode)
			if (params.version) {
				def version = params.version.toLong()
				if (sampleInstance.version > version) {		
					sampleInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'sample.label', default: 'Sample')] as Object[], "Another user has updated this Sample while you were editing")
					render(view: "edit", model: [sampleInstance: sampleInstance])
					return
				}
			}
			sampleInstance.properties = params
			if (!sampleInstance.hasErrors() && sampleInstance.save(flush: true)) {
				notificationService.sendNotification(
					notification: notificationService.NOTIFY_RECEIVED, 
					view: 'received',
					subject: "Sample Received: " + sampleInstance.barcode,
					model: [sampleInstance:sampleInstance]
				)
				
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'sample.label', default: 'Sample'), sampleInstance.id])}"
				redirect(action: "show", id: sampleInstance.id)
			}
			else {
				render(view: "show", model: [sampleInstance: sampleInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
			redirect(action: "list")
		}
	}
	
	@Secured(['ROLE_GPS-CONTRIBUTORS'])
    def update = {
        def sampleInstance = Sample.get(params.id)
		
		if (sampleInstance) {
			log.info("Updating sample: barcode: " + sampleInstance.barcode)
            if (params.version) {
                def version = params.version.toLong()
                if (sampleInstance.version > version) {
                    
                    sampleInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'sample.label', default: 'Sample')] as Object[], "Another user has updated this Sample while you were editing")
                    render(view: "edit", model: [sampleInstance: sampleInstance])
                    return
                }
            }
			
			sampleInstance.properties = params

            if (!sampleInstance.hasErrors() && sampleInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'sample.label', default: 'Sample'), sampleInstance.id])}"
                redirect(action: "show", id: sampleInstance.id)
            }
            else {
                render(view: "edit", model: [sampleInstance: sampleInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
            redirect(action: "list")
        }
    }

	/**
	 * Deletes a sample, although at present there is no UI access to this. 
	 */
	
	@Secured(['ROLE_GPS-CONTRIBUTORS'])
    def delete = {
        def sampleInstance = Sample.get(params.id)
        if (sampleInstance) {
			log.info("Deleting sample: " + sampleInstance.barcode)
            try {
                sampleInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
            redirect(action: "list")
        }
    }
}
