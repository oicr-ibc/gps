package ca.on.oicr.gps.pipeline.step.parse;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ca.on.oicr.gps.pipeline.model.PipelineState;

import com.google.common.base.Joiner;

abstract class FieldParser {

	protected final Field field;

	protected final String columnName;

	protected final int columnIndex;

	public static FieldParser forField(List<String> header, Field field) {
		Class<?> fieldType = field.getType();
		if (fieldType.isEnum()) {
			return new EnumFieldParser(header, field);
		} else if (Number.class.isAssignableFrom(fieldType)) {
			return new ValueOfFieldParser(header, field);
		} else if (String.class.isAssignableFrom(fieldType)) {
			return new FieldParser(header, field) {

				@Override
				protected Object valueOf(PipelineState state, ParsedRow.ColumnType type, Object value) {
					return value.toString();
				}

			};
		} else if (Date.class.isAssignableFrom(fieldType)) {
			return new DateFieldParser(header, field);
		} else if(Collection.class.isAssignableFrom(fieldType)) {
			return null;
		}
		throw new IllegalArgumentException("no fieldparser for type "
				+ fieldType);
	}

	protected FieldParser(List<String> header, Field field) {
		this.field = field;
		this.field.setAccessible(true);
		this.columnIndex = inferColumnIndexFromField(header);
		this.columnName = header.get(this.columnIndex);
	}

	public void parse(PipelineState state, Object object, ParsedRow row) {
		// row may not have data for each column

		ParsedRow.ColumnType type = row.getNativeType(columnIndex);
		Object nativeValue = row.getColumnValue(columnIndex);
		Object value = nativeValue != null ? valueOf(state, type, nativeValue)
				: null;
		try {
			field.set(object, value);
		} catch (IllegalArgumentException e) {
			state.error("data.invalid.value", row.getLineNumber(), columnName, nativeValue);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract Object valueOf(PipelineState state, ParsedRow.ColumnType type,
			Object value);

	private int inferColumnIndexFromField(List<String> header) {
		for (int i = 0; i < header.size(); i++) {
			String column = header.get(i);
			String fieldName = field.getName();
			if (fieldName.equalsIgnoreCase(column)) {
				return i;
			} else if (unCamelCase(fieldName, '_').equalsIgnoreCase(column)) {
				return i;
			} else if (unCamelCase(fieldName, ' ').equalsIgnoreCase(column)) {
				return i;
			}
		}
		throw new IllegalArgumentException("cannot infer column for field "
				+ field.getName() + " from " + Joiner.on(' ').join(header));
	}

	private String unCamelCase(String name, char sep) {
		StringCharacterIterator sci = new StringCharacterIterator(name);
		StringBuilder sb = new StringBuilder();
		for (char c = sci.first(); c != CharacterIterator.DONE; c = sci.next()) {
			if (Character.isUpperCase(c)) {
				sb.append(sep);
			}
			sb.append(Character.toUpperCase(c));
		}
		return sb.toString();
	}

	private static class EnumFieldParser extends FieldParser {
		Enum<? extends Enum<?>> enums[];

		@SuppressWarnings("unchecked")
		EnumFieldParser(List<String> header, Field field) {
			super(header, field);
			this.enums = ((Class<? extends Enum<?>>) field.getType()).getEnumConstants();
		}

		@Override
		protected Object valueOf(PipelineState state, ParsedRow.ColumnType type,
				Object value) {
			switch (type) {
			case STRING:
				for (Enum<? extends Enum<?>> e : enums) {
					if (e.name().equalsIgnoreCase(value.toString())) {
						return e;
					}
				}
				break;
			}
			state.error("data.invalid.value.enum", columnName, value.toString(), Arrays.toString(enums));
			return null;
		}
	}

	private static class DateFieldParser extends FieldParser {
		private static final DateFormat ISO_8601 = new SimpleDateFormat(
				"yyyy-MM-dd");

		DateFieldParser(List<String> header, Field field) {
			super(header, field);
		}

		@Override
		protected Object valueOf(PipelineState state, ParsedRow.ColumnType type,
				Object value) {
			switch (type) {
			case STRING:
				try {
					return ISO_8601.parseObject(value.toString());
				} catch (ParseException e) {
					// fall through
				}
				break;
			case DATE:
				return value;
			}
			state.error("data.invalid.date", columnName, value);
			return null;

		}

	}

	private static class ValueOfFieldParser extends FieldParser {

		private final Method valueOf;

		ValueOfFieldParser(List<String> header, Field field) {
			super(header, field);
			try {
				this.valueOf = field.getType().getMethod("valueOf",
						String.class);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected Object valueOf(PipelineState state, ParsedRow.ColumnType type,
				Object value) {
			switch(type) {
			case NUMBER:
				if(field.getType().equals(Integer.class)) {
					return new Integer(((Number)value).intValue());
				}
				if(field.getType().equals(Float.class)) {
					return new Float(((Number)value).floatValue());
				}
				if(field.getType().equals(Double.class)) {
					return new Double(((Number)value).doubleValue());
				}
				if(field.getType().equals(Long.class)) {
					return new Long(((Number)value).longValue());
				}
			case STRING:
				try {
					return this.valueOf.invoke(null, value.toString());
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					state.error("data.invalid.value", columnName, value.toString());
					return null;
				}
			}
			state.error("data.invalid.value", columnName, value);
			return null;
		}
	}
}