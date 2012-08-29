package ca.on.oicr.gps.service

import org.joda.time.DateTime;

import ca.on.oicr.gps.util.TimeUtilities;

import grails.test.*

class TimeUtilitiesTests extends GrailsUnitTestCase {

	public void setUp() {
        super.setUp()
    }

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Checks a span in the middle of a week
	 */
	void testElapsedWorkingDays1() {
		DateTime calStart = new DateTime(2011, 5, 3, 0, 0, 0, 0)
		DateTime calEnd = new DateTime(2011, 5, 5, 0, 0, 0, 0)
		assertEquals(2, TimeUtilities.calculateWorkingDays(calStart, calEnd))
	}
		
	/**
	 * Checks a span that crosses several weeks
	 */
	void testElapsedWorkingDays2() {
		DateTime calStart = new DateTime(2011, 5, 1, 0, 0, 0, 0)
		DateTime calEnd = new DateTime(2011, 5, 23, 0, 0, 0, 0)
		assertEquals(15, TimeUtilities.calculateWorkingDays(calStart, calEnd))
	}
		
	/**
	 * Checks a span that crosses several weeks
	 */
	void testElapsedWorkingDays3() {
		DateTime calStart = new DateTime(2011, 5, 2, 0, 0, 0, 0)
		DateTime calEnd = new DateTime(2011, 5, 23, 0, 0, 0, 0)
		assertEquals(15, TimeUtilities.calculateWorkingDays(calStart, calEnd))
	}

	/**
	 * Checks a span that crosses several weeks
	 */
	void testElapsedWorkingDays4() {
		DateTime calStart = new DateTime(2011, 5, 3, 0, 0, 0, 0)
		DateTime calEnd = new DateTime(2011, 5, 23, 0, 0, 0, 0)
		assertEquals(14, TimeUtilities.calculateWorkingDays(calStart, calEnd))
	}

	/**
	 * Checks a span for a single day (weekday)
	 */
	void testElapsedWorkingDays5() {
		DateTime calStart = new DateTime(2011, 5, 6, 0, 0, 0, 0)
		DateTime calEnd = new DateTime(2011, 5, 6, 0, 0, 0, 0)
		assertEquals(0, TimeUtilities.calculateWorkingDays(calStart, calEnd))
	}
		
	/**
	 * Checks a span for a single day (weekend)
	 */
	void testElapsedWorkingDays6() {
		DateTime calStart = new DateTime(2011, 05, 07, 0, 0, 0, 0)
		DateTime calEnd = new DateTime(2011, 05, 07, 0, 0, 0, 0)
		assertEquals(0, TimeUtilities.calculateWorkingDays(calStart, calEnd))
	}

	/**
	 * Checks a span for a two days (weekend)
	 */
	void testElapsedWorkingDays7() {
		DateTime calStart = new DateTime(2011, 5, 6, 0, 0, 0, 0)
		DateTime calEnd = new DateTime(2011, 5, 7, 0, 0, 0, 0)
		assertEquals(0, TimeUtilities.calculateWorkingDays(calStart, calEnd))
	}

	/**
	 * Checks a span for three days (weekend)
	 */
	void testElapsedWorkingDays8() {
		DateTime calStart = new DateTime(2011, 5, 6, 0, 0, 0, 0)
		DateTime calEnd = new DateTime(2011, 5, 8, 0, 0, 0, 0)
		assertEquals(0, TimeUtilities.calculateWorkingDays(calStart, calEnd))
	}

	/**
	 * Checks a span for four days (weekend)
	 */
	void testElapsedWorkingDays9() {
		DateTime calStart = new DateTime(2011, 5, 6, 0, 0, 0, 0)
		DateTime calEnd = new DateTime(2011, 5, 9, 0, 0, 0, 0)
		assertEquals(1, TimeUtilities.calculateWorkingDays(calStart, calEnd))
	}

	/**
	 * Checks a one-day span in the middle of a week
	 */
	void testElapsedWorkingDays10() {
		DateTime calStart = new DateTime(2011, 5, 3, 0, 0, 0, 0)
		DateTime calEnd = new DateTime(2011, 5, 4, 0, 0, 0, 0)
		assertEquals(1, TimeUtilities.calculateWorkingDays(calStart, calEnd))
	}
		
	/**
	 * Checks a regression issue associated with GPS-52
	 */
	void testElapsedWorkingDays11() {
		DateTime calStart = new DateTime(2011, 7, 19, 0, 0, 0, 0)
		DateTime calEnd = new DateTime(2011, 8, 3, 0, 0, 0, 0)
		assertEquals(11, TimeUtilities.calculateWorkingDays(calStart, calEnd))
	}
}
