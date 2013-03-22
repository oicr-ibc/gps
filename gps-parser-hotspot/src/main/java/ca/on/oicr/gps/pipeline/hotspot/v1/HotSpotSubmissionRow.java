package ca.on.oicr.gps.pipeline.hotspot.v1;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.base.Objects;

public class HotSpotSubmissionRow {

	private String patientId;
	private String sequencingRun;
	private String dnaSampleBarcode;

	private String panelScreened;
	private Date runDate;

	private String chromosome;
	private String gene;
	private Integer start;
	private Integer stop;
	private String rsId;
	private String isoform;
	
	private String varAa;
	private String refAllele;
	private String allele;
	private Float vrf;
	private Integer depth;

	public String getSequencingRun() {
		return sequencingRun;
	}

	public String getChromosome() {
		return chromosome;
	}

	public Integer getStart() {
		return start;
	}

	public Integer getStop() {
		return stop;
	}

	public String getVarAa() {
		return varAa;
	}

	public String getRefAllele() {
		return refAllele;
	}

	public Float getVrf() {
		return vrf;
	}

	public Integer getDepth() {
		return depth;
	}

	public String getPatientId() {
		return patientId;
	}

	public String getDnaSampleBarcode() {
		return dnaSampleBarcode;
	}

	public String getPanelScreened() {
		return panelScreened;
	}

	public Date getRunDate() {
		return runDate;
	}

	public String getGene() {
		return gene;
	}

	public String getAllele() {
		return allele;
	}

	public String getRsId() {
		return rsId;
	}

	public String getIsoform() {
		return isoform;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public SampleInfo sampleInfo() {
		return new SampleInfo();
	}

	public final class SampleInfo {

		private SampleInfo() {

		}

		public String getPatientId() {
			return patientId;
		}
		
		public String getSequencingRun() {
			return sequencingRun;
		}

		public String getDnaSampleBarcode() {
			return dnaSampleBarcode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj instanceof SampleInfo == false) {
				return super.equals(obj);
			}
			SampleInfo rhs = (SampleInfo) obj;
			return new EqualsBuilder().append(patientId, rhs.getPatientId())
					.append(sequencingRun, rhs.getSequencingRun())
					.append(dnaSampleBarcode, rhs.getDnaSampleBarcode())
					.isEquals();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(patientId, sequencingRun, dnaSampleBarcode);
		}
	}
}
