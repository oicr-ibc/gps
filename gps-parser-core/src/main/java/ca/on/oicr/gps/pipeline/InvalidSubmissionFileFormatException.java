package ca.on.oicr.gps.pipeline;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class InvalidSubmissionFileFormatException extends RuntimeException {

	private static final long serialVersionUID = -6533193318401523599L;

	public InvalidSubmissionFileFormatException(String msg, InvalidFormatException e) {
		super(msg,e);
	}

	public InvalidSubmissionFileFormatException(String msg) {
		super(msg);
	}

}
