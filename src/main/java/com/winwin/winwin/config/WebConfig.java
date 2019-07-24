/**
 * Configuration class to enable Spring MVC
 */
package com.winwin.winwin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@EnableWebMvc
@Configuration
public abstract class WebConfig implements WebMvcConfigurer {

}
