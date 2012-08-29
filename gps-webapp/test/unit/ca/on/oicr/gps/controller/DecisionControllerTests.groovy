package ca.on.oicr.gps.controller

import ca.on.oicr.gps.model.data.Decision;
import ca.on.oicr.gps.model.data.ObservedMutation;
import ca.on.oicr.gps.model.data.Process;
import ca.on.oicr.gps.model.data.ReportableMutation;
import ca.on.oicr.gps.model.data.RunSample;
import ca.on.oicr.gps.model.data.Sample;
import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Summary;
import ca.on.oicr.gps.model.knowledge.KnownMutation;
import ca.on.oicr.gps.model.laboratory.Target;
import ca.on.oicr.gps.model.laboratory.Panel;
import grails.test.*

class DecisionControllerTests extends ControllerUnitTestCase {
	
	def subject
	def decision
	def observedMutation1
	def observedMutation2
	def observedMutation3
	
    public void setUp() {
        super.setUp()
		
		mockDomain(Subject)
		mockDomain(Summary)
		mockDomain(Decision)
		mockDomain(ReportableMutation)
		
		// Make sure we test the case where there are two observed mutations for the same known mutation.
		// These should be merged in the reporting process. Somehow. 
		def knownMutation1 = new KnownMutation(gene: 'BRAF', mutation: 'G649S', chromosome: 7)
		def knownMutation2 = new KnownMutation(gene: 'KRAS', mutation: 'G12A', chromosome: 12)
		mockDomain(KnownMutation, [knownMutation1, knownMutation2])
		
		observedMutation1 = new ObservedMutation(status: ObservedMutation.MUTATION_STATUS_FOUND, knownMutation: knownMutation1)
		observedMutation2 = new ObservedMutation(status: ObservedMutation.MUTATION_STATUS_FOUND, knownMutation: knownMutation2)
		observedMutation3 = new ObservedMutation(status: ObservedMutation.MUTATION_STATUS_FOUND, knownMutation: knownMutation2)
		mockDomain(ObservedMutation, [observedMutation1, observedMutation2, observedMutation3])
		
		def panel1 = new Panel(technology: 'Sequenom')
		def panel2 = new Panel(technology: 'ABI')
		mockDomain(Panel, [panel1, panel2])
		
		def process1 = new Process(panel: panel1)
		def process2 = new Process(panel: panel2)
		mockDomain(Process, [process1, process2])
		
		def runSample1 = new RunSample(process: process1, mutations: [observedMutation1])
		def runSample2 = new RunSample(process: process2, mutations: [observedMutation2, observedMutation3])
		mockDomain(RunSample, [runSample1, runSample2])

		observedMutation1.runSample = runSample1
		observedMutation2.runSample = runSample2
		observedMutation3.runSample = runSample2
		
		def sample = new Sample(runSamples: [runSample1, runSample2])
		mockDomain(Sample, [sample])
		
		subject = new Subject(samples: [sample])
		decision = new Decision(subject: subject)
		
		subject.patientId = "XXX-001"
		subject.gender = subject.SEX_FEMALE
		subject.summary = new Summary()
		
		decision.noTumour = false
		decision.insufficientMaterial = false
		decision.noMutationsFound = false
		decision.unanimous = true
		
		decision.date = new Date(112, 2, 1)
		decision.decisionType = Decision.TYPE_FINAL
		decision.decision = "Blah"

		subject.save(flush: true, failOnError: true)
		decision.save(flush: true, failOnError: true)
    }

    public void tearDown() {
        super.tearDown()
    }

    void testShow() {
		controller.params.id = decision.id
		def model = controller.show()
		assertTrue model.containsKey('decisionInstance')
		assertEquals decision.decision, model.decisionInstance.decision
    }
	
    void testFailedCreate() {
		controller.params._subject = -1
		def model
		
		shouldFail {
			model = controller.create()
		}
    }
	
    void testCreate() {
		controller.params._subject = subject.id
		def model = controller.create()
		assertTrue model.containsKey('decisionInstance')
    }
	
    void testFailedSave() {
		controller.params._subject = -1
		def model
		
		shouldFail {
			model = controller.save()
		}
    }
	
	private void prepareSave() {
		controller.metaClass.message = { LinkedHashMap arg1 -> return 'test message output'}
		
		controller.params._subject = subject.id
		controller.params.source = Sample.SOURCE_STUDY_SAMPLE
		controller.params.decision = "Apples"
		controller.params.decisionType = Decision.TYPE_FINAL
		controller.params.noTumour = false
		controller.params.insufficientMaterial = true
		controller.params.noMutationsFound = false
		controller.params.unanimous = true
				
		controller.params._mutationIds = [
			String.valueOf(observedMutation1.id) + "," + String.valueOf(observedMutation2.id),
			String.valueOf(observedMutation3.id)
		]
		controller.params.reportable = ["0"]
		controller.params.actionable = ["1"]
		controller.params.levelOfEvidence = ['Level I', 'Level II']
		controller.params.levelOfEvidenceGene = ['Level V', 'Level III']
		controller.params.comment = ['comment1', 'comment 2']
		controller.params.justification = ['justification1', 'justification2']
	}
	
	void testWithdrawPrevious() {
		prepareSave()
		controller.params.source = Sample.SOURCE_ARCHIVAL_SAMPLE
		controller.params.decisionType = Decision.TYPE_INTERIM
		def model = controller.save()
		
		// This should mean that we have a single decision for the subject, and that it will be
		// of type final.
		assertEquals 1, subject.decisions.size()
		def firstDecision = subject.decisions.iterator().next()
		assertEquals Decision.TYPE_INTERIM, firstDecision.decisionType
		
		// Now, let's create a new decision (for the same source), and see what happens...
		prepareSave()
		controller.params.source = Sample.SOURCE_ARCHIVAL_SAMPLE
		controller.params.decisionType = Decision.TYPE_FINAL
		model = controller.save()
		assertEquals 2, subject.decisions.size()
		assertEquals 1, subject.decisions.findAll { it.decisionType == 'final' }.size()
		assertEquals Decision.TYPE_WITHDRAWN, firstDecision.decisionType
		
		// And now let's create a decision of a different source
		prepareSave()
		controller.params.source = Sample.SOURCE_STUDY_SAMPLE
		controller.params.decisionType = Decision.TYPE_FINAL
		model = controller.save()
		assertEquals 3, subject.decisions.size()
		assertEquals 2, subject.decisions.findAll { it.decisionType == 'final' }.size()
	}
	
	void testSave() {
		prepareSave()
		def model = controller.save()
		def redirects = redirectArgs
		assertEquals "show", redirectArgs?.action
		assertTrue redirectArgs.containsKey('id')
		
		def subject = Subject.get(redirectArgs.id)
		assertTrue subject != null
		
		def decision = subject.findDecision(Sample.SOURCE_STUDY_SAMPLE)
		assertTrue decision != null
		
		// Check the main decision fields
		assertEquals "Apples", decision.decision
		assertEquals Decision.TYPE_FINAL, decision.decisionType
		assertEquals false, decision.noTumour
		assertEquals true, decision.insufficientMaterial
		assertEquals false, decision.noMutationsFound
		assertEquals true, decision.unanimous
		
		def reportableMutations = decision.getReportableMutations()
		assertEquals 2, reportableMutations.size()
		
		reportableMutations.collect {
			assertTrue it.observedMutations != null
			assertTrue it.observedMutations.size() >= 1
		}
		
		// Sneaky, find 1st mutation by knowing it doesn't have observedMutation3
		def firstMutation = reportableMutations.find {
			it.observedMutations.asList().head().id != observedMutation3.id
		}
		
		assertEquals true, firstMutation.reportable
		assertEquals false, firstMutation.actionable
		assertEquals 'Level I', firstMutation.levelOfEvidence
		assertEquals 'Level V', firstMutation.levelOfEvidenceGene
		assertEquals 'comment1', firstMutation.comment
		assertEquals 'justification1', firstMutation.justification
		assertEquals 2, firstMutation.observedMutations.size()
		
		// Sneaky, find 2nd mutation by knowing it must only be observedMutation3
		def secondMutation = reportableMutations.find {
			it.observedMutations.asList().head().id == observedMutation3.id
		}
		
		assertEquals 1, secondMutation.observedMutations.size()
		
		assertEquals Decision.TYPE_FINAL, decision.decisionType
	}
	
	void testPanels() {
		prepareSave()
		def model = controller.save()
		
		def subject = Subject.get(redirectArgs.id)
		assertTrue subject != null
		
		def decision = subject.findDecision(Sample.SOURCE_STUDY_SAMPLE)
		assertTrue decision != null

		def reportableMutations = decision.getReportableMutations()
		assertEquals 2, reportableMutations.size()
			
		def firstMutation = reportableMutations.find {
			it.observedMutations.asList().head().id != observedMutation3.id
		}
		
		def panels = firstMutation.getPanels()
		
		// This is the form used in the view to detect which panels were used
		assertTrue panels.find { it.technology == 'Sequenom' } != null
		assertTrue panels.find { it.technology == 'ABI' } != null
		
		def secondMutation = reportableMutations.find {
			it.observedMutations.asList().head().id == observedMutation3.id
		}

		panels = secondMutation.getPanels()

		// This is the form used in the view to detect which panels were used
		assertTrue panels.find { it.technology == 'ABI' } != null
	}
}
