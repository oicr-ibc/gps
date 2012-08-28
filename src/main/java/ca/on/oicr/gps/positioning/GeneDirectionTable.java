package ca.on.oicr.gps.positioning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

public class GeneDirectionTable {
	
	private static final Logger log = LoggerFactory.getLogger(GeneDirectionTable.class);
	
	private static volatile GeneDirectionTable instance = null;
	
	public static GeneDirectionTable getInstance() {
        if (instance == null) {
        	synchronized (GeneDirectionTable.class) {
        		if (instance == null) {
        			instance = new GeneDirectionTable();
        		}
        	}
        }
        return instance;
	}

	private final Map<String, String> refTable = new HashMap<String, String>();
	
	private static Pattern patt = Pattern.compile("\\.\\d+$");
	
	private GeneDirectionTable() {
		buildDirectionTable();
	}
	
	public String getDirection(String refGene) {
		return refTable.get(refGene);
	}
	
	public void buildDirectionTable() {
		
		log.debug("Loading gene direction table");
		InputStream refGene = GeneDirectionTable.class.getResourceAsStream("/refGene.txt");
		Reader bufferedReader = new BufferedReader(new InputStreamReader(refGene));

		CSVReader reader = new CSVReader(bufferedReader, '\t');
		String [] nextLine;
		
		try {
		    while ((nextLine = reader.readNext()) != null) {
		    	
		    	String reference = nextLine[1];
				Matcher match = patt.matcher(reference);
				reference = match.replaceFirst("");

		    	if (refTable.containsKey(reference)) {
		    		continue;
		    	}
		    	
		    	String direction = nextLine[3];
		    	refTable.put(reference, direction); 
		    }
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
