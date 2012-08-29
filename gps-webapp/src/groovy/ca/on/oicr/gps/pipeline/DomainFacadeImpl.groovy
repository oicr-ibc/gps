package ca.on.oicr.gps.pipeline

import java.util.Collections;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import ca.on.oicr.gps.service.MutationKnowledgeService;
import ca.on.oicr.gps.model.data.ObservedMutation;
import ca.on.oicr.gps.model.data.RunSample;
import ca.on.oicr.gps.model.data.Sample;
import ca.on.oicr.gps.model.data.Process;
import ca.on.oicr.gps.model.data.Submission;
import ca.on.oicr.gps.model.data.RunSample;
import ca.on.oicr.gps.model.knowledge.KnownMutation;
import ca.on.oicr.gps.model.laboratory.Panel;
import ca.on.oicr.gps.model.laboratory.Target;
import ca.on.oicr.gps.pipeline.domain.DomainFacade;
import ca.on.oicr.gps.pipeline.domain.DomainTarget;
import ca.on.oicr.gps.pipeline.domain.DomainRunSample;
import ca.on.oicr.gps.pipeline.domain.DomainKnownMutation;
import ca.on.oicr.gps.pipeline.domain.DomainObservedMutation;
import ca.on.oicr.gps.pipeline.model.PipelineError;
import ca.on.oicr.gps.pipeline.model.PipelineException;
import ca.on.oicr.gps.pipeline.domain.DomainProcess;
import ca.on.oicr.gps.util.DirectionCompensator;


/**
 * DomainFacadeImpl implements the DomainFacade interface, but in Groovy, so it
 * can handle the persistence needed from the Java pipeline implementations. 
 * 
 * @author swatt
 *
 */

class DomainFacadeImpl implements DomainFacade {
	
	static final Logger log = Logger.getLogger(this)
	
	SessionFactory sessionFactory
	MutationKnowledgeService mutationKnowledgeService
	Submission submission
	Set<KnownMutation> observedKnownMutations = new HashSet<KnownMutation>();
	private Sample sample
	
	public DomainFacadeImpl(SubmissionSource source, MutationKnowledgeService service) {
		super()
		submission = source.domainSubmission
		mutationKnowledgeService = service
	}
	
	public DomainProcess newProcess(String runId, String panelName, String panelVersion) throws PipelineException {
		
		// If we find an old process, we need to remove its links. Do so before we start
		// the pipeline, relying on transactions to roll this back if needed. We do
		// this because GORM has a habit of weaving old and new objects together. 
		//
		// NOTE: WE RELY ON TRANSACTIONS ROLLING THIS BACK IN CASE OF ERROR. But we
		// cannot delete the object later due to GORM stuff. 
		
		log.trace("New process: run id: " + runId)
		panelName = panelName.replaceAll(" ", "")
		
		Panel panel = Panel.findByNameAndVersionString(panelName, panelVersion);
		if (! panel) {
			throw new PipelineException("data.unknown.panel", panelName, panelVersion);
		}
		
		Process oldProcess = Process.findByRunId(runId)
		if (oldProcess) {
			
			// Delete all the old run samples, safely during iteration
			def runSamples = []
			for(RunSample rs : oldProcess.runSamples) {
				runSamples.add(rs)
			}
			
			for(RunSample runSample : runSamples) {
				runSample.sample.removeFromRunSamples(runSample)
				runSample.process.removeFromRunSamples(runSample)
				runSample.delete()
			}
			
			oldProcess.submission?.removeFromProcesses(oldProcess)
			submission.addToProcesses(oldProcess)
			oldProcess.submission = submission
			oldProcess.panel = panel
			
			log.trace("Updating process with " + runId + ": " + oldProcess)
			return (DomainProcess) oldProcess
		} else {
		
			Process newProcess = new Process(runId: runId)
			submission.addToProcesses(newProcess)
			newProcess.submission = submission
			newProcess.panel = panel

			log.trace("New process with " + runId + ": " + newProcess)
			return (DomainProcess) newProcess 
		}
	}
	
	public DomainRunSample newRunSample(DomainProcess domainProcess, String patientId, String sampleId) throws PipelineException {
		
		Process process = (Process) domainProcess

		assert(patientId)
		assert(sampleId)
		assert(process)
		
		log.trace("New run sample: " + sampleId + ", for patient id: " + patientId)
		
		sample = Sample.findByBarcode(sampleId)
		if (! sample) {
			throw new PipelineException("data.missing.sample", sampleId)
		}
		
		String expectedPatientId = sample.subject.patientId
		if (expectedPatientId != patientId) {
			throw new PipelineException("data.incorrect.patient", patientId, expectedPatientId)
		}
		
		RunSample runSample = new RunSample()
		process.addToRunSamples(runSample)
		sample.addToRunSamples(runSample)
		runSample.process = process
		runSample.sample = sample
		return (DomainRunSample) runSample;
	}
	
	public List<DomainTarget> findTargets(DomainProcess domainProcess, Map<String, Object> criteria) {
		
		Process process = (Process) domainProcess
		
		log.trace("Looking for assays: " + criteria);
		
		List<Target> found = Target.withCriteria {
			eq("panel", process.panel)
			if (criteria.chromosome) {
				eq("chromosome", criteria.chromosome)
			}
			if (criteria.gene) {
				eq("gene", criteria.gene)
			}
			if (criteria.mutation) {
				eq("mutation", criteria.mutation)
			}
			if (criteria.varAllele) {
				eq("varAllele", criteria.varAllele)
			}
			// Start and stop define a range
			if (criteria.start && criteria.stop) {
				le("start", criteria.start)
				ge("stop", criteria.stop)
			}
		}
		
		if (found.size() > 0) {
			log.trace("Found targets: " + found)
		} else {
			log.trace("Failed to find any targets using criteria: " + criteria)
		}
		
		return (List<DomainTarget>) found
	}
	
	/**
	 * Finds and returns a known mutation, if we can find one.
	 * @param criteria 
	 * @return a mutation, if one can be found
	 */
	public DomainKnownMutation findKnownMutation(Map<String, Object> criteria) {
		
		log.trace("Looking for known mutation: " + criteria)
		KnownMutation found = mutationKnowledgeService.findBy(criteria)
		
		if (found) {
			log.trace("Found known mutation: " + found)
			return (DomainKnownMutation) found
		} else {
			log.trace("Failed to find known mutation using criteria: " + criteria)
		}
		
		log.trace("Changing reference and variant alleles known mutation: " + criteria)
		if (criteria.getAt("refAllele")) {
			criteria.putAt("refAllele", DirectionCompensator.compensate(criteria.getAt("refAllele")))
		}
		if (criteria.getAt("varAllele")) {
			criteria.putAt("varAllele", DirectionCompensator.compensate(criteria.getAt("varAllele")))
		}
		found = mutationKnowledgeService.findBy(criteria)
		
		if (found) {
			log.trace("Found known mutation: " + found)
		} else {
			log.trace("Failed (again) to find known mutation using criteria: " + criteria)
		}

		return (DomainKnownMutation) found
	}

	public DomainKnownMutation newKnownMutation(Map<String, Object> criteria) {
		
		KnownMutation found = mutationKnowledgeService.newKnownMutation(criteria)
		return (DomainKnownMutation) found
	}
	
	/**
	 * Generates a new (observed) mutation, from a reference to a known mutation, which
	 * we should have previously found or constructed.
	 */
	public DomainObservedMutation newObservedMutation(DomainRunSample domainRunSample, DomainKnownMutation domainKnown) {
		
		RunSample runSample = (RunSample) domainRunSample
		KnownMutation known = (KnownMutation) domainKnown
		
		log.trace("New observed mutation: " + known.toString());
		ObservedMutation mut = new ObservedMutation()
		mut.knownMutation = known
		runSample.addToMutations(mut)
		mut.runSample = runSample
		
		observedKnownMutations.add(known)
		
		return (DomainObservedMutation) mut
	}
	
	/**
	 * Saves a Run
	 */
	public void finishProcess(DomainProcess process) {
		Process run = (Process) process
		log.trace("Called finishProcess")
		run.save(validate: true, flush: true, failOnError: true)
		
		for(KnownMutation mut : observedKnownMutations) {
			mut.visible = true
			mut.save()
		}
	}
}
