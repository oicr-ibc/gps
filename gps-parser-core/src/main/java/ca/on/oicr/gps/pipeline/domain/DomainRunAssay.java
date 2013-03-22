package ca.on.oicr.gps.pipeline.domain;

public interface DomainRunAssay {
	public static final String STATUS_YES = "YES";
	public static final String STATUS_NO = "NO";
	public static final String STATUS_FAIL = "FAIL";
	
	public void setStatus(String status);
}
