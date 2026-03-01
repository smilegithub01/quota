package com.bank.quota.core.dto.systemconfig;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSystemConfigRequest {
    @NotBlank(message = "配置Key不能为空")
    private String configKey;
    
    @NotBlank(message = "配置Value不能为空")
    private String configValue;
    
    @NotBlank(message = "配置名称不能为空")
    private String configName;
    
    @NotBlank(message = "配置分类不能为空")
    private String category;
    
    private String description;
    
    private String createBy;
}
