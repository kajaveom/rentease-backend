package com.rentease.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Simple in-memory cache configuration for development/testing
 * when Redis is not available. Uses ConcurrentHashMap-based caching.
 */
@Configuration
@EnableCaching
@Profile("test")
public class SimpleCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "listings",
                "listing",
                "users",
                "recentListings",
                "categories"
        );
    }
}
