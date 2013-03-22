package ca.on.oicr.gps.controller

import ca.on.oicr.gps.model.data.ReportableMutation;
import ca.on.oicr.gps.model.data.Summary;

/**
 * DataController is simply a place where we can get data values for driving front-end
 * configuration. A very typical example is to get a list of legal values for a 
 * <select> tag out of the server, rather than coding that in the front-end independently.
 * 
 * @author swatt
 */

class DataController {
	
	def appConfigService
	
	// And now we render (as HTML, really)
	
	// Beware: hack follows.
	// See: http://grails.1312388.n4.nabble.com/Trouble-with-render-contentType-text-xml-tp1358804p1358807.html
	//
	// To explain, something is shadowing out select, so we can never get a tag unless
	// we do something like this. 
		
	private void renderValues(values) {
		def xmlClosure = {
			select(){
				for(opt in values) {
					option(value: opt, opt)
				}
			}
		}
		
		// change order of resolving
		xmlClosure.resolveStrategy = Closure.DELEGATE_FIRST
		
		render(contentType: "text/xml", xmlClosure)
	}

    def institutions = { 
		def values = appConfigService.institutions().tokenize(',;').collect{ it.trim() }
		renderValues(values)
	}

    def sexes = { 
		renderValues(['F', 'M'])
	}
	
	// This simply looks up the data from the domain constraint. We ought to be doing
	// this elsewhere to avoid explicitly defining all the options. 
	def psychosocial = {
		renderValues(Summary.constraints.psychosocial.inList)
	}

	def levelsOfEvidence = {
		renderValues(ReportableMutation.constraints.levelOfEvidence.inList)
	}

	def levelsOfEvidenceGene = {
		renderValues(ReportableMutation.constraints.levelOfEvidenceGene.inList)
	}
}
