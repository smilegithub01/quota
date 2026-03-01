package com.bank.quota.core.dto.systemconfig;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemConfigResponse {
    private Long id;
    private String configKey;
    private String configValue;
    private String configName;
    private String category;
    private String description;
    private String status;
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
