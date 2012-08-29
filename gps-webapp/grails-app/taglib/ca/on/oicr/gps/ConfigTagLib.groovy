package ca.on.oicr.gps

import ca.on.oicr.gps.model.system.AppConfig;

class ConfigTagLib {
	
	static returnObjectForTags = ['config']
	
	def config = {attrs, body ->
		
		return AppConfig?.findByConfigKey(attrs.name)?.configValue
	}
		
}
