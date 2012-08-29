package ca.on.oicr.gps

import java.text.DateFormat;
import java.util.Locale;

class LocaleFormattedDateTagLib {

	def dateFormat = { attrs, body ->
		
		if (attrs.date) {
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
			out << df.format(attrs.date as Date)
		}
	}

	def dateAndTimeFormat = { attrs, body ->
		
		if (attrs.date) {
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			out << df.format(attrs.date as Date)
		}
	}
}
