/**
 * Scheduler Class to Clear all the caches with delay of 1hr after application start
 */
package com.winwin.winwin.schedular;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Component
public class ClearAllCacheSchedular {
	@Autowired
	private CacheManager cacheManager;

	private static final Logger LOGGER = LoggerFactory.getLogger(ClearAllCacheSchedular.class);

	// reset cache in every 1hr, with delay of 1hr after application start
	@Scheduled(fixedRateString = "3600000", initialDelayString = "3600000")
	public void reportCurrentTime() {
		cacheManager.getCacheNames().parallelStream().forEach(name -> cacheManager.getCache(name).clear());
		LOGGER.info("Clearing all the application caches");
	}
}
