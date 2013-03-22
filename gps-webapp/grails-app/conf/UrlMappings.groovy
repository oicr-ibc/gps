class UrlMappings {

	static mappings = {
		
		// The /api prefix allows basic authentication over SSL, so we can do some 
		// simple web service authentication. This is important to allow maintenance without
		// loss of security. However, we limit which controllers which work this way. Also,
		// we tighten the controllers to require appropriate roles, which is generally
		// ADMIN for write, and USER for read. 
		
		"/api/panel/$id?"(controller:"panel", parseRequest:true) {
			action = [GET:"list", PUT:"update", DELETE:"delete", POST:"save"]
		}
		"/api/mutation/$id?"(controller:"mutation", parseRequest:true) {
			action = [GET:"show", PUT:"update", DELETE:"delete", POST:"save"]
		}
		
		"/panel/$id?"(controller:"panel") {
			action = [GET:"show", PUT:"update", DELETE:"delete", POST:"save"]
			constraints {
				id(matches:/\d+/)
			}
		}

		// Now for the remainder of the web system - some of this may break when you
		// try to use a REST api, but tough - you can't.
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(controller:"summary", action:"home")
		
		"500"(view:'/error')
	}
}
