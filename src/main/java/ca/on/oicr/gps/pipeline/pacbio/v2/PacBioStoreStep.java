package ca.on.oicr.gps.pipeline.pacbio.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
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

public class PacBioStoreStep implements PipelineStep {

	private static final Logger log = LoggerFactory.getLogger(PacBioStoreStep.class);
	
	private static final Pattern p = Pattern.compile("OncoCarta PacBio v(\\d+)[._-](\\d+)");

	/*
	 * A nested immutable class we can use as a map key. This determines the row uniqueness
	 * when merging data. 
	 */
	private final class PacBioAssociation implements Comparable<PacBioAssociation> {
		private final String runId;
		private final String patientId;
		private final String sampleId;
		public PacBioAssociation(String newRunId, String newPatientId, String newSampleId) {
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

		public int compareTo(PacBioAssociation arg0) {
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
		
		PacBioSubmission mutations = (PacBioSubmission) state.get(Mutations.class);
		
		List<PacBioSubmissionRow> rowData = mutations.getRows();
		
		/*
		 * PacBio needs a little more clunk handling. Each row may actually refer to
		 * the same sample, so we need to do some aggregation to merge together rows
		 * which refer to the same sample. When done, we can generate a mutation record
		 * per sample. 
		 */
		
		String panelName = "OncoCarta PacBio";
		String panelVersion = "1.0.0";

		Map<PacBioAssociation, List<PacBioSubmissionRow>> table = new TreeMap<PacBioAssociation, List<PacBioSubmissionRow>>();
		Map<String, DomainProcess> processTable = new HashMap<String, DomainProcess>();
		
		for(PacBioSubmissionRow row : rowData) {
			
			String patientId = row.getPatientId();
			String sampleBarcode = row.getDnaSampleBarcode();
			String sequencingRun = row.getSequencingRun();
			
			String panelHeaderName = row.getPanelScreened();
			panelHeaderName = panelHeaderName.trim();
			
			Matcher m = p.matcher(panelHeaderName);
			if (m.matches()) {
				String majorVersion = m.group(1);
				String minorVersion = m.group(2);
				panelVersion = majorVersion + "." + minorVersion + "." + "0";
			} else {
				state.error("data.invalid.panel", panelHeaderName);
				return;
			}

			String runId = sequencingRun;
			
			PacBioAssociation assoc = new PacBioAssociation(runId, patientId, sampleBarcode);
			
			if (! table.containsKey(assoc)) {
				table.put(assoc, new ArrayList<PacBioSubmissionRow>());
			}
			
			table.get(assoc).add(row);
			
			try {
				log.debug("Checking run ID: " + runId);
				log.debug("Checked: " + processTable.containsKey(runId));
				if (! processTable.containsKey(runId)) {
					log.debug("Adding run ID: " + runId);
					DomainProcess process = domainFacade.newProcess(runId, panelName, panelVersion);
					processTable.put(runId, process);
				}
			} catch (PipelineException e) {
				log.debug("Error: "  + e.getError().getKey() + ", " + e.getError().getArgs());
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
		
		for(PacBioAssociation assoc : table.keySet()) {
			List<PacBioSubmissionRow> rows = table.get(assoc);
			String runId = assoc.getRunId();
			DomainProcess process = processTable.get(runId);
			
			if (process == null) {
				// Something bad happened, but we should have already reported it. 
				continue;
			}
			
			log.debug("Run ID: " + runId);
			log.debug("Process: " + process);
			log.debug("Rows: " + rows);
			try {
				addPacBioData(state, process, assoc, rows);
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

	private void addPacBioData(PipelineState state, DomainProcess process, PacBioAssociation assoc, List<PacBioSubmissionRow> rows) throws PipelineException {
		
		DomainFacade domainFacade = state.getDomainFacade();

		PacBioSubmissionRow first = rows.get(0);
		log.debug("First row: " + first);
		
		process.setDate(first.getRunDate());
		
		// The association is unique by run identifier, sample identifier, and patient identifier, so
		// we can handle all those settings outside the loop through the list of mutations. 
		String sampleId = assoc.getSampleId();
		String patientId = assoc.getPatientId();
		
		try {
			DomainRunSample runSample = domainFacade.newRunSample(process, patientId, sampleId);
			addPacBioMutationData(state, process, runSample, rows);
			
		} catch (PipelineException e) {
			state.error(e.getError());
		}
	}
	
	private void addPacBioMutationData(PipelineState state, DomainProcess process, DomainRunSample runSample, List<PacBioSubmissionRow> rows) throws PipelineException {
		// Now go through each row creating a mutation record and adding it to the
		// sequencing run. Maybe we shouldn't actually do this for all records, but
		// I am sure by now that you get the idea.
		
		DomainFacade domainFacade = state.getDomainFacade();

		for(PacBioSubmissionRow row : rows) {
			
			// Here we skip a blank/unknown row. This will leave a Run with YES mutations,
			// which is probably the best way of allowing for this to be collated
			// during reporting. 
			
			if (row.getGene() == null || "".equals(row.getGene())) {
				continue;
			}
			
			// YES rows have a mutation and are therefore more specific
			Map<String, Object> targetCriteria = new HashMap<String, Object>();
			targetCriteria.put("chromosome", row.getChromosome().replace("chr", ""));
			targetCriteria.put("start", row.getStart());
			targetCriteria.put("stop", row.getStop());
			
			// Purely for validation, check that we can find a target for the given
			// row. We don't create an association with the target, but basically shout
			// if the panel didn't include the given target. 
			List<DomainTarget> targets = domainFacade.findTargets(process, targetCriteria);
			if (targets.size() < 1) {
				throw new PipelineException("data.missing.target", targetCriteria.toString());
			}
			
			Map<String, Object> criteria = getKnownMutationCriteria(row);
			Map<String, Object> searchCriteria = new HashMap<String, Object>(criteria);
			
			// Remove criteria values that we consider unreliable. In the case of PacBio they 
			// ought to be reliable, but they actually aren't. Yet. 
			//searchCriteria.remove("start");
			//searchCriteria.remove("stop");
			//searchCriteria.remove("refAllele");
			//searchCriteria.remove("varAllele");
			searchCriteria.remove("gene");
			searchCriteria.remove("mutation");
			
			DomainKnownMutation known = domainFacade.findKnownMutation(searchCriteria);
			if (known == null) {
				known = domainFacade.newKnownMutation(criteria);
			}
			
			DomainObservedMutation mut = domainFacade.newObservedMutation(runSample, known);
			mut.setFrequency(row.getVrf());
			
			mut.setStatus(DomainObservedMutation.MUTATION_STATUS_FOUND);
			mut.setConfidence(DomainObservedMutation.MUTATION_CONFIDENCE_HIGH);
		}
	}
	
	private Map<String, Object> getKnownMutationCriteria(PacBioSubmissionRow row) throws PipelineException {
		Map<String, Object> criteria = new HashMap<String, Object>();
		criteria.put("gene", row.getGene());
		criteria.put("mutation", row.getVarAa());
		criteria.put("start", row.getStart());
		criteria.put("stop", row.getStop());
		criteria.put("refAllele", row.getRefAllele());
		criteria.put("varAllele", row.getAllele());
		criteria.put("chromosome", row.getChromosome().replace("chr", ""));

		return criteria;
	}
}
