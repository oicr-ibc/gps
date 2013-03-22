package ca.on.oicr.gps.util

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.Interval;

class TimeUtilities {
	
	static Integer calculateWorkingDays(DateTime start, DateTime end) {
		
		// We occasionally get times in the future, which are meaningless
		if (start.isAfter(end)) {
			return null
		}
		
		Interval range = new Interval(start, end)
		int days = Days.daysIn(range).getDays()
		
		int startDay = range.getStart().dayOfWeek().get()
		int endDay = range.getEnd().dayOfWeek().get()
		int nonWorkingDays = 0
		
		// The logic here is more than a little subtle. Basically, the idea is that we chop up
		// the range into a set of chunks, up to the first weekend, a set of n weeks, and after
		// the first weekend. A brief span within a single week is treated by adding a week in
		// and subtracting a weekend, essentially. Each segment can then be calculated
		// into a set of weekend days, which we can then subtract from the original days.
		//
		// The logic isn't quite that clear: it is not very well defined what counts as an
		// elapsed working day, especially bearing in mind we may not be especially clear about
		// the times within the days.
		
		if (days == 0) {
			return 0
		} else if (days < 7 && startDay < endDay) {
			nonWorkingDays -= 2
		}
		
		// First segment
		def firstWeekendDays = Math.min(2, DateTimeConstants.SUNDAY - startDay + 1)
		nonWorkingDays += firstWeekendDays
		
		// And the whole weeks
		def wholeWeekDays = Math.max(0, days - (DateTimeConstants.SUNDAY - startDay) - endDay)
		nonWorkingDays += 2 * (wholeWeekDays / 7)
		
		// And the last segment
		def lastWeekendDays = Math.max(0, endDay - DateTimeConstants.FRIDAY)
		nonWorkingDays += lastWeekendDays
		
		return days - nonWorkingDays
	}
}
