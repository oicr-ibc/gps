package ca.on.oicr.gps.pipeline.sequenom.v1;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.base.Objects;

public class SequenomSubmissionRow {

	public enum Gender {
		Female, Male
	}

	public enum Type {
		FFPE, Frozen, Blood, FNA
	}

	public enum Status {
		YES, UNKNOWN
	}

	private String patientId;
	private Gender gender;
	private String sequenomNum;
	private String dnaSampleBarcode;
	private Type type;

	private String panelScreened;
	private Date runDate;
	private String chipBarcode;
	private Integer yes;
	private Integer no;
	private Integer unknown;

	private Status mutationStatus;
	private String gene;
	private String assay;
	private String mutation;
	private String allele;
	private Float freq;
	private String confidence;

	public String getPatientId() {
		return patientId;
	}

	public String getSequenomNum() {
		return sequenomNum;
	}

	public String getDnaSampleBarcode() {
		return dnaSampleBarcode;
	}

	public Gender getGender() {
		return gender;
	}

	public Type getType() {
		return type;
	}

	public String getChipBarcode() {
		return chipBarcode;
	}

	public String getPanelScreened() {
		return panelScreened;
	}

	public Date getRunDate() {
		return runDate;
	}

	public Integer getYes() {
		return yes;
	}

	public Integer getNo() {
		return no;
	}

	public Integer getUnknown() {
		return unknown;
	}

	public Status getStatus() {
		return mutationStatus;
	}

	public String getGene() {
		return gene;
	}

	public String getAssay() {
		return assay;
	}

	public String getMutation() {
		return mutation;
	}

	public String getAllele() {
		return allele;
	}

	public Float getFreq() {
		return freq;
	}

	public String getConfidence() {
		return confidence;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public SampleInfo sampleInfo() {
		return new SampleInfo();
	}

	public AssayInfo assayInfo() {
		return new AssayInfo();
	}

	public final class SampleInfo {

		private SampleInfo() {

		}

		public String getPatientId() {
			return patientId;
		}

		public String getSequenomNum() {
			return sequenomNum;
		}

		public String getDnaSampleBarcode() {
			return dnaSampleBarcode;
		}

		public Gender getGender() {
			return gender;
		}

		public Type getType() {
			return type;
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
					.append(sequenomNum, rhs.getSequenomNum())
					.append(dnaSampleBarcode, rhs.getDnaSampleBarcode())
					.isEquals();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(patientId, sequenomNum, dnaSampleBarcode);
		}
	}

	public final class AssayInfo {

		public String getChipBarcode() {
			return chipBarcode;
		}

		public String getPanelScreened() {
			return panelScreened;
		}

		public Date getRunDate() {
			return runDate;
		}

		public Integer getYes() {
			return yes;
		}

		public Integer getNo() {
			return no;
		}

		public Integer getUnknown() {
			return unknown;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj instanceof AssayInfo == false) {
				return super.equals(obj);
			}
			AssayInfo rhs = (AssayInfo) obj;
			return new EqualsBuilder()
					.append(chipBarcode, rhs.getChipBarcode()).isEquals();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(chipBarcode);
		}
	}
}
