package ca.on.oicr.gps.pipeline.domain;

public interface DomainTarget {
	public String getChromosome();
	public String getGene();
	public int getStart();
	public int getStop();
	public String getRefAllele();
	public String getVarAllele();
	public String getMutation();
}
