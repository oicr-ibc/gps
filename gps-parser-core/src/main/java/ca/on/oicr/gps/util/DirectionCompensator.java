package ca.on.oicr.gps.util;

/**
 * Represents the logic which flips base pairs dependent possibly on the strand
 * direction, without having to bundle it with strand direction awareness. 
 * 
 * @author swatt
 */

public class DirectionCompensator {
	
	private static final String table = "ACGT";
	
	private static final String outcome = "TGCA";
	
	/**
	 * Flips base pairs to the alternate based on the opposing strand direction. 
	 * The base pairs are also reversed. 
	 * 
	 * @param allele
	 * @return
	 */
	public static String compensate(String allele) {
		int length = allele.length();
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < length; i++) {
			char ch = allele.charAt(i);
			int index = table.indexOf(ch);
			if (index != -1) {
				result.append(outcome.charAt(index));
			} else {
				result.append(ch);
			}
		}
		return result.reverse().toString();
	}
	
	/**
	 * Possibly flips and reverses base pairs to the alternate based on the opposing 
	 * strand direction, with a boolean parameter to indicate when to do this. 
	 * 
	 * @param allele
	 * @param maybe
	 * @return
	 */
	public static String compensate(String allele, boolean maybe) {
		if (maybe) {
			return compensate(allele);
		} else {
			return allele;
		}
	}
}
