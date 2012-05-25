package ca.on.oicr.gps.pipeline;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.*;

import ca.on.oicr.gps.pipeline.domain.DomainAssay;
import ca.on.oicr.gps.pipeline.domain.DomainFacade;
import ca.on.oicr.gps.pipeline.mock.DomainAssayImpl;
import ca.on.oicr.gps.pipeline.mock.DomainFacadeImpl;
import ca.on.oicr.gps.pipeline.model.MutationSubmission;
import ca.on.oicr.gps.pipeline.model.PipelineError;
import ca.on.oicr.gps.pipeline.model.PipelineState;
import ca.on.oicr.gps.pipeline.pacbio.v1.PacBioPipeline;

public class PacbioV1PipelineTest {
	
	private Pipeline pipeline;
	
	@Before
    public void setUp() {
        pipeline = new PacBioPipeline();
    }

	@Test
	public void testHandleSubmission() {
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "PacBio");
		replay(submission);
		assert pipeline.canHandleSubmission(submission);
	}

	@Test
	public void testProcessSubmission() throws Exception {
		
		InputStream input = new FileInputStream("src/test/resources/pacbio_test_01.txt"); 
		
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "PacBio");
		expect(submission.getSubmissionInputStream()).andReturn((InputStream) input);
		replay(submission);
		
		DomainFacadeImpl domain = new DomainFacadeImpl();
		domain.setAssays(
			"chromosome=4;gene=PDGFRA;start=55141055;stop=55141055",
			Arrays.asList((DomainAssay) new DomainAssayImpl("PDGFRA_9")));
		domain.setAssays(
			"chromosome=4;gene=PDGFRA;start=55161391;stop=55161391",
			Arrays.asList((DomainAssay) new DomainAssayImpl("PDGFRA_6")));
		domain.setAssays(
			"chromosome=7;gene=EGFR;start=55249063;stop=55249063",
			Arrays.asList((DomainAssay) new DomainAssayImpl("EGFR_20")));
				
		PipelineState state = pipeline.newState(submission, domain);
		PipelineRunner runner = new PipelineRunner(state);
		runner.run();
		
		assert state.isDone();
		
		List<PipelineError> errors = state.errors();
		assertEquals(0, errors.size());
		
		assert ! state.hasFailed();
	}
}
