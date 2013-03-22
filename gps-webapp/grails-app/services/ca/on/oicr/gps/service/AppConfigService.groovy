package ca.on.oicr.gps.service

import ca.on.oicr.gps.model.system.AppConfig;

class AppConfigService {
	
    static transactional = false 
    
	/**
	 * Time delay (in minutes) for email reminders
	 * @return an Integer for the time delay in minutes
	 */
	def delay() {
		(AppConfig?.findByConfigKey('reminderDelayInMinutes')?.configValue ?: 120) as Integer 
    }
	
	/**
	 * A comma-separated list of email addresses for new sample notifications
	 * @return
	 */
	def createSampleEmail() {
		AppConfig?.findByConfigKey('createSampleEmail')?.configValue?.split(',')*.trim()
    }
	
	/**
	 * A comma-separated list of email addresses for reminder notifications
	 * @return
	 */
	def reminderSampleEmail() {
		AppConfig?.findByConfigKey('reminderSampleEmail')?.configValue?.split(',')*.trim()
	}
	
	/**
	 * A comma-separated list of email addresses for received sample notifications
	 * @return
	 */
	def receivedSampleEmail() {
		AppConfig?.findByConfigKey('receivedSampleEmail')?.configValue?.split(',')*.trim()
	}
	
	/**
	 * A comma-separated list of email addresses for information notifications
	 * @return
	 */
	def informationEmail() {
		AppConfig?.findByConfigKey('informationEmail')?.configValue?.split(',')*.trim()
    }
	
	/**
	 * A comma-separated list of email addresses for scheduled update notifications
	 * @return
	 */
	def updateEmail() {
		AppConfig?.findByConfigKey('updateEmail')?.configValue?.split(',')*.trim()
    }
	
	/**
	 * Used as a subject label added into email notifications
	 * @return
	 */
	def emailSubjectPrefix() {
		AppConfig?.findByConfigKey('emailSubjectPrefix')?.configValue
	}

	/**
	 * A comma-separated list of institutions allowed
	 * @return
	 */
	def institutions() {
		AppConfig?.findByConfigKey('institutions')?.configValue
	}

	/**
	 * The URL base for the knowledge base
	 * @return
	 */
	def knowledgeBaseURL() {
		AppConfig?.findByConfigKey('knowledgeBaseURL')?.configValue
	}
}
