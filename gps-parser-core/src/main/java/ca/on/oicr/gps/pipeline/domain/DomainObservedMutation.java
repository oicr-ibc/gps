package ca.on.oicr.gps.pipeline.domain;

public interface DomainObservedMutation {
	public static int MUTATION_STATUS_FOUND = 1;
	public static String MUTATION_CONFIDENCE_HIGH = "HIGH";
	public void setFrequency(Float value);
	public void setStatus(int value);
	public void setConfidence(String value);
}
