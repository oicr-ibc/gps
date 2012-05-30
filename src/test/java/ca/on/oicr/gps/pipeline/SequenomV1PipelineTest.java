package ca.on.oicr.gps.pipeline;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.*;

import ca.on.oicr.gps.pipeline.domain.DomainAssay;
import ca.on.oicr.gps.pipeline.mock.DomainAssayImpl;
import ca.on.oicr.gps.pipeline.mock.DomainFacadeImpl;
import ca.on.oicr.gps.pipeline.mock.DomainKnownMutationImpl;
import ca.on.oicr.gps.pipeline.model.MutationSubmission;
import ca.on.oicr.gps.pipeline.model.PipelineError;
import ca.on.oicr.gps.pipeline.model.PipelineState;
import ca.on.oicr.gps.pipeline.sequenom.v1.SequenomPipeline;

public class SequenomV1PipelineTest {
	
	private Pipeline pipeline;
	
	@Before
    public void setUp() {
        pipeline = new SequenomPipeline();
    }

	@Test
	public void testHandleSubmission() {
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "Sequenom");
		replay(submission);
		assert pipeline.canHandleSubmission(submission);
	}

	@Test
	public void testProcessSubmission() throws Exception {
		
		InputStream input = new FileInputStream("src/test/resources/sequenom_test_01.xls"); 
		
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "ABI");
		expect(submission.getSubmissionInputStream()).andReturn((InputStream) input);
		replay(submission);
		
		DomainFacadeImpl domain = new DomainFacadeImpl();
		domain.setAssays(
			"gene=PIK3CA;name=PIK3CA_5",
			Arrays.asList((DomainAssay) new DomainAssayImpl("PIK3CA_5")));
		domain.setKnownMutation("gene=PIK3CA;mutation=E542K", 
			new DomainKnownMutationImpl("PIK3CA E542K"));
				
		PipelineState state = pipeline.newState(submission, domain);
		PipelineRunner runner = new PipelineRunner(state);
		runner.run();
		
		assert state.isDone();
		
		List<PipelineError> errors = state.errors();
		assertEquals(0, errors.size());
		
		assert ! state.hasFailed();
	}
}
