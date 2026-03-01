package com.bank.quota.core.dto.lifecycle;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuotaLifecycleResponse {
    
    private Long quotaId;
    private String quotaNo;
    private String quotaType;
    private String status;
    private BigDecimal totalQuota;
    private BigDecimal usedQuota;
    private BigDecimal availableQuota;
    private BigDecimal frozenQuota;
    private LocalDateTime effectiveTime;
    private LocalDateTime expiryTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<QuotaLifecycleEventDTO> events;
    
    @Data
    public static class QuotaLifecycleEventDTO {
        private String eventId;
        private String eventType;
        private BigDecimal amount;
        private String reason;
        private String operator;
        private LocalDateTime eventTime;
    }
}
