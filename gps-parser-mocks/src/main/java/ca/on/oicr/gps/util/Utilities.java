package ca.on.oicr.gps.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

public class Utilities {
	
	public static String criteriaAsString(Map<String, Object> criteria) {
		StringBuilder result = new StringBuilder();

		SortedMap<String, Object> data = new TreeMap<String, Object>();
		data.putAll(criteria);
		for(Entry<String, Object> e : data.entrySet()) {
			if (result.length() != 0) {
				result.append(';');
			}
			result.append(e.getKey());
			result.append('=');
			result.append(e.getValue());
		}

		return result.toString();
	}
}
