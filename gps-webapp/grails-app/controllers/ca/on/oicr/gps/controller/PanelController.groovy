package ca.on.oicr.gps.controller

import java.io.InputStream;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import ca.on.oicr.gps.model.laboratory.Target;
import ca.on.oicr.gps.model.laboratory.Panel;

import grails.plugins.springsecurity.Secured

import grails.converters.*

/**
 * The panel controller is really there for REST-like access to maintaining the panels stored in the
 * rest of the system. There doesn't need to be much of a GUI here, but there will be a JSON rendering
 * for the panels and their assays. 
 * 
 * Due to fundamental limitations in the way Grails handles file uploads, this is something of a manual
 * process when it comes to writing a new panel. 
 * 
 * @author Stuart Watt
 */

@Secured(['ROLE_GPS-ADMINS'])
class PanelController {
	
	def loadingService

	// To list all panels, use:
	// curl --user "swatt:xxxxxxx" -i http://localhost:8080/gps/panel

    @Secured(['ROLE_GPS-USERS'])
	def index() { 
				
		withFormat {
			html {
				redirect(action: "list", params: params)
			}
			js { 
				indexJson()
			}
		}
    }
	
    @Secured(['ROLE_GPS-USERS'])
	def show() { 
        def panelInstance = Panel.get(params.id)
        if (!panelInstance) {
			if (params.id != null) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'panel.label', default: 'Panel'), params.id])}"
			}
            redirect(action: "list")
        }
        else {
            [panelInstance: panelInstance]
        }
    }
	
	@Secured(['ROLE_GPS-USERS'])
	def list() {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.sort = params.sort ?: 'name'
		params.order = params.order ?: 'desc'
		params.offset = params.offset ?: '0'
		
		def criteria = Panel.createCriteria()
		
		def panelList = criteria.list {
			order(params.sort, params.order)
			maxResults(params.max)
			firstResult(Integer.parseInt(params.offset))
		}
		
		def panelCount = Panel.count()

        [panelInstanceList: panelList, panelInstanceTotal: panelCount]
	}
	
	/**
	 * Action to set up the form to enter a new panel. When this is run, the panel 
	 * data file is passed to the save action
	 * @return a model with a single key, a new panel instance
	 */
    def create() {
        def panelInstance = new Panel()
        panelInstance.properties = params
        return [panelInstance: panelInstance]
    }

	// To create a new panel, use:
	// curl --user "swatt:xxxxxxx" -i --request POST -d name="MyPanel" -d versionString="1.0.0" -d technology="ABI" http://localhost:8080/gps/panel/create
	def save(PanelCommand sc) {
		
		def panel = new Panel()
		panel.name = sc.name
		panel.technology = sc.technology
		panel.versionString = sc.versionString
		panel.validate()
		
		if (!panel.hasErrors() && panel.save(flush: true)) {
			try {
				if (sc.dataFile.getSize() != 0) {
					def inputStream = sc.dataFile.getInputStream()
					def contentType = sc.dataFile.getContentType()
					def fileName = sc.dataFile.getOriginalFilename() 
					Reader inputReader = new InputStreamReader(inputStream)
					
					if (fileName.toLowerCase().endsWith(".bed") && panel.technology == "HotSpot" ) {
						loadingService.loadHotSpotPanelTargets(contentType, fileName, panel, inputReader)
					} else {
						loadingService.loadPanelTargets(contentType, fileName, panel, inputReader)
					}
				}
			} catch (Exception e) {
				panel.errors.reject('dataFile', e.getLocalizedMessage())
			}
		}
			
		// If we have a data file set, we can also proceed to load up the assays. This will be an 
		// inline multipart form style. This may very well fail, and if it does, we ought to
		// throw out to the create page (if there is one, and that is where we came from) with
		// any appropriate error codes. 
		
		
		withFormat {
			html {
				if (panel.hasErrors()) {
					return render(view: "create", model: [panelInstance: panel])
				} else {
					return render(view: "show", model: [panelInstance: panel])
				}
			}
			js {
				if (panel.hasErrors()) {
					response.setStatus(response.SC_FORBIDDEN)
					return render(contentType: "text/json", model: [ id: panel.id, name: panel.name, versionString: panel.versionString, technology: panel.technology, error: panel.errors.getAllErrors().join(', ') ])
				} else {
					return render(contentType: "text/json", model: [ id: panel.id, name: panel.name, versionString: panel.versionString, technology: panel.technology ])
				}
			}
		}
	}
	
	/**
	 * Action to return the contents of a panel in JSON. 
	 */
	
	// To list a current panel, use:
	// curl --user "swatt:xxxxxxx" -i http://localhost:8080/gps/panel/$id
	@Secured(['ROLE_GPS-USERS'])
	private def indexJson() {
		
		if (! params.id) {
			def panels = Panel.list()
			
			render(contentType: "text/json") {
				panels.collect { [ id: it.id, name: it.name, versionString: it.versionString, technology: it.technology ] };
			}
			
			return
		} 
				
		def panel = Panel.get(params.id)
		
		render(contentType: "text/json") {
			panel.targets.collect { [ id: it.id, 
				                     name: it.name,
									 chromosome: it.chromosome,
				                     gene: it.gene,
									 start: it.start,
									 stop: it.stop ] };
		}
	}
	
	/**
	 * Action to load a panel with a set of assays. This action expects the request body 
	 * to be a CSV file. 
	 */
	// To load a panel use:
	// curl --user "swatt:xxxxxxx" -i --upload-file "myfile.csv" http://localhost:8080/gps/panel/$id
	def update() { 
		
		def panel = Panel.get(params.id)
		
		// First of all, remove all the current assays
		for(Target target : panel.targets) {
			target.delete()
		}
		panel.targets.clear()
		
		InputStream	inputStream = request.getInputStream()
		Reader inputReader = new InputStreamReader(inputStream)
		def contentType = request.getContentType()
		
		loadingService.loadPanelAssays(contentType, (String) null, panel, inputReader)
		
		this.list()
	}
	
	/**
	 * Action to delete a panel.
	 */
	// To delete panel, use:
	// curl -i --request DELETE http://localhost:8080/gps/panel/5
	def delete() {
		
		def panel = Panel.get(params.id)
		panel.delete(flush: true)
		
		render(contentType: "text/json") {
			[]
		}
	}
}

final class PanelCommand {
	String name
	String technology
	String versionString
	MultipartFile dataFile
	
	static constraints = {
        name(blank: false)
        technology(blank: false)
        versionString(blank: false)
    }}