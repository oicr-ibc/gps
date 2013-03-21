package ca.on.oicr.gps.pipeline.model;

public class PipelineRuntimeException extends RuntimeException {

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = -7577845904977029348L;

	private PipelineState state = null;
	
	public PipelineRuntimeException(PipelineState thisState) {
		super();
		state = thisState;
	}
	
	public PipelineState getState() {
		return state;
	}
}
