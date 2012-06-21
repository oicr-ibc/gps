package ca.on.oicr.gps.pipeline.domain;

import java.util.List;
import java.util.Map;

import ca.on.oicr.gps.pipeline.domain.DomainTarget;
import ca.on.oicr.gps.pipeline.domain.DomainKnownMutation;
import ca.on.oicr.gps.pipeline.domain.DomainObservedMutation;
import ca.on.oicr.gps.pipeline.domain.DomainProcess;
import ca.on.oicr.gps.pipeline.domain.DomainRunSample;
import ca.on.oicr.gps.pipeline.model.PipelineException;

/**
 * A DomainResolver allows the Java pipeline system to look up domain components
 * to add to the state information and to build into domain objects. This is used
 * both to validate data in the pipeline system, and to build the domain objects
 * appropriately. 
 * 
 * @author swatt
 *
 */
public interface DomainFacade {
	
	public DomainProcess newProcess(String runId, String panelName, String panelVersion) throws PipelineException;
	
	public DomainRunSample newRunSample(DomainProcess process, String patientId, String sampleId) throws PipelineException;
	
	public List<DomainTarget> findTargets(DomainProcess process, Map<String, Object> criteria);
	
	public DomainKnownMutation findKnownMutation(Map<String, Object> criteria);
	
	public DomainKnownMutation newKnownMutation(Map<String, Object> criteria);

	public DomainObservedMutation newObservedMutation(DomainRunSample runSample, DomainKnownMutation known);
	
	public void finishProcess(DomainProcess s);
}
