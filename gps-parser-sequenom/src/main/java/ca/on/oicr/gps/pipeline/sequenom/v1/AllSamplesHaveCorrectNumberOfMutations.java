package ca.on.oicr.gps.pipeline.sequenom.v1;

import java.util.List;
import java.util.Map;

import ca.on.oicr.gps.pipeline.model.Mutations;
import ca.on.oicr.gps.pipeline.model.PipelineState;
import ca.on.oicr.gps.pipeline.sequenom.v1.SequenomSubmission;
import ca.on.oicr.gps.pipeline.sequenom.v1.SequenomSubmissionRow;
import ca.on.oicr.gps.pipeline.sequenom.v1.SequenomSubmissionRow.AssayInfo;
import ca.on.oicr.gps.pipeline.sequenom.v1.SequenomSubmissionRow.SampleInfo;
import ca.on.oicr.gps.pipeline.sequenom.v1.SequenomSubmissionRow.Status;
import ca.on.oicr.gps.pipeline.step.validate.MutationsValidationRule;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Validates that each sample has the correct number of mutations (assayInfo.yes
 * + assayInfo.unknown)
 */
public class AllSamplesHaveCorrectNumberOfMutations implements
		MutationsValidationRule {

	public void validate(PipelineState validation, Mutations submission) {
		SequenomSubmission sequenom = (SequenomSubmission) submission;

		for (Map.Entry<SampleInfo, List<SequenomSubmissionRow>> e : sequenom
				.bySample().entrySet()) {
			SampleInfo sample = e.getKey();
			List<SequenomSubmissionRow> rows = e.getValue();
			AssayInfo assay = rows.get(0).assayInfo();
			if(assay.getYes() != count(Status.YES, rows) || assay.getUnknown() != count(Status.UNKNOWN, rows)) {
				validation.error("data.invalid.mutation.count", sample.getSequenomNum());
			}
		}
	}
	
	private int count(final SequenomSubmissionRow.Status status, Iterable<SequenomSubmissionRow> rows) {
		return Iterables.size(Iterables.filter(rows, new Predicate<SequenomSubmissionRow>() {

			public boolean apply(SequenomSubmissionRow input) {
				return input.getStatus() != null && input.getStatus() == status;
			}
			
		}));
	}
}
