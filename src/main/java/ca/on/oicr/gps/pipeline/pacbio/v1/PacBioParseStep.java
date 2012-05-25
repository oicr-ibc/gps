package ca.on.oicr.gps.pipeline.pacbio.v1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.on.oicr.gps.pipeline.PipelineStep;
import ca.on.oicr.gps.pipeline.model.MutationSubmission;
import ca.on.oicr.gps.pipeline.model.Mutations;
import ca.on.oicr.gps.pipeline.model.PipelineRuntimeException;
import ca.on.oicr.gps.pipeline.model.PipelineState;
import ca.on.oicr.gps.pipeline.step.parse.Parser;
import ca.on.oicr.gps.pipeline.step.parse.TsvParsedRow;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class PacBioParseStep implements PipelineStep {

	private static final Logger log = LoggerFactory.getLogger(PacBioParseStep.class);

	private Pattern blankLinePattern = Pattern.compile("^\\s*$");

	private Pattern headerPattern = Pattern.compile("^(.*):(.*)$");

	public void execute(PipelineState state) {
		Preconditions.checkNotNull(state);

		final MutationSubmission submission = state
				.get(MutationSubmission.class);

		Preconditions.checkNotNull(submission);

		try {
			final LineNumberReader reader = new LineNumberReader(
					new InputStreamReader(submission.getSubmissionInputStream(),
							Charsets.UTF_8));

			/*
			 * This used to be a header, enforced to detect the file format
			 * properly. It's not there any longer, so this is not sufficient
			 * to validate the file format. Instead, we'll parse the headers and
			 * then check we get what we need. 
			 */
			
			Map<String, String> headers = Maps.newLinkedHashMap();
			String line = reader.readLine();
			while (line != null && (isEmptyLine(line) || isHeaderLine(line))) {
				parseHeader(line, headers);
				line = reader.readLine();
			}
			
			// Probably other validation issues would be a good idea at this stage, 
			// but need to determine which should really be errors. These ones are
			// most serious, as they will cause us to fail to locate the right 
			// details. Even when present, if we can't locate the right components,
			// we will signal errors later. 
			
			if (! headers.containsKey("Patient ID")) {
				state.error("data.missing.patient.id");
			}
			if (! headers.containsKey("Sample ID")) {
				state.error("data.missing.sample.id");
			}

			Parser<PacBioMutations> parser = new Parser<PacBioMutations>(
					ImmutableList.copyOf(headers.keySet()),
					PacBioMutations.class);

			PacBioMutations mutations = parser.parse(state,
					new PacBioMutations(), new TsvParsedRow(0, Joiner.on(TsvParsedRow.TAB)
							.join(headers.values())));

			List<String> columns = ImmutableList.copyOf(Splitter.on(TsvParsedRow.TAB)
					.trimResults().split(line));

			Parser<PacBioMutationRow> rowParser = new Parser<PacBioMutationRow>(
					columns, PacBioMutationRow.class);

			line = reader.readLine();
			while (line != null) {
				if (isEmptyLine(line) == false) {
					PacBioMutationRow row = new PacBioMutationRow();
					rowParser.parse(state, row,
							new TsvParsedRow(reader.getLineNumber(), line));
					mutations.addRow(row);
				}
				line = reader.readLine();
			}
			state.set(Mutations.class, mutations);
		} catch (IOException e) {
			state.error("data.unreadable.file", "PacBio");
			throw new PipelineRuntimeException(state);
		} catch (IllegalArgumentException e) {
			state.error("data.illegal.argument", e.getMessage());
			throw new PipelineRuntimeException(state);
		}
	}
	
	private boolean isEmptyLine(String line) {
		Preconditions.checkNotNull(line);
		return blankLinePattern.matcher(line).matches();
	}

	private boolean isHeaderLine(String line) {
		Preconditions.checkNotNull(line);
		return headerPattern.matcher(line).matches();
	}

	private void parseHeader(String line, Map<String, String> header) {
		Preconditions.checkNotNull(line);
		Preconditions.checkNotNull(header);

		Matcher m = headerPattern.matcher(line);
		if (m.matches()) {
			header.put(m.group(1).trim(), m.group(2).trim());
		}
	}
}
