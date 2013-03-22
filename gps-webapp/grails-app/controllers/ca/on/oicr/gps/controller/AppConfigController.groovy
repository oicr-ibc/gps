package ca.on.oicr.gps.controller

import ca.on.oicr.gps.model.system.AppConfig
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_GPS-ADMINS'])
class AppConfigController {

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [appConfigInstanceList: AppConfig.list(params), appConfigInstanceTotal: AppConfig.count()]
    }

    def create = {
        def appConfigInstance = new AppConfig()
        appConfigInstance.properties = params
        return [appConfigInstance: appConfigInstance]
    }

    def save = {
        def appConfigInstance = new AppConfig(params)
        if (appConfigInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'appConfig.label', default: 'Setting'), appConfigInstance.id])}"
            redirect(action: "show", id: appConfigInstance.id)
        }
        else {
            render(view: "create", model: [appConfigInstance: appConfigInstance])
        }
    }

    def show = {
        def appConfigInstance = AppConfig.get(params.id)
        if (!appConfigInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'appConfig.label', default: 'Setting'), params.id])}"
            redirect(action: "list")
        }
        else {
            [appConfigInstance: appConfigInstance]
        }
    }

    def edit = {
        def appConfigInstance = AppConfig.get(params.id)
        if (!appConfigInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'appConfig.label', default: 'Setting'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [appConfigInstance: appConfigInstance]
        }
    }

    def update = {
        def appConfigInstance = AppConfig.get(params.id)
        if (appConfigInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (appConfigInstance.version > version) {
                    
                    appConfigInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'appConfig.label', default: 'Setting')] as Object[], "Another user has updated this AppConfig while you were editing")
                    render(view: "edit", model: [appConfigInstance: appConfigInstance])
                    return
                }
            }
            appConfigInstance.properties = params
            if (!appConfigInstance.hasErrors() && appConfigInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'appConfig.label', default: 'Setting'), appConfigInstance.id])}"
                redirect(action: "show", id: appConfigInstance.id)
            }
            else {
                render(view: "edit", model: [appConfigInstance: appConfigInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'appConfig.label', default: 'Setting'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def appConfigInstance = AppConfig.get(params.id)
        if (appConfigInstance) {
            try {
                appConfigInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'appConfig.label', default: 'Setting'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'appConfig.label', default: 'Setting'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'appConfig.label', default: 'Setting'), params.id])}"
            redirect(action: "list")
        }
    }
}
