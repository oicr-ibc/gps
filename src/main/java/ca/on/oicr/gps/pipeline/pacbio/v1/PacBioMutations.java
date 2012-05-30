package ca.on.oicr.gps.pipeline.pacbio.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import ca.on.oicr.gps.pipeline.model.Mutations;

public class PacBioMutations implements Mutations {

	private String patientId;
	private String sampleId;
	private String sequencingRunId;
	private String assayPanelId;
	private Date date;

	private final List<PacBioMutationRow> rows = new ArrayList<PacBioMutationRow>();

	public PacBioMutations() {
	}

	public String getPatientId() {
		return patientId;
	}

	public String getSampleId() {
		return sampleId;
	}

	public String getSequencingRunId() {
		return sequencingRunId;
	}

	public String getAssayPanelId() {
		return assayPanelId;
	}

	public Date getDate() {
		return date;
	}

	public void addRow(PacBioMutationRow row) {
		rows.add(row);
	}

	public List<PacBioMutationRow> getRows() {
		return Collections.unmodifiableList(rows);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
