package ca.on.oicr.gps.pipeline;

import ca.on.oicr.gps.pipeline.model.PipelineState;

public interface PipelineStep {

	public void execute(PipelineState state);

}
