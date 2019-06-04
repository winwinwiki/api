
package com.winwin.winwin.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ArvindKhatik
 *
 */
public class CommonUtils {

	/**
	 * 
	 */
	public CommonUtils() {
		// TODO Auto-generated constructor stub
	}

	public static String getFormattedDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
		return formattedDte;
	}
}
