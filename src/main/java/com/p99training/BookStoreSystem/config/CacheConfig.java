package com.p99training.BookStoreSystem.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    /**
     * Registers Spring's built-in in-process CacheManager.
     * No external cache server needed — uses ConcurrentHashMap under the hood.
     *
     * Cache names must match the values used in @Cacheable / @CacheEvict annotations:
     *   "books"           — used by BookServiceImpl.getBookById()
     *   "inventoryReport" — used by ReportServiceImpl.generateInventoryReport()
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("books", "inventoryReport");
    }
}
