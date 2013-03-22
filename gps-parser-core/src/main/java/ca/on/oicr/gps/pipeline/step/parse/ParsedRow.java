package ca.on.oicr.gps.pipeline.step.parse;


public interface ParsedRow {

	public enum ColumnType {
		NUMBER, DATE, STRING, EMPTY
	}
	
	public int getLineNumber();

	public ColumnType getNativeType(int index);
	
	public Object getColumnValue(int index);

}
