package ca.on.oicr.gps.pipeline.step.validate;

import ca.on.oicr.gps.pipeline.model.Mutations;
import ca.on.oicr.gps.pipeline.model.PipelineState;

public interface MutationsValidationRule {

	public void validate(PipelineState validation, Mutations mutations);

}
