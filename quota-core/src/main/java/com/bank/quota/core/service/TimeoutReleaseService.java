package com.bank.quota.core.service;

import com.bank.quota.core.dto.quota.QuotaUsageDetailResponse;

import java.util.List;

/**
 * 锁定超时释放服务
 * 
 * <p>提供银行级信贷额度管控平台的锁定超时释放功能。
 * 自动释放超过指定时间未处理的占用锁定。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
public interface TimeoutReleaseService {
    
    /**
     * 释放超时的占用锁定
     * 
     * <p>扫描并释放超过设定时间未处理的占用锁定。</p>
     * 
     * @return 释放的占用锁定数量
     */
    int releaseTimeoutOccupancies();
    
    /**
     * 释放超时的申请锁定
     * 
     * <p>扫描并释放超过设定时间未处理的申请锁定。</p>
     * 
     * @return 释放的申请锁定数量
     */
    int releaseTimeoutApplications();
    
    /**
     * 释放超时的审批节点
     * 
     * <p>扫描并处理超过设定时间未处理的审批节点。</p>
     * 
     * @return 处理的审批节点数量
     */
    int releaseTimeoutApprovals();
    
    /**
     * 清理过期的临时数据
     * 
     * <p>清理系统中过期的临时数据和历史记录。</p>
     * 
     * @return 清理的数据数量
     */
    int cleanupExpiredData();
    
    /**
     * 手动触发超时释放
     * 
     * <p>手动触发一次完整的超时释放流程。</p>
     * 
     * @return 超时释放的详细结果
     */
    TimeoutReleaseResult manualReleaseTimeout();
    
    /**
     * 超时释放结果
     */
    class TimeoutReleaseResult {
        private int occupanciesReleased;
        private int applicationsReleased;
        private int approvalsReleased;
        private int expiredDataCleaned;
        private boolean success;
        private String message;
        
        public TimeoutReleaseResult(int occupanciesReleased, int applicationsReleased, 
                                   int approvalsReleased, int expiredDataCleaned, 
                                   boolean success, String message) {
            this.occupanciesReleased = occupanciesReleased;
            this.applicationsReleased = applicationsReleased;
            this.approvalsReleased = approvalsReleased;
            this.expiredDataCleaned = expiredDataCleaned;
            this.success = success;
            this.message = message;
        }
        
        // Getters
        public int getOccupanciesReleased() { return occupanciesReleased; }
        public int getApplicationsReleased() { return applicationsReleased; }
        public int getApprovalsReleased() { return approvalsReleased; }
        public int getExpiredDataCleaned() { return expiredDataCleaned; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}