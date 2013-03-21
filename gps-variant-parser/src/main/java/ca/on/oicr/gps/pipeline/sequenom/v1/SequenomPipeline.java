package ca.on.oicr.gps.pipeline.sequenom.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.on.oicr.gps.pipeline.BasePipeline;
import ca.on.oicr.gps.pipeline.Pipeline;
import ca.on.oicr.gps.pipeline.PipelineStep;
import ca.on.oicr.gps.pipeline.domain.DomainFacade;
import ca.on.oicr.gps.pipeline.model.MutationSubmission;
import ca.on.oicr.gps.pipeline.model.PipelineState;
import ca.on.oicr.gps.pipeline.step.validate.ValidationStep;

public class SequenomPipeline extends BasePipeline implements Pipeline {

	static final Logger log = LoggerFactory.getLogger(SequenomPipeline.class);

	public String getTypeKey() {
		return "Sequenom";
	}

	public PipelineState newState(MutationSubmission submission, DomainFacade domain) {
		PipelineStep steps[] = new PipelineStep[] { 
			new SequenomParseStep(),
			new ValidationStep(new AllSamplesHaveCorrectNumberOfMutations()),
			new SequenomStoreStep()
		};

		PipelineState state = new PipelineState(steps, domain);
		state.set(MutationSubmission.class, submission);
		return state;
	}

	public static Logger getLogger() {
		return log;
	}
}
