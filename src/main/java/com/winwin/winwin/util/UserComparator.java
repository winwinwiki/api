/**
 * 
 */
package com.winwin.winwin.util;

import java.util.Comparator;

import com.winwin.winwin.payload.UserPayload;

/**
 * @author ArvindKhatik
 *
 */
public class UserComparator implements Comparator<UserPayload> {

	/**
	 * Sort user by name in ascending order
	 */
	@Override
	public int compare(UserPayload userFirst, UserPayload userSecond) {
		if (userFirst.getUserDisplayName() == null) {
			return (userSecond.getUserDisplayName() == null) ? 0 : -1;
		}
		if (userSecond.getUserDisplayName() == null) {
			return 1;
		}
		return userFirst.getUserDisplayName().compareToIgnoreCase(userSecond.getUserDisplayName());
	}

}
