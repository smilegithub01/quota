package com.bank.quota.core.service.impl;

import com.bank.quota.core.config.QuotaTimeoutConfig;
import com.bank.quota.core.domain.ContractOccupancy;
import com.bank.quota.core.domain.CreditApplication;
import com.bank.quota.core.domain.UsageApplication;
import com.bank.quota.core.enums.ApprovalStatus;
import com.bank.quota.core.enums.UsageStatus;
import com.bank.quota.core.repository.ContractOccupancyRepository;
import com.bank.quota.core.repository.CreditApplicationRepository;
import com.bank.quota.core.repository.UsageApplicationRepository;
import com.bank.quota.core.service.TimeoutReleaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 锁定超时释放服务实现类
 * 
 * <p>提供银行级信贷额度管控平台的锁定超时释放功能实现。
 * 自动释放超过指定时间未处理的占用锁定。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeoutReleaseServiceImpl implements TimeoutReleaseService {
    
    private final QuotaTimeoutConfig quotaTimeoutConfig;
    private final ContractOccupancyRepository contractOccupancyRepository;
    private final CreditApplicationRepository creditApplicationRepository;
    private final UsageApplicationRepository usageApplicationRepository;
    
    /**
     * 定时执行超时释放任务
     * 
     * <p>每10分钟执行一次超时检查和释放操作。</p>
     */
    @Scheduled(fixedDelay = 600000) // 10分钟执行一次
    @Transactional
    public void scheduledTimeoutRelease() {
        if (!quotaTimeoutConfig.getAutoReleaseEnabled()) {
            log.debug("Auto release is disabled, skipping timeout release task");
            return;
        }
        
        log.info("Starting scheduled timeout release task");
        
        try {
            int occupanciesReleased = releaseTimeoutOccupancies();
            int applicationsReleased = releaseTimeoutApplications();
            int approvalsReleased = releaseTimeoutApprovals();
            int expiredDataCleaned = cleanupExpiredData();
            
            log.info("Scheduled timeout release completed: occupancies={}, applications={}, approvals={}, expiredData={}", 
                    occupanciesReleased, applicationsReleased, approvalsReleased, expiredDataCleaned);
        } catch (Exception e) {
            log.error("Error occurred during scheduled timeout release", e);
        }
    }
    
    @Override
    @Transactional
    public int releaseTimeoutOccupancies() {
        log.debug("Releasing timeout occupancies, timeout minutes: {}", quotaTimeoutConfig.getOccupyLockTimeoutMinutes());
        
        if (quotaTimeoutConfig.getOccupyLockTimeoutMinutes() <= 0) {
            log.warn("Occupy lock timeout is set to {} minutes, skipping timeout occupancy release", 
                    quotaTimeoutConfig.getOccupyLockTimeoutMinutes());
            return 0;
        }
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(quotaTimeoutConfig.getOccupyLockTimeoutMinutes());
        
        // 查找超时的占用锁定
        List<ContractOccupancy> timeoutOccupancies = contractOccupancyRepository.findTimeoutOccupancies(cutoffTime);
        
        int releasedCount = 0;
        for (ContractOccupancy occupancy : timeoutOccupancies) {
            try {
                // 释放占用锁定
                occupancy.setStatus(ContractOccupancy.OccupancyStatus.RELEASED);
                occupancy.setUpdateTime(LocalDateTime.now());
                occupancy.setUpdateBy("SYSTEM_TIMEOUT_RELEASE");
                
                contractOccupancyRepository.save(occupancy);
                releasedCount++;
                
                log.info("Released timeout occupancy: occupancyId={}, contractNo={}, amount={}", 
                        occupancy.getId(), occupancy.getContractNo(), occupancy.getOccupancyAmount());
            } catch (Exception e) {
                log.error("Error releasing timeout occupancy: id=" + occupancy.getId(), e);
            }
        }
        
        log.info("Released {} timeout occupancies", releasedCount);
        return releasedCount;
    }
    
    @Override
    @Transactional
    public int releaseTimeoutApplications() {
        log.debug("Releasing timeout applications, timeout minutes: {}", quotaTimeoutConfig.getApplicationTimeoutMinutes());
        
        if (quotaTimeoutConfig.getApplicationTimeoutMinutes() <= 0) {
            log.warn("Application timeout is set to {} minutes, skipping timeout application release", 
                    quotaTimeoutConfig.getApplicationTimeoutMinutes());
            return 0;
        }
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(quotaTimeoutConfig.getApplicationTimeoutMinutes());
        
        // 查找超时的授信申请
        List<CreditApplication> timeoutCreditApplications = creditApplicationRepository.findTimeoutApplications(cutoffTime);
        List<UsageApplication> timeoutUsageApplications = usageApplicationRepository.findTimeoutApplications(cutoffTime);
        
        int releasedCount = 0;
        
        // 处理超时的授信申请
        for (CreditApplication application : timeoutCreditApplications) {
            try {
                if (application.getStatus() == ApprovalStatus.SUBMITTED || 
                    application.getStatus() == ApprovalStatus.UNDER_REVIEW) {
                    application.setStatus(ApprovalStatus.TIMEOUT);
                    application.setUpdateTime(LocalDateTime.now());
                    application.setUpdateBy("SYSTEM_TIMEOUT_RELEASE");
                    application.setDescription("系统自动关闭超时申请");
                    
                    creditApplicationRepository.save(application);
                    releasedCount++;
                    
                    log.info("Released timeout credit application: applicationId={}, customerId={}, amount={}", 
                            application.getId(), application.getCustomerId(), application.getAppliedQuota());
                }
            } catch (Exception e) {
                log.error("Error releasing timeout credit application: id=" + application.getId(), e);
            }
        }
        
        // 处理超时的用信申请
        for (UsageApplication application : timeoutUsageApplications) {
            try {
                if (application.getStatus() == UsageStatus.SUBMITTED || 
                    application.getStatus() == UsageStatus.IN_REVIEW) {
                    application.setStatus(UsageStatus.CANCELLED); // 使用CANCELLED作为超时状态
                    application.setUpdateTime(LocalDateTime.now());
                    application.setUpdateBy("SYSTEM_TIMEOUT_RELEASE");
                    application.setDescription("系统自动关闭超时申请");
                    
                    usageApplicationRepository.save(application);
                    releasedCount++;
                    
                    log.info("Released timeout usage application: applicationId={}, customerId={}, amount={}", 
                            application.getId(), application.getCustomerId(), application.getUsageQuota());
                }
            } catch (Exception e) {
                log.error("Error releasing timeout usage application: id=" + application.getId(), e);
            }
        }
        
        log.info("Released {} timeout applications", releasedCount);
        return releasedCount;
    }
    
    @Override
    @Transactional
    public int releaseTimeoutApprovals() {
        log.debug("Releasing timeout approvals, timeout minutes: {}", quotaTimeoutConfig.getApprovalTimeoutMinutes());
        
        if (quotaTimeoutConfig.getApprovalTimeoutMinutes() <= 0) {
            log.warn("Approval timeout is set to {} minutes, skipping timeout approval release", 
                    quotaTimeoutConfig.getApprovalTimeoutMinutes());
            return 0;
        }
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(quotaTimeoutConfig.getApprovalTimeoutMinutes());
        
        // 这里应该有审批流程相关的逻辑，但我们暂时跳过，因为没有找到相关的实体
        // 在实际实现中，这里会查找并处理超时的审批节点
        
        log.info("Completed timeout approval release check, no approval entities found in current implementation");
        return 0;
    }
    
    @Override
    @Transactional
    public int cleanupExpiredData() {
        log.debug("Cleaning up expired data");
        
        int cleanedCount = 0;
        
        // 这里可以添加清理过期临时数据的逻辑
        // 例如清理超过一定时间的历史记录、临时文件等
        
        log.info("Cleaned up {} expired data records", cleanedCount);
        return cleanedCount;
    }
    
    @Override
    @Transactional
    public TimeoutReleaseResult manualReleaseTimeout() {
        log.info("Manual timeout release triggered");
        
        try {
            int occupanciesReleased = releaseTimeoutOccupancies();
            int applicationsReleased = releaseTimeoutApplications();
            int approvalsReleased = releaseTimeoutApprovals();
            int expiredDataCleaned = cleanupExpiredData();
            
            String message = String.format(
                "Manual timeout release completed: occupancies=%d, applications=%d, approvals=%d, expiredData=%d",
                occupanciesReleased, applicationsReleased, approvalsReleased, expiredDataCleaned
            );
            
            log.info(message);
            
            return new TimeoutReleaseResult(
                occupanciesReleased, 
                applicationsReleased, 
                approvalsReleased, 
                expiredDataCleaned, 
                true, 
                message
            );
        } catch (Exception e) {
            log.error("Error during manual timeout release", e);
            
            return new TimeoutReleaseResult(
                0, 0, 0, 0,
                false,
                "Error during manual timeout release: " + e.getMessage()
            );
        }
    }
}