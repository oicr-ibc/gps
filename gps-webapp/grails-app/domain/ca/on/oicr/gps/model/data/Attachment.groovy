package ca.on.oicr.gps.model.data

import java.util.UUID

class Attachment {
	
	byte[] attachment
	String uniqueIdentifier = UUID.randomUUID().toString()
	String attachmentFileName
	String attachmentContentType

    static constraints = {
		attachment(nullable: false, maxSize:Integer.MAX_VALUE)
		uniqueIdentifier(nullable: false, maxSize: 40, blank: false, unique: true)
		attachmentFileName(nullable: false)
		attachmentContentType(nullable: false)
    }
}
