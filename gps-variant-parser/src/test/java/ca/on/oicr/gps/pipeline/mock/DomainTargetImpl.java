package ca.on.oicr.gps.pipeline.mock;

import ca.on.oicr.gps.pipeline.domain.DomainTarget;

public class DomainTargetImpl implements DomainTarget {
	
	String chromosome = null;
	String gene = null;
	int start;
	int stop;
	String refAllele = null;
	String varAllele = null;
	String mutation = null;
	
	public DomainTargetImpl(String newChromosome, String newGene, int newStart, int newStop, 
			                String newRefAllele, String newVarAllele, String newMutation) {
		chromosome = newChromosome;
		gene = newGene;
		start = newStart;
		stop = newStop;
		refAllele = newRefAllele;
		varAllele = newVarAllele;
		mutation = newMutation;		
	}

	public String getChromosome() {
		return chromosome;
	}

	public String getGene() {
		return gene;
	}

	public int getStart() {
		return start;
	}

	public int getStop() {
		return stop;
	}

	public String getRefAllele() {
		return refAllele;
	}

	public String getVarAllele() {
		return varAllele;
	}

	public String getMutation() {
		return mutation;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("chromosome=");
		builder.append(chromosome);
		builder.append(";");
		builder.append("gene=");
		builder.append(gene);
		builder.append(";");
		builder.append("start=");
		builder.append(start);
		builder.append(";");
		builder.append("stop=");
		builder.append(stop);
		builder.append(";");
		builder.append("refAllele=");
		builder.append(refAllele);
		builder.append(";");
		builder.append("varAllele=");
		builder.append(varAllele);
		builder.append(";");
		builder.append("mutation=");
		builder.append(mutation);
		return builder.toString();
	}
}
