/**
 * The Class WinwinApplication is a SpringApplication Class for Project WinWin
 */
package com.winwin.winwin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class WinwinApplication {

	public static void main(String[] args) {
		SpringApplication.run(WinwinApplication.class, args);
	}

}
