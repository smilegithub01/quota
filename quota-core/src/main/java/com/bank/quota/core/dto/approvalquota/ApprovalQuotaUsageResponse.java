package com.bank.quota.core.dto.approvalquota;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ApprovalQuotaUsageResponse {
    private Long id;
    private String approvalNo;
    private String approvalType;
    private Long customerQuotaSubId;
    private BigDecimal approvalQuota;
    private BigDecimal usedQuota;
    private BigDecimal availableQuota;
    private BigDecimal usageRate;
    private List<ContractOccupancyInfo> contractOccupancies;
    
    @Data
    public static class ContractOccupancyInfo {
        private String contractNo;
        private BigDecimal occupancyAmount;
        private String status;
        private java.time.LocalDateTime createTime;
    }
}
