package ca.on.oicr.gps.pipeline.step.parse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

public class ExcelParsedRow implements ParsedRow {

	private final Row row;

	public ExcelParsedRow(Row row) {
		this.row = row;
	}

	public int getLineNumber() {
		return row.getRowNum();
	}

	public ColumnType getNativeType(int index) {
		Cell cell = row.getCell(index);
		if (cell != null) {
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_BLANK:
				return ColumnType.EMPTY;
			case Cell.CELL_TYPE_BOOLEAN:
			case Cell.CELL_TYPE_ERROR:
			case Cell.CELL_TYPE_FORMULA:
			case Cell.CELL_TYPE_STRING:
				return ColumnType.STRING;
			case Cell.CELL_TYPE_NUMERIC:
				return DateUtil.isCellDateFormatted(cell) ? ColumnType.DATE
						: ColumnType.NUMBER;
			}
		}
		return ColumnType.STRING;
	}

	public Object getColumnValue(int index) {
		Cell cell = row.getCell(index);
		if (cell != null) {
			switch (getNativeType(index)) {
			case EMPTY:
				return null;
			case STRING:
				return cell.getStringCellValue().trim();
			case NUMBER:
				return cell.getNumericCellValue();
			case DATE:
				return cell.getDateCellValue();
			}
		}
		return null;
	}

}
