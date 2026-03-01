package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.domain.Whitelist;
import com.bank.quota.core.repository.WhitelistRepository;
import com.bank.quota.core.service.WhitelistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhitelistServiceImpl implements WhitelistService {
    
    private final WhitelistRepository whitelistRepository;
    
    @Override
    @Transactional
    public Whitelist applyWhitelist(Whitelist whitelist) {
        log.info("Applying whitelist for customer: {}, type: {}", 
                whitelist.getCustomerId(), whitelist.getWhitelistType());
        
        if (whitelist.getEffectiveTime().isAfter(whitelist.getExpiryTime())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                    "生效时间必须早于失效时间");
        }
        
        whitelist.setStatus(Whitelist.WhitelistStatus.PENDING);
        whitelist.setApplyTime(LocalDateTime.now());
        
        Whitelist saved = whitelistRepository.save(whitelist);
        log.info("Whitelist applied successfully, whitelistNo: {}", saved.getWhitelistNo());
        
        return saved;
    }
    
    @Override
    @Transactional
    public void approveWhitelist(String whitelistNo, boolean approved, String approveRemark, String approver) {
        log.info("Approving whitelist: {}, approved: {}, approver: {}", 
                whitelistNo, approved, approver);
        
        Whitelist whitelist = whitelistRepository.findByWhitelistNo(whitelistNo)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.WHITELIST_NOT_FOUND.getCode(),
                        "白名单不存在: " + whitelistNo));
        
        if (whitelist.getStatus() != Whitelist.WhitelistStatus.PENDING) {
            throw new BusinessException(ErrorCode.WHITELIST_INVALID.getCode(),
                    "只有待审批状态的白名单可以审批");
        }
        
        whitelist.setApprover(approver);
        whitelist.setApproveRemark(approveRemark);
        whitelist.setApproveTime(LocalDateTime.now());
        
        if (approved) {
            whitelist.setStatus(Whitelist.WhitelistStatus.ACTIVE);
            log.info("Whitelist approved successfully: {}", whitelistNo);
        } else {
            whitelist.setStatus(Whitelist.WhitelistStatus.INVALID);
            log.info("Whitelist rejected: {}, remark: {}", whitelistNo, approveRemark);
        }
        
        whitelistRepository.save(whitelist);
    }
    
    @Override
    @Transactional
    public void invalidateWhitelist(String whitelistNo, String invalidateReason, String operator) {
        log.info("Invalidating whitelist: {}, reason: {}, operator: {}", 
                whitelistNo, invalidateReason, operator);
        
        Whitelist whitelist = whitelistRepository.findByWhitelistNo(whitelistNo)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.WHITELIST_NOT_FOUND.getCode(),
                        "白名单不存在: " + whitelistNo));
        
        if (whitelist.getStatus() == Whitelist.WhitelistStatus.INVALID) {
            throw new BusinessException(ErrorCode.WHITELIST_INVALID.getCode(),
                    "白名单已失效");
        }
        
        whitelist.setStatus(Whitelist.WhitelistStatus.INVALID);
        whitelist.setApproveRemark(invalidateReason);
        whitelist.setApprover(operator);
        whitelist.setApproveTime(LocalDateTime.now());
        
        whitelistRepository.save(whitelist);
        log.info("Whitelist invalidated successfully: {}", whitelistNo);
    }
    
    @Override
    public Optional<Whitelist> getByWhitelistNo(String whitelistNo) {
        return whitelistRepository.findByWhitelistNo(whitelistNo);
    }
    
    @Override
    public List<Whitelist> getActiveByCustomerId(Long customerId) {
        return whitelistRepository.findActiveByCustomerId(customerId, LocalDateTime.now());
    }
    
    @Override
    public List<Whitelist> getActiveByCustomerIdAndBusinessType(Long customerId, String businessType) {
        return whitelistRepository.findActiveByCustomerIdAndBusinessType(
                customerId, businessType, LocalDateTime.now());
    }
    
    @Override
    public List<Whitelist> getAllActive(LocalDateTime now) {
        return whitelistRepository.findAllActive(now);
    }
    
    @Override
    public List<Whitelist> getByStatus(Whitelist.WhitelistStatus status) {
        return whitelistRepository.findByStatus(status);
    }
    
    @Override
    public List<Whitelist> getByApplicant(String applicant) {
        return whitelistRepository.findByApplicant(applicant);
    }
    
    @Override
    public boolean isWhitelisted(Long customerId, String businessType) {
        List<Whitelist> whitelists = getActiveByCustomerIdAndBusinessType(customerId, businessType);
        return !whitelists.isEmpty();
    }
    
    @Override
    public Whitelist checkWhitelist(Long customerId, String businessType, BigDecimal amount) {
        log.debug("Checking whitelist for customer: {}, businessType: {}, amount: {}", 
                customerId, businessType, amount);
        
        List<Whitelist> whitelists = getActiveByCustomerIdAndBusinessType(customerId, businessType);
        
        if (whitelists.isEmpty()) {
            whitelists = getActiveByCustomerId(customerId);
        }
        
        if (whitelists.isEmpty()) {
            return null;
        }
        
        Whitelist whitelist = whitelists.get(0);
        
        switch (whitelist.getExemptRule()) {
            case FULL:
                log.info("Customer {} fully whitelisted for business {}", customerId, businessType);
                return whitelist;
                
            case PARTIAL:
                if (MonetaryUtils.isLessThanOrEqual(amount, whitelist.getExemptAmount())) {
                    log.info("Customer {} partially whitelisted, exempt amount: {}", 
                            customerId, whitelist.getExemptAmount());
                    return whitelist;
                }
                break;
                
            case THRESHOLD:
                if (MonetaryUtils.isLessThanOrEqual(amount, whitelist.getExemptAmount())) {
                    log.info("Customer {} whitelisted by threshold: {}", 
                            customerId, whitelist.getExemptAmount());
                    return whitelist;
                }
                break;
        }
        
        return null;
    }
}
