package ca.on.oicr.gps.pipeline;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.*;

import ca.on.oicr.gps.pipeline.mock.DomainFacadeImpl;
import ca.on.oicr.gps.pipeline.model.MutationSubmission;
import ca.on.oicr.gps.pipeline.model.PipelineError;
import ca.on.oicr.gps.pipeline.model.PipelineState;
import ca.on.oicr.gps.pipeline.hotspot.v1.HotSpotPipeline;

public class HotSpotV1PipelineTest {
	
	private Pipeline pipeline;
	
	@Before
    public void setUp() {
        pipeline = new HotSpotPipeline();
    }

	@Test
	public void testHandleSubmission() {
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "HotSpotV1");
		replay(submission);
		assert pipeline.canHandleSubmission(submission);
	}

	@Test
	public void testProcessSubmission() throws Exception {
		
		InputStream input = new FileInputStream("src/test/resources/hotspot_test_01.xlsx"); 
		
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "HotSpotV1");
		expect(submission.getSubmissionInputStream()).andReturn((InputStream) input);
		replay(submission);
		
		DomainFacadeImpl domain = new DomainFacadeImpl();
		PipelineState state = pipeline.newState(submission, domain);
		PipelineRunner runner = new PipelineRunner(state);
		runner.run();
		
		assert state.isDone();
		
		List<PipelineError> errors = state.errors();
		assertEquals(0, errors.size());
		
		assert ! state.hasFailed();
	}
}
