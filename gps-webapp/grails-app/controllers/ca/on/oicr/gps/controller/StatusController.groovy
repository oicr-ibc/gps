package ca.on.oicr.gps.controller

import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.reporting.Query;
import ca.on.oicr.gps.model.reporting.QueryResult;
import grails.converters.*

import grails.plugins.springsecurity.Secured

@Secured(['ROLE_GPS-USERS'])
class StatusController {
	
    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.sort = params.sort ?: 'patientId'
		params.order = params.order ?: 'desc'
		
		def subjects
		def subjectCount
		
		// When building this list, we ought to take account of the criteria that will allow projections
		// to the fields that we need for displaying status. Basically, however, all this ought to belong
		// in a service, or (better) in the domain class. 
		
		subjects = Subject.getAllReportable(params)
		subjectCount = subjects.size()
		
        [subjectReportableInstanceList: subjects, subjectReportableInstanceTotal: subjectCount]
    }
	
	/**
	 * Generates the MediData report. This is primarily domain stuff, but with a bit of web stuff too. The
	 * report can be generated in different forms, and is then rendered, typically as a zip file
	 * containing the data for upload in the defined format. 
	 */
	def generateMediData = {
		def subjectInstance = Subject.get(params.id)
		
		withFormat {
			html subjectInstance: subjectInstance
			xml { render subjectInstance as XML }
		}
	}
	
	/**
	 * Action to generate a set of queries for dashboard-type reporting purposes. This was
	 * to meet the requirements originally set by Lincoln as support of the Genome Canada
	 * proposal, in November 2012.
	 */
	def status = {
		Query patientCountQuery = new Query(name: "Patient counts", body: """
SELECT CONCAT(MONTHNAME(s.consent_date), ' ', YEAR(s.consent_date)) AS date, 
       COUNT(s.id) AS patients_registered,
       (SELECT COUNT(DISTINCT CONCAT(sub.patient_id, ' ', IFNULL(om.mutation, km.mutation))) 
        FROM summary xs
        JOIN subject sub ON sub.summary_id = xs.id
        JOIN sample s ON s.subject_id = sub.id
        JOIN run_sample rs ON rs.sample_id = s.id
        JOIN observed_mutation om ON om.run_sample_id = rs.id
        LEFT JOIN known_mutation km ON om.known_mutation_id = km.id
        WHERE YEAR(xs.expert_panel_decision_date) = YEAR(s.consent_date)
        AND MONTH(xs.expert_panel_decision_date) = MONTH(s.consent_date)) AS observed_mutations,
       (SELECT COUNT(DISTINCT CONCAT(sub.patient_id, ' ', IFNULL(om.mutation, km.mutation))) 
        FROM summary xs
        JOIN subject sub ON sub.summary_id = xs.id
        JOIN sample s ON s.subject_id = sub.id
        JOIN run_sample rs ON rs.sample_id = s.id
        JOIN observed_mutation om ON om.run_sample_id = rs.id
        JOIN reportable_mutation_observed_mutation rmom ON rmom.observed_mutation_id = om.id
        JOIN reportable_mutation rm ON rmom.reportable_mutation_observed_mutations_id = rm.id
        LEFT JOIN known_mutation km ON om.known_mutation_id = km.id
        WHERE rm.actionable
        AND YEAR(xs.expert_panel_decision_date) = YEAR(s.consent_date)
        AND MONTH(xs.expert_panel_decision_date) = MONTH(s.consent_date)) AS actionable_mutations
FROM summary s
WHERE s.consent_date IS NOT NULL 
GROUP BY YEAR(s.consent_date), MONTH(s.consent_date); 
		""")
		
		QueryResult patientCountResultSet = patientCountQuery.executeQuery()
		[patientCountResultSet: patientCountResultSet]
	}
}
