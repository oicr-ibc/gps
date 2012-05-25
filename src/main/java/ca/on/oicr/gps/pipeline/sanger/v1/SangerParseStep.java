package ca.on.oicr.gps.pipeline.sanger.v1;

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

class SangerParseStep implements PipelineStep {

	private static final Logger log = LoggerFactory.getLogger(SangerParseStep.class);
	
	public void execute(PipelineState state) {

		log.debug("Running pipeline step");

		MutationSubmission submission = state.get(MutationSubmission.class);
		
		try {
			Workbook wb = WorkbookFactory
					.create(submission.getSubmissionInputStream());
			if (wb.getNumberOfSheets() < 1) {
				SangerPipeline.getLogger().warn("Not enough worksheets");
				state.error("data.invalid.file.format", "ABI");
				return;
			}
			Sheet sheet = wb.getSheetAt(0);
			if (sheet.getRow(0) == null || sheet.getRow(1) == null) {
				SangerPipeline.getLogger().warn("No header rows found");
				state.error("data.invalid.file.format", "ABI");
				return;
			}
			Parser<SangerSubmissionRow> parser = new Parser<SangerSubmissionRow>(
					getRowValues(sheet.getRow(1)),
					SangerSubmissionRow.class);

			int rowIndex = 2;
			SangerSubmission sangerSubmission = new SangerSubmission();
			Row row = null;
			while ((row = sheet.getRow(rowIndex++)) != null) {
				Cell firstCell = row.getCell(0);
				if (firstCell == null || firstCell.getStringCellValue() == null || firstCell.getStringCellValue().isEmpty())
					continue;
				SangerSubmissionRow seqRow = parser.parse(state,
						new SangerSubmissionRow(), new ExcelParsedRow(row));
				SangerPipeline.log.debug("Parsed {}", seqRow);
				sangerSubmission.addRow(seqRow);
			}
			state.set(Mutations.class, sangerSubmission);
		} catch (IllegalArgumentException e) {
			SangerPipeline.getLogger().warn(e.getClass().getCanonicalName() + ": " + e.getMessage());
			state.error("data.invalid.file.format", "ABI");
			return;
		} catch (InvalidFormatException e) {
			SangerPipeline.getLogger().warn(e.getClass().getCanonicalName() + ": " + e.getMessage());
			state.error("data.invalid.file.format", "ABI");
			return;
		} catch (IOException e) {
			SangerPipeline.getLogger().warn(e.getClass().getCanonicalName() + ": " + e.getMessage());
			state.error("data.unreadable.file", "ABI");
			return;
		}

	}

	// Modified by Stuart to add a .trim() - based on the fact that trailing and leading spaces
	// in cells are never significant
	List<String> getRowValues(Row row) {
		List<String> values = new ArrayList<String>();
		for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
			String cellData = row.getCell(i).getStringCellValue().trim();
			values.add(cellData);
		}
		return values;
	}
}