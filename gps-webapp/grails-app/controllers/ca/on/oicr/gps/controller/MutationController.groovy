package ca.on.oicr.gps.controller

import ca.on.oicr.gps.model.knowledge.KnownMutation;

import grails.plugins.springsecurity.Secured

@Secured(['ROLE_GPS-ADMINS'])
class MutationController {
	
	def loadingService

    @Secured(['ROLE_GPS-USERS'])
	def show = { 
			
		def panels = KnownMutation.list()
		
		render(contentType: "text/json") {
			panels.collect { [ 
				id: it.id, 
				publicId: it.publicId, 
				guid: it.guid,
				gene: it.gene,
				chromosome: it.chromosome,
				start: it.start,
				stop: it.stop,
				mutation: it.mutation,
				ncbiReference: it.ncbiReference,
				refAllele: it.refAllele,
				varAllele: it.varAllele
			] };
		}
	}
	
	def update = {
		if (request.method == 'PUT') {
			
			InputStream	inputStream = request.getInputStream()
			Reader inputReader = new InputStreamReader(inputStream)
			
			loadingService.loadKnownMutations(inputReader)
		}
	}
}
