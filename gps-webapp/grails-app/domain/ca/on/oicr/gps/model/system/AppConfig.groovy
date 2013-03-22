package ca.on.oicr.gps.model.system

class AppConfig {
	
	String configKey
	String configValue

    static constraints = {
		configKey(nullable: false, blank:false, maxSize: 32)
		configValue(nullable: false, blank:false, maxSize: 4096)
    }
}
