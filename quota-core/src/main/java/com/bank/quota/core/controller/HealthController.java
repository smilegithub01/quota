package com.bank.quota.core.controller;

import com.bank.quota.common.result.Result;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    
    @Autowired(required = false)
    private java.util.List<HealthIndicator> healthIndicators;
    
    @GetMapping
    public Result<HealthResponse> health() {
        Map<String, Object> healthDetails = new HashMap<>();
        healthDetails.put("status", "UP");
        healthDetails.put("timestamp", LocalDateTime.now().toString());
        
        if (healthIndicators != null) {
            for (HealthIndicator indicator : healthIndicators) {
                String name = indicator.getClass().getSimpleName().replace("HealthIndicator", "");
                Health health = indicator.health();
                Map<String, Object> indicatorDetails = new HashMap<>();
                indicatorDetails.put("status", health.getStatus().getCode());
                if (health.getDetails() != null) {
                    indicatorDetails.put("details", health.getDetails());
                }
                healthDetails.put(name, indicatorDetails);
            }
        }
        
        HealthResponse response = new HealthResponse();
        response.setStatus("UP");
        response.setTimestamp(LocalDateTime.now());
        response.setDetails(healthDetails);
        
        return Result.success(response);
    }
    
    @GetMapping("/ready")
    public Result<HealthResponse> readiness() {
        return health();
    }
    
    @GetMapping("/live")
    public Result<HealthResponse> liveness() {
        HealthResponse response = new HealthResponse();
        response.setStatus("UP");
        response.setTimestamp(LocalDateTime.now());
        return Result.success(response);
    }
    
    @Data
    public static class HealthResponse {
        private String status;
        private LocalDateTime timestamp;
        private Map<String, Object> details;
    }
}
