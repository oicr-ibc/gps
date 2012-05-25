package ca.on.oicr.gps.pipeline.pacbio.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.on.oicr.gps.pipeline.domain.DomainAssay;
import ca.on.oicr.gps.pipeline.domain.DomainFacade;
import ca.on.oicr.gps.pipeline.domain.DomainKnownMutation;
import ca.on.oicr.gps.pipeline.domain.DomainObservedMutation;
import ca.on.oicr.gps.pipeline.domain.DomainProcess;
import ca.on.oicr.gps.pipeline.domain.DomainRunAssay;
import ca.on.oicr.gps.pipeline.domain.DomainRunSample;
import ca.on.oicr.gps.pipeline.PipelineStep;
import ca.on.oicr.gps.pipeline.model.Mutations;
import ca.on.oicr.gps.pipeline.model.PipelineError;
import ca.on.oicr.gps.pipeline.model.PipelineException;
import ca.on.oicr.gps.pipeline.model.PipelineState;

/**
 * This class implements the last step in the PacBio pipeline, and writes information
 * into the domain model. This is done by injecting a DomainFacade into the step, which
 * can be used during the execution of this step to write components into the persistence
 * layers in Grails. As usual, errors can be reported. As a final step, this should not
 * actually store anything if there are any errors. 
 * 
 * @author swatt
 */
public class PacBioStoreStep implements PipelineStep {
	
	private static final Logger log = LoggerFactory.getLogger(PacBioStoreStep.class);
	
	private static final Pattern p = Pattern.compile("Oncocarta_PacBio_Panel_(\\d+)[._-](\\d+).txt");
	
	public void execute(PipelineState state) {
		/*
		 * At this stage, the pipeline will have validated the PacBio information, and
		 * we are ready to write into the domain model information. This will depend 
		 * substantially on the parsed data. 
		 */
		
		log.debug("Running pipeline step");

		String panelName = "OncoCarta PacBio";
		String panelVersion = "1.0.0";
		
		DomainFacade domainFacade = state.getDomainFacade();
		
		DomainProcess process = null;
				
		PacBioMutations mutations = (PacBioMutations) state.get(Mutations.class);
		String panelHeaderName = mutations.getAssayPanelId();
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
		
		log.debug("Got mutation data");
		
		String sampleId = mutations.getSampleId();
		String patientId = mutations.getPatientId();
		
		try {
			process = domainFacade.newProcess(mutations.getSequencingRunId(), panelName, panelVersion);
		} catch (PipelineException e) {
			state.error(e.getError());
		}
		
		if (process == null) {
			state.error("data.missing.result");
		}
		
		if (state.hasFailed()) {
			return;
		}
		
		process.setDate(mutations.getDate());
		
		// And again, we may have a similar issue of bundling tests against assays. We need to
		// identify the pool of assays, and associate each row with an assay. This requires us
		// to find the assay for each row.
		
		Map<DomainAssay, List<PacBioMutationRow>> assayRows = getAssayRows(state, process, mutations.getRows());
		
		try {
			DomainRunSample runSample = domainFacade.newRunSample(process, patientId, sampleId);
			
			for(DomainAssay assay : assayRows.keySet()) {
				DomainRunAssay runAssay = domainFacade.newRunAssay(runSample, assay);
				
				// PacBio only reports successful runs
				runAssay.setStatus(DomainRunAssay.STATUS_YES);
				
				// Now add the mutations
				addMutations(state, runAssay, assayRows.get(assay));
			}
			
		} catch (PipelineException e) {
			state.error(e.getError());
		}

		// If all is well, we can simple save the whole bundle of data out to
		// the DB by persisting it. This might fail. But it shouldn't
		
		if (! state.hasFailed()) {
			domainFacade.saveProcess(process);
		}
	}
	
	private Map<DomainAssay, List<PacBioMutationRow>> getAssayRows(PipelineState state, DomainProcess process, List<PacBioMutationRow> mutationRows) {

		DomainFacade domainFacade = state.getDomainFacade();

		Map<DomainAssay, List<PacBioMutationRow>> assayRows = new HashMap<DomainAssay, List<PacBioMutationRow>>();
		
		for(PacBioMutationRow row : mutationRows) {
			Map<String, Object> assayCriteria = new HashMap<String, Object>();
			String chromosomeString = row.getChr();
			chromosomeString = chromosomeString.replace("chr", "");
			assayCriteria.put("chromosome", Integer.parseInt(chromosomeString));
			assayCriteria.put("gene", row.getGene());
			assayCriteria.put("start", row.getStart());
			assayCriteria.put("stop", row.getStop());
			List<DomainAssay> assays = domainFacade.findAssays(process, assayCriteria);
			DomainAssay assay = null;
			if (assays.size() == 1) {
				assay = assays.get(0);
			} else if (assays.size() == 0){
				state.error(new PipelineError("data.assay.not.found", assayCriteria));
				continue;
			} else {
				
				// With PacBio, it is very possible to find more than one assay matching. This is OK,
				// so long as the name of one of the matches the row. If this is the case, use that 
				// assay. Otherwise, we error:
				
				for(DomainAssay maybe : assays) {
					if (maybe.getName().equalsIgnoreCase(row.getAssayID())) {
						assay = maybe;
					}
				}
				
				if (assay == null) {
					state.error(new PipelineError("data.assay.not.unique", assayCriteria));
					continue;
				}
			}
			
			if (! assayRows.containsKey(assay)) {
				assayRows.put(assay, new ArrayList<PacBioMutationRow>());
			}
			assayRows.get(assay).add(row);
		}

		return assayRows;
	}
	
	/**
	 * Adds a set of mutations, read from mutations, into the passed run. 
	 * @param process the current sequencing run
	 * @param mutations the passed mutations
	 * @throws PipelineException
	 */
	private void addMutations(PipelineState state, DomainRunAssay runAssay, List<PacBioMutationRow> mutationRows) throws PipelineException {
		
		DomainFacade domainFacade = state.getDomainFacade();

		for(PacBioMutationRow row : mutationRows) {
			Map<String, Object> criteria = new HashMap<String, Object>();
			
			String chromosomeString = row.getChr();
			try {
				chromosomeString = chromosomeString.replace("chr", "");
				criteria.put("chromosome", Integer.parseInt(chromosomeString));
			} catch (NumberFormatException err) {
				throw new PipelineException("data.invalid.chromosome", chromosomeString);
			}
			
			criteria.put("gene", row.getGene());
			criteria.put("refAllele", row.getRefAll());
			criteria.put("varAllele", row.getAllele());
			criteria.put("start", row.getStart());
			criteria.put("stop", row.getStop());
			criteria.put("mutation", row.getVarAa());
			
			DomainKnownMutation found = domainFacade.findKnownMutation(criteria);
			if (found == null) {
				found = domainFacade.newKnownMutation(criteria);
			}
			
			DomainObservedMutation observed = domainFacade.newObservedMutation(runAssay, found);
			observed.setFrequency(row.getVrf());
			observed.setStatus(DomainObservedMutation.MUTATION_STATUS_FOUND);

			log.debug("Added new mutation: " + observed.toString());
		}
	}
}
