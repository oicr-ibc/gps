package ca.on.oicr.gps.pages

import grails.test.*
import org.htmlparser.Parser
import org.htmlparser.filters.TagNameFilter

import ca.on.oicr.gps.controller.SampleController;
import ca.on.oicr.gps.model.data.Sample;
import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Summary;

class SampleGroovyPagesTests extends GroovyPagesTestCase {

	def submissionId
	def subjectId
	def sampleId

	public void setUp() {
		super.setUp()

		def random = new Random()
		def dna = ['Good', 'Bad', 'Ugly']
		def types = ['FFPE','Frozen','Blood','FNA']
		def i = 0
		
		def newSubject = new Subject()
		newSubject.patientId = '9839483984'
		newSubject.gender = 'F'
		newSubject.summary = new Summary()
		newSubject.summary.subject = newSubject
		newSubject.save(failOnError:true)
		
		def newSample = new Sample(
							barcode: random.nextInt(100000) ,
							type: types[i%4],
							dnaConcentration: random.nextFloat(),
							dnaQuality: dna[i%3],
							dateReceived: i%2 ? null : new Date(),
							dateCreated: new Date(),
							lastUpdated: new Date()
						)
		
		newSubject.addToSamples(newSample)
		
		newSample.save(failOnError:true)
				
		subjectId = newSubject.id
		sampleId = newSample.id
	}

	public void tearDown() {
		super.tearDown()
	}

	/**
	 * Test the submission controller's ability to set up to create a new
	 * submission.
	 */
	void testSampleList() {
		def file = new File(System.properties['base.dir'], "grails-app/views/sample/list.gsp")
		
		def subController = new SampleController()
		def model = subController.list()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
	}

	/**
	 * Test the submission controller's ability to set up to create a new
	 * submission.
	 */
	void testSampleShow() {
		def file = new File(System.properties['base.dir'], "grails-app/views/sample/show.gsp")
		
		def subController = new SampleController()
		subController.params.id = sampleId
		def model = subController.show()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
	}

	/**
	 * Test the submission controller's ability to set up to create a new
	 * submission.
	 */
	void testSampleEdit() {
		def file = new File(System.properties['base.dir'], "grails-app/views/sample/edit.gsp")
		
		def subController = new SampleController()
		subController.params.id = sampleId
		def model = subController.edit()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
	}

	/**
	 * Test the submission controller's ability to set up to create a new
	 * submission; actually, this will generate the form, but not the
	 * submission itself.
	 */
	void testSampleCreate() {
		def file = new File(System.properties['base.dir'], "grails-app/views/sample/create.gsp")
		
		def subController = new SampleController()
		def model = subController.create()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
	}

	/**
	 * Test the submission controller's ability to set up to create a new
	 * submission.
	 */
	void testSampleReceive() {
		def file = new File(System.properties['base.dir'], "grails-app/views/sample/receive.gsp")
		
		def subController = new SampleController()
		def model = subController.receive()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
	}
}
