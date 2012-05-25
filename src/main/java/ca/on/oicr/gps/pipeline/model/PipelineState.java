package ca.on.oicr.gps.pipeline.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.on.oicr.gps.pipeline.PipelineStep;
import ca.on.oicr.gps.pipeline.domain.DomainFacade;

public class PipelineState {

	private static final Logger log = LoggerFactory.getLogger(PipelineState.class);

	private final PipelineStep steps[];

	private final List<PipelineError> errors = new ArrayList<PipelineError>();

	private final Map<Class<?>, Object> state = new HashMap<Class<?>, Object>();
	
	private DomainFacade domainFacade = null;

	private int nextStep = 0;

	public PipelineState(PipelineStep steps[], DomainFacade domainFacade) {
		this.steps = steps;
		this.domainFacade = domainFacade;
	}

	public void next() {
		PipelineStep step = steps[nextStep++];
		log.debug("Executing step {}", step);
		step.execute(this);
	}

	public boolean canContinue() {
		return isDone() == false && hasFailed() == false;
	}

	public boolean isDone() {
		return nextStep >= steps.length;
	}

	public boolean hasFailed() {
		return errors().size() > 0;
	}

	public List<PipelineError> errors() {
		return Collections.unmodifiableList(errors);
	}

	public void error(String key, Object... args) {
		errors.add(new PipelineError(key, args));
	}

	public void error(PipelineError err) {
		errors.add(err);
	}

	public <T> Object set(Class<T> key, T o) {
		return state.put(key, o);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> key) {
		return (T)state.get(key);
	}

	public DomainFacade getDomainFacade() {
		return domainFacade;
	}

	public void setDomainFacade(DomainFacade domainFacade) {
		this.domainFacade = domainFacade;
	}
}
