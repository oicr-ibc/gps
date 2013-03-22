package ca.on.oicr.gps.pipeline.sanger.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.on.oicr.gps.pipeline.model.Mutations;
import ca.on.oicr.gps.pipeline.sanger.v1.SangerSubmissionRow.SampleInfo;

public class SangerSubmission implements Mutations {

	private final List<SangerSubmissionRow> rows = new ArrayList<SangerSubmissionRow>();

	public SangerSubmission() {
	}

	public void addRow(SangerSubmissionRow row) {
		rows.add(row);
	}

	public List<SangerSubmissionRow> getRows() {
		return Collections.unmodifiableList(rows);
	}

	public Map<SampleInfo, List<SangerSubmissionRow>> bySample() {
		Map<SampleInfo, List<SangerSubmissionRow>> sampleRows = new HashMap<SampleInfo, List<SangerSubmissionRow>>();
		for (SangerSubmissionRow row : rows) {
			SampleInfo sample = row.sampleInfo();
			List<SangerSubmissionRow> l = sampleRows.get(sample);
			if (l == null) {
				l = new ArrayList<SangerSubmissionRow>();
				sampleRows.put(sample, l);
			}
			l.add(row);
		}
		return sampleRows;
	}

}
