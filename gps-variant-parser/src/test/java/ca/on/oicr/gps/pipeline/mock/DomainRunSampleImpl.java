package ca.on.oicr.gps.pipeline.mock;

import ca.on.oicr.gps.pipeline.domain.DomainProcess;
import ca.on.oicr.gps.pipeline.domain.DomainRunSample;

public class DomainRunSampleImpl implements DomainRunSample {
	
	private DomainProcess process = null;
	
	public DomainRunSampleImpl(DomainProcess theProcess) {
		
	}

	public DomainProcess getProcess() {
		return process;
	}

}
