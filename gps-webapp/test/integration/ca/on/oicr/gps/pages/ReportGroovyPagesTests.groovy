package ca.on.oicr.gps.pages

import grails.test.*
import org.htmlparser.Parser

import ca.on.oicr.gps.controller.ReportController;
import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Summary;
import ca.on.oicr.gps.model.reporting.Report;
import ca.on.oicr.gps.model.reporting.ReportDocument;

/**
 * @author swatt
 * 
 * This test class does some round-trip testing between the data and the results
 * of the GSP rendering. The actual tests are fairly limited, as most of the actual 
 * test - apart from the data - can be mangled by internationalization. 
 */
class ReportGroovyPagesTests extends GroovyPagesTestCase {
	
	def submissionId
	def subjectId
	
    public void setUp() {
        super.setUp()

		def newSubject = new Subject()
		newSubject.patientId = 'GEN-003'
		newSubject.gender = 'F'
		newSubject.summary = new Summary()
		newSubject.summary.subject = newSubject
		
		String reportXML = '''<?xml version='1.0'?>
		<report version='1.0' subjectId='21' patientId='GEN-003' gender='F' date='Thu Jun 23 16:50:51 EDT 2011'>
		  <sampleType type='FFPE'>
			<sample id='22' barcode='PMH003BIOXFOR1' name='' type='FFPE' dnaConcentration='' dnaQuality='' dateReceived='2011-06-23 16:49:58.632' dateCreated='2011-06-23 16:49:58.653'>
			  <process id='3' runId='G0698756' panelName='OncoCarta' panelVersion='1.0.0' date='Tue Apr 26 00:00:00 EDT 2011' />
			  <mutation chromosome='3' gene='PIK3CA' mutation='E542K' frequency='0.53' panelId='1' panelName='OncoCarta' panelVersion='1.0.0' panelTechnology='Sequenom' />
			</sample>
			<sample id='23' barcode='PMH003BIOXFOR2' name='' type='FFPE' dnaConcentration='' dnaQuality='' dateReceived='2011-06-23 16:49:58.654' dateCreated='2011-06-23 16:49:58.665'>
			  <process id='1' runId='017536' panelName='OncoCarta PacBio' panelVersion='1.0.0' date='Fri Apr 29 00:00:00 EDT 2011' />
			</sample>
		  </sampleType>
		  <sampleType type='Frozen'>
			<sample id='21' barcode='PMH003BIOXFRZ1' name='' type='Frozen' dnaConcentration='' dnaQuality='' dateReceived='2011-06-23 16:49:58.624' dateCreated='2011-06-23 16:49:58.631'>
			  <process id='2' runId='G0691483' panelName='OncoCarta' panelVersion='1.0.0' date='Wed Apr 20 00:00:00 EDT 2011' />
			  <mutation chromosome='3' gene='PIK3CA' mutation='E542K' frequency='0.536' panelId='1' panelName='OncoCarta' panelVersion='1.0.0' panelTechnology='Sequenom' />
			</sample>
		  </sampleType>
		</report>
'''
		
		Report report = new Report()
		report.subject = newSubject
		report.generated = new Date()
		report.document = new ReportDocument()
		report.document.type = "text/xml"
		report.document.body = reportXML.getBytes()
		
		newSubject.reports = [ report ] as SortedSet<Report>
		
		newSubject.save(failOnError:true)
		
		subjectId = newSubject.id
	}

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Test the submission controller's ability to set up to create a new
	 * submission.
	 */
    void testSubmissionCreate() {
		def file = new File(System.properties['base.dir'], "grails-app/views/report/subject.gsp")
		
		def reportController = new ReportController()
		reportController.params.id = subjectId
		def model = reportController.subject()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
    }
}
