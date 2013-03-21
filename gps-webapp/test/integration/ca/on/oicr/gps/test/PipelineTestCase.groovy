package ca.on.oicr.gps.test;

import static org.junit.Assert.*;
import ca.on.oicr.gps.model.data.Process;
import ca.on.oicr.gps.model.data.Sample;
import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Submission;
import ca.on.oicr.gps.model.data.Summary;
import ca.on.oicr.gps.pipeline.SubmissionSource;
import grails.test.GrailsUnitTestCase;

class PipelineTestCase extends GroovyTestCase {
	
	static transactional = true
	
	def pipelineService
	
	private Subject sj1
	private Subject sj2
	private Subject sj3
	private Subject sj4
	
	private Sample sample1
	private Sample sample2
	private Sample sample3
	private Sample sample4
	private Sample sample5
	private Sample sample6
	private Sample sample7
	private Sample sample8
	
	public void setUp() {
		super.setUp()
		
		sj1 = new Subject(patientId: "TST-001", gender: "F", summary: new Summary())
		sj2 = new Subject(patientId: "TST-002", gender: "F", summary: new Summary())
		sj3 = new Subject(patientId: "TST-003", gender: "F", summary: new Summary())
		sj4 = new Subject(patientId: "TST-004", gender: "F", summary: new Summary())
		sj1.summary.subject = sj1
		sj2.summary.subject = sj2
		sj3.summary.subject = sj3
		sj4.summary.subject = sj4
		sj1.save(failOnError:true)
		sj2.save(failOnError:true)
		sj3.save(failOnError:true)
		sj4.save(failOnError:true)
		
		sample1 = new Sample(barcode: "TST001BIO2FRZ1",
								type: "Frozen",
								subject: sj1,
								dateReceived: new Date(),
								dateCreated: new Date()).save(failOnError:true)
		sample2 = new Sample(barcode: "TST001BIO2FRZ2",
								type: "Frozen",
								subject: sj1,
								dateReceived: new Date(),
								dateCreated: new Date()).save(failOnError:true)
		sample3 = new Sample(barcode: "TST002BIOXFOR1",
								type: "FFPE",
								subject: sj2,
								dateReceived: new Date(),
								dateCreated: new Date()).save(failOnError:true)
		sample4 = new Sample(barcode: "TST002BIOXFOR2",
								type: "FFPE",
								subject: sj2,
								dateReceived: new Date(),
								dateCreated: new Date()).save(failOnError:true)
		sample5 = new Sample(barcode: "TST003BIOXFRZ2",
								type: "Frozen",
								subject: sj3,
								dateReceived: new Date(),
								dateCreated: new Date()).save(failOnError:true)
		sample6 = new Sample(barcode: "TST003BIOXFOR2",
								type: "FFPE",
								subject: sj3,
								dateReceived: new Date(),
								dateCreated: new Date()).save(failOnError:true)
		sample7 = new Sample(barcode: "TST004BIOXFOR1",
								type: "FFPE",
								subject: sj4,
								dateReceived: new Date(),
								dateCreated: new Date()).save(failOnError:true)
		sample8 = new Sample(barcode: "TST004BIOXFOR2",
								type: "FFPE",
								subject: sj4,
								dateReceived: new Date(),
								dateCreated: new Date()).save(failOnError:true)
	
		// Detach these objects so they aren't retrieved preferentially from the cache
		sj1.discard()
		sj2.discard()
		sj3.discard()
		sj4.discard()
		sample1.discard()
		sample2.discard()
		sample3.discard()
		sample4.discard()
		sample5.discard()
		sample6.discard()
		sample7.discard()
		sample8.discard()
	}

	/**
	* Public method used to generate and persist a submission preparatory to testing
	* a pipeline
	* @return a new submission
	*/
	public Submission processSubmission(String type, String filename) {
		Submission sub = new Submission()
		
		File file = new File(filename);
		sub.dataType = type
		sub.userName = 'user'
		sub.dateSubmitted = new Date()
		sub.fileName = file.name
		sub.fileContents = file.getBytes()
		sub.save(failOnError:true)
		
		def state = pipelineService.getPipelineState(sub)
		pipelineService.runPipeline(state)
	   
		def errors = state.errors
		assertEquals(0, errors.size())
		
		return sub
	}
}
