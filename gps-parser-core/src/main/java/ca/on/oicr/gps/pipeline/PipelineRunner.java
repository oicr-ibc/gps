package ca.on.oicr.gps.pipeline;

import ca.on.oicr.gps.pipeline.model.PipelineState;

public class PipelineRunner implements Runnable {
	
	private final PipelineState state;

	public PipelineRunner(PipelineState state) {
		this.state = state;
	}

	public void run() {
		while (state.canContinue()) {
			state.next();
		}
	}
}
