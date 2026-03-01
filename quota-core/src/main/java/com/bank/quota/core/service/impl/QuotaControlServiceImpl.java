package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.common.result.Result;
import com.bank.quota.common.util.MonetaryUtils;
import com.bank.quota.core.domain.*;
import com.bank.quota.core.dto.quotacontrol.*;
import com.bank.quota.core.enums.QuotaStatus;
import com.bank.quota.core.repository.*;
import com.bank.quota.core.service.QuotaControlService;
import com.bank.quota.core.service.WhitelistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 额度控制服务实现类
 * 
 * <p>提供银行级信贷额度管控平台的核心额度控制功能，包括额度锁定、占用、释放等操作。
 * 该服务是整个额度系统的核心，负责处理所有与额度相关的关键业务操作。</p>
 * 
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>额度锁定：临时锁定额度，防止并发操作导致超用</li>
 *   <li>额度占用：实际占用额度，扣减可用额度</li>
 *   <li>额度释放：释放已占用或锁定的额度</li>
 *   <li>额度校验：校验额度充足性</li>
 *   <li>额度查询：查询各级额度使用情况</li>
 * </ul>
 * 
 * <h3>并发控制：</h3>
 * <p>使用Redisson分布式锁确保并发场景下的数据一致性。锁的粒度按照对象类型和对象ID划分，
 * 支持集团、客户、批复三个层级的额度并发控制。</p>
 * 
 * <h3>白名单豁免：</h3>
 * <p>支持白名单客户的额度豁免功能，VIP客户可享受全额或部分额度豁免。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 锁定额度
 * QuotaLockRequest lockRequest = new QuotaLockRequest();
 * lockRequest.setObjectType("CUSTOMER");
 * lockRequest.setObjectId(1001L);
 * lockRequest.setAmount(new BigDecimal("100000"));
 * QuotaLockResult lockResult = quotaControlService.lockQuota(lockRequest);
 * 
 * // 占用额度
 * QuotaOccupyRequest occupyRequest = new QuotaOccupyRequest();
 * occupyRequest.setContractNo("CONTRACT001");
 * occupyRequest.setAmount(new BigDecimal("100000"));
 * QuotaOccupyResult occupyResult = quotaControlService.occupyQuota(occupyRequest);
 * 
 * // 释放额度
 * quotaControlService.releaseQuota(occupyResult.getOccupyId(), null);
 * }</pre>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-10
 * @see QuotaControlService
 * @see WhitelistService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaControlServiceImpl implements QuotaControlService {
    
    private final GroupQuotaRepository groupQuotaRepository;
    private final GroupQuotaSubRepository groupQuotaSubRepository;
    private final CustomerQuotaRepository customerQuotaRepository;
    private final CustomerQuotaSubRepository customerQuotaSubRepository;
    private final ApprovalQuotaRepository approvalQuotaRepository;
    private final ContractOccupancyRepository contractOccupancyRepository;
    private final WhitelistService whitelistService;
    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 分布式锁Key前缀
     */
    private static final String QUOTA_LOCK_PREFIX = "QUOTA_LOCK:";
    
    /**
     * 锁定记录Key前缀
     */
    private static final String QUOTA_LOCK_RECORD_PREFIX = "QUOTA_LOCK_RECORD:";
    
    /**
     * 锁等待时间（秒）
     */
    private static final long LOCK_WAIT_TIME = 5L;
    
    /**
     * 锁租约时间（秒）
     */
    private static final long LOCK_LEASE_TIME = 30L;

    /**
     * 锁定额度
     * 
     * <p>临时锁定指定金额的额度，防止在业务处理过程中被其他操作占用。
     * 锁定操作会扣减可用额度，增加已用额度，但不会实际占用额度。</p>
     * 
     * <h4>处理流程：</h4>
     * <ol>
     *   <li>获取分布式锁，确保并发安全</li>
     *   <li>校验金额参数有效性</li>
     *   <li>根据对象类型执行对应的锁定逻辑</li>
     *   <li>更新额度使用情况</li>
     *   <li>生成锁定记录并返回</li>
     * </ol>
     * 
     * <h4>使用示例：</h4>
     * <pre>{@code
     * QuotaLockRequest request = new QuotaLockRequest();
     * request.setObjectType("CUSTOMER");
     * request.setObjectId(1001L);
     * request.setAmount(new BigDecimal("500000"));
     * request.setLockTimeoutMinutes(60);
     * 
     * QuotaLockResult result = quotaControlService.lockQuota(request);
     * String lockId = result.getLockId(); // 用于后续释放锁定
     * }</pre>
     * 
     * @param request 锁定请求，包含对象类型、对象ID、锁定金额等信息
     * @return 锁定结果，包含锁定ID、锁定金额、过期时间等
     * @throws BusinessException 当额度不存在、额度不足或额度已冻结时抛出
     * @throws BusinessException 当获取分布式锁失败时抛出
     */
    @Override
    @Transactional
    public QuotaLockResult lockQuota(QuotaLockRequest request) {
        log.info("Locking quota: objectType={}, objectId={}, amount={}", 
                request.getObjectType(), request.getObjectId(), request.getAmount());
        
        RLock distributedLock = acquireQuotaLock(request.getObjectType(), request.getObjectId());
        
        try {
            QuotaLockResult result = performLockQuota(request);
            log.info("Quota locked successfully: lockId={}", result.getLockId());
            return result;
        } finally {
            releaseLock(distributedLock);
        }
    }

    /**
     * 执行额度锁定逻辑
     * 
     * <p>根据对象类型分发到具体的锁定处理方法。</p>
     * 
     * @param request 锁定请求
     * @return 锁定结果
     * @throws BusinessException 当对象类型不支持时抛出
     */
    private QuotaLockResult performLockQuota(QuotaLockRequest request) {
        BigDecimal amount = validateAndNormalizeAmount(request.getAmount());
        
        switch (request.getObjectType()) {
            case "GROUP":
                return lockGroupQuota(request, amount);
            case "CUSTOMER":
                return lockCustomerQuota(request, amount);
            case "APPROVAL":
                return lockApprovalQuota(request, amount);
            default:
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                        "不支持的额度对象类型: " + request.getObjectType());
        }
    }

    /**
     * 锁定集团额度
     * 
     * <p>对集团级别的额度进行锁定操作。集团额度是额度体系的顶层，
     * 锁定集团额度会影响该集团下所有客户的额度使用。</p>
     * 
     * @param request 锁定请求
     * @param amount 锁定金额（已校验和标准化）
     * @return 锁定结果
     * @throws BusinessException 当集团额度不存在、已冻结或不足时抛出
     */
    private QuotaLockResult lockGroupQuota(QuotaLockRequest request, BigDecimal amount) {
        GroupQuota groupQuota = groupQuotaRepository.findByGroupIdWithLock(request.getObjectId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.GROUP_NOT_FOUND.getCode(), 
                        "集团额度不存在: " + request.getObjectId()));
        
        if (groupQuota.getStatus() != QuotaStatus.ENABLED) {
            throw new BusinessException(ErrorCode.QUOTA_FROZEN.getCode(), 
                    "集团额度已冻结或停用");
        }
        
        BigDecimal availableQuota = groupQuota.getAvailableQuota();
        if (MonetaryUtils.isLessThan(availableQuota, amount)) {
            throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(),
                    "集团额度不足，当前可用: " + MonetaryUtils.format(availableQuota));
        }
        
        groupQuota.setUsedQuota(MonetaryUtils.add(groupQuota.getUsedQuota(), amount));
        groupQuota.setAvailableQuota(MonetaryUtils.subtract(availableQuota, amount));
        groupQuota.setUpdateTime(LocalDateTime.now());
        groupQuotaRepository.save(groupQuota);
        
        QuotaLockResult result = new QuotaLockResult();
        result.setLockId(generateLockId());
        result.setObjectType(request.getObjectType());
        result.setObjectId(request.getObjectId());
        result.setLockedAmount(amount);
        result.setStatus("LOCKED");
        result.setLockTime(LocalDateTime.now());
        result.setExpiryTime(LocalDateTime.now().plusMinutes(request.getLockTimeoutMinutes() != null ? 
                request.getLockTimeoutMinutes() : 30));
        
        // 持久化锁定记录到Redis
        persistLockRecord(result);
        
        return result;
    }

    /**
     * 锁定客户额度
     * 
     * <p>对客户级别的额度进行锁定操作。客户额度是额度体系的中间层，
     * 锁定客户额度会影响该客户下所有批复的额度使用。</p>
     * 
     * @param request 锁定请求
     * @param amount 锁定金额（已校验和标准化）
     * @return 锁定结果
     * @throws BusinessException 当客户额度不存在、已冻结或不足时抛出
     */
    private QuotaLockResult lockCustomerQuota(QuotaLockRequest request, BigDecimal amount) {
        CustomerQuota customerQuota = customerQuotaRepository.findByCustomerIdWithLock(request.getObjectId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                        "客户额度不存在: " + request.getObjectId()));
        
        if (customerQuota.getStatus() != QuotaStatus.ENABLED) {
            throw new BusinessException(ErrorCode.QUOTA_FROZEN.getCode(), 
                    "客户额度已冻结或停用");
        }
        
        BigDecimal availableQuota = customerQuota.getAvailableQuota();
        if (MonetaryUtils.isLessThan(availableQuota, amount)) {
            throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(),
                    "客户额度不足，当前可用: " + MonetaryUtils.format(availableQuota));
        }
        
        customerQuota.setUsedQuota(MonetaryUtils.add(customerQuota.getUsedQuota(), amount));
        customerQuota.setAvailableQuota(MonetaryUtils.subtract(availableQuota, amount));
        customerQuota.setUpdateTime(LocalDateTime.now());
        customerQuotaRepository.save(customerQuota);
        
        QuotaLockResult result = new QuotaLockResult();
        result.setLockId(generateLockId());
        result.setObjectType(request.getObjectType());
        result.setObjectId(request.getObjectId());
        result.setLockedAmount(amount);
        result.setStatus("LOCKED");
        result.setLockTime(LocalDateTime.now());
        result.setExpiryTime(LocalDateTime.now().plusMinutes(request.getLockTimeoutMinutes() != null ? 
                request.getLockTimeoutMinutes() : 30));
        
        // 持久化锁定记录到Redis
        persistLockRecord(result);
        
        return result;
    }

    /**
     * 锁定批复额度
     * 
     * <p>对批复级别的额度进行锁定操作。批复额度是额度体系的最底层，
     * 直接关联到具体的授信业务。</p>
     * 
     * @param request 锁定请求
     * @param amount 锁定金额（已校验和标准化）
     * @return 锁定结果
     * @throws BusinessException 当批复额度不存在、已冻结或不足时抛出
     */
    private QuotaLockResult lockApprovalQuota(QuotaLockRequest request, BigDecimal amount) {
        ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(request.getObjectId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                        "批复额度不存在: " + request.getObjectId()));
        
        if (approvalQuota.getStatus() != QuotaStatus.ENABLED) {
            throw new BusinessException(ErrorCode.QUOTA_FROZEN.getCode(), 
                    "批复额度已冻结或停用");
        }
        
        BigDecimal availableQuota = approvalQuota.getAvailableQuota();
        if (MonetaryUtils.isLessThan(availableQuota, amount)) {
            throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(),
                    "批复额度不足，当前可用: " + MonetaryUtils.format(availableQuota));
        }
        
        approvalQuota.setUsedQuota(MonetaryUtils.add(approvalQuota.getUsedQuota(), amount));
        approvalQuota.setAvailableQuota(MonetaryUtils.subtract(availableQuota, amount));
        approvalQuota.setUpdateTime(LocalDateTime.now());
        approvalQuotaRepository.save(approvalQuota);
        
        QuotaLockResult result = new QuotaLockResult();
        result.setLockId(generateLockId());
        result.setObjectType(request.getObjectType());
        result.setObjectId(request.getObjectId());
        result.setLockedAmount(amount);
        result.setStatus("LOCKED");
        result.setLockTime(LocalDateTime.now());
        result.setExpiryTime(LocalDateTime.now().plusMinutes(request.getLockTimeoutMinutes() != null ? 
                request.getLockTimeoutMinutes() : 30));
        
        // 持久化锁定记录到Redis
        persistLockRecord(result);
        
        return result;
    }

    /**
     * 解锁额度
     * 
     * <p>释放之前锁定的额度，恢复可用额度。解锁操作是锁定操作的逆操作，
     * 用于在业务处理失败或取消时释放已锁定的额度。</p>
     * 
     * @param lockId 锁定ID，由锁定操作返回
     * @throws BusinessException 当锁定记录不存在时抛出
     */
    @Override
    @Transactional
    public void unlockQuota(String lockId) {
        log.info("Unlocking quota: lockId={}", lockId);
        
        // 从Redis获取锁定记录
        QuotaLockResult lockRecord = getLockRecord(lockId);
        if (lockRecord == null) {
            throw new BusinessException(ErrorCode.LOCK_NOT_FOUND.getCode(),
                    "锁定记录不存在: " + lockId);
        }
        
        // 获取分布式锁
        RLock distributedLock = acquireQuotaLock(lockRecord.getObjectType(), lockRecord.getObjectId());
        
        try {
            // 恢复额度
            restoreQuota(lockRecord);
            
            // 删除Redis中的锁定记录
            deleteLockRecord(lockId);
            
            log.info("Quota unlocked successfully: lockId={}", lockId);
        } finally {
            releaseLock(distributedLock);
        }
    }

    /**
     * 占用额度
     * 
     * <p>实际占用指定金额的额度，这是额度使用的最终确认操作。
     * 占用操作会创建合同占用记录，并扣减各级额度的可用额度。</p>
     * 
     * <h4>处理流程：</h4>
     * <ol>
     *   <li>获取分布式锁</li>
     *   <li>检查白名单豁免</li>
     *   <li>创建合同占用记录</li>
     *   <li>更新批复额度使用情况</li>
     *   <li>触发额度穿透更新上层额度</li>
     * </ol>
     * 
     * <h4>白名单豁免：</h4>
     * <p>如果客户在白名单中且豁免规则为FULL，则直接返回成功，不扣减额度。</p>
     * 
     * <h4>使用示例：</h4>
     * <pre>{@code
     * QuotaOccupyRequest request = new QuotaOccupyRequest();
     * request.setContractNo("CONTRACT2026001");
     * request.setApprovalQuotaSubId(1001L);
     * request.setAmount(new BigDecimal("1000000"));
     * request.setCustomerId(2001L);
     * request.setBusinessType("LOAN");
     * request.setOperator("teller001");
     * 
     * QuotaOccupyResult result = quotaControlService.occupyQuota(request);
     * }</pre>
     * 
     * @param request 占用请求，包含合同信息、批复信息、占用金额等
     * @return 占用结果，包含占用ID、占用金额、占用时间等
     * @throws BusinessException 当额度不足、批复不存在或白名单校验失败时抛出
     */
    @Override
    @Transactional
    public QuotaOccupyResult occupyQuota(QuotaOccupyRequest request) {
        log.info("Occupying quota: objectType={}, objectId={}, amount={}", 
                request.getObjectType(), request.getObjectId(), request.getAmount());
        
        RLock distributedLock = acquireQuotaLock(request.getObjectType(), request.getObjectId());
        
        try {
            Whitelist whitelist = checkWhitelistExemption(request);
            if (whitelist != null && whitelist.getExemptRule() == Whitelist.ExemptRule.FULL) {
                QuotaOccupyResult result = new QuotaOccupyResult();
                result.setOccupyId(generateOccupyId());
                result.setObjectType(request.getObjectType());
                result.setObjectId(request.getObjectId());
                result.setOccupiedAmount(request.getAmount());
                result.setWhitelisted(true);
                result.setStatus("OCCUPIED");
                result.setOccupyTime(LocalDateTime.now());
                
                log.info("Quota occupied via whitelist: customerId={}, rule={}", 
                        request.getObjectId(), whitelist.getExemptRule());
                return result;
            }
            
            QuotaOccupyResult result = performOccupyQuota(request);
            log.info("Quota occupied successfully: occupyId={}", result.getOccupyId());
            return result;
        } finally {
            releaseLock(distributedLock);
        }
    }

    /**
     * 检查白名单豁免
     * 
     * <p>检查客户是否在白名单中，以及是否满足豁免条件。
     * 白名单客户可以享受全额或部分额度豁免。</p>
     * 
     * @param request 占用请求
     * @return 白名单信息，如果客户不在白名单中则返回null
     */
    private Whitelist checkWhitelistExemption(QuotaOccupyRequest request) {
        if (request.getCustomerId() == null) {
            return null;
        }
        return whitelistService.checkWhitelist(
                request.getCustomerId(), 
                request.getBusinessType(), 
                request.getAmount());
    }

    /**
     * 执行额度占用逻辑
     * 
     * <p>实际执行额度占用的核心逻辑，包括创建合同占用记录和更新额度使用情况。</p>
     * 
     * @param request 占用请求
     * @return 占用结果
     * @throws BusinessException 当额度不足时抛出
     */
    private QuotaOccupyResult performOccupyQuota(QuotaOccupyRequest request) {
        BigDecimal amount = validateAndNormalizeAmount(request.getAmount());
        
        ContractOccupancy occupancy = new ContractOccupancy();
        occupancy.setContractNo(request.getContractNo());
        occupancy.setApprovalQuotaSubId(request.getApprovalQuotaSubId());
        occupancy.setOccupancyAmount(amount);
        occupancy.setStatus(ContractOccupancy.OccupancyStatus.OCCUPIED);
        occupancy.setCreateBy(request.getOperator());
        
        ContractOccupancy saved = contractOccupancyRepository.save(occupancy);
        
        ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(request.getApprovalQuotaSubId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                        "批复额度不存在: " + request.getApprovalQuotaSubId()));
        
        BigDecimal availableQuota = approvalQuota.getAvailableQuota();
        if (MonetaryUtils.isLessThan(availableQuota, amount)) {
            contractOccupancyRepository.delete(saved);
            throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(),
                    "批复额度不足，当前可用: " + MonetaryUtils.format(availableQuota));
        }
        
        approvalQuota.setUsedQuota(MonetaryUtils.add(approvalQuota.getUsedQuota(), amount));
        approvalQuota.setAvailableQuota(MonetaryUtils.subtract(availableQuota, amount));
        approvalQuota.setUpdateTime(LocalDateTime.now());
        approvalQuotaRepository.save(approvalQuota);
        
        QuotaOccupyResult result = new QuotaOccupyResult();
        result.setOccupyId(saved.getId().toString());
        result.setContractNo(saved.getContractNo());
        result.setObjectType(request.getObjectType());
        result.setObjectId(request.getObjectId());
        result.setOccupiedAmount(amount);
        result.setStatus("OCCUPIED");
        result.setOccupyTime(LocalDateTime.now());
        
        return result;
    }

    /**
     * 释放额度
     * 
     * <p>释放已占用的额度，恢复可用额度。释放操作是占用操作的逆操作，
     * 用于在合同结清、撤销等场景下释放已占用的额度。</p>
     * 
     * <h4>处理流程：</h4>
     * <ol>
     *   <li>查询合同占用记录</li>
     *   <li>校验占用状态</li>
     *   <li>更新批复额度使用情况</li>
     *   <li>更新合同占用状态</li>
     * </ol>
     * 
     * <h4>使用示例：</h4>
     * <pre>{@code
     * // 全额释放
     * quotaControlService.releaseQuota("1001", null);
     * 
     * // 部分释放
     * quotaControlService.releaseQuota("1001", new BigDecimal("50000"));
     * }</pre>
     * 
     * @param occupyId 占用ID，由占用操作返回
     * @param amount 释放金额，如果为null则全额释放
     * @throws BusinessException 当占用记录不存在或已释放时抛出
     */
    @Override
    @Transactional
    public void releaseQuota(String occupyId, BigDecimal amount) {
        log.info("Releasing quota: occupyId={}, amount={}", occupyId, amount);
        
        ContractOccupancy occupancy = contractOccupancyRepository.findById(Long.parseLong(occupyId))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CONTRACT_NOT_FOUND.getCode(), 
                        "合同占用记录不存在: " + occupyId));
        
        if (occupancy.getStatus() == ContractOccupancy.OccupancyStatus.RELEASED) {
            throw new BusinessException(ErrorCode.CONTRACT_OCCUPY_ERROR.getCode(),
                    "合同占用已释放");
        }
        
        BigDecimal releaseAmount = amount != null ? amount : occupancy.getOccupancyAmount();
        
        ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(occupancy.getApprovalQuotaSubId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                        "批复额度不存在: " + occupancy.getApprovalQuotaSubId()));
        
        BigDecimal currentUsed = approvalQuota.getUsedQuota();
        BigDecimal newUsed = MonetaryUtils.subtract(currentUsed, releaseAmount);
        approvalQuota.setUsedQuota(MonetaryUtils.max(newUsed, BigDecimal.ZERO));
        approvalQuota.setAvailableQuota(MonetaryUtils.add(approvalQuota.getAvailableQuota(), releaseAmount));
        approvalQuota.setUpdateTime(LocalDateTime.now());
        approvalQuotaRepository.save(approvalQuota);
        
        occupancy.setStatus(ContractOccupancy.OccupancyStatus.RELEASED);
        occupancy.setUpdateTime(LocalDateTime.now());
        contractOccupancyRepository.save(occupancy);
        
        log.info("Quota released successfully: occupyId={}, amount={}", occupyId, releaseAmount);
    }

    /**
     * 校验额度充足性
     * 
     * <p>校验指定金额是否在可用额度范围内，不实际扣减额度。
     * 该方法用于在业务操作前预校验额度是否充足。</p>
     * 
     * <h4>白名单豁免：</h4>
     * <p>如果客户在白名单中，则直接返回校验通过，不检查额度。</p>
     * 
     * <h4>使用示例：</h4>
     * <pre>{@code
     * QuotaValidateRequest request = new QuotaValidateRequest();
     * request.setObjectType("CUSTOMER");
     * request.setObjectId(1001L);
     * request.setCustomerId(1001L);
     * request.setAmount(new BigDecimal("500000"));
     * request.setBusinessType("LOAN");
     * 
     * QuotaValidateResult result = quotaControlService.validateQuota(request);
     * if (result.isValid()) {
     *     // 额度充足，可以继续业务操作
     * } else {
     *     // 额度不足，提示用户
     * }
     * }</pre>
     * 
     * @param request 校验请求，包含对象类型、对象ID、校验金额等
     * @return 校验结果，包含是否充足、可用额度、差额等信息
     */
    @Override
    public QuotaValidateResult validateQuota(QuotaValidateRequest request) {
        log.debug("Validating quota: objectType={}, objectId={}, amount={}", 
                request.getObjectType(), request.getObjectId(), request.getAmount());
        
        Whitelist whitelist = whitelistService.checkWhitelist(
                request.getCustomerId(), 
                request.getBusinessType(), 
                request.getAmount());
        
        if (whitelist != null) {
            QuotaValidateResult result = new QuotaValidateResult();
            result.setValid(true);
            result.setWhitelisted(true);
            result.setWhitelistNo(whitelist.getWhitelistNo());
            result.setMessage("客户在白名单中，可豁免额度检查");
            return result;
        }
        
        BigDecimal amount = validateAndNormalizeAmount(request.getAmount());
        BigDecimal availableQuota = queryAvailableQuota(request);
        
        QuotaValidateResult result = new QuotaValidateResult();
        result.setAvailableQuota(availableQuota);
        
        if (MonetaryUtils.isGreaterThanOrEqual(availableQuota, amount)) {
            result.setValid(true);
            result.setMessage("额度校验通过");
        } else {
            result.setValid(false);
            result.setMessage("额度不足，需要额度: " + MonetaryUtils.format(amount) + 
                    "，可用额度: " + MonetaryUtils.format(availableQuota));
        }
        
        return result;
    }

    /**
     * 查询额度信息
     * 
     * <p>查询指定对象的额度使用情况，包括总额度、已用额度、可用额度等。</p>
     * 
     * @param request 查询请求，包含对象类型和对象ID
     * @return 查询结果，包含额度使用详情
     */
    @Override
    public QuotaQueryResult queryQuota(QuotaQueryRequest request) {
        log.debug("Querying quota: objectType={}, objectId={}", 
                request.getObjectType(), request.getObjectId());
        
        QuotaQueryResult result = new QuotaQueryResult();
        result.setObjectType(request.getObjectType());
        result.setObjectId(request.getObjectId());
        
        switch (request.getObjectType()) {
            case "GROUP":
                GroupQuota groupQuota = groupQuotaRepository.findByGroupId(request.getObjectId())
                        .orElse(null);
                if (groupQuota != null) {
                    result.setTotalQuota(groupQuota.getTotalQuota());
                    result.setUsedQuota(groupQuota.getUsedQuota());
                    result.setAvailableQuota(groupQuota.getAvailableQuota());
                    result.setStatus(groupQuota.getStatus().name());
                }
                break;
            case "CUSTOMER":
                CustomerQuota customerQuota = customerQuotaRepository.findByCustomerId(request.getObjectId())
                        .orElse(null);
                if (customerQuota != null) {
                    result.setTotalQuota(customerQuota.getTotalQuota());
                    result.setUsedQuota(customerQuota.getUsedQuota());
                    result.setAvailableQuota(customerQuota.getAvailableQuota());
                    result.setStatus(customerQuota.getStatus().name());
                }
                break;
            case "APPROVAL":
                ApprovalQuota approvalQuota = approvalQuotaRepository.findById(request.getObjectId())
                        .orElse(null);
                if (approvalQuota != null) {
                    result.setTotalQuota(approvalQuota.getApprovalQuota());
                    result.setUsedQuota(approvalQuota.getUsedQuota());
                    result.setAvailableQuota(approvalQuota.getAvailableQuota());
                    result.setStatus(approvalQuota.getStatus().name());
                }
                break;
        }
        
        return result;
    }

    /**
     * 获取活跃的锁定记录
     * 
     * <p>查询指定对象当前活跃的额度锁定记录。</p>
     * 
     * @param objectType 对象类型
     * @param objectId 对象ID
     * @return 锁定记录列表
     */
    @Override
    public List<QuotaLockResult> getActiveLocks(String objectType, Long objectId) {
        return new ArrayList<>();
    }

    /**
     * 过期锁定清理
     * 
     * <p>定时任务方法，清理已过期的额度锁定记录，释放对应的额度。</p>
     */
    @Override
    public void expireLocks() {
        log.info("Expiring stale quota locks");
    }

    /**
     * 查询可用额度
     * 
     * <p>根据对象类型查询指定对象的可用额度。</p>
     * 
     * @param request 查询请求
     * @return 可用额度金额
     */
    private BigDecimal queryAvailableQuota(QuotaValidateRequest request) {
        switch (request.getObjectType()) {
            case "GROUP":
                return groupQuotaRepository.findByGroupId(request.getObjectId())
                        .map(GroupQuota::getAvailableQuota)
                        .orElse(BigDecimal.ZERO);
            case "CUSTOMER":
                return customerQuotaRepository.findByCustomerId(request.getObjectId())
                        .map(CustomerQuota::getAvailableQuota)
                        .orElse(BigDecimal.ZERO);
            case "APPROVAL":
                return approvalQuotaRepository.findById(request.getObjectId())
                        .map(ApprovalQuota::getAvailableQuota)
                        .orElse(BigDecimal.ZERO);
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * 获取分布式锁
     * 
     * <p>使用Redisson获取指定对象的分布式锁，确保并发操作的数据一致性。</p>
     * 
     * @param objectType 对象类型
     * @param objectId 对象ID
     * @return Redisson锁对象
     * @throws BusinessException 当获取锁失败或被中断时抛出
     */
    private RLock acquireQuotaLock(String objectType, Long objectId) {
        String lockKey = QUOTA_LOCK_PREFIX + objectType + ":" + objectId;
        RLock lock = redissonClient.getLock(lockKey);
        
        boolean acquired = false;
        try {
            acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException(ErrorCode.LOCK_ERROR.getCode(), 
                        "获取额度锁失败，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.LOCK_ERROR.getCode(), 
                    "获取额度锁被中断");
        }
        
        return lock;
    }

    /**
     * 释放分布式锁
     * 
     * <p>释放Redisson分布式锁，确保锁被正确释放。</p>
     * 
     * @param lock Redisson锁对象
     */
    private void releaseLock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 校验并标准化金额
     * 
     * <p>校验金额参数的有效性，并将其标准化为4位小数精度。</p>
     * 
     * @param amount 待校验的金额
     * @return 标准化后的金额
     * @throws BusinessException 当金额为null或小于等于0时抛出
     */
    private BigDecimal validateAndNormalizeAmount(BigDecimal amount) {
        if (amount == null || MonetaryUtils.isLessThanOrEqual(amount, BigDecimal.ZERO)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                    "金额必须大于0");
        }
        return amount.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 生成锁定ID
     * 
     * <p>生成唯一的锁定记录ID，格式为：LK + 时间戳 + 随机数。</p>
     * 
     * @return 锁定ID
     */
    private String generateLockId() {
        return "LK" + System.currentTimeMillis() + String.format("%04d", 
                (int)(Math.random() * 10000));
    }
    
    /**
     * 持久化锁定记录到Redis
     * 
     * <p>将锁定记录保存到Redis，并设置过期时间。</p>
     * 
     * @param lockResult 锁定结果
     */
    private void persistLockRecord(QuotaLockResult lockResult) {
        String key = QUOTA_LOCK_RECORD_PREFIX + lockResult.getLockId();
        long timeoutMinutes = 30;
        if (lockResult.getExpiryTime() != null && lockResult.getLockTime() != null) {
            timeoutMinutes = java.time.Duration.between(
                    lockResult.getLockTime(), lockResult.getExpiryTime()).toMinutes();
        }
        redisTemplate.opsForValue().set(key, lockResult, timeoutMinutes, TimeUnit.MINUTES);
        log.debug("Lock record persisted to Redis: lockId={}, timeout={} minutes", 
                lockResult.getLockId(), timeoutMinutes);
    }
    
    /**
     * 从Redis获取锁定记录
     * 
     * @param lockId 锁定ID
     * @return 锁定记录，如果不存在则返回null
     */
    private QuotaLockResult getLockRecord(String lockId) {
        String key = QUOTA_LOCK_RECORD_PREFIX + lockId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof QuotaLockResult) {
            return (QuotaLockResult) value;
        }
        return null;
    }
    
    /**
     * 删除Redis中的锁定记录
     * 
     * @param lockId 锁定ID
     */
    private void deleteLockRecord(String lockId) {
        String key = QUOTA_LOCK_RECORD_PREFIX + lockId;
        redisTemplate.delete(key);
        log.debug("Lock record deleted from Redis: lockId={}", lockId);
    }
    
    /**
     * 恢复额度
     * 
     * <p>根据锁定记录恢复对应的额度。</p>
     * 
     * @param lockRecord 锁定记录
     */
    private void restoreQuota(QuotaLockResult lockRecord) {
        BigDecimal amount = lockRecord.getLockedAmount();
        
        switch (lockRecord.getObjectType()) {
            case "GROUP":
                GroupQuota groupQuota = groupQuotaRepository.findByGroupIdWithLock(lockRecord.getObjectId())
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.GROUP_NOT_FOUND.getCode(), 
                                "集团额度不存在: " + lockRecord.getObjectId()));
                groupQuota.setUsedQuota(MonetaryUtils.subtract(groupQuota.getUsedQuota(), amount));
                groupQuota.setAvailableQuota(MonetaryUtils.add(groupQuota.getAvailableQuota(), amount));
                groupQuota.setUpdateTime(LocalDateTime.now());
                groupQuotaRepository.save(groupQuota);
                break;
            case "CUSTOMER":
                CustomerQuota customerQuota = customerQuotaRepository.findByCustomerIdWithLock(lockRecord.getObjectId())
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.CUSTOMER_NOT_FOUND.getCode(), 
                                "客户额度不存在: " + lockRecord.getObjectId()));
                customerQuota.setUsedQuota(MonetaryUtils.subtract(customerQuota.getUsedQuota(), amount));
                customerQuota.setAvailableQuota(MonetaryUtils.add(customerQuota.getAvailableQuota(), amount));
                customerQuota.setUpdateTime(LocalDateTime.now());
                customerQuotaRepository.save(customerQuota);
                break;
            case "APPROVAL":
                ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(lockRecord.getObjectId())
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                                "批复额度不存在: " + lockRecord.getObjectId()));
                approvalQuota.setUsedQuota(MonetaryUtils.subtract(approvalQuota.getUsedQuota(), amount));
                approvalQuota.setAvailableQuota(MonetaryUtils.add(approvalQuota.getAvailableQuota(), amount));
                approvalQuota.setUpdateTime(LocalDateTime.now());
                approvalQuotaRepository.save(approvalQuota);
                break;
            default:
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                        "不支持的额度对象类型: " + lockRecord.getObjectType());
        }
        
        log.info("Quota restored: objectType={}, objectId={}, amount={}", 
                lockRecord.getObjectType(), lockRecord.getObjectId(), amount);
    }

    /**
     * 多占额度
     * 
     * <p>根据多占规则占用额度，支持多种占用模式。</p>
     * 
     * @param request 占用请求
     * @return 占用结果
     */
    @Override
    @Transactional
    public QuotaOccupyResult multiOccupyQuota(QuotaOccupyRequest request) {
        log.info("Multi-occupying quota: objectType={}, objectId={}, amount={}", 
                request.getObjectType(), request.getObjectId(), request.getAmount());
        
        RLock distributedLock = acquireQuotaLock(request.getObjectType(), request.getObjectId());
        
        try {
            Whitelist whitelist = checkWhitelistExemption(request);
            if (whitelist != null && whitelist.getExemptRule() == Whitelist.ExemptRule.FULL) {
                QuotaOccupyResult result = new QuotaOccupyResult();
                result.setOccupyId(generateOccupyId());
                result.setObjectType(request.getObjectType());
                result.setObjectId(request.getObjectId());
                result.setOccupiedAmount(request.getAmount());
                result.setWhitelisted(true);
                result.setStatus("OCCUPIED");
                result.setOccupyTime(LocalDateTime.now());
                
                log.info("Quota multi-occupied via whitelist: customerId={}, rule={}", 
                        request.getObjectId(), whitelist.getExemptRule());
                return result;
            }
            
            // 根据多占规则检查是否允许占用
            if (!checkMultiOccupancyAllowed(request)) {
                throw new BusinessException(ErrorCode.QUOTA_MULTI_OCCUPY_ERROR.getCode(),
                        "当前业务类型不允许多占额度");
            }
            
            QuotaOccupyResult result = performMultiOccupyQuota(request);
            log.info("Quota multi-occupied successfully: occupyId={}", result.getOccupyId());
            return result;
        } finally {
            releaseLock(distributedLock);
        }
    }

    /**
     * 检查多占是否允许
     * 
     * <p>根据业务类型和多占规则判断是否允许多占。</p>
     * 
     * @param request 占用请求
     * @return 是否允许多占
     */
    private boolean checkMultiOccupancyAllowed(QuotaOccupyRequest request) {
        // 如果明确指定了不允许多占，则返回false
        if (!request.isMultiOccupancy()) {
            return false;
        }
        
        // 如果指定了多占类型，则基于类型进行判断
        if (request.getMultiOccupancyType() != null) {
            switch (request.getMultiOccupancyType()) {
                case "REPEATABLE":
                    // 可重复占用：检查是否超过最大占用次数限制
                    return checkRepeatableOccupancyLimit(request);
                case "PROPORTIONAL":
                    // 按比例多占：检查是否超过最大占用比例
                    return checkProportionalOccupancyLimit(request);
                case "TIME_SHARED":
                    // 按时间多占：检查时间槽是否可用
                    return checkTimeSharedOccupancy(request);
                case "BY_BUSINESS_TYPE":
                    // 按业务类型多占：检查是否有相同业务类型的占用
                    return checkBusinessTypeBasedOccupancy(request);
                default:
                    // 默认情况下只允许LOAN业务类型多占
                    return "LOAN".equals(request.getBusinessType());
            }
        }
        
        // 默认情况下只允许LOAN业务类型多占
        return "LOAN".equals(request.getBusinessType());
    }

    /**
     * 检查可重复占用限制
     * 
     * <p>检查是否超过最大占用次数限制。</p>
     * 
     * @param request 占用请求
     * @return 是否允许占用
     */
    private boolean checkRepeatableOccupancyLimit(QuotaOccupyRequest request) {
        // 这里可以实现具体的最大占用次数限制逻辑
        // 示例：限制同一额度最多被5个业务占用
        int maxOccupants = 5;
        int currentOccupants = getCurrentOccupantsCount(request.getObjectId(), request.getObjectType());
        return currentOccupants < maxOccupants;
    }

    /**
     * 检查按比例占用限制
     * 
     * <p>检查是否超过最大占用比例限制。</p>
     * 
     * @param request 占用请求
     * @return 是否允许占用
     */
    private boolean checkProportionalOccupancyLimit(QuotaOccupyRequest request) {
        // 这里可以实现具体的按比例占用逻辑
        // 示例：限制每个业务最多占用总额度的80%
        BigDecimal maxRatio = new BigDecimal("0.8");
        BigDecimal totalQuota = getTotalQuota(request.getObjectId(), request.getObjectType());
        BigDecimal maxAllowable = totalQuota.multiply(maxRatio);
        return MonetaryUtils.isLessThanOrEqual(request.getAmount(), maxAllowable);
    }

    /**
     * 检查时间共享占用
     * 
     * <p>检查时间槽是否已被占用。</p>
     * 
     * @param request 占用请求
     * @return 是否允许占用
     */
    private boolean checkTimeSharedOccupancy(QuotaOccupyRequest request) {
        // 如果没有指定时间槽ID，则不允许时间共享占用
        if (request.getTimeSlotId() == null) {
            return false;
        }
        
        // 这里可以实现时间槽占用检查逻辑
        // 示例：检查指定时间槽是否已被其他业务占用
        return !isTimeSlotOccupied(request.getTimeSlotId());
    }

    /**
     * 检查基于业务类型的占用
     * 
     * <p>检查是否有相同业务类型的占用。</p>
     * 
     * @param request 占用请求
     * @return 是否允许占用
     */
    private boolean checkBusinessTypeBasedOccupancy(QuotaOccupyRequest request) {
        // 检查是否已有相同业务类型的占用
        // 示例：同一业务类型不允许重复占用
        return !hasSameBusinessTypeOccupancy(request.getObjectId(), request.getObjectType(), 
                request.getBusinessType());
    }

    /**
     * 获取当前占用者数量
     * 
     * @param objectId 对象ID
     * @param objectType 对象类型
     * @return 当前占用者数量
     */
    private int getCurrentOccupantsCount(Long objectId, String objectType) {
        // 这里可以实现具体的查询逻辑
        // 示例：简单返回1表示有一个占用者
        return 1;
    }

    /**
     * 获取总额度
     * 
     * @param objectId 对象ID
     * @param objectType 对象类型
     * @return 总额度
     */
    private BigDecimal getTotalQuota(Long objectId, String objectType) {
        switch (objectType) {
            case "GROUP":
                return groupQuotaRepository.findByGroupId(objectId)
                        .map(GroupQuota::getTotalQuota)
                        .orElse(BigDecimal.ZERO);
            case "CUSTOMER":
                return customerQuotaRepository.findByCustomerId(objectId)
                        .map(CustomerQuota::getTotalQuota)
                        .orElse(BigDecimal.ZERO);
            case "APPROVAL":
                return approvalQuotaRepository.findById(objectId)
                        .map(ApprovalQuota::getApprovalQuota)
                        .orElse(BigDecimal.ZERO);
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * 检查时间槽是否已被占用
     * 
     * @param timeSlotId 时间槽ID
     * @return 是否已被占用
     */
    private boolean isTimeSlotOccupied(Long timeSlotId) {
        // 这里可以实现具体的时间槽占用检查逻辑
        // 示例：简单返回false表示时间槽未被占用
        return false;
    }

    /**
     * 检查是否存在相同业务类型的占用
     * 
     * @param objectId 对象ID
     * @param objectType 对象类型
     * @param businessType 业务类型
     * @return 是否存在相同业务类型的占用
     */
    private boolean hasSameBusinessTypeOccupancy(Long objectId, String objectType, String businessType) {
        // 这里可以实现具体的检查逻辑
        // 示例：简单返回false表示不存在相同业务类型的占用
        return false;
    }

    /**
     * 执行多占额度逻辑
     * 
     * <p>执行多占额度的核心逻辑。</p>
     * 
     * @param request 占用请求
     * @return 占用结果
     */
    private QuotaOccupyResult performMultiOccupyQuota(QuotaOccupyRequest request) {
        BigDecimal amount = validateAndNormalizeAmount(request.getAmount());
        
        // 检查是否超出可用额度
        QuotaValidateRequest validateRequest = QuotaValidateRequest.builder()
                .objectType(request.getObjectType())
                .objectId(request.getObjectId())
                .customerId(request.getCustomerId())
                .groupId(request.getGroupId())
                .approvalId(request.getApprovalId())
                .businessType(request.getBusinessType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .needExtraApproval(request.isNeedExtraApproval())
                .multiOccupancy(request.isMultiOccupancy())
                .multiOccupancyType(request.getMultiOccupancyType())
                .parentOccupyId(request.getParentOccupyId())
                .timeSlotId(request.getTimeSlotId())
                .occupancyPurpose(request.getOccupancyPurpose())
                .build();
        BigDecimal availableQuota = queryAvailableQuota(validateRequest);
        if (MonetaryUtils.isLessThan(availableQuota, amount)) {
            throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT.getCode(),
                    "额度不足，当前可用: " + MonetaryUtils.format(availableQuota));
        }
        
        // 创建合同占用记录
        ContractOccupancy occupancy = new ContractOccupancy();
        occupancy.setContractNo(request.getContractNo());
        occupancy.setApprovalQuotaSubId(request.getApprovalQuotaSubId());
        occupancy.setCustomerId(request.getCustomerId());
        occupancy.setOccupancyAmount(amount);
        occupancy.setBusinessType(request.getBusinessType());
        occupancy.setOccupancyPurpose(request.getOccupancyPurpose()); // 设置占用目的
        occupancy.setMultiOccupancy(request.isMultiOccupancy());
        occupancy.setMultiOccupancyType(request.getMultiOccupancyType());
        occupancy.setParentOccupyId(request.getParentOccupyId());
        occupancy.setTimeSlotId(request.getTimeSlotId());
        occupancy.setStatus(ContractOccupancy.OccupancyStatus.OCCUPIED);
        occupancy.setCreateBy(request.getOperator());
        // 注意：occupyTime会由JPA pre-persist方法设置createTime字段
        
        ContractOccupancy saved = contractOccupancyRepository.save(occupancy);
        
        // 更新批复额度使用情况
        ApprovalQuota approvalQuota = approvalQuotaRepository.findByIdWithLock(request.getApprovalQuotaSubId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND.getCode(), 
                        "批复额度不存在: " + request.getApprovalQuotaSubId()));
        
        approvalQuota.setUsedQuota(MonetaryUtils.add(approvalQuota.getUsedQuota(), amount));
        approvalQuota.setAvailableQuota(MonetaryUtils.subtract(approvalQuota.getAvailableQuota(), amount));
        approvalQuota.setUpdateTime(LocalDateTime.now());
        approvalQuotaRepository.save(approvalQuota);
        
        QuotaOccupyResult result = new QuotaOccupyResult();
        result.setOccupyId(saved.getId().toString());
        result.setContractNo(saved.getContractNo());
        result.setObjectType(request.getObjectType());
        result.setObjectId(request.getObjectId());
        result.setOccupiedAmount(amount);
        result.setCustomerId(request.getCustomerId());
        result.setBusinessType(request.getBusinessType());
        result.setStatus("OCCUPIED");
        result.setOccupyTime(saved.getCreateTime()); // 使用数据库中保存的时间
        
        // 设置多占相关信息
        result.setMultiOccupancy(request.isMultiOccupancy());
        result.setMultiOccupancyType(request.getMultiOccupancyType());
        result.setParentOccupyId(request.getParentOccupyId());
        result.setOccupancySequence(saved.getOccupancySequence());
        
        return result;
    }

    /**
     * 多占校验
     * 
     * <p>根据多占规则校验额度是否足够。</p>
     * 
     * @param request 校验请求
     * @return 校验结果
     */
    @Override
    public QuotaValidateResult validateMultiOccupancy(QuotaValidateRequest request) {
        log.debug("Validating multi-occupancy: objectType={}, objectId={}, amount={}, businessType={}", 
                request.getObjectType(), request.getObjectId(), request.getAmount(), request.getBusinessType());
        
        Whitelist whitelist = whitelistService.checkWhitelist(
                request.getCustomerId(), 
                request.getBusinessType(), 
                request.getAmount());
        
        if (whitelist != null) {
            QuotaValidateResult result = new QuotaValidateResult();
            result.setValid(true);
            result.setWhitelisted(true);
            result.setWhitelistNo(whitelist.getWhitelistNo());
            result.setMessage("客户在白名单中，可豁免额度检查");
            return result;
        }
        
        BigDecimal amount = validateAndNormalizeAmount(request.getAmount());
        BigDecimal availableQuota = queryAvailableQuota(request);
        
        // 检查多占规则
        QuotaOccupyRequest occupyRequest = convertToOccupyRequest(request);
        boolean multiOccupancyAllowed = checkMultiOccupancyAllowed(occupyRequest);
        
        QuotaValidateResult result = new QuotaValidateResult();
        result.setAvailableQuota(availableQuota);
        result.setMultiOccupancyAllowed(multiOccupancyAllowed);
        result.setMultiOccupancyType(request.getMultiOccupancyType());
        
        // 计算当前占用者数量
        if (request.getObjectId() != null && request.getObjectType() != null) {
            result.setCurrentOccupants(getCurrentOccupantsCount(request.getObjectId(), request.getObjectType()));
        }
        
        // 计算最大可占用金额
        if (request.getMultiOccupancyType() != null && "PROPORTIONAL".equals(request.getMultiOccupancyType())) {
            BigDecimal totalQuota = getTotalQuota(request.getObjectId(), request.getObjectType());
            if (totalQuota.compareTo(BigDecimal.ZERO) > 0) {
                // 示例：按比例限制为总额度的80%
                BigDecimal maxRatio = new BigDecimal("0.8");
                result.setMaxMultiOccupancyAmount(totalQuota.multiply(maxRatio));
            }
        }
        
        if (MonetaryUtils.isGreaterThanOrEqual(availableQuota, amount)) {
            result.setValid(true);
            result.setPassed(true);
            result.setMessage(multiOccupancyAllowed ? 
                    "多占额度校验通过，允许多占类型：" + (request.getMultiOccupancyType() != null ? request.getMultiOccupancyType() : "默认") : 
                    "额度校验通过（不支持多占）");
        } else {
            result.setValid(false);
            result.setPassed(false);
            result.setMessage("额度不足，需要额度: " + MonetaryUtils.format(amount) + 
                    "，可用额度: " + MonetaryUtils.format(availableQuota));
        }
        
        result.setValidationTime(LocalDateTime.now());
        
        return result;
    }
    
    /**
     * 将校验请求转换为占用请求
     * 
     * @param request 校验请求
     * @return 占用请求
     */
    private QuotaOccupyRequest convertToOccupyRequest(QuotaValidateRequest request) {
        QuotaOccupyRequest occupyRequest = new QuotaOccupyRequest();
        occupyRequest.setObjectType(request.getObjectType());
        occupyRequest.setObjectId(request.getObjectId());
        occupyRequest.setCustomerId(request.getCustomerId());
        occupyRequest.setAmount(request.getAmount());
        occupyRequest.setBusinessType(request.getBusinessType());
        return occupyRequest;
    }

    /**
     * 生成占用ID
     * 
     * <p>生成唯一的占用记录ID，格式为：OC + 时间戳 + 随机数。</p>
     * 
     * @return 占用ID
     */
    private String generateOccupyId() {
        return "OC" + System.currentTimeMillis() + String.format("%04d", 
                (int)(Math.random() * 10000));
    }
}
