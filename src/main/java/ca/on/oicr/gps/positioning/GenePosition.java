package ca.on.oicr.gps.positioning;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenePosition {

	int start;
	int stop;
	String varAllele;
	
	Pattern patt = Pattern.compile("c\\.(\\d+)(?:_(\\d+))?(?:ins([ACGT]+)|del(\\d+|[ACGT]+)|([ACGT]+)>([ACGT]+))");
	
	public GenePosition(String position) throws RuntimeException {
		Matcher	matcher = patt.matcher(position); 
		if (matcher.matches()) {
			String start = matcher.group(1);
			String stop = matcher.group(2);
			String insertAllele = matcher.group(3);
			String deleteAllele = matcher.group(4);
			@SuppressWarnings("unused")
			String replaceFromAllele = matcher.group(5);
			String replaceToAllele = matcher.group(6);
			if (stop == null) {
				stop = start;
			}
			this.start = Integer.parseInt(start);
			this.stop = Integer.parseInt(stop);
			
			if (deleteAllele != null) {
				this.varAllele = "-";
			} else if (insertAllele != null) {
				this.varAllele = insertAllele;
			} else if (replaceToAllele != null) {
				this.varAllele = replaceToAllele;
			}
		} else {
			throw new RuntimeException("Failed to parse gene position: " + position);
		}
	}

	public int getStart() {
		return start;
	}

	public int getStop() {
		return stop;
	}

	public String getVarAllele() {
		return varAllele;
	}
}
