package ca.on.oicr.gps.pipeline.hotspot.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.on.oicr.gps.pipeline.model.Mutations;
import ca.on.oicr.gps.pipeline.hotspot.v1.HotSpotSubmissionRow.SampleInfo;

public class HotSpotSubmission implements Mutations {

	private final List<HotSpotSubmissionRow> rows = new ArrayList<HotSpotSubmissionRow>();

	public HotSpotSubmission() {
	}

	public void addRow(HotSpotSubmissionRow row) {
		rows.add(row);
	}

	public List<HotSpotSubmissionRow> getRows() {
		return Collections.unmodifiableList(rows);
	}

	public Map<SampleInfo, List<HotSpotSubmissionRow>> bySample() {
		Map<SampleInfo, List<HotSpotSubmissionRow>> sampleRows = new HashMap<SampleInfo, List<HotSpotSubmissionRow>>();
		for (HotSpotSubmissionRow row : rows) {
			SampleInfo sample = row.sampleInfo();
			List<HotSpotSubmissionRow> l = sampleRows.get(sample);
			if (l == null) {
				l = new ArrayList<HotSpotSubmissionRow>();
				sampleRows.put(sample, l);
			}
			l.add(row);
		}
		return sampleRows;
	}

}
