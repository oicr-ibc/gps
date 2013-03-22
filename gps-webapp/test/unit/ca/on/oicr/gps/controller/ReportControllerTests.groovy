package ca.on.oicr.gps.controller

import java.util.SortedSet;

import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Summary;
import ca.on.oicr.gps.model.reporting.Report;
import ca.on.oicr.gps.model.reporting.ReportDocument;
import grails.test.*
import groovy.util.XmlParser;

class ReportControllerTests extends ControllerUnitTestCase {
	
	def sub
	
    public void setUp() {
        super.setUp()
		
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

		sub = new Subject()
		mockDomain(Subject, [sub])
		
		sub.patientId = '0123456789'
		sub.gender = 'F'
		
		sub.summary = new Summary()
		
		mockDomain(Report)
		mockDomain(ReportDocument)

		Report report = new Report()
		report.generated = new Date()
		report.document = new ReportDocument()
		report.document.type = "text/xml"
		report.document.body = reportXML.getBytes()
		
		sub.reports = [ report ] as SortedSet<Report>
    }

    public void tearDown() {
        super.tearDown()
    }

	// Ok, let's try and get ControllerUnitTestCase to actually do something useful for a 
	// bit of a change. Here, we can mock up a subject and a report, and get that make
	// accessible for rendering. 
    
	void testReportFromXML() {
		def data = sub.reports.toList().getAt(0).data
		assertNotNull(data)
    }
}
