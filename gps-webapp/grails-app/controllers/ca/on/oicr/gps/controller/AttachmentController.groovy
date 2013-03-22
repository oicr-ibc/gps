package ca.on.oicr.gps.controller

import ca.on.oicr.gps.model.data.Attachment;
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_GPS-USERS'])
class AttachmentController {

	@Secured(['ROLE_GPS-USERS'])
	def download = {
		def attachmentInstance = Attachment.findByUniqueIdentifier(params.id)
		
		if (attachmentInstance) {
			response.setContentType(attachmentInstance.attachmentContentType)
			response.setHeader("Content-disposition", "attachment;filename=${attachmentInstance.attachmentFileName}")

			response.outputStream << attachmentInstance.attachment // Performing a binary stream copy
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'attachment.label', default: 'Attachment'), params.id])}"
			redirect(action: "list")
		}

	}
}
