package ca.on.oicr.gps.pipeline.pacbio.v2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.on.oicr.gps.pipeline.PipelineStep;
import ca.on.oicr.gps.pipeline.model.MutationSubmission;
import ca.on.oicr.gps.pipeline.model.Mutations;
import ca.on.oicr.gps.pipeline.model.PipelineState;
import ca.on.oicr.gps.pipeline.step.parse.ExcelParsedRow;
import ca.on.oicr.gps.pipeline.step.parse.Parser;

class PacBioParseStep implements PipelineStep {

	private static final Logger log = LoggerFactory.getLogger(PacBioParseStep.class);

	public void execute(PipelineState state) {

		log.debug("Running pipeline step");

		MutationSubmission submission = state.get(MutationSubmission.class);
		try {
			Workbook wb = WorkbookFactory
					.create(submission.getSubmissionInputStream());
			if (wb.getNumberOfSheets() < 1) {
				PacBioPipeline.getLogger().warn("Not enough worksheets");
				state.error("data.invalid.file.format", "PacBio");
				return;
			}
			Sheet sheet = wb.getSheetAt(0);
			if (sheet.getRow(0) == null || sheet.getRow(1) == null) {
				PacBioPipeline.getLogger().warn("No header rows found");
				state.error("data.invalid.file.format", "PacBio");
				return;
			}
			Parser<PacBioSubmissionRow> parser = new Parser<PacBioSubmissionRow>(
					getRowValues(sheet.getRow(1)),
					PacBioSubmissionRow.class);

			int rowIndex = 2;
			PacBioSubmission pacBioSubmission = new PacBioSubmission();
			Row row = null;
			while ((row = sheet.getRow(rowIndex++)) != null) {
				Cell firstCell = row.getCell(0);
				if (firstCell == null || firstCell.getStringCellValue() == null || firstCell.getStringCellValue().isEmpty())
					continue;
				PacBioSubmissionRow seqRow = parser.parse(state,
						new PacBioSubmissionRow(), new ExcelParsedRow(row));
				PacBioPipeline.log.debug("Parsed {}", seqRow);
				pacBioSubmission.addRow(seqRow);
			}
			state.set(Mutations.class, pacBioSubmission);
		} catch (IllegalArgumentException e) {
			PacBioPipeline.getLogger().warn(e.getClass().getCanonicalName() + ": " + e.getMessage());
			e.printStackTrace();
			state.error("data.invalid.file.format", "PacBio");
			return;
		} catch (InvalidFormatException e) {
			PacBioPipeline.getLogger().warn(e.getClass().getCanonicalName() + ": " + e.getMessage());
			state.error("data.invalid.file.format", "PacBio");
			return;
		} catch (IOException e) {
			PacBioPipeline.getLogger().warn(e.getClass().getCanonicalName() + ": " + e.getMessage());
			state.error("data.unreadable.file", "PacBio");
			return;
		}

	}

	List<String> getRowValues(Row row) {
		List<String> values = new ArrayList<String>();
		for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
			String cellData = row.getCell(i).getStringCellValue().trim();
			values.add(cellData);
		}
		return values;
	}

}