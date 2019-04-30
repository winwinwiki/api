package com.winwin.winwin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author ArvindKhatik
 *
 */
@EnableWebMvc
@Configuration
public abstract class WebConfig implements WebMvcConfigurer {

}
