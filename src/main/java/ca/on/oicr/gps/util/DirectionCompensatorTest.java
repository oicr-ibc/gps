package ca.on.oicr.gps.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Designed to test the logic which flips base pairs dependent possibly on the strand
 * direction, without having to bundle it with strand direction awareness. 
 * 
 * @author swatt
 */
public class DirectionCompensatorTest {

	@Test
	public void testG() {
		assertEquals("C", DirectionCompensator.compensate("G"));
	}

	@Test
	public void testC() {
		assertEquals("G", DirectionCompensator.compensate("C"));
	}

	@Test
	public void testT() {
		assertEquals("A", DirectionCompensator.compensate("T"));
	}
	
	@Test
	public void testA() {
		assertEquals("T", DirectionCompensator.compensate("A"));
	}

	@Test
	public void testCombined() {
		assertEquals("TAGT", DirectionCompensator.compensate("ACTA"));
	}

	@Test
	public void testCombinedMaybeTrue() {
		assertEquals("TAGT", DirectionCompensator.compensate("ACTA", true));
	}

	@Test
	public void testCombinedMaybeFalse() {
		assertEquals("ACTA", DirectionCompensator.compensate("ACTA", false));
	}
}
