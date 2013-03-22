package ca.on.oicr.gps.pipeline.pacbio.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.on.oicr.gps.pipeline.model.Mutations;
import ca.on.oicr.gps.pipeline.pacbio.v2.PacBioSubmissionRow.SampleInfo;

public class PacBioSubmission implements Mutations {

	private final List<PacBioSubmissionRow> rows = new ArrayList<PacBioSubmissionRow>();

	public PacBioSubmission() {
	}

	public void addRow(PacBioSubmissionRow row) {
		rows.add(row);
	}

	public List<PacBioSubmissionRow> getRows() {
		return Collections.unmodifiableList(rows);
	}

	public Map<SampleInfo, List<PacBioSubmissionRow>> bySample() {
		Map<SampleInfo, List<PacBioSubmissionRow>> sampleRows = new HashMap<SampleInfo, List<PacBioSubmissionRow>>();
		for (PacBioSubmissionRow row : rows) {
			SampleInfo sample = row.sampleInfo();
			List<PacBioSubmissionRow> l = sampleRows.get(sample);
			if (l == null) {
				l = new ArrayList<PacBioSubmissionRow>();
				sampleRows.put(sample, l);
			}
			l.add(row);
		}
		return sampleRows;
	}

}
