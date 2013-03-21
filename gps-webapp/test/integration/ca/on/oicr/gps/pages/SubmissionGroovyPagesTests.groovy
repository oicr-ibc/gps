package ca.on.oicr.gps.pages

import grails.test.*
import org.htmlparser.Parser
import org.htmlparser.filters.TagNameFilter

import ca.on.oicr.gps.controller.PipelineController;
import ca.on.oicr.gps.controller.SubmissionController;
import ca.on.oicr.gps.controller.SummaryController;
import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Submission;
import ca.on.oicr.gps.model.data.Summary;
import ca.on.oicr.gps.pipeline.model.PipelineError;

/**
 * @author swatt
 * 
 * This test class does some round-trip testing between the data and the results
 * of the GSP rendering. The actual tests are fairly limited, as most of the actual 
 * test - apart from the data - can be mangled by internationalization. 
 */
class SubmissionGroovyPagesTests extends GroovyPagesTestCase {
	
	def submissionId
	def subjectId
	
    public void setUp() {
        super.setUp()

		def newSubject = new Subject()
		newSubject.patientId = '9839483984'
		newSubject.gender = 'F'
		newSubject.summary = new Summary()
		newSubject.summary.subject = newSubject
		newSubject.save(failOnError:true)
		
		def newSubmissionFileName = 'test/data/sequenom_test_01.xls'
		def newSubmissionFile = new File(newSubmissionFileName)

		def newSubmission = new Submission(
			dataType: 'Sequenom',
			userName: 'swatt',
			dateSubmitted: new Date(),
			fileName: newSubmissionFile.path,
			fileContents: newSubmissionFile.getBytes(),
			processes: []
		)
		newSubmission.save(failOnError:true)
		
		submissionId = newSubmission.id
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
		def file = new File("grails-app/views/submission/create.gsp")
		
		def subController = new SubmissionController()
		def model = subController.create()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
    }

	/**
	 * Test the submission controller's ability to list submissions.
	 */
	void testSubmissionList() {
		def file = new File("grails-app/views/submission/list.gsp")
		
		def subController = new SubmissionController()
		def model = subController.list()
		
		def htmlString = applyTemplate(file.text, model)
		def textString = (htmlString =~ /<[^>]+>/).replaceAll("")
		
		assertTrue(htmlString.contains("sequenom_test_01.xls"))
    }
	
	/**
	 * Test the submission controller's ability to edit a submission.
	 */
	void testSubmissionEdit() {
		def file = new File("grails-app/views/submission/edit.gsp")
		
		def subController = new SubmissionController()
		subController.params.id = submissionId
		def model = subController.edit()
		
		def htmlString = applyTemplate(file.text, model)
		
		// Throw this into an HTML parser for testing
		def parser = new Parser(htmlString)
		def nodes = parser.parse(null)
		
		// Now check we have some inputs
		def inputs = nodes.extractAllNodesThatMatch(new TagNameFilter("input"), true)
		assertTrue(inputs.size > 1)
	}
	
	void testPipelineSuccess() {
		def file = new File("grails-app/views/pipeline/run.gsp")

		def submission = Submission.get(submissionId)
		
		def model = [submissionInstance: submission, pipelineErrors: []]

		def htmlString = applyTemplate(file.text, model)

		// Throw this into an HTML parser for testing
		def parser = new Parser(htmlString)
		def nodes = parser.parse(null)
	}

	void testPipelineErrors() {
		def file = new File("grails-app/views/pipeline/run.gsp")

		def submission = Submission.get(submissionId)
		
		PipelineError error1 = new PipelineError('some.random.error', 1, 2, 3)
		
		def model = [submissionInstance: submission, pipelineErrors: [error1]]

		def htmlString = applyTemplate(file.text, model)

		// Throw this into an HTML parser for testing
		def parser = new Parser(htmlString)
		def nodes = parser.parse(null)
	}

	void testListOfMutations() {
		def file = new File("grails-app/views/summary/mutations.gsp")

		def summaryController = new SummaryController()
		def model = summaryController.mutations()
		
		def htmlString = applyTemplate(file.text, model)
		
		// Throw this into an HTML parser for testing
		def parser = new Parser(htmlString)
		def nodes = parser.parse(null)
		
		// Now check we have some data
		def rows = nodes.extractAllNodesThatMatch(new TagNameFilter("tr"), true)
		assertTrue(rows.size == 1)
	}
}
