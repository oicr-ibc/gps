package ca.on.oicr.gps.pipeline.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.on.oicr.gps.pipeline.domain.DomainAssay;
import ca.on.oicr.gps.pipeline.domain.DomainFacade;
import ca.on.oicr.gps.pipeline.domain.DomainKnownMutation;
import ca.on.oicr.gps.pipeline.domain.DomainObservedMutation;
import ca.on.oicr.gps.pipeline.domain.DomainProcess;
import ca.on.oicr.gps.pipeline.domain.DomainRunAssay;
import ca.on.oicr.gps.pipeline.domain.DomainRunSample;
import ca.on.oicr.gps.pipeline.model.PipelineException;
import ca.on.oicr.gps.pipeline.pacbio.v1.PacBioStoreStep;
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

	Map<String, List<DomainAssay>> assayTable = new HashMap<String, List<DomainAssay>>();
	
	public List<DomainAssay> findAssays(DomainProcess process, Map<String, Object> criteria) {
		String key = Utilities.criteriaAsString(criteria);
		log.info("Looking for assays: {}", key);
		List<DomainAssay> result = assayTable.get(key);
		log.info("Found assays: {}", result);
		return result;
	}

	public DomainAssay newAssay(DomainRunSample runSample, String gene, String name) {
		return new DomainAssayImpl(name);
	}

	public DomainRunAssay newRunAssay(DomainRunSample runSample, DomainAssay runAssay) {
		return new DomainRunAssayImpl();
	}
	
	private Map<String, DomainKnownMutation> mutationTable = new HashMap<String, DomainKnownMutation>();

	public DomainKnownMutation findKnownMutation(Map<String, Object> criteria) {
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

	public DomainObservedMutation newObservedMutation(DomainRunAssay runAssay, DomainKnownMutation known) {
		return new DomainObservedMutationImpl();
	}

	public void saveProcess(DomainProcess s) {
		return;
	}

	public void setAssays(String key, List<DomainAssay> arrayList) {
		assayTable.put(key, arrayList);
	}

	public void setKnownMutation(String key, DomainKnownMutation known) {
		mutationTable.put(key, known);
	}
}
