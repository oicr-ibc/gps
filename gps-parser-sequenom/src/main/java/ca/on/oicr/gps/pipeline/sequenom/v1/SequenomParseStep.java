package ca.on.oicr.gps.pipeline.sequenom.v1;

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

class SequenomParseStep implements PipelineStep {

	private static final Logger log = LoggerFactory.getLogger(SequenomParseStep.class);

	public void execute(PipelineState state) {

		log.debug("Running pipeline step");

		MutationSubmission submission = state.get(MutationSubmission.class);
		try {
			Workbook wb = WorkbookFactory
					.create(submission.getSubmissionInputStream());
			if (wb.getNumberOfSheets() < 1) {
				SequenomPipeline.getLogger().warn("Not enough worksheets");
				state.error("data.invalid.file.format", "Sequenom");
				return;
			}
			Sheet sheet = wb.getSheetAt(0);
			if (sheet.getRow(0) == null || sheet.getRow(1) == null) {
				SequenomPipeline.getLogger().warn("No header rows found");
				state.error("data.invalid.file.format", "Sequenom");
				return;
			}
			Parser<SequenomSubmissionRow> parser = new Parser<SequenomSubmissionRow>(
					getRowValues(sheet.getRow(1)),
					SequenomSubmissionRow.class);

			int rowIndex = 2;
			SequenomSubmission sequenomSubmission = new SequenomSubmission();
			Row row = null;
			while ((row = sheet.getRow(rowIndex++)) != null) {
				Cell firstCell = row.getCell(0);
				if (firstCell == null || firstCell.getStringCellValue() == null || firstCell.getStringCellValue().isEmpty())
					continue;
				SequenomSubmissionRow seqRow = parser.parse(state,
						new SequenomSubmissionRow(), new ExcelParsedRow(row));
				SequenomPipeline.log.debug("Parsed {}", seqRow);
				sequenomSubmission.addRow(seqRow);
			}
			state.set(Mutations.class, sequenomSubmission);
		} catch (IllegalArgumentException e) {
			SequenomPipeline.getLogger().warn(e.getClass().getCanonicalName() + ": " + e.getMessage());
			e.printStackTrace();
			state.error("data.invalid.file.format", "Sequenom");
			return;
		} catch (InvalidFormatException e) {
			SequenomPipeline.getLogger().warn(e.getClass().getCanonicalName() + ": " + e.getMessage());
			state.error("data.invalid.file.format", "Sequenom");
			return;
		} catch (IOException e) {
			SequenomPipeline.getLogger().warn(e.getClass().getCanonicalName() + ": " + e.getMessage());
			state.error("data.unreadable.file", "Sequenom");
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