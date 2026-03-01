package com.bank.quota.core.dto.whitelist;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WhitelistQueryRequest {
    private String whitelistNo;
    private String whitelistType;
    private Long customerId;
    private String customerName;
    private String businessType;
    private String status;
    private String applicant;
    private LocalDateTime applyTimeStart;
    private LocalDateTime applyTimeEnd;
    private Integer pageNum;
    private Integer pageSize;
}
