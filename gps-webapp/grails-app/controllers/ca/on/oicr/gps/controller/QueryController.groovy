package ca.on.oicr.gps.controller

import grails.plugins.springsecurity.Secured

import ca.on.oicr.gps.model.reporting.Query;

@Secured(['ROLE_GPS-CONTRIBUTORS'])
class QueryController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }
	
	def run = {
		def queryInstance = Query.get(params.id)
		if (!queryInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), params.id])}"
			redirect(action: "list")
		}
		else {
			def resultSet = queryInstance.executeQuery()
			[queryInstance: queryInstance, resultSet: resultSet]
		}
	}
	
	def export = {
		def queryInstance = Query.get(params.id)
		if (!queryInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), params.id])}"
			redirect(action: "list")
		}
		else {
			def resultSet = queryInstance.executeQuery()
			response.setHeader("Content-Disposition", "attachment; filename=file.xls")
			response.setHeader("Pragma", "no-cache")
			response.setContentType("application/vnd.ms-excel")
			[queryInstance: queryInstance, resultSet: resultSet]
		}
	}

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [queryInstanceList: Query.list(params), queryInstanceTotal: Query.count()]
    }

    def create = {
        def queryInstance = new Query()
        queryInstance.properties = params
        return [queryInstance: queryInstance]
    }

    def save = {
        def queryInstance = new Query(params)
        if (queryInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'query.label', default: 'Query'), queryInstance.id])}"
            redirect(action: "show", id: queryInstance.id)
        }
        else {
            render(view: "create", model: [queryInstance: queryInstance])
        }
    }

    def show = {
        def queryInstance = Query.get(params.id)
        if (!queryInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), params.id])}"
            redirect(action: "list")
        }
        else {
            [queryInstance: queryInstance]
        }
    }

    def edit = {
        def queryInstance = Query.get(params.id)
        if (!queryInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [queryInstance: queryInstance]
        }
    }

    def update = {
        def queryInstance = Query.get(params.id)
        if (queryInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (queryInstance.version > version) {
                    
                    queryInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'query.label', default: 'Query')] as Object[], "Another user has updated this Query while you were editing")
                    render(view: "edit", model: [queryInstance: queryInstance])
                    return
                }
            }
            queryInstance.properties = params
            if (!queryInstance.hasErrors() && queryInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'query.label', default: 'Query'), queryInstance.id])}"
                redirect(action: "show", id: queryInstance.id)
            }
            else {
                render(view: "edit", model: [queryInstance: queryInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def queryInstance = Query.get(params.id)
        if (queryInstance) {
            try {
                queryInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'query.label', default: 'Query'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'query.label', default: 'Query'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), params.id])}"
            redirect(action: "list")
        }
    }
}
