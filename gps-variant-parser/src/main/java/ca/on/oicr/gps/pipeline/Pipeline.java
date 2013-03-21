package ca.on.oicr.gps.pipeline;

import ca.on.oicr.gps.pipeline.domain.DomainFacade;
import ca.on.oicr.gps.pipeline.model.MutationSubmission;
import ca.on.oicr.gps.pipeline.model.PipelineState;

public interface Pipeline {

	public String getTypeKey();

	public PipelineState newState(MutationSubmission submission, DomainFacade domain);

	public boolean canHandleSubmission(MutationSubmission submission);
}
