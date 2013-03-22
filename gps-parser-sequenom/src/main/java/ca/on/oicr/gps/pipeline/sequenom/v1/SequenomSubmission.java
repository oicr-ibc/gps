package ca.on.oicr.gps.pipeline.sequenom.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.on.oicr.gps.pipeline.model.Mutations;
import ca.on.oicr.gps.pipeline.sequenom.v1.SequenomSubmissionRow.SampleInfo;

public class SequenomSubmission implements Mutations {

	private final List<SequenomSubmissionRow> rows = new ArrayList<SequenomSubmissionRow>();

	public SequenomSubmission() {
	}

	public void addRow(SequenomSubmissionRow row) {
		rows.add(row);
	}

	public List<SequenomSubmissionRow> getRows() {
		return Collections.unmodifiableList(rows);
	}

	public Map<SampleInfo, List<SequenomSubmissionRow>> bySample() {
		Map<SampleInfo, List<SequenomSubmissionRow>> sampleRows = new HashMap<SampleInfo, List<SequenomSubmissionRow>>();
		for (SequenomSubmissionRow row : rows) {
			SampleInfo sample = row.sampleInfo();
			List<SequenomSubmissionRow> l = sampleRows.get(sample);
			if (l == null) {
				l = new ArrayList<SequenomSubmissionRow>();
				sampleRows.put(sample, l);
			}
			l.add(row);
		}
		return sampleRows;
	}

}
