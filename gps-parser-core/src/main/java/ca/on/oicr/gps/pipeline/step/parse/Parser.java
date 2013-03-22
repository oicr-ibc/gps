package ca.on.oicr.gps.pipeline.step.parse;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ca.on.oicr.gps.pipeline.model.PipelineState;

public final class Parser<T> {

	private final List<FieldParser> fieldParsers = new ArrayList<FieldParser>();

	public Parser(List<String> header, Class<T> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			FieldParser fieldParser = FieldParser.forField(header, field);
			if (fieldParser != null) {
				fieldParsers.add(fieldParser);
			}
		}
	}

	public T parse(PipelineState state, T instance, ParsedRow row) {
		for (FieldParser parser : fieldParsers) {
			parser.parse(state, instance, row);
		}
		return instance;
	}

}