package com.bank.quota.core.dto.penetrating;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PenetratingOccupyResult {
    
    private boolean success;
    private String occupyNo;
    private String message;
    private BigDecimal totalAmount;
    private BigDecimal occupiedAmount;
    private LocalDateTime occupyTime;
    private List<QuotaLevelResult> levelResults;
    
    @Data
    public static class QuotaLevelResult {
        private int level;
        private String levelName;
        private Long quotaId;
        private String quotaNo;
        private BigDecimal beforeAvailable;
        private BigDecimal afterAvailable;
        private BigDecimal beforeUsed;
        private BigDecimal afterUsed;
        private BigDecimal occupyAmount;
        private boolean success;
        private String message;
    }
}
