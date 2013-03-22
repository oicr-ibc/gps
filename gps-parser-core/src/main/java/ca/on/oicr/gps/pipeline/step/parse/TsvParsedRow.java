package ca.on.oicr.gps.pipeline.step.parse;

import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public class TsvParsedRow implements ParsedRow {
	
	public static final char TAB = '\t';

	private static final Splitter splitter = Splitter.on(TAB).trimResults();

	private final int lineNumber;
	private final List<String> line;

	public TsvParsedRow(int lineNumber, String line) {
		this.lineNumber = lineNumber;
		
		ImmutableList.Builder<String> data = ImmutableList.builder();
		for(String value : splitter.split(line)) {
			data.add(value.trim());
		}
		
		this.line = data.build();
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public ColumnType getNativeType(int index) {
		return ColumnType.STRING;
	}

	public Object getColumnValue(int index) {
		return line.get(index);
	}
}