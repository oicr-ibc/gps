package ca.on.oicr.gps.pipeline.pacbio.v1;

import org.apache.commons.lang.builder.ToStringBuilder;

public class PacBioMutationRow {

	public enum MutationType {
		SNV, SNP, INS, DEL
	}

	private String assayID;
	private String gene;
	private String chr;
	private Integer start;
	private Integer stop;
	private MutationType type;
	private String refAll;
	private String allele;
	private Float vrf;
	private Integer depth;
	private String varAa;

	public String getAssayID() {
		return assayID;
	}

	public String getGene() {
		return gene;
	}

	public String getChr() {
		return chr;
	}

	public Integer getStart() {
		return start;
	}

	public Integer getStop() {
		return stop;
	}

	public MutationType getType() {
		return type;
	}

	public String getRefAll() {
		return refAll;
	}

	public String getAllele() {
		return allele;
	}

	public Float getVrf() {
		return vrf;
	}

	public Integer getDepth() {
		return depth;
	}

	public String getVarAa() {
		return varAa;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
