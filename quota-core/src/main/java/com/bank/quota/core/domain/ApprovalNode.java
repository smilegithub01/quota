package com.bank.quota.core.domain;

import com.bank.quota.core.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批节点实体
 * 
 * <p>代表审批流程中的节点的领域实体。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-14
 */
@Entity
@Table(name = "approval_node", indexes = {
    @Index(name = "idx_process_id", columnList = "process_id"),
    @Index(name = "idx_node_sequence", columnList = "node_sequence"),
    @Index(name = "idx_approver_id", columnList = "approver_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalNode implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_id", nullable = false)
    private Long processId;

    @Column(name = "node_name", nullable = false, length = 100)
    private String nodeName;

    @Column(name = "node_sequence", nullable = false)
    private Integer nodeSequence;

    @Column(name = "approver_id", nullable = false, length = 50)
    private String approverId;

    @Column(name = "approver_name", nullable = false, length = 100)
    private String approverName;

    @Column(name = "approver_role", length = 50)
    private String approverRole;

    @Column(name = "approver_dept", length = 100)
    private String approverDept;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    @Column(name = "comments", length = 500)
    private String comments;

    @Column(name = "result", length = 20)
    private String result; // APPROVE, REJECT, DELEGATE, RETURN

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_by", length = 50)
    private String updateBy;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = ApprovalStatus.PENDING;
        }
        startTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}