package com.winwin.winwin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.winwin.winwin.config.WebConfig;

/**
 * @author ArvindKhatik
 *
 */
@SpringBootApplication
public class WinwinApplication {

	public static void main(String[] args) {
		SpringApplication.run(WinwinApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebConfig() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("*").allowedOrigins("*");
			}
		};
	}

}
