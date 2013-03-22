package ca.on.oicr.gps.pipeline.sanger.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.on.oicr.gps.pipeline.BasePipeline;
import ca.on.oicr.gps.pipeline.Pipeline;
import ca.on.oicr.gps.pipeline.PipelineStep;
import ca.on.oicr.gps.pipeline.domain.DomainFacade;
import ca.on.oicr.gps.pipeline.model.MutationSubmission;
import ca.on.oicr.gps.pipeline.model.PipelineState;

public class SangerPipeline extends BasePipeline implements Pipeline {

	static final Logger log = LoggerFactory.getLogger(SangerPipeline.class);

	public String getTypeKey() {
		return "ABI";
	}

	public PipelineState newState(MutationSubmission submission, DomainFacade domain) {
		PipelineStep steps[] = new PipelineStep[] { 
			new SangerParseStep(),
			new SangerStoreStep()
		};

		PipelineState state = new PipelineState(steps, domain);
		state.set(MutationSubmission.class, submission);
		return state;
	}
	
	public static Logger getLogger() {
		return log;
	}
}
