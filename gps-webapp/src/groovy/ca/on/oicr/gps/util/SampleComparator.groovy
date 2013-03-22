package ca.on.oicr.gps.util

import java.util.Comparator;

import ca.on.oicr.gps.model.data.Sample;

class SampleComparator implements Comparator<Sample> {
	
	int compare(Sample a, Sample b) {
		return a.barcode.compareTo(b.barcode)
	}
}
