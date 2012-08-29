package ca.on.oicr.gps.util

import java.util.Comparator;

import ca.on.oicr.gps.model.data.ObservedMutation;

class ObservedMutationComparator implements Comparator<ObservedMutation> {
	
	int compare(ObservedMutation a, ObservedMutation b) {
		int result = a.knownMutation.gene.compareTo(b.knownMutation.gene)
		if (result == 0) {
			result = a.knownMutation.mutation.compareTo(b.knownMutation.mutation)	
		}
		if (result == 0) {
			result = a.runSample.process.panel.technology.compareTo(b.runSample.process.panel.technology)
		}
		return result;
	}
}
