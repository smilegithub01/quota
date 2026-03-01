package com.bank.quota.core.service.impl;

import com.bank.quota.common.constant.ErrorCode;
import com.bank.quota.common.exception.BusinessException;
import com.bank.quota.core.domain.ApprovalNode;
import com.bank.quota.core.domain.ApprovalProcess;
import com.bank.quota.core.dto.approval.*;
import com.bank.quota.core.enums.ApprovalStatus;
import com.bank.quota.core.enums.BusinessType;
import com.bank.quota.core.repository.ApprovalNodeRepository;
import com.bank.quota.core.repository.ApprovalProcessRepository;
import com.bank.quota.core.service.ApprovalProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 审批流程服务实现类
 * 
 * <p>提供银行级信贷额度管控平台的审批流程功能实现。
 * 包括审批流程的发起、查询、审批操作等功能。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalProcessServiceImpl implements ApprovalProcessService {
    
    private final ApprovalProcessRepository approvalProcessRepository;
    private final ApprovalNodeRepository approvalNodeRepository;
    
    @Override
    @Transactional
    public ApprovalProcessResponse startApprovalProcess(ApprovalProcessRequest request) {
        log.info("Starting approval process: businessId={}, businessType={}, amount={}", 
                request.getBusinessId(), request.getBusinessType(), request.getAmount());
        
        // 创建审批流程
        ApprovalProcess process = ApprovalProcess.builder()
                .businessId(request.getBusinessId())
                .businessType(request.getBusinessType())
                .businessName(request.getBusinessName())
                .applicantId(request.getApplicantId())
                .applicantName(request.getApplicantName())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .estimatedDurationDays(request.getEstimatedDurationDays())
                .description(request.getDescription())
                .status(ApprovalStatus.SUBMITTED)
                .createBy(request.getApplicantId())
                .build();
        
        ApprovalProcess savedProcess = approvalProcessRepository.save(process);
        
        // 创建初始审批节点
        ApprovalNode initialNode = ApprovalNode.builder()
                .processId(savedProcess.getId())
                .nodeName("初审")
                .nodeSequence(1)
                .approverId(request.getApplicantId())
                .approverName(request.getApplicantName())
                .approverRole("申请人")
                .status(ApprovalStatus.PENDING)
                .comments("等待审批")
                .startTime(LocalDateTime.now())
                .build();
        
        ApprovalNode savedNode = approvalNodeRepository.save(initialNode);
        
        // 更新流程的当前节点
        savedProcess.setCurrentNodeId(savedNode.getId());
        savedProcess.setCurrentApproverId(savedNode.getApproverId());
        savedProcess.setCurrentApproverName(savedNode.getApproverName());
        approvalProcessRepository.save(savedProcess);
        
        log.info("Approval process started successfully: processNo={}, processId={}", 
                savedProcess.getProcessNo(), savedProcess.getId());
        
        return buildProcessResponse(savedProcess, java.util.Collections.singletonList(savedNode));
    }
    
    @Override
    public ApprovalProcessResponse getApprovalProcess(Long processId) {
        log.debug("Getting approval process: processId={}", processId);
        
        ApprovalProcess process = approvalProcessRepository.findById(processId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "审批流程不存在: " + processId));
        
        List<ApprovalNode> nodes = approvalNodeRepository.findByProcessIdOrderByNodeSequenceAsc(processId);
        
        return buildProcessResponse(process, nodes);
    }
    
    @Override
    public List<ApprovalProcessResponse> getUserPendingTasks(ApprovalTaskQueryRequest request) {
        log.debug("Getting pending tasks for user: approverId={}, approverName={}", 
                request.getApproverId(), request.getApproverName());
        
        List<ApprovalProcess> processes = approvalProcessRepository.findPendingProcessesByApprover(
                request.getApproverId(), request.getApproverName());
        
        List<ApprovalProcessResponse> responses = new ArrayList<>();
        for (ApprovalProcess process : processes) {
            List<ApprovalNode> nodes = approvalNodeRepository.findByProcessIdOrderByNodeSequenceAsc(process.getId());
            responses.add(buildProcessResponse(process, nodes));
        }
        
        return responses;
    }
    
    @Override
    @Transactional
    public ApprovalProcessResponse performApprovalAction(ApprovalActionRequest request) {
        log.info("Performing approval action: processId={}, nodeId={}, result={}", 
                request.getProcessId(), request.getNodeId(), request.getResult());
        
        // 获取审批流程
        ApprovalProcess process = approvalProcessRepository.findById(request.getProcessId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "审批流程不存在: " + request.getProcessId()));
        
        // 获取当前审批节点
        ApprovalNode currentNode = approvalNodeRepository.findById(request.getNodeId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "审批节点不存在: " + request.getNodeId()));
        
        // 验证当前节点状态
        if (currentNode.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), 
                    "审批节点状态不是待审批状态，无法执行审批操作");
        }
        
        // 验证操作人权限
        if (!currentNode.getApproverId().equals(request.getApproverId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED.getCode(), 
                    "您没有权限审批此节点");
        }
        
        // 更新当前节点信息
        currentNode.setStatus(request.getStatus());
        currentNode.setResult(request.getResult());
        currentNode.setComments(request.getComments());
        currentNode.setApprovalTime(LocalDateTime.now());
        currentNode.setEndTime(LocalDateTime.now());
        currentNode.setUpdateBy(request.getOperator());
        
        approvalNodeRepository.save(currentNode);
        
        // 根据审批结果决定下一步
        if ("APPROVE".equalsIgnoreCase(request.getResult())) {
            // 查找下一个节点
            ApprovalNode nextNode = getNextApprovalNode(process.getId(), currentNode.getNodeSequence());
            if (nextNode != null) {
                // 激活下一个节点
                nextNode.setStatus(ApprovalStatus.PENDING);
                nextNode.setStartTime(LocalDateTime.now());
                if (request.getDeadline() != null) {
                    nextNode.setDeadline(request.getDeadline());
                }
                approvalNodeRepository.save(nextNode);
                
                // 更新流程的当前节点信息
                process.setCurrentNodeId(nextNode.getId());
                process.setCurrentApproverId(nextNode.getApproverId());
                process.setCurrentApproverName(nextNode.getApproverName());
                process.setStatus(ApprovalStatus.UNDER_REVIEW);
            } else {
                // 没有更多节点，流程结束
                process.setStatus(ApprovalStatus.APPROVED);
                process.setEndTime(LocalDateTime.now());
            }
        } else if ("REJECT".equalsIgnoreCase(request.getResult())) {
            // 审批被拒绝，流程结束
            process.setStatus(ApprovalStatus.REJECTED);
            process.setEndTime(LocalDateTime.now());
        }
        
        process.setUpdateBy(request.getOperator());
        approvalProcessRepository.save(process);
        
        log.info("Approval action performed successfully: processNo={}, status={}", 
                process.getProcessNo(), process.getStatus());
        
        // 重新获取所有节点以构建响应
        List<ApprovalNode> allNodes = approvalNodeRepository.findByProcessIdOrderByNodeSequenceAsc(process.getId());
        return buildProcessResponse(process, allNodes);
    }
    
    @Override
    public List<ApprovalProcessResponse> getHistoricalApprovals(String approverId, Integer pageNum, Integer pageSize) {
        log.debug("Getting historical approvals for approver: approverId={}, pageNum={}, pageSize={}", 
                approverId, pageNum, pageSize);
        
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        List<ApprovalProcess> processes = approvalProcessRepository.findHistoricalApprovalsByApprover(
                approverId, offset, pageSize);
        
        List<ApprovalProcessResponse> responses = new ArrayList<>();
        for (ApprovalProcess process : processes) {
            List<ApprovalNode> nodes = approvalNodeRepository.findByProcessIdOrderByNodeSequenceAsc(process.getId());
            responses.add(buildProcessResponse(process, nodes));
        }
        
        return responses;
    }
    
    @Override
    @Transactional
    public ApprovalProcessResponse withdrawApprovalProcess(Long processId, String reason) {
        log.info("Withdrawing approval process: processId={}, reason={}", processId, reason);
        
        ApprovalProcess process = approvalProcessRepository.findById(processId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "审批流程不存在: " + processId));
        
        if (process.getStatus() != ApprovalStatus.SUBMITTED && process.getStatus() != ApprovalStatus.UNDER_REVIEW) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), 
                    "只能撤回已提交或审核中的审批流程");
        }
        
        // 检查是否是申请人本人操作
        if (!process.getApplicantId().equals(getCurrentUserId())) { // 简化实现，实际应该从上下文获取当前用户
            throw new BusinessException(ErrorCode.ACCESS_DENIED.getCode(), 
                    "只有申请人才能撤回审批流程");
        }
        
        process.setStatus(ApprovalStatus.WITHDRAWN);
        process.setEndTime(LocalDateTime.now());
        process.setUpdateBy(getCurrentUserId());
        
        // 更新相关节点的状态
        List<ApprovalNode> nodes = approvalNodeRepository.findByProcessIdOrderByNodeSequenceAsc(processId);
        for (ApprovalNode node : nodes) {
            if (node.getStatus() == ApprovalStatus.PENDING) {
                node.setStatus(ApprovalStatus.CANCELLED);
                node.setComments("流程已撤回: " + reason);
                node.setEndTime(LocalDateTime.now());
                approvalNodeRepository.save(node);
            }
        }
        
        approvalProcessRepository.save(process);
        
        log.info("Approval process withdrawn successfully: processNo={}", process.getProcessNo());
        
        List<ApprovalNode> allNodes = approvalNodeRepository.findByProcessIdOrderByNodeSequenceAsc(process.getId());
        return buildProcessResponse(process, allNodes);
    }
    
    @Override
    @Transactional
    public ApprovalProcessResponse delegateApprovalTask(Long processId, Long nodeId, String newApproverId, 
                                                     String newApproverName, String operator, String reason) {
        log.info("Delegating approval task: processId={}, nodeId={}, newApproverId={}, newApproverName={}", 
                processId, nodeId, newApproverId, newApproverName);
        
        ApprovalProcess process = approvalProcessRepository.findById(processId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_ALLOWED.getCode(), 
                        "审批流程不存在: " + processId));
        
        ApprovalNode node = approvalNodeRepository.findById(nodeId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND.getCode(), 
                        "审批节点不存在: " + nodeId));
        
        if (node.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), 
                    "只能转办待审批的节点");
        }
        
        // 更新节点信息
        node.setApproverId(newApproverId);
        node.setApproverName(newApproverName);
        node.setComments("转办原因: " + reason + "，转办人: " + operator);
        node.setUpdateBy(operator);
        
        approvalNodeRepository.save(node);
        
        // 如果是当前正在处理的节点，也更新流程信息
        if (process.getCurrentNodeId() != null && process.getCurrentNodeId().equals(nodeId)) {
            process.setCurrentApproverId(newApproverId);
            process.setCurrentApproverName(newApproverName);
            process.setUpdateBy(operator);
            approvalProcessRepository.save(process);
        }
        
        log.info("Approval task delegated successfully: processNo={}, nodeId={}", 
                process.getProcessNo(), node.getId());
        
        List<ApprovalNode> allNodes = approvalNodeRepository.findByProcessIdOrderByNodeSequenceAsc(process.getId());
        return buildProcessResponse(process, allNodes);
    }
    
    @Override
    public Object getApprovalStatistics(String approverId) {
        log.debug("Getting approval statistics for approver: approverId={}", approverId);
        
        // 获取该审批人的各种状态的流程数量
        List<Object[]> statusCountList = approvalProcessRepository.countProcessesByApproverAndStatus(approverId);
        Map<String, Long> statusCount = new java.util.HashMap<>();
        for (Object[] row : statusCountList) {
            statusCount.put((String) row[0], (Long) row[1]);
        }
        
        // 获取平均审批时长等统计信息
        Object[] avgDurationArray = approvalProcessRepository.getAverageProcessingTimeByApprover(approverId);
        Map<String, Object> avgDuration = new java.util.HashMap<>();
        if (avgDurationArray != null && avgDurationArray.length >= 2) {
            avgDuration.put("avgProcessingTime", avgDurationArray[0]);
            avgDuration.put("totalProcessed", avgDurationArray[1]);
        }
        
        Map<String, Object> statistics = new java.util.HashMap<>();
        statistics.put("statusCount", statusCount);
        statistics.put("avgProcessingTime", avgDuration.get("avgProcessingTime"));
        statistics.put("totalProcessed", avgDuration.get("totalProcessed"));
        
        return statistics;
    }
    
    /**
     * 获取下一个审批节点
     */
    private ApprovalNode getNextApprovalNode(Long processId, Integer currentSequence) {
        List<ApprovalNode> allNodes = approvalNodeRepository.findByProcessIdOrderByNodeSequenceAsc(processId);
        return allNodes.stream()
                .filter(node -> node.getNodeSequence() > currentSequence)
                .min((n1, n2) -> Integer.compare(n1.getNodeSequence(), n2.getNodeSequence()))
                .orElse(null);
    }
    
    /**
     * 构建流程响应对象
     */
    private ApprovalProcessResponse buildProcessResponse(ApprovalProcess process, List<ApprovalNode> nodes) {
        List<ApprovalNodeResponse> nodeResponses = nodes.stream()
                .map(this::buildNodeResponse)
                .collect(Collectors.toList());
        
        return ApprovalProcessResponse.builder()
                .id(process.getId())
                .processNo(process.getProcessNo())
                .businessId(process.getBusinessId())
                .businessType(process.getBusinessType())
                .businessName(process.getBusinessName())
                .applicantId(process.getApplicantId())
                .applicantName(process.getApplicantName())
                .amount(process.getAmount())
                .currency(process.getCurrency())
                .currentNodeId(process.getCurrentNodeId())
                .currentApproverId(process.getCurrentApproverId())
                .currentApproverName(process.getCurrentApproverName())
                .status(process.getStatus())
                .startTime(process.getStartTime())
                .endTime(process.getEndTime())
                .estimatedDurationDays(process.getEstimatedDurationDays())
                .description(process.getDescription())
                .createBy(process.getCreateBy())
                .updateBy(process.getUpdateBy())
                .createTime(process.getCreateTime())
                .updateTime(process.getUpdateTime())
                .approvalNodes(nodeResponses)
                .build();
    }
    
    /**
     * 构建节点响应对象
     */
    private ApprovalNodeResponse buildNodeResponse(ApprovalNode node) {
        return ApprovalNodeResponse.builder()
                .id(node.getId())
                .processId(node.getProcessId())
                .nodeName(node.getNodeName())
                .nodeSequence(node.getNodeSequence())
                .approverId(node.getApproverId())
                .approverName(node.getApproverName())
                .approverRole(node.getApproverRole())
                .approverDept(node.getApproverDept())
                .status(node.getStatus())
                .approvalTime(node.getApprovalTime())
                .comments(node.getComments())
                .result(node.getResult())
                .startTime(node.getStartTime())
                .endTime(node.getEndTime())
                .deadline(node.getDeadline())
                .createTime(node.getCreateTime())
                .updateTime(node.getUpdateTime())
                .build();
    }
    
    /**
     * 获取当前用户ID（简化实现）
     */
    private String getCurrentUserId() {
        // 在实际应用中，这里应该从安全上下文获取当前用户ID
        return "SYSTEM";
    }
}