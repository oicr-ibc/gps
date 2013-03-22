package ca.on.oicr.gps.controller

import ca.on.oicr.gps.model.data.Subject
import ca.on.oicr.gps.model.data.RunSample
import grails.plugins.springsecurity.Secured

/**
 * The SummaryController doesn't need to do all that much, but it is the main provider
 * for the summary display. All the interesting action is driven by the .gsp file. 
 * 
 * @author swatt
 *
 */

/*
 * Secured to at least guest access. All the other roles at least meet this too, so
 * this is intended to cover all authenticated users. In practice, some parts of this,
 * and especially editing, are not intended to be available to guests, but require
 * rather higher levels of authority. 
 */

@Secured(['ROLE_GPS-USERS'])
class SummaryController {

	def home = {}

    def index = { }
	
	def show = {
		def subjectId = params.id
		def subject = Subject.get(subjectId)
		
		if (! subject) {
			throw new RuntimeException("Can't find subject: " + subjectId)
		}
		
		[subjectInstance: subject]
	}
	
	def mutations = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.sort = params.sort ?: 'patientId'
		params.order = params.order ?: 'desc'
				
		def runSamples = RunSample.getSortedRunSamples(params)
		def runSamplesCount = runSamples.size()
		
        [runSampleList: runSamples, runSampleTotal: runSamplesCount]
	}
}
