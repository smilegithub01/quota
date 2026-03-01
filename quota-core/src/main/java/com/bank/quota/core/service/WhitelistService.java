package com.bank.quota.core.service;

import com.bank.quota.core.domain.Whitelist;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WhitelistService {
    
    Whitelist applyWhitelist(Whitelist whitelist);
    
    void approveWhitelist(String whitelistNo, boolean approved, String approveRemark, String approver);
    
    void invalidateWhitelist(String whitelistNo, String invalidateReason, String operator);
    
    Optional<Whitelist> getByWhitelistNo(String whitelistNo);
    
    List<Whitelist> getActiveByCustomerId(Long customerId);
    
    List<Whitelist> getActiveByCustomerIdAndBusinessType(Long customerId, String businessType);
    
    List<Whitelist> getAllActive(LocalDateTime now);
    
    List<Whitelist> getByStatus(Whitelist.WhitelistStatus status);
    
    List<Whitelist> getByApplicant(String applicant);
    
    boolean isWhitelisted(Long customerId, String businessType);
    
    Whitelist checkWhitelist(Long customerId, String businessType, java.math.BigDecimal amount);
}
