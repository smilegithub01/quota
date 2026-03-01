package com.bank.quota.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    
    public static final String GROUP_QUOTA_CACHE = "groupQuotaCache";
    public static final String CUSTOMER_QUOTA_CACHE = "customerQuotaCache";
    public static final String APPROVAL_QUOTA_CACHE = "approvalQuotaCache";
    public static final String WHITELIST_CACHE = "whitelistCache";
    public static final String SYSTEM_CONFIG_CACHE = "systemConfigCache";
    public static final String QUOTA_LOCK_CACHE = "quotaLockCache";
    
    @Bean
    @Primary
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        CaffeineCache groupQuotaCaffeineCache = new CaffeineCache(GROUP_QUOTA_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .recordStats()
                        .build());
        
        CaffeineCache customerQuotaCaffeineCache = new CaffeineCache(CUSTOMER_QUOTA_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(5000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .recordStats()
                        .build());
        
        CaffeineCache approvalQuotaCaffeineCache = new CaffeineCache(APPROVAL_QUOTA_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(10000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .recordStats()
                        .build());
        
        CaffeineCache whitelistCaffeineCache = new CaffeineCache(WHITELIST_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(2000)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .recordStats()
                        .build());
        
        CaffeineCache systemConfigCaffeineCache = new CaffeineCache(SYSTEM_CONFIG_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .recordStats()
                        .build());
        
        CaffeineCache quotaLockCaffeineCache = new CaffeineCache(QUOTA_LOCK_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(10000)
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .recordStats()
                        .build());
        
        cacheManager.setCaches(Arrays.asList(
                groupQuotaCaffeineCache,
                customerQuotaCaffeineCache,
                approvalQuotaCaffeineCache,
                whitelistCaffeineCache,
                systemConfigCaffeineCache,
                quotaLockCaffeineCache
        ));
        
        return cacheManager;
    }
    
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats();
    }
}
