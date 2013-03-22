package ca.on.oicr.gps.pipeline.sanger.v1;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.base.Objects;

public class SangerSubmissionRow {

	public enum Gender {
		Female, Male
	}

	public enum Type {
		FFPE, Frozen, Blood, FNA
	}

	public enum Status {
		NO, YES, UNKNOWN, FAILED
	}

	private String patientId;
	private Gender gender;
	private String dnaSampleBarcode;
	private Type type;

	private Date runDate;

	private Status mutationStatus;
	private String ncbiReference;
	private String gene;
	private String cdnaMutation;
	private String aaMutation;
	private String seqNum;
	private Float freq;
	private String concordantWSequenom;

	public String getPatientId() {
		return patientId;
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

	public Date getRunDate() {
		return runDate;
	}

	public Status getStatus() {
		return mutationStatus;
	}

	public String getGene() {
		return gene;
	}

	public Float getFreq() {
		return freq;
	}

	public String getNcbiReference() {
		return ncbiReference;
	}

	public String getCdnaMutation() {
		return cdnaMutation;
	}

	public String getAaMutation() {
		return aaMutation;
	}

	public String getSeqNum() {
		return seqNum;
	}

	public String getConcordantWSequenom() {
		return concordantWSequenom;
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
					.append(dnaSampleBarcode, rhs.getDnaSampleBarcode())
					.isEquals();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(patientId, dnaSampleBarcode);
		}
	}
}
