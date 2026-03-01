package com.bank.quota.core.dto.systemconfig;

import lombok.Data;

@Data
public class UpdateSystemConfigRequest {
    private String configValue;
    private String configName;
    private String description;
    private String updateBy;
}
