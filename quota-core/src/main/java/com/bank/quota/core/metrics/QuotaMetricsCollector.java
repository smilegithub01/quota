package com.bank.quota.core.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class QuotaMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Counter> counterMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timerMap = new ConcurrentHashMap<>();
    
    public QuotaMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void recordQuotaOperation(String operationType, String status) {
        String metricName = "quota.operation." + operationType.toLowerCase() + "." + status.toLowerCase();
        
        counterMap.computeIfAbsent(metricName, name -> 
                Counter.builder(name)
                        .description("Quota operation count by type and status")
                        .register(meterRegistry))
                .increment();
    }
    
    public void recordQuotaCreation(String quotaType) {
        String metricName = "quota.created." + quotaType.toLowerCase();
        
        counterMap.computeIfAbsent(metricName, name -> 
                Counter.builder(name)
                        .description("Count of created quotas by type")
                        .register(meterRegistry))
                .increment();
    }
    
    public void recordQuotaAdjustment(String quotaType, String direction) {
        String metricName = "quota.adjusted." + quotaType.toLowerCase() + "." + direction.toLowerCase();
        
        counterMap.computeIfAbsent(metricName, name -> 
                Counter.builder(name)
                        .description("Count of quota adjustments by type and direction")
                        .register(meterRegistry))
                .increment();
    }
    
    public void recordQuotaLock(String objectType, boolean success) {
        String metricName = "quota.lock." + objectType.toLowerCase() + "." + (success ? "success" : "failure");
        
        counterMap.computeIfAbsent(metricName, name -> 
                Counter.builder(name)
                        .description("Count of quota lock operations")
                        .register(meterRegistry))
                .increment();
    }
    
    public void recordQuotaOccupancy(String approvalType, boolean success) {
        String metricName = "quota.occupancy." + approvalType.toLowerCase() + "." + (success ? "success" : "failure");
        
        counterMap.computeIfAbsent(metricName, name -> 
                Counter.builder(name)
                        .description("Count of quota occupancy operations")
                        .register(meterRegistry))
                .increment();
    }
    
    public void recordAuditLog(String operationType) {
        String metricName = "audit.log." + operationType.toLowerCase();
        
        counterMap.computeIfAbsent(metricName, name -> 
                Counter.builder(name)
                        .description("Count of audit logs by operation type")
                        .register(meterRegistry))
                .increment();
    }
    
    public void recordRiskWarning(String warningLevel) {
        String metricName = "risk.warning." + warningLevel.toLowerCase();
        
        counterMap.computeIfAbsent(metricName, name -> 
                Counter.builder(name)
                        .description("Count of risk warnings by level")
                        .register(meterRegistry))
                .increment();
    }
    
    public Timer startTimer(String operation) {
        String metricName = "quota.timer." + operation.toLowerCase();
        
        return timerMap.computeIfAbsent(metricName, name -> 
                Timer.builder(name)
                        .description("Timer for quota operations")
                        .register(meterRegistry));
    }
    
    public void recordOperationDuration(String operation, long durationMs) {
        Timer timer = startTimer(operation);
        timer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    public void recordDatabaseQueryTime(String queryType, long durationMs) {
        String metricName = "database.query." + queryType.toLowerCase() + ".time";
        
        timerMap.computeIfAbsent(metricName, name -> 
                Timer.builder(name)
                        .description("Database query time by type")
                        .register(meterRegistry))
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    public void recordCacheHit(String cacheType) {
        String metricName = "cache.hit." + cacheType.toLowerCase();
        
        counterMap.computeIfAbsent(metricName, name -> 
                Counter.builder(name)
                        .description("Cache hit count by type")
                        .register(meterRegistry))
                .increment();
    }
    
    public void recordCacheMiss(String cacheType) {
        String metricName = "cache.miss." + cacheType.toLowerCase();
        
        counterMap.computeIfAbsent(metricName, name -> 
                Counter.builder(name)
                        .description("Cache miss count by type")
                        .register(meterRegistry))
                .increment();
    }
}
