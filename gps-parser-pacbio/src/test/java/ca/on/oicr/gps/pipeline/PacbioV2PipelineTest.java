package ca.on.oicr.gps.pipeline;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.*;

import ca.on.oicr.gps.pipeline.domain.DomainTarget;
import ca.on.oicr.gps.pipeline.mock.DomainFacadeImpl;
import ca.on.oicr.gps.pipeline.mock.DomainTargetImpl;
import ca.on.oicr.gps.pipeline.model.MutationSubmission;
import ca.on.oicr.gps.pipeline.model.PipelineError;
import ca.on.oicr.gps.pipeline.model.PipelineState;
import ca.on.oicr.gps.pipeline.pacbio.v2.PacBioPipeline;

public class PacbioV2PipelineTest {
	
	private Pipeline pipeline;
	
	@Before
    public void setUp() {
        pipeline = new PacBioPipeline();
    }

	@Test
	public void testHandleSubmission() {
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "PacBioV2");
		replay(submission);
		assert pipeline.canHandleSubmission(submission);
	}

	@Test
	public void testProcessSubmission() throws Exception {
		
		InputStream input = ClassLoader.getSystemResourceAsStream("pacbio_test_04.xls");
		
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "PacBioV2");
		expect(submission.getSubmissionInputStream()).andReturn((InputStream) input);
		replay(submission);
		
		DomainFacadeImpl domain = new DomainFacadeImpl();
		domain.setTargets(
			Arrays.asList((DomainTarget) 
					new DomainTargetImpl("4", null, 55161391, 55161391, null, null, null),
					new DomainTargetImpl("7", null, 55242364, 55242697, null, null, null),
					new DomainTargetImpl("7", null, 55248895, 55249207, null, null, null)));
				
		PipelineState state = pipeline.newState(submission, domain);
		PipelineRunner runner = new PipelineRunner(state);
		runner.run();
		
		assertTrue(state.isDone());
		
		List<PipelineError> errors = state.errors();
		assertEquals(0, errors.size());
		
		assertTrue(! state.hasFailed());
	}

	@Test
	public void testProcessSubmissionWithMissingTarget() throws Exception {
		
		InputStream input = new FileInputStream("src/test/resources/pacbio_test_04.xls"); 
		
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "PacBioV2");
		expect(submission.getSubmissionInputStream()).andReturn((InputStream) input);
		replay(submission);
		
		DomainFacadeImpl domain = new DomainFacadeImpl();
		domain.setTargets(
			Arrays.asList((DomainTarget) 
					new DomainTargetImpl("4", null, 55161391, 55161391, null, null, null),
					new DomainTargetImpl("7", null, 55242364, 55242697, null, null, null)));
				
		PipelineState state = pipeline.newState(submission, domain);
		PipelineRunner runner = new PipelineRunner(state);
		runner.run();
		
		assertTrue(state.isDone());
		
		List<PipelineError> errors = state.errors();
		assertEquals(1, errors.size());
		
		assertTrue(state.hasFailed());
	}
}
