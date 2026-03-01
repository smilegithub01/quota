package com.bank.quota.core.dto.quotacontrol;

import lombok.Data;

@Data
public class QuotaQueryRequest {
    private String objectType;
    private Long objectId;
}
