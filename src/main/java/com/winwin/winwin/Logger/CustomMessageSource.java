/**
 * The Class CustomMessageSource defines the support for the parameterization and internationalization of messages. 
 */
package com.winwin.winwin.Logger;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Component
public class CustomMessageSource {

	@Autowired
	MessageSource messageSource;

	private static final Locale LOCALE = Locale.ENGLISH;

	/*
	 * Constructor
	 * 
	 */

	public CustomMessageSource() {
	}

	public String getMessage(String code) {
		return messageSource.getMessage(code, null, LOCALE);
	}

	public String getMessage(String code, Object[] args) {
		return messageSource.getMessage(code, args, LOCALE);
	}

	public String getMessage(String code, Object[] args, String defaultMessage) {
		return messageSource.getMessage(code, args, defaultMessage, LOCALE);
	}

	public String getMessage(String code, String defaultMessage) {
		return messageSource.getMessage(code, null, defaultMessage, LOCALE);
	}
}