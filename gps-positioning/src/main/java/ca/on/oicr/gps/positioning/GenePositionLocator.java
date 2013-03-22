package ca.on.oicr.gps.positioning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;

import au.com.bytecode.opencsv.CSVReader;

public class GenePositionLocator {
	
	private static final Logger log = LoggerFactory.getLogger(GenePositionLocator.class);
	
	private Pattern patt = Pattern.compile("\\.\\d+$");
	
	private String getUsableNcbiReference(GeneReference ref) {
		String reference = ref.getNcbiReference();
		Matcher match = patt.matcher(reference);
		return match.replaceFirst("");
	}
	
	private Map<String, List<GeneReference>> buildReferenceTable(List<GeneReference> references) {
		Map<String, List<GeneReference>> result = new HashMap<String, List<GeneReference>>();
		for(GeneReference ref : references) {
			String ncbiReference = getUsableNcbiReference(ref);
			if (! result.containsKey(ncbiReference)) {
				result.put(ncbiReference, new ArrayList<GeneReference>());
			}
			result.get(ncbiReference).add(ref);
		}
		return result;
	}
	
	private int convertPosition(int position, String direction, int codingStart, int codingStop, List<Integer> starts, List<Integer> stops) {
		int length = starts.size();
		if (direction.equals("+")) {
			for(int i = 0; i < length; i++) {
				int a = starts.get(i).intValue();
				int b = stops.get(i).intValue();
				if (b < codingStart) {
		        	continue;
		        } else if (a < codingStart) {
		        	a = codingStart;
		        }
				int exonLength = b - a;
				if (position > exonLength) {
		        	position -= exonLength;
		        } else {
		        	return a + position;
		        }
			}
		} else if (direction.equals("-")) {
			for(int i = length - 1; i >= 0; i--) {
	            int a = starts.get(i).intValue();
	            int b = stops.get(i).intValue();
	            if (a > codingStop) {
	                continue;
	            } else if (b > codingStop) {
	                b = codingStop;
	            }
	            int exonLength = b - a;
	            
	            if (position > exonLength) {
	                position -= exonLength;
	            } else {
	                return b - position + 1;
	            }
			}
		}
		return -1;
	}
	
	private void translateReference(GeneReference ref, String chromosome, String direction, int codingStart, int codingStop, List<Integer> starts, List<Integer> stops) {
		GenePosition pos = new GenePosition(ref.getMutationCds());
		int genomeStart = convertPosition(pos.getStart(), direction, codingStart, codingStop, starts, stops);
		int genomeStop = (pos.getStart() == pos.getStop()) 
				         ? genomeStart
				         : convertPosition(pos.getStop(), direction, codingStart, codingStop, starts, stops);
		if (genomeStart > genomeStop) {
			int temp = genomeStop;
			genomeStop = genomeStart;
			genomeStart = temp;
		}
		
		ref.setStart(Integer.valueOf(genomeStart));
		ref.setStop(Integer.valueOf(genomeStop));
		ref.setChromosome(chromosome);
		ref.setVarAllele(pos.getVarAllele());
	}
	
	private void translateReferences(List<GeneReference> references, String chromosome, String direction, int codingStart, int codingStop, List<Integer> starts, List<Integer> stops) {
		for(GeneReference ref : references) {
			translateReference(ref, chromosome, direction, codingStart, codingStop, starts, stops);
		}
	}
	
	public void translateReference(List<GeneReference> references) {
		
		Map<String, List<GeneReference>> refTable = buildReferenceTable(references);
		
		String [] nextLine;
		
		try {
			InputStream refGene = GenePositionLocator.class.getResourceAsStream("/refGene.txt.gz");
			GZIPInputStream gzippedStream = new GZIPInputStream(refGene);
			Reader bufferedReader = new BufferedReader(new InputStreamReader(gzippedStream));

			CSVReader reader = new CSVReader(bufferedReader, '\t');

			while ((nextLine = reader.readNext()) != null) {
		    	
		    	String reference = nextLine[1];

		    	if (! refTable.containsKey(reference)) {
		    		continue;
		    	}
		    	
		    	String chromosome = nextLine[2];
		    	String direction = nextLine[3];
		    	@SuppressWarnings("unused")
				String start1 = nextLine[4];
		    	@SuppressWarnings("unused")
				String stop1 = nextLine[5];
		    	String codingStart = nextLine[6];
		    	String codingStop = nextLine[7];
		    	@SuppressWarnings("unused")
				String exonCount = nextLine[8];
		    	String exonStarts = nextLine[9];
		    	String exonStops = nextLine[10];
		    	
		    	@SuppressWarnings("unused")
				String gene = nextLine[12];
		    	
		    	if (chromosome.startsWith("chr")) {
		    		chromosome = chromosome.substring(3);
		    	}
		    	
		    	Iterable<String> starts = Splitter.on(',').omitEmptyStrings().split(exonStarts);
		    	Iterator<String> stops = Splitter.on(',').omitEmptyStrings().split(exonStops).iterator();
		    	List<Integer> listStarts = new ArrayList<Integer>();
		    	List<Integer> listStops = new ArrayList<Integer>();
		    	for(String start : starts) {
		    		String stop = stops.next();
		    		if (start == null || stop == null) {
		    			break;
		    		}
		    		listStarts.add(Ints.tryParse(start));
		    		listStops.add(Ints.tryParse(stop));
		    	}
		    	
		    	translateReferences(refTable.get(reference), chromosome, direction, Integer.parseInt(codingStart), Integer.parseInt(codingStop), listStarts, listStops);
		    }
			
			reader.close();
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace(System.err);
		}
	}

}
