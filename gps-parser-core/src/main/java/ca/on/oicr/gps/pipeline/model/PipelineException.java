package ca.on.oicr.gps.pipeline.model;

public class PipelineException extends Exception {
	private static final long serialVersionUID = -7643585095361495825L;
	
	PipelineError error;
	
	public PipelineException(PipelineError thisError) {
		super();
		error = thisError;
	}

	public PipelineException(String key, Object... args) {
		super();
		error = new PipelineError(key, args);
	}
	
	public PipelineError getError() {
		return error;
	}
}
