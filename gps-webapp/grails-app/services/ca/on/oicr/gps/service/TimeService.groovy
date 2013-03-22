package ca.on.oicr.gps.service

import org.joda.time.DateTime;

/**
 * TimeService is mainly part of the system to assist in testing, as it allows the provider of the
 * current time to be mocked. 
 *  
 * @author swatt
 */

class TimeService {

    static transactional = false

	/**
	 * Returns the current time
	 * @return the current time, as a DateTime
	 */
    def now() {
		return new DateTime()
    }
}
