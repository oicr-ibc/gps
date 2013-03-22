package ca.on.oicr.gps.pipeline.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Data structure representing an individual pipeline error. The code and the arguments
 * are in a form where they can be rendered in the front end. 
 * 
 * @author swatt
 */

public class PipelineError {

	private final String key;
	private final List<Object> args;

	/**
	 * Constructor for this immutable object
	 * @param key
	 * @param args
	 */
	public PipelineError(String key, Object... args) {
		this.key = key;
		if (args != null) {
			this.args = Arrays.asList(args);
		} else {
			this.args = Collections.emptyList();
		}
	}

	/**
	 * Retrieves the error code, a string
	 * @return
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Retrieves a list of arguments
	 * @return
	 */
	public List<Object> getArgs() {
		return args;
	}
	
	/**
	 * Stringify the error, which is very handy for lazy coders who get errors
	 * out of their pipelines.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName());
		builder.append(": ");
		builder.append(key);
		builder.append(": ");
		boolean doneOne = false;
		for(Object arg : args) {
			if (doneOne) {
				builder.append(", ");
			}
			builder.append(arg.toString());
			doneOne = true;
		}
		return builder.toString();
	}
}
