package ca.on.oicr.gps.service

import ca.on.oicr.gps.model.system.AppData;

class AppDataService {

    static transactional = false

    def getAttribute(String attribute) {
		def data = AppData?.findByDataKey(attribute)
		
		if (data) {
			return data.dataValue
		}
    }

    def setAttribute(String attribute, String value) {
		def data = AppData?.findByDataKey(attribute)
		
		if (data) {
			data.dataValue = value
		} else {
			data = new AppData(dataKey: attribute, dataValue: value)
		}
		
		data.save()
		return this
    }
}
