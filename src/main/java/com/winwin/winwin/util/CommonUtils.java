
package com.winwin.winwin.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ArvindKhatik
 *
 */
public class CommonUtils {

	/**
	 * 
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

	public CommonUtils() {
		// TODO Auto-generated constructor stub
	}

	public static Date getFormattedDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDte = sdf.format(new Date(System.currentTimeMillis()));
		Date date = null;
		try {
			date = sdf.parse(formattedDte);
		} catch (ParseException e) {
			LOGGER.error("exception occured while fetching date", e);
		}
		return date;
	}
}
