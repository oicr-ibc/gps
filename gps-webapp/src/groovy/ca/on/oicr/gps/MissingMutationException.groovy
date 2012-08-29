package ca.on.oicr.gps

/**
 * Used to signal a mutation that cannot be found. This is only required when handling 
 * a mutation that needs to be reported as missing. 
 * 
 * @author swatt
 */

class MissingMutationException extends RuntimeException {
	private String label
	
	MissingMutationException(String label) {
		this.label = label
	}
	
	String getMessage() {
		return "Missing mutation: " + label
	}
}
