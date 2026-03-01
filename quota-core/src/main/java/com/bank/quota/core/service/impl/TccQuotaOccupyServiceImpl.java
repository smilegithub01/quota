package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.core.domain.*;
import com.bank.quota.core.dto.quotacontrol.QuotaOccupyRequest;
import com.bank.quota.core.dto.quotacontrol.QuotaOccupyResult;
import com.bank.quota.core.enums.QuotaStatus;
import com.bank.quota.core.repository.*;
import com.bank.quota.core.service.QuotaValidationService;
import com.bank.quota.core.service.TccQuotaOccupyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class TccQuotaOccupyServiceImpl implements TccQuotaOccupyService {

    private final TccTransactionLogRepository tccTransactionLogRepository;
    private final GroupQuotaRepository groupQuotaRepository;
    private final GroupQuotaSubRepository groupQuotaSubRepository;
    private final CustomerQuotaRepository customerQuotaRepository;
    private final CustomerQuotaSubRepository customerQuotaSubRepository;
    private final ApprovalQuotaRepository approvalQuotaRepository;
    private final ContractOccupancyRepository contractOccupancyRepository;
    private final QuotaValidationService validationService;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    private static final String LOCK_PREFIX = "quota:lock:";
    private static final long LOCK_WAIT_TIME = 10;
    private static final long LOCK_LEASE_TIME = 60;

    @Override
    @Transactional
    public QuotaOccupyResult tryOccupy(QuotaOccupyRequest request) {
        String xid = generateXid();
        log.info("Starting TCC Try phase: xid={}, customerId={}, amount={}", 
                xid, request.getCustomerId(), request.getAmount());

        List<RLock> locks = acquireAllLocks(request);
        
        try {
            LockResultSnapshot snapshot = performTryOccupy(request, xid);
            
            TccTransactionLog transactionLog = TccTransactionLog.builder()
                    .xid(xid)
                    .transactionName("QUOTA_OCCUPY")
                    .status(TccTransactionLog.TransactionStatus.TRY)
                    .lockResults(objectMapper.writeValueAsString(snapshot))
                    .build();
            tccTransactionLogRepository.save(transactionLog);

            return QuotaOccupyResult.builder()
                    .success(true)
                    .xid(xid)
                    .contractNo(request.getContractNo())
                    .occupyAmount(request.getAmount())
                    .status("PRE_OCCUPIED")
                    .message("额度预占成功")
                    .occupyTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("TCC Try phase failed: xid={}, error={}", xid, e.getMessage(), e);
            releaseAllLocks(locks);
            throw new BusinessException(ErrorCode.QUOTA_OCCUPY_FAILED.getCode(), "额度预占失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean confirmOccupy(String xid) {
        log.info("Starting TCC Confirm phase: xid={}", xid);

        TccTransactionLog transactionLog = tccTransactionLogRepository.findByXid(xid)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND.getCode(), "事务不存在: " + xid));

        if (transactionLog.getStatus() == TccTransactionLog.TransactionStatus.CONFIRMED) {
            log.warn("Transaction already confirmed: xid={}", xid);
            return true;
        }

        try {
            LockResultSnapshot snapshot = objectMapper.readValue(
                    transactionLog.getLockResults(), LockResultSnapshot.class);

            confirmGroupQuota(snapshot.getGroupQuotaId(), snapshot.getAmount());
            confirmCustomerQuota(snapshot.getCustomerQuotaId(), snapshot.getAmount());
            confirmApprovalQuota(snapshot.getApprovalQuotaId(), snapshot.getAmount());
            confirmContractOccupancy(snapshot.getContractNo());

            transactionLog.setStatus(TccTransactionLog.TransactionStatus.CONFIRMED);
            transactionLog.setUpdateTime(LocalDateTime.now());
            tccTransactionLogRepository.save(transactionLog);

            log.info("TCC Confirm phase completed: xid={}", xid);
            return true;

        } catch (Exception e) {
            log.error("TCC Confirm phase failed: xid={}, error={}", xid, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean cancelOccupy(String xid) {
        log.info("Starting TCC Cancel phase: xid={}", xid);

        TccTransactionLog transactionLog = tccTransactionLogRepository.findByXid(xid)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND.getCode(), "事务不存在: " + xid));

        if (transactionLog.getStatus() == TccTransactionLog.TransactionStatus.CANCELLED) {
            log.warn("Transaction already cancelled: xid={}", xid);
            return true;
        }

        try {
            LockResultSnapshot snapshot = objectMapper.readValue(
                    transactionLog.getLockResults(), LockResultSnapshot.class);

            cancelGroupQuota(snapshot.getGroupQuotaId(), snapshot.getAmount());
            cancelCustomerQuota(snapshot.getCustomerQuotaId(), snapshot.getAmount());
            cancelApprovalQuota(snapshot.getApprovalQuotaId(), snapshot.getAmount());
            cancelContractOccupancy(snapshot.getContractNo());

            transactionLog.setStatus(TccTransactionLog.TransactionStatus.CANCELLED);
            transactionLog.setUpdateTime(LocalDateTime.now());
            tccTransactionLogRepository.save(transactionLog);

            log.info("TCC Cancel phase completed: xid={}", xid);
            return true;

        } catch (Exception e) {
            log.error("TCC Cancel phase failed: xid={}, error={}", xid, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public QuotaOccupyResult occupyWithTcc(QuotaOccupyRequest request) {
        log.info("Starting TCC quota occupy process: customerId={}, amount={}", 
                request.getCustomerId(), request.getAmount());

        QuotaOccupyResult tryResult = tryOccupy(request);
        
        if (!tryResult.isSuccess()) {
            return tryResult;
        }

        boolean confirmed = confirmOccupy(tryResult.getXid());
        
        if (confirmed) {
            return QuotaOccupyResult.builder()
                    .success(true)
                    .xid(tryResult.getXid())
                    .contractNo(request.getContractNo())
                    .occupyAmount(request.getAmount())
                    .status("OCCUPIED")
                    .message("额度占用成功")
                    .occupyTime(LocalDateTime.now())
                    .build();
        } else {
            cancelOccupy(tryResult.getXid());
            return QuotaOccupyResult.builder()
                    .success(false)
                    .xid(tryResult.getXid())
                    .message("额度占用失败，已回滚")
                    .build();
        }
    }

    private String generateXid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private List<RLock> acquireAllLocks(QuotaOccupyRequest request) {
        List<RLock> locks = new ArrayList<>();

        if (request.getGroupId() != null) {
            RLock groupLock = redissonClient.getLock(LOCK_PREFIX + "group:" + request.getGroupId());
            locks.add(groupLock);
        }

        RLock customerLock = redissonClient.getLock(LOCK_PREFIX + "customer:" + request.getCustomerId());
        locks.add(customerLock);

        if (request.getApprovalId() != null) {
            RLock approvalLock = redissonClient.getLock(LOCK_PREFIX + "approval:" + request.getApprovalId());
            locks.add(approvalLock);
        }

        for (RLock lock : locks) {
            try {
                boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
                if (!acquired) {
                    releaseAllLocks(locks);
                    throw new BusinessException(ErrorCode.LOCK_ACQUIRE_FAILED.getCode(), "获取分布式锁失败");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                releaseAllLocks(locks);
                throw new BusinessException(ErrorCode.LOCK_ACQUIRE_FAILED.getCode(), "获取分布式锁被中断");
            }
        }

        return locks;
    }

    private void releaseAllLocks(List<RLock> locks) {
        for (RLock lock : locks) {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private LockResultSnapshot performTryOccupy(QuotaOccupyRequest request, String xid) {
        BigDecimal amount = request.getAmount();
        LockResultSnapshot snapshot = new LockResultSnapshot();
        snapshot.setAmount(amount);
        snapshot.setContractNo(request.getContractNo());
        snapshot.setXid(xid);

        if (request.getGroupId() != null) {
            GroupQuota groupQuota = groupQuotaRepository.findByGroupIdWithLock(request.getGroupId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.QUOTA_NOT_FOUND.getCode(), "集团额度不存在"));
            
            if (groupQuota.getAvailableQuota().compareTo(amount) < 0) {
                throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(), "集团额度不足");
            }
            
            groupQuota.setLockedQuota(groupQuota.getLockedQuota().add(amount));
            groupQuota.setAvailableQuota(groupQuota.getAvailableQuota().subtract(amount));
            groupQuotaRepository.save(groupQuota);
            
            snapshot.setGroupQuotaId(groupQuota.getId());
        }

        CustomerQuota customerQuota = customerQuotaRepository.findByCustomerIdWithLock(request.getCustomerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.QUOTA_NOT_FOUND.getCode(), "客户额度不存在"));
        
        if (customerQuota.getStatus() != QuotaStatus.ENABLED) {
            throw new BusinessException(ErrorCode.QUOTA_FROZEN.getCode(), "客户额度已冻结或停用");
        }
        
        if (customerQuota.getAvailableQuota().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(), "客户额度不足");
        }
        
        customerQuota.setLockedQuota(customerQuota.getLockedQuota().add(amount));
        customerQuota.setAvailableQuota(customerQuota.getAvailableQuota().subtract(amount));
        customerQuotaRepository.save(customerQuota);
        
        snapshot.setCustomerQuotaId(customerQuota.getId());

        if (request.getApprovalId() != null) {
            ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(request.getApprovalId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.QUOTA_NOT_FOUND.getCode(), "批复额度不存在"));
            
            if (approvalQuota.getAvailableQuota().compareTo(amount) < 0) {
                throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(), "批复额度不足");
            }
            
            approvalQuota.setLockedQuota(approvalQuota.getLockedQuota().add(amount));
            approvalQuota.setAvailableQuota(approvalQuota.getAvailableQuota().subtract(amount));
            approvalQuotaRepository.save(approvalQuota);
            
            snapshot.setApprovalQuotaId(approvalQuota.getId());
        }

        ContractOccupancy occupancy = ContractOccupancy.builder()
                .contractNo(request.getContractNo())
                .customerId(request.getCustomerId())
                .occupancyAmount(amount)
                .status(ContractOccupancy.OccupancyStatus.PRE_OCCUPIED)
                .transactionId(xid)
                .build();
        contractOccupancyRepository.save(occupancy);

        return snapshot;
    }

    private void confirmGroupQuota(Long groupQuotaId, BigDecimal amount) {
        if (groupQuotaId == null) return;
        
        GroupQuota groupQuota = groupQuotaRepository.findByIdWithLock(groupQuotaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUOTA_NOT_FOUND.getCode(), "集团额度不存在"));
        
        groupQuota.setLockedQuota(groupQuota.getLockedQuota().subtract(amount));
        groupQuota.setUsedQuota(groupQuota.getUsedQuota().add(amount));
        groupQuota.setUpdateTime(LocalDateTime.now());
        groupQuotaRepository.save(groupQuota);
    }

    private void confirmCustomerQuota(Long customerQuotaId, BigDecimal amount) {
        CustomerQuota customerQuota = customerQuotaRepository.findByIdWithLock(customerQuotaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUOTA_NOT_FOUND.getCode(), "客户额度不存在"));
        
        customerQuota.setLockedQuota(customerQuota.getLockedQuota().subtract(amount));
        customerQuota.setUsedQuota(customerQuota.getUsedQuota().add(amount));
        customerQuota.setUpdateTime(LocalDateTime.now());
        customerQuotaRepository.save(customerQuota);
    }

    private void confirmApprovalQuota(Long approvalQuotaId, BigDecimal amount) {
        if (approvalQuotaId == null) return;
        
        ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(approvalQuotaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUOTA_NOT_FOUND.getCode(), "批复额度不存在"));
        
        approvalQuota.setLockedQuota(approvalQuota.getLockedQuota().subtract(amount));
        approvalQuota.setUsedQuota(approvalQuota.getUsedQuota().add(amount));
        approvalQuota.setUpdateTime(LocalDateTime.now());
        approvalQuotaRepository.save(approvalQuota);
    }

    private void confirmContractOccupancy(String contractNo) {
        ContractOccupancy occupancy = contractOccupancyRepository.findByContractNo(contractNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTRACT_NOT_FOUND.getCode(), "合同占用记录不存在"));
        
        occupancy.setStatus(ContractOccupancy.OccupancyStatus.OCCUPIED);
        occupancy.setUpdateTime(LocalDateTime.now());
        contractOccupancyRepository.save(occupancy);
    }

    private void cancelGroupQuota(Long groupQuotaId, BigDecimal amount) {
        if (groupQuotaId == null) return;
        
        GroupQuota groupQuota = groupQuotaRepository.findByIdWithLock(groupQuotaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUOTA_NOT_FOUND.getCode(), "集团额度不存在"));
        
        groupQuota.setLockedQuota(groupQuota.getLockedQuota().subtract(amount));
        groupQuota.setAvailableQuota(groupQuota.getAvailableQuota().add(amount));
        groupQuota.setUpdateTime(LocalDateTime.now());
        groupQuotaRepository.save(groupQuota);
    }

    private void cancelCustomerQuota(Long customerQuotaId, BigDecimal amount) {
        CustomerQuota customerQuota = customerQuotaRepository.findByIdWithLock(customerQuotaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUOTA_NOT_FOUND.getCode(), "客户额度不存在"));
        
        customerQuota.setLockedQuota(customerQuota.getLockedQuota().subtract(amount));
        customerQuota.setAvailableQuota(customerQuota.getAvailableQuota().add(amount));
        customerQuota.setUpdateTime(LocalDateTime.now());
        customerQuotaRepository.save(customerQuota);
    }

    private void cancelApprovalQuota(Long approvalQuotaId, BigDecimal amount) {
        if (approvalQuotaId == null) return;
        
        ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(approvalQuotaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUOTA_NOT_FOUND.getCode(), "批复额度不存在"));
        
        approvalQuota.setLockedQuota(approvalQuota.getLockedQuota().subtract(amount));
        approvalQuota.setAvailableQuota(approvalQuota.getAvailableQuota().add(amount));
        approvalQuota.setUpdateTime(LocalDateTime.now());
        approvalQuotaRepository.save(approvalQuota);
    }

    private void cancelContractOccupancy(String contractNo) {
        ContractOccupancy occupancy = contractOccupancyRepository.findByContractNo(contractNo)
                .orElse(null);
        
        if (occupancy != null) {
            occupancy.setStatus(ContractOccupancy.OccupancyStatus.CANCELLED);
            occupancy.setUpdateTime(LocalDateTime.now());
            contractOccupancyRepository.save(occupancy);
        }
    }

    public static class LockResultSnapshot implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String xid;
        private String contractNo;
        private BigDecimal amount;
        private Long groupQuotaId;
        private Long customerQuotaId;
        private Long approvalQuotaId;
        
        public LockResultSnapshot() {}
        
        public LockResultSnapshot(String xid, String contractNo, BigDecimal amount, 
                Long groupQuotaId, Long customerQuotaId, Long approvalQuotaId) {
            this.xid = xid;
            this.contractNo = contractNo;
            this.amount = amount;
            this.groupQuotaId = groupQuotaId;
            this.customerQuotaId = customerQuotaId;
            this.approvalQuotaId = approvalQuotaId;
        }

        public String getXid() { return xid; }
        public void setXid(String xid) { this.xid = xid; }

        public String getContractNo() { return contractNo; }
        public void setContractNo(String contractNo) { this.contractNo = contractNo; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public Long getGroupQuotaId() { return groupQuotaId; }
        public void setGroupQuotaId(Long groupQuotaId) { this.groupQuotaId = groupQuotaId; }

        public Long getCustomerQuotaId() { return customerQuotaId; }
        public void setCustomerQuotaId(Long customerQuotaId) { this.customerQuotaId = customerQuotaId; }

        public Long getApprovalQuotaId() { return approvalQuotaId; }
        public void setApprovalQuotaId(Long approvalQuotaId) { this.approvalQuotaId = approvalQuotaId; }
    }
}
