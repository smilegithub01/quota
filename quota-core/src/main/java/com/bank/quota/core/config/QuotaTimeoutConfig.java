package com.bank.quota.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 额度过期配置
 * 
 * <p>管理额度相关过期时间配置。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Data
@Component
@ConfigurationProperties(prefix = "quota.timeout")
public class QuotaTimeoutConfig {
    
    /**
     * 占用锁定超时时间（分钟）
     */
    private Integer occupyLockTimeoutMinutes = 30;
    
    /**
     * 申请超时时间（分钟）
     */
    private Integer applicationTimeoutMinutes = 120;
    
    /**
     * 审批超时时间（分钟）
     */
    private Integer approvalTimeoutMinutes = 1440; // 24小时
    
    /**
     * 启用自动释放功能
     */
    private Boolean autoReleaseEnabled = true;
}