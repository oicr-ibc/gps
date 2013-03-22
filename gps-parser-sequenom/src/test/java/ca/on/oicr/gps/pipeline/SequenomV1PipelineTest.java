package ca.on.oicr.gps.pipeline;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.*;

import ca.on.oicr.gps.pipeline.domain.DomainTarget;
import ca.on.oicr.gps.pipeline.mock.DomainFacadeImpl;
import ca.on.oicr.gps.pipeline.mock.DomainKnownMutationImpl;
import ca.on.oicr.gps.pipeline.mock.DomainTargetImpl;
import ca.on.oicr.gps.pipeline.model.MutationSubmission;
import ca.on.oicr.gps.pipeline.model.PipelineError;
import ca.on.oicr.gps.pipeline.model.PipelineState;
import ca.on.oicr.gps.pipeline.sequenom.v1.SequenomPipeline;

public class SequenomV1PipelineTest {
	
	private static final Logger log = LoggerFactory.getLogger(SequenomV1PipelineTest.class);
	
	private Pipeline pipeline;
	
	@Before
    public void setUp() {
        pipeline = new SequenomPipeline();
    }

	@Test
	public void testRejectSubmission() {
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "ABI");
		replay(submission);
		assertTrue(! pipeline.canHandleSubmission(submission));
	}

	@Test
	public void testHandleSubmission() {
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "Sequenom");
		replay(submission);
		assertTrue(pipeline.canHandleSubmission(submission));
	}
	
	private void validateState(PipelineState state) {
		assert state.isDone();
		
		List<PipelineError> errors = state.errors();
		for(PipelineError error : errors) {
			log.error("Error reported: " + error.getKey() + ": " + error.getArgs().toString());
		}
		assertEquals(0, errors.size());
		
		assertTrue(! state.hasFailed());
	}

	@Test
	public void testProcessSubmission1() throws Exception {
		
		InputStream input = ClassLoader.getSystemResourceAsStream("sequenom_test_01.xls");
		
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "Sequenom");
		expect(submission.getSubmissionInputStream()).andReturn((InputStream) input);
		replay(submission);
		
		DomainFacadeImpl domain = new DomainFacadeImpl();
		domain.setTargets(
			Arrays.asList((DomainTarget) 
					new DomainTargetImpl("3", "PIK3CA", 178936082, 178936082, "G", "A", "E542K")));
		domain.setKnownMutation("chromosome=3;gene=PIK3CA;mutation=E542K;refAllele=G;start=178936082;stop=178936082;varAllele=A", 
			new DomainKnownMutationImpl("PIK3CA E542K"));
				
		PipelineState state = pipeline.newState(submission, domain);
		PipelineRunner runner = new PipelineRunner(state);
		runner.run();
		validateState(state);
	}

	@Test
	public void testProcessSubmission2() throws Exception {
		
		InputStream input = ClassLoader.getSystemResourceAsStream("sequenom_test_02.xls");
		
		MutationSubmission submission = createMock(MutationSubmission.class);
		expect(submission.getType()).andReturn((String) "Sequenom");
		expect(submission.getSubmissionInputStream()).andReturn((InputStream) input);
		replay(submission);
		
		DomainFacadeImpl domain = new DomainFacadeImpl();
		domain.setTargets(
				Arrays.asList((DomainTarget) 
						new DomainTargetImpl("12", "KRAS", 25398284, 25398284, "C", "T", "G12D"),
						new DomainTargetImpl("12", "KRAS", 25398284, 25398284, "C", "A", "G12V"),
						new DomainTargetImpl("3", "PIK3CA", 178952085, 178952085, "A", "T", "H1047L")));

		domain.setKnownMutation("chromosome=12;gene=KRAS;mutation=G12D;refAllele=C;start=25398284;stop=25398284;varAllele=T", new DomainKnownMutationImpl("KRAS G12D"));
		domain.setKnownMutation("chromosome=12;gene=KRAS;mutation=G12V;refAllele=C;start=25398284;stop=25398284;varAllele=A", new DomainKnownMutationImpl("KRAS G12V"));
		domain.setKnownMutation("chromosome=3;gene=PIK3CA;mutation=H1047L;refAllele=A;start=178952085;stop=178952085;varAllele=T", new DomainKnownMutationImpl("PIK3CA H1047L"));
				
		PipelineState state = pipeline.newState(submission, domain);
		PipelineRunner runner = new PipelineRunner(state);
		runner.run();
		validateState(state);
	}
}
