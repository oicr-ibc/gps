package ca.on.oicr.gps.service

import java.io.StringWriter;

import groovy.xml.MarkupBuilder;
import ca.on.oicr.gps.model.data.ObservedMutation;
import ca.on.oicr.gps.model.data.Process;
import ca.on.oicr.gps.model.data.RunSample;
import ca.on.oicr.gps.model.data.Sample;
import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.reporting.Report;
import ca.on.oicr.gps.model.reporting.ReportDocument;

/**
 * Returns an XML document which can be used to serialize a report for persistence
 * and auditing. 
 * @return
 */
// It would be possible to refactor this into a buildReportXML method across the
// various modules. This has been considered. However, although it provides better
// management of the upgrade path, it fails to provide a coherent representation of
// a report for versioning purposes. Instead, we factor it as a service and run it
// independently. 

class ReportingService {

    static transactional = true

    String buildSubjectReportXML(Subject sub) {
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		xml.mkp.xmlDeclaration(version:'1.0')
		xml.report(version:"1.0", 
			       subjectId: sub.id, 
				   patientId: sub.patientId, 
				   gender: sub.gender, 
				   date: new Date()) {
			
			def data = sub.getSubjectMutationReportData()

			for(String type in data.keySet()) {
				sampleType(type: type) {
					for(Sample mySample in data.getAt(type).keySet()) {
						sample(id: mySample.id,
							   barcode: mySample.barcode,
							   name: mySample.name,
							   type: mySample.type,
							   dnaConcentration: mySample.dnaConcentration,
							   dnaQuality: mySample.dnaQuality,
							   dateReceived: mySample.dateReceived,
							   dateCreated: mySample.dateCreated) {
							
							// Record the processes used for each sample. In principle there could be
							// several. We will probably ignore this, but it could be handy. 
							Set<Process> processes = [] as Set<Process>
							for(RunSample runSample in mySample.runSamples) {
								processes.add(runSample.process)
							}
							for(Process myProcess in processes) {
								def panel = myProcess.panel
								process(
									id: myProcess.id,
									runId: myProcess.runId,
									panelId: panel.id,
									panelName: panel.name,
									panelVersion: panel.versionString,
									date: myProcess.date
								)
							}
							
							// Now add the mutations
							for(ObservedMutation mut in data.getAt(type).getAt(mySample)) {
								def panel = mut.runSample.process.panel
								mutation(
									chromosome: mut.knownMutation.chromosome,
									gene: mut.knownMutation.gene,
									mutation: mut.knownMutation.mutation,
									publicId: mut.knownMutation.publicId,
									frequency: mut.frequency,
									panelId: panel.id,
									panelName: panel.name,
									panelVersion: panel.versionString,
									panelTechnology: panel.technology
								)
							}
						}
					}
				}
			}
		}
		return writer.toString()
    }
	
	/**
	 * Updates the subject's summary if needed. This mainly means writing in an indication
	 * that a sample has been received for a subject, if we have got as far as reporting on 
	 * it. The updates here only affect the arrival dates for PacBio and Sequenom, and even
	 * only these are affected if they are currently empty. 
	 * 
	 * @param sub the Subject
	 */
	void updateSummaryDates (Subject sub) {
		
		def summary = sub.summary
		
		for(Sample sample : sub.samples) {
			for(RunSample runSample : sample.runSamples) {
				def process = runSample.process
				def panelType = process.panel.technology
				if (panelType == 'Sequenom' && ! summary.sequenomArrivalDate) {
					summary.sequenomArrivalDate = process.date
				} else if (panelType == 'PacBio' && ! summary.pacbioArrivalDate) {
					summary.pacbioArrivalDate = process.date
				}
			}
		}
		
		if (summary.isDirty()) {
			summary.save()
		}
	}
	
	/**
	 * Persists the subject report, as an XML entry, in a new Report object and 
	 * associated ReportDocument object. 
	 * 
	 * @param sub the Subject
	 */
	
	void saveSubjectReport (Subject sub) {
		
		Report newReport = new Report()
		newReport.generated = new Date()
		newReport.document = new ReportDocument()
		
		String xml = buildSubjectReportXML(sub)
		newReport.document.type = 'text/xml'
		newReport.document.body = xml.getBytes()
		
		sub.addToReports(newReport)
		newReport.subject = sub
		
		newReport.save(validate:true)
		
	}
}
