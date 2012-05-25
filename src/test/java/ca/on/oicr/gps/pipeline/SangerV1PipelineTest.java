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
import ca.on.oicr.gps.pipeline.sanger.v1.SangerPipeline;

public class SangerV1PipelineTest {
	
	private Pipeline pipeline;
	
	@Before
    public void setUp() {
        pipeline = new SangerPipeline();
    }

	@Test
	public void testHandleSubmission() {
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "ABI");
		replay(submission);
		assert pipeline.canHandleSubmission(submission);
	}

	@Test
	public void testProcessSubmission() throws Exception {
		
		InputStream input = new FileInputStream("src/test/resources/sanger_test_01.xls"); 
		
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "ABI");
		expect(submission.getSubmissionInputStream()).andReturn((InputStream) input);
		replay(submission);
		
		DomainFacadeImpl domain = new DomainFacadeImpl();
		domain.setAssays(
			"gene=PDGFRA;name=PDGFRA_6",
			Arrays.asList((DomainAssay) new DomainAssayImpl("PDGFRA_6")));
		domain.setAssays(
			"gene=EGFR;name=EGFR_5",
			Arrays.asList((DomainAssay) new DomainAssayImpl("EGFR_5")));
				
		PipelineState state = pipeline.newState(submission, domain);
		PipelineRunner runner = new PipelineRunner(state);
		runner.run();
		
		assert state.isDone();
		
		List<PipelineError> errors = state.errors();
		assertEquals(0, errors.size());
		
		assert ! state.hasFailed();
	}
}
