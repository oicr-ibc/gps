package ca.on.oicr.gps.model.system

class AppData {

	String dataKey
	String dataValue

    static constraints = {
		dataKey nullable: false, blank:false
		dataValue nullable: false, blank:true
    }
}
