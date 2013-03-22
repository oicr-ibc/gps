package ca.on.oicr.gps.service

import ca.on.oicr.gps.model.data.Sample;
import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Summary;
import grails.test.*

class StatusTests extends GroovyTestCase {
	
	Subject sj1
	Subject sj2
	Subject sj3
	
	Sample sample1
	Sample sample2
	Sample sample3
	Sample sample4
	Sample sample5

    public void setUp() {
        super.setUp()

		sj1 = new Subject(patientId: "TGWS-001", gender: "F", summary: new Summary())
		sj2 = new Subject(patientId: "GEN-002", gender: "F", summary: new Summary())
		sj3 = new Subject(patientId: "GEN-003", gender: "F", summary: new Summary())
		sj1.summary.subject = sj1
		sj2.summary.subject = sj2
		sj3.summary.subject = sj3
		sj1.save(failOnError:true)
		sj2.save(failOnError:true)
		sj3.save(failOnError:true)
		
		// Define some samples
		sample1 = new Sample(barcode: "PMH001BIO2FRZ2",
							 type: "FFPE",
							 subject: sj1,
							 dateCreated: new Date()).save(failOnError:true)
		sample2 = new Sample(barcode: "PMH002BIOXFOR1",
							 type: "FFPE",
							 subject: sj2,
							 dateCreated: new Date()).save(failOnError:true)
		sample3 = new Sample(barcode: "PMH003BIOXFRZ1",
							 type: "FFPE",
							 subject: sj3,
							 dateCreated: new Date()).save(failOnError:true)
		sample4 = new Sample(barcode: "PMH003BIOXFOR1",
							 type: "FFPE",
							 subject: sj3,
							 dateCreated: new Date()).save(failOnError:true)
		sample5 = new Sample(barcode: "PMH003BIOXFOR2",
							 type: "FFPE",
							 subject: sj3,
							 dateCreated: new Date()).save(failOnError:true)
		
		// Now define some Processes and RunSamples, which associate the samples and the subjects. 
		// We probably also need at least one submission to act as a parent for this lot. Then we
		// can do this. Mocking would be nice, but it's likely implemented as criteria queries, which
		// are going to be more interesting. 
    }

    public void tearDown() {
        super.tearDown()
		
		sj1.delete()
		sj2.delete()
		sj3.delete()
    }

    void testStatusService1() {
		
		def params = [:]
		List<Subject> result = Subject.getAllReportable(params)
		
		// Those above, plus the test data we bootstrapped with - there were 18 of those
		assertEquals(21, result.size())
    }

    void testStatusService2() {
		
		def params = [patientId: "GEN-003"]
		List<Subject> result = Subject.getAllReportable(params)
		assertEquals(1, result.size())
    }

    void testStatusService3() {
		
		def params = [patientId: "GEN"]
		List<Subject> result = Subject.getAllReportable(params)
		assertEquals(2, result.size())
    }
}
