package ca.on.oicr.gps.util

import ca.on.oicr.gps.system.User;

class UserComparator implements Comparator<User> {

	int compare(User a, User b) {
		int result = a.familyName.compareTo(b.familyName)
		if (result == 0) {
			result = a.givenName.compareTo(b.givenName)
		}
		return result;
	}

}
