package ca.on.oicr.gps.pipeline.step.validate;

import java.util.ArrayList;
import java.util.List;

import ca.on.oicr.gps.pipeline.PipelineStep;
import ca.on.oicr.gps.pipeline.model.Mutations;
import ca.on.oicr.gps.pipeline.model.PipelineState;

public class ValidationStep implements PipelineStep {

	private final List<MutationsValidationRule> rules = new ArrayList<MutationsValidationRule>();

	public ValidationStep(MutationsValidationRule... rules) {
		for (MutationsValidationRule rule : rules) {
			this.rules.add(rule);
		}
	}

	public ValidationStep() {

	}

	public ValidationStep addRule(MutationsValidationRule rule) {
		this.rules.add(rule);
		return this;
	}

	public void execute(PipelineState state) {
		validate(state, state.get(Mutations.class));
	}

	protected void validate(PipelineState state, Mutations submission) {
		if (submission != null) {
			for (MutationsValidationRule rule : rules) {
				rule.validate(state, submission);
			}
		}
	}

}
