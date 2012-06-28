package ca.on.oicr.gps.pipeline.sanger.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.on.oicr.gps.pipeline.domain.DomainTarget;
import ca.on.oicr.gps.pipeline.domain.DomainFacade;
import ca.on.oicr.gps.pipeline.domain.DomainKnownMutation;
import ca.on.oicr.gps.pipeline.domain.DomainObservedMutation;
import ca.on.oicr.gps.pipeline.domain.DomainProcess;
import ca.on.oicr.gps.pipeline.domain.DomainRunAssay;
import ca.on.oicr.gps.pipeline.domain.DomainRunSample;
import ca.on.oicr.gps.pipeline.PipelineStep;
import ca.on.oicr.gps.pipeline.model.Mutations;
import ca.on.oicr.gps.pipeline.model.PipelineException;
import ca.on.oicr.gps.pipeline.model.PipelineState;
import ca.on.oicr.gps.pipeline.sanger.v1.SangerSubmissionRow.Status;
import ca.on.oicr.gps.positioning.GenePositionLocator;
import ca.on.oicr.gps.positioning.GeneReference;

public class SangerStoreStep implements PipelineStep {

	private static final Logger log = LoggerFactory.getLogger(SangerStoreStep.class);
	
	public SangerStoreStep() {
		super();
	}
	
	/*
	 * A nested immutable class we can use as a map key. This determines the row uniqueness
	 * when merging data. 
	 */
	private final class SangerAssociation implements Comparable<SangerAssociation> {
		private final String runId;
		private final String patientId;
		private final String sampleId;
		public SangerAssociation(String newRunId, String newPatientId, String newSampleId) {
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

		public int compareTo(SangerAssociation arg0) {
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
		
		SangerSubmission mutations = (SangerSubmission) state.get(Mutations.class);
		
		List<SangerSubmissionRow> rowData = mutations.getRows();
		
		DomainFacade domainFacade = state.getDomainFacade();
		
		/*
		 * Sequenom needs a little more clunk handling. Each row may actually refer to
		 * the same sample, so we need to do some aggregation to merge together rows
		 * which refer to the same sample. When done, we can generate a mutation record
		 * per sample. 
		 */
		
		Map<SangerAssociation, List<SangerSubmissionRow>> table = new TreeMap<SangerAssociation, List<SangerSubmissionRow>>();
		Map<String, DomainProcess> processTable = new HashMap<String, DomainProcess>();
		
		for(SangerSubmissionRow row : rowData) {
			
			String runId = row.getDnaSampleBarcode();
			String patientId = row.getPatientId();
			String sampleBarcode = row.getDnaSampleBarcode();
			String seqNum = row.getSeqNum();
			
			SangerAssociation assoc = new SangerAssociation(runId, patientId, sampleBarcode);
			
			if (! table.containsKey(assoc)) {
				table.put(assoc, new ArrayList<SangerSubmissionRow>());
			}
			
			table.get(assoc).add(row);
			
			try {
				if (! processTable.containsKey(runId)) {
					DomainProcess process = domainFacade.newProcess(runId, "OncoCarta Sanger", "1.0.0");
					process.setChipcode(seqNum);
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
		
		for(SangerAssociation assoc : table.keySet()) {
			List<SangerSubmissionRow> rows = table.get(assoc);
			String runId = assoc.getRunId();
			DomainProcess process = processTable.get(runId);
			try {
				addSangerData(state, process, assoc, rows);
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

	private void addSangerData(PipelineState state, DomainProcess process, SangerAssociation assoc, List<SangerSubmissionRow> rows) throws PipelineException {
		
		DomainFacade domainFacade = state.getDomainFacade();
		
		SangerSubmissionRow first = rows.get(0);
		
		process.setDate(first.getRunDate());
		
		// The association is unique by run identifier, sample identifier, and patient identifier, so
		// we can handle all those settings outside the loop through the list of mutations. 
		String sampleId = assoc.getSampleId();
		String patientId = assoc.getPatientId();
		
		try {
			DomainRunSample runSample = domainFacade.newRunSample(process, patientId, sampleId);
			addSangerMutationData(state, process, runSample, rows);
		} catch (PipelineException e) {
			state.error(e.getError());
		}
	}
	
	private void addSangerMutationData(PipelineState state, DomainProcess process, DomainRunSample runSample, List<SangerSubmissionRow> rows) throws PipelineException {		
		// Now go through each row creating a mutation record and adding it to the
		// sequencing run. Maybe we shouldn't actually do this for all records, but
		// I am sure by now that you get the idea.
		
		DomainFacade domainFacade = state.getDomainFacade();

		for(SangerSubmissionRow row : rows) {
			
			// Here we skip a blank row. This will leave a Run with no mutations,
			// which is probably the best way of allowing for this to be collated
			// during reporting. 
			
			Status status = row.getStatus();
			if (status == null || status == Status.NO) {
				continue;
			}
			
			DomainKnownMutation found = null;

			String gene = row.getGene();
			String mutation = row.getAaMutation();
			if (mutation.startsWith("p.")) {
				mutation = mutation.substring(2);
			}
			
			Map<String, Object> criteria = new HashMap<String, Object>();
			criteria.put("gene", gene);
			criteria.put("mutation", mutation);
			
			String nbciReference = row.getNcbiReference();
			String cdnaMutation = row.getCdnaMutation();
			cdnaMutation = cdnaMutation.replace(" ", "");
			
			GenePositionLocator loc = new GenePositionLocator();
			GeneReference ref = new GeneReference(nbciReference, cdnaMutation);
			List<GeneReference> refs = new ArrayList<GeneReference>();
			refs.add(ref);
			loc.translateReference(refs);
			
			criteria.put("start", Integer.valueOf(ref.getStart()));
			criteria.put("stop", Integer.valueOf(ref.getStop()));
			criteria.put("chromosome", ref.getChromosome());
			criteria.put("varAllele", ref.getVarAllele());
			
			if (row.getStatus().equals(SangerSubmissionRow.Status.YES)) {
				found = domainFacade.findKnownMutation(criteria);
				if (found == null) {
					found = domainFacade.newKnownMutation(criteria);
				}
			}
			
			if (row.getStatus().equals(SangerSubmissionRow.Status.YES)) {
				assert (found != null);
				DomainObservedMutation mut = domainFacade.newObservedMutation(runSample, found);
				mut.setStatus(DomainObservedMutation.MUTATION_STATUS_FOUND);
				mut.setFrequency(row.getFreq());
			}
		}
	}
	
}
