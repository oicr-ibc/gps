package ca.on.oicr.gps.pipeline.sequenom.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.on.oicr.gps.pipeline.domain.DomainTarget;
import ca.on.oicr.gps.pipeline.domain.DomainFacade;
import ca.on.oicr.gps.pipeline.domain.DomainKnownMutation;
import ca.on.oicr.gps.pipeline.domain.DomainObservedMutation;
import ca.on.oicr.gps.pipeline.domain.DomainProcess;
import ca.on.oicr.gps.pipeline.domain.DomainRunSample;
import ca.on.oicr.gps.pipeline.PipelineStep;
import ca.on.oicr.gps.pipeline.model.Mutations;
import ca.on.oicr.gps.pipeline.model.PipelineException;
import ca.on.oicr.gps.pipeline.model.PipelineState;

public class SequenomStoreStep implements PipelineStep {
	
	private String resourceName = null;
	private List<SequenomMutation> mutationTable = new ArrayList<SequenomMutation>();
	
	Pattern p = Pattern.compile("^[ACGT]*$");
	
	class SequenomMutation {
		public SequenomMutation() {};
		String chromosome;
		int start;
		int stop;
		String refAllele;
		String varAllele;
		String name;
	}
	
	private static final Logger log = LoggerFactory.getLogger(SequenomStoreStep.class);
	
	/*
	 * A nested immutable class we can use as a map key. This determines the row uniqueness
	 * when merging data. 
	 */
	private final class SequenomAssociation implements Comparable<SequenomAssociation> {
		private final String runId;
		private final String patientId;
		private final String sampleId;
		public SequenomAssociation(String newRunId, String newPatientId, String newSampleId) {
			runId = newRunId;
			patientId = newPatientId;
			sampleId = newSampleId;
		}
		
		public String getRunId() {
			return runId;
		}

		public String getSampleId() {
			return sampleId;
		}

		public String getPatientId() {
			return patientId;
		}

		public int compareTo(SequenomAssociation arg0) {
			int diff = runId.compareTo(arg0.runId);
			if (diff != 0) {
				return diff;
			}
			diff = patientId.compareTo(arg0.patientId);
			if (diff != 0) {
				return diff;
			}
			return sampleId.compareTo(arg0.sampleId);
		}
	}

	public void execute(PipelineState state) {
		
		log.debug("Running pipeline step");

		DomainFacade domainFacade = state.getDomainFacade();
		
		SequenomSubmission mutations = (SequenomSubmission) state.get(Mutations.class);
		
		List<SequenomSubmissionRow> rowData = mutations.getRows();
		
		/*
		 * Sequenom needs a little more clunk handling. Each row may actually refer to
		 * the same sample, so we need to do some aggregation to merge together rows
		 * which refer to the same sample. When done, we can generate a mutation record
		 * per sample. 
		 */
		
		Map<SequenomAssociation, List<SequenomSubmissionRow>> table = new TreeMap<SequenomAssociation, List<SequenomSubmissionRow>>();
		Map<String, DomainProcess> processTable = new HashMap<String, DomainProcess>();
		
		for(SequenomSubmissionRow row : rowData) {
			
			String chipBarcode = row.getChipBarcode();
			String patientId = row.getPatientId();
			String sampleBarcode = row.getDnaSampleBarcode();
			String sequenomNum = row.getSequenomNum();
			
			String runId = sequenomNum + "-" + chipBarcode;
			
			SequenomAssociation assoc = new SequenomAssociation(runId, patientId, sampleBarcode);
			
			if (! table.containsKey(assoc)) {
				table.put(assoc, new ArrayList<SequenomSubmissionRow>());
			}
			
			table.get(assoc).add(row);
			
			try {
				if (! processTable.containsKey(runId)) {
					DomainProcess process = domainFacade.newProcess(runId, "OncoCarta", "1.0.0");
					process.setChipcode(chipBarcode);
					processTable.put(runId, process);
				}
			} catch (PipelineException e) {
				state.error(e.getError());
				continue;
			}
		}
		
		/*
		 * At this stage we have merged the mutations into a tree map, and can generate 
		 * records from each. Using a treemap means this will generally be sorted. The remaining
		 * issue is the handling of runs. A run may contain more or less anything, and is identified by
		 * the chip barcode. Two different samples from the same patient could well be part of the
		 * same run. Two samples from different patients could be, too. 
		 */
		
		for(SequenomAssociation assoc : table.keySet()) {
			List<SequenomSubmissionRow> rows = table.get(assoc);
			String runId = assoc.getRunId();
			DomainProcess process = processTable.get(runId);
			try {
				addSequenomData(state, process, assoc, rows);
			} catch (PipelineException e) {
				state.error(e.getError());
				continue;
			}
		}
		
		/*
		 * Now, assuming all is well, and only when all is well, persist all the runs. 
		 */
		
		if (! state.hasFailed()) {
			for(DomainProcess process : processTable.values()) {
				domainFacade.finishProcess(process);
			}
		}
	}

	private void addSequenomData(PipelineState state, DomainProcess process, SequenomAssociation assoc, List<SequenomSubmissionRow> rows) throws PipelineException {
		
		DomainFacade domainFacade = state.getDomainFacade();

		SequenomSubmissionRow first = rows.get(0);
		
		process.setDate(first.getRunDate());
		
		// The association is unique by run identifier, sample identifier, and patient identifier, so
		// we can handle all those settings outside the loop through the list of mutations. 
		String sampleId = assoc.getSampleId();
		String patientId = assoc.getPatientId();
		
		try {
			DomainRunSample runSample = domainFacade.newRunSample(process, patientId, sampleId);
			addSequenomMutationData(state, process, runSample, rows);
			
		} catch (PipelineException e) {
			state.error(e.getError());
		}
	}
	
	private void addSequenomMutationData(PipelineState state, DomainProcess process, DomainRunSample runSample, List<SequenomSubmissionRow> rows) throws PipelineException {
		// Now go through each row creating a mutation record and adding it to the
		// sequencing run. Maybe we shouldn't actually do this for all records, but
		// I am sure by now that you get the idea.
		
		DomainFacade domainFacade = state.getDomainFacade();

		for(SequenomSubmissionRow row : rows) {
			
			// Here we skip a blank/unknown row. This will leave a Run with YES mutations,
			// which is probably the best way of allowing for this to be collated
			// during reporting, as well as unknown assay results. We need to record 
			// the assays either way. 
			
			SequenomSubmissionRow.Status status = row.getStatus();
			if (! SequenomSubmissionRow.Status.YES.equals(status)) {
				continue;
			}
			
			// YES rows have a mutation and are therefore more specific
			Map<String, Object> targetCriteria = new HashMap<String, Object>();
			targetCriteria.put("gene", row.getGene());
			targetCriteria.put("mutation", row.getMutation());
			
			// If the allele consists entirely if ACGT, we can include it, otherwise
			// we should omit it from the target criteria. 
			String allele = row.getAllele();
			if (p.matcher(allele).matches()) {
				targetCriteria.put("varAllele", allele);
			}
			
			List<DomainTarget> targets = domainFacade.findTargets(process, targetCriteria);
			if (targets.size() == 0) {
				throw new PipelineException("data.missing.target", targetCriteria.toString());
			} else if (targets.size() > 1) {
				throw new PipelineException("data.ambiguous.target", targetCriteria.toString());
			}
			
			DomainTarget target = targets.get(0);
			Map<String, Object> criteria = new HashMap<String, Object>();
			criteria.put("gene", row.getGene());
			criteria.put("mutation", row.getMutation());
			criteria.put("chromosome", target.getChromosome());
			criteria.put("start", target.getStart());
			criteria.put("stop", target.getStop());
			criteria.put("refAllele", target.getRefAllele());
			criteria.put("varAllele", target.getVarAllele());
			
			DomainKnownMutation known = domainFacade.findKnownMutation(criteria);
			if (known == null) {
				state.error("data.unknown.mutation", criteria);
				continue;
			}
			
			DomainObservedMutation mut = domainFacade.newObservedMutation(runSample, known);
			mut.setFrequency(row.getFreq());
			
			// This is about all we get from Sequenom for an observed mutation
			if (row.getStatus().equals(SequenomSubmissionRow.Status.YES)) {
				mut.setStatus(DomainObservedMutation.MUTATION_STATUS_FOUND);
			}
			
			// And be careful here too, this way we don't worry about what the encoding might
			// be. 
			String confidence = row.getConfidence();
			if (confidence == null) {
				// Don't set anything
			} else if (row.getConfidence().equalsIgnoreCase("High")) {
				mut.setConfidence(DomainObservedMutation.MUTATION_CONFIDENCE_HIGH);
			}
		}
	}
}
