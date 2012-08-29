package ca.on.oicr.gps.controller

import ca.on.oicr.gps.model.data.Subject;
import grails.converters.*

import grails.plugins.springsecurity.Secured

class StatusController {
	
    @Secured(['ROLE_GPS-USERS'])
    def index = {
        redirect(action: "list", params: params)
    }

    @Secured(['ROLE_GPS-USERS'])
    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.sort = params.sort ?: 'patientId'
		params.order = params.order ?: 'desc'
		
		def subjects
		def subjectCount
		
		// When building this list, we ought to take account of the criteria that will allow projections
		// to the fields that we need for displaying status. Basically, however, all this ought to belong
		// in a service, or (better) in the domain class. 
		
		subjects = Subject.getAllReportable(params)
		subjectCount = subjects.size()
		
        [subjectReportableInstanceList: subjects, subjectReportableInstanceTotal: subjectCount]
    }
	
	/**
	 * Generates the MediData report. This is primarily domain stuff, but with a bit of web stuff too. The
	 * report can be generated in different forms, and is then rendered, typically as a zip file
	 * containing the data for upload in the defined format. 
	 */
	def generateMediData = {
		def subjectInstance = Subject.get(params.id)
		
		withFormat {
			html subjectInstance: subjectInstance
			xml { render subjectInstance as XML }
		}
	}
}
