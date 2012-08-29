package ca.on.oicr.gps.pages

import grails.test.*
import org.htmlparser.Parser
import org.htmlparser.filters.TagNameFilter

import ca.on.oicr.gps.controller.SummaryController;
import ca.on.oicr.gps.model.data.ReportableMutation;
import ca.on.oicr.gps.model.data.Sample;
import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Summary;
import ca.on.oicr.gps.model.data.Decision;

class SummaryGroovyPagesTests extends GroovyPagesTestCase {

	def subjectId

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
		
		def decision = new Decision(subject: newSubject)
		decision.noTumour = false
		decision.insufficientMaterial = false
		decision.noMutationsFound = false
		decision.unanimous = true
		decision.date = new Date(112, 2, 1)
		decision.decisionType = decision.TYPE_INTERIM
		decision.decision = "Blah"
		decision.reportableMutations = []
		
		newSubject.addToDecisions(decision)
		
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
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Test the submission controller's ability to set up to create a new
	 * submission.
	 */
    void testSummaryShow() {
		def file = new File("grails-app/views/summary/show.gsp")
		
		def summController = new SummaryController()
		summController.params.id = subjectId
		def model = summController.show()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
		
		// This ought to contain some reference to the interim report. 
    }
}
