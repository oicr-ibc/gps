package ca.on.oicr.gps.pipeline.pacbio.v1;

import ca.on.oicr.gps.pipeline.BasePipeline;
import ca.on.oicr.gps.pipeline.Pipeline;
import ca.on.oicr.gps.pipeline.PipelineStep;
import ca.on.oicr.gps.pipeline.domain.DomainFacade;
import ca.on.oicr.gps.pipeline.model.MutationSubmission;
import ca.on.oicr.gps.pipeline.model.PipelineState;
import ca.on.oicr.gps.pipeline.step.validate.ValidationStep;

public class PacBioPipeline extends BasePipeline implements Pipeline {

	public String getTypeKey() {
		return "PacBio";
	}
	
	public PipelineState newState(MutationSubmission submission, DomainFacade domain) {
		
		PipelineStep steps[] = new PipelineStep[] { 
			new PacBioParseStep(),
			new ValidationStep(),
			new PacBioStoreStep()
		};
		
		PipelineState state = new PipelineState(steps, domain);
		state.set(MutationSubmission.class, submission);
		return state;
	}
}
