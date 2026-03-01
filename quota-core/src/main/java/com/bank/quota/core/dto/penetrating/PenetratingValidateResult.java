package com.bank.quota.core.dto.penetrating;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PenetratingValidateResult {
    
    private boolean valid;
    private String message;
    private List<QuotaLevelValidation> levelValidations;
    
    @Data
    public static class QuotaLevelValidation {
        private int level;
        private String levelName;
        private Long quotaId;
        private String quotaNo;
        private BigDecimal totalQuota;
        private BigDecimal usedQuota;
        private BigDecimal availableQuota;
        private BigDecimal frozenQuota;
        private BigDecimal requestedAmount;
        private boolean sufficient;
        private BigDecimal shortage;
    }
}
