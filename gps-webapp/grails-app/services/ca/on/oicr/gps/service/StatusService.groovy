package ca.on.oicr.gps.service

import ca.on.oicr.gps.model.data.Subject;

class StatusService {

    static transactional = true

    def getSubjectsStatus(params) {
		
		List<Object> subjectData = Subject.getAllReportable(params)
		return subjectData
    }
}
