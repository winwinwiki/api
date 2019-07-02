package com.winwin.winwin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author ArvindKhatik
 *
 */
@SpringBootApplication
@EnableCaching
public class WinwinApplication {

	public static void main(String[] args) {
		SpringApplication.run(WinwinApplication.class, args);
	}

}
