/**
 * Configuration class to support Internationalization,to load the specified properties file
 */
package com.winwin.winwin.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Configuration
public class BeanConfig {

	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("winwinlabels");
		return messageSource;
	}
}
