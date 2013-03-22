package ca.on.oicr.gps.model.reporting

import ca.on.oicr.gps.model.data.Subject;
import grails.test.*

class ReportTests extends GrailsUnitTestCase {
	
	def report
	
    public void setUp() {
        super.setUp()
		
		Subject subject = new Subject()
		mockDomain(Subject, [subject])

		report = new Report()
		mockDomain(Report, [report])

		report.document = new ReportDocument()
		report.generated = new Date()
		report.subject = subject

		String reportXML = '''<?xml version='1.0'?>
<report version='1.0' subjectId='21' patientId='GEN-003' gender='F' date='Thu Jun 23 11:53:28 EDT 2011'>
  <sample id='23' barcode='PMH003BIOXFOR2' name='' type='FFPE' dnaConcentration='' dnaQuality='' dateReceived='2011-06-23 11:53:27.135' dateCreated='2011-06-23 11:53:27.143' />
  <sample id='21' barcode='PMH003BIOXFRZ1' name='' type='Frozen' dnaConcentration='' dnaQuality='' dateReceived='2011-06-23 11:53:27.12' dateCreated='2011-06-23 11:53:27.126' />
  <sample id='22' barcode='PMH003BIOXFOR1' name='' type='FFPE' dnaConcentration='' dnaQuality='' dateReceived='2011-06-23 11:53:27.127' dateCreated='2011-06-23 11:53:27.135' />
  <process id='1' runId='017536' panelName='OncoCarta PacBio' panelVersion='1.0.0' date='Fri Apr 29 00:00:00 EDT 2011' />
  <process id='2' runId='G0691483' panelName='OncoCarta' panelVersion='1.0.0' date='Wed Apr 20 00:00:00 EDT 2011' />
  <process id='3' runId='G0698756' panelName='OncoCarta' panelVersion='1.0.0' date='Tue Apr 26 00:00:00 EDT 2011' />
  <sampleType type='FFPE'>
    <sample barcode='PMH003BIOXFOR1'>
      <mutation chromosome='3' gene='PIK3CA' mutation='E542K' frequency='0.53' panelId='1' panelName='OncoCarta' panelVersion='1.0.0' panelTechnology='Sequenom' />
    </sample>
  </sampleType>
  <sampleType type='Frozen'>
    <sample barcode='PMH003BIOXFRZ1'>
      <mutation chromosome='3' gene='PIK3CA' mutation='E542K' frequency='0.536' panelId='1' panelName='OncoCarta' panelVersion='1.0.0' panelTechnology='Sequenom' />
    </sample>
  </sampleType>
</report>
'''

		report.document.type = "text/xml"
		report.document.body = reportXML.getBytes()
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks that leaving the owning components null fails validation
	 */
    void testOwners() {
		def old = report.subject
		
		report.subject = null
		assertFalse report.validate()
		assertTrue report.hasErrors()
		
		report.subject = old
    }

	/**
	 * Checks that the untouched report object passes validation
	 */
    void testValidation() {
		def result = report.validate()
		report.errors.allErrors.each { log.info it.toString() }
		assertTrue result
		assertFalse report.hasErrors()
    }
	
	/**
	 * Checks that the document body data can be read successfully.
	 */
	void testReportData() {
		def result = report.data
		
		def allSamples = result.sample
		assertEquals(3, allSamples.size())
		
		def allProcesses = result.process
		assertEquals(3, allProcesses.size())
	}
}
