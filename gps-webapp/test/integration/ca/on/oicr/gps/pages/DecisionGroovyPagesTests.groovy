package ca.on.oicr.gps.pages

import grails.test.*
import org.htmlparser.Parser
import org.htmlparser.filters.TagNameFilter

import ca.on.oicr.gps.controller.DecisionController;
import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Summary;
import ca.on.oicr.gps.model.data.Decision;

class DecisionGroovyPagesTests extends GroovyPagesTestCase {

	def decisionId
	def subjectId

    public void setUp() {
        super.setUp()

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
		
		newSubject.addToDecisions(decision)
		
		newSubject.save(failOnError:true)
		
		decisionId = decision.id
		subjectId = newSubject.id
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Test the decision controller's ability to set up to show a
	 * decision.
	 */
    void testDecisionShow() {
		def file = new File("grails-app/views/decision/show.gsp")
		
		def decisionController = new DecisionController()
		decisionController.params.id = decisionId
		def model = decisionController.show()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
		
		// This ought to contain some reference to the interim report. 
    }

	/**
	 * Test the decision controller's ability to set up to show a
	 * decision.
	 */
    void testDecisionCreate() {
		def file = new File("grails-app/views/decision/create.gsp")
		
		def decisionController = new DecisionController()
		decisionController.params._subject = subjectId
		def model = decisionController.create()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")		
    }
}
