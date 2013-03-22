package ca.on.oicr.gps.pipeline.hotspot.v1;

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

class HotSpotParseStep implements PipelineStep {

	private static final Logger log = LoggerFactory.getLogger(HotSpotParseStep.class);

	public void execute(PipelineState state) {

		log.debug("Running pipeline step");

		MutationSubmission submission = state.get(MutationSubmission.class);
		try {
			Workbook wb = WorkbookFactory
					.create(submission.getSubmissionInputStream());
			if (wb.getNumberOfSheets() < 1) {
				HotSpotPipeline.getLogger().warn("Not enough worksheets");
				state.error("data.invalid.file.format", "HotSpot");
				return;
			}
			Sheet sheet = wb.getSheetAt(0);
			if (sheet.getRow(0) == null || sheet.getRow(1) == null) {
				HotSpotPipeline.getLogger().warn("No header rows found");
				state.error("data.invalid.file.format", "HotSpot");
				return;
			}
			Parser<HotSpotSubmissionRow> parser = new Parser<HotSpotSubmissionRow>(
					getRowValues(sheet.getRow(1)),
					HotSpotSubmissionRow.class);

			int rowIndex = 2;
			HotSpotSubmission hotSpotSubmission = new HotSpotSubmission();
			Row row = null;
			while ((row = sheet.getRow(rowIndex++)) != null) {
				Cell firstCell = row.getCell(0);
				if (firstCell == null || firstCell.getStringCellValue() == null || firstCell.getStringCellValue().isEmpty())
					continue;
				HotSpotSubmissionRow seqRow = parser.parse(state,
						new HotSpotSubmissionRow(), new ExcelParsedRow(row));
				HotSpotPipeline.log.debug("Parsed {}", seqRow);
				hotSpotSubmission.addRow(seqRow);
			}
			state.set(Mutations.class, hotSpotSubmission);
		} catch (IllegalArgumentException e) {
			HotSpotPipeline.getLogger().warn(e.getClass().getCanonicalName() + ": " + e.getMessage());
			e.printStackTrace();
			state.error("data.invalid.file.format", "HotSpot");
			return;
		} catch (InvalidFormatException e) {
			HotSpotPipeline.getLogger().warn(e.getClass().getCanonicalName() + ": " + e.getMessage());
			state.error("data.invalid.file.format", "HotSpot");
			return;
		} catch (IOException e) {
			HotSpotPipeline.getLogger().warn(e.getClass().getCanonicalName() + ": " + e.getMessage());
			state.error("data.unreadable.file", "HotSpot");
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