package ca.on.oicr.gps.positioning;

public class GeneReference {
	
	String ncbiReference = null;
	String mutationCds = null;
	String chromosome = null;
	String varAllele = null;
	int start;
	int stop;
	
	public GeneReference(String ncbiReference, String mutationCds) {
		this.ncbiReference = ncbiReference;
		this.mutationCds = mutationCds;
	}
	public String getNcbiReference() {
		return ncbiReference;
	}

	public void setNcbiReference(String ncbiReference) {
		this.ncbiReference = ncbiReference;
	}

	public String getMutationCds() {
		return mutationCds;
	}

	public void setMutationCds(String mutationCds) {
		this.mutationCds = mutationCds;
	}

	public String getChromosome() {
		return chromosome;
	}

	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getStop() {
		return stop;
	}

	public void setStop(int stop) {
		this.stop = stop;
	}
	
	public String getVarAllele() {
		return varAllele;
	}
	
	public void setVarAllele(String varAllele) {
		this.varAllele = varAllele;
	}
}