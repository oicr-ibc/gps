package ca.on.oicr.gps.pipeline.mock;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.on.oicr.gps.pipeline.domain.DomainTarget;
import ca.on.oicr.gps.pipeline.domain.DomainFacade;
import ca.on.oicr.gps.pipeline.domain.DomainKnownMutation;
import ca.on.oicr.gps.pipeline.domain.DomainObservedMutation;
import ca.on.oicr.gps.pipeline.domain.DomainProcess;
import ca.on.oicr.gps.pipeline.domain.DomainRunAssay;
import ca.on.oicr.gps.pipeline.domain.DomainRunSample;
import ca.on.oicr.gps.pipeline.model.PipelineException;
import ca.on.oicr.gps.util.Utilities;

public class DomainFacadeImpl implements DomainFacade {
	
	private static final Logger log = LoggerFactory.getLogger(DomainFacadeImpl.class);
	
	private List<DomainProcess> processes = new ArrayList<DomainProcess>();
	
	public List<DomainProcess> getProcesses() {
		return processes;
	}

	public DomainProcess newProcess(String runId, String panelName, String panelVersion) throws PipelineException {
		log.info("Creating process: {}, {}, {}", new Object[] {runId, panelName, panelVersion});
		DomainProcess p = new DomainProcessImpl(runId, panelName, panelVersion);
		processes.add(p);
		return p;
	}

	public DomainRunSample newRunSample(DomainProcess process, String patientId, String sampleId) throws PipelineException {
		return new DomainRunSampleImpl(process);
	}

	List<DomainTarget> targetTable = new ArrayList<DomainTarget>();
	
	public List<DomainTarget> findTargets(DomainProcess process, Map<String, Object> criteria) {
		
		String key = Utilities.criteriaAsString(criteria);
		log.info("Looking for targets: {}", key);

		List<DomainTarget> result = new ArrayList<DomainTarget>();
		for(DomainTarget target : targetTable) {
			if (criteria.containsKey("chromosome") && ! target.getChromosome().equals(criteria.get("chromosome"))) continue;
			if (criteria.containsKey("gene") && ! target.getGene().equals(criteria.get("gene"))) continue;
			if (criteria.containsKey("start") && target.getStart() > ((Integer) criteria.get("start")).intValue()) continue;
			if (criteria.containsKey("stop") && target.getStop() < ((Integer) criteria.get("stop")).intValue()) continue;
			if (criteria.containsKey("refAllele") && ! target.getRefAllele().equals(criteria.get("refAllele"))) continue;
			if (criteria.containsKey("varAllele") && ! target.getVarAllele().equals(criteria.get("varAllele"))) continue;
			if (criteria.containsKey("mutation") && ! target.getMutation().equals(criteria.get("mutation"))) continue;
			result.add(target);
		}
		
		log.info("Found targets: {}", result);
		return result;
	}

	private Map<String, DomainKnownMutation> mutationTable = new HashMap<String, DomainKnownMutation>();

	public DomainKnownMutation findKnownMutation(Map<String, Object> criteria) {
		
		assertTrue(criteria.containsKey("chromosome"));
		assertTrue(criteria.containsKey("start"));
		assertTrue(criteria.containsKey("stop"));
		assertTrue(criteria.containsKey("varAllele"));

		String key = Utilities.criteriaAsString(criteria);
		log.info("Looking for known mutation: {}", key);
		DomainKnownMutation result = mutationTable.get(key);
		log.info("Found known mutation: {}", result);
		return result;
	}

	public DomainKnownMutation newKnownMutation(Map<String, Object> criteria) {
		String key = Utilities.criteriaAsString(criteria);
		log.info("Creating new known mutation: {}", key);
		return new DomainKnownMutationImpl((String)criteria.get("mutation"), key);
	}

	public DomainObservedMutation newObservedMutation(DomainRunSample runSample, DomainKnownMutation known) {
		return new DomainObservedMutationImpl();
	}

	public void finishProcess(DomainProcess s) {
		return;
	}

	public void setTargets(List<DomainTarget> arrayList) {
		targetTable = arrayList;
	}

	public void setKnownMutation(String key, DomainKnownMutation known) {
		mutationTable.put(key, known);
	}
}
