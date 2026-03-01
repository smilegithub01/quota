-- 额度生命周期管理表结构脚本
-- 本脚本包含额度调整、冻结、终止及生命周期事件相关的表结构

USE quota_system;

-- ========================================
-- 1. 额度调整记录表
-- ========================================
CREATE TABLE IF NOT EXISTS quota_adjustment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    adjustment_no VARCHAR(50) NOT NULL UNIQUE COMMENT '调整编号',
    quota_id BIGINT NOT NULL COMMENT '额度ID',
    quota_type VARCHAR(20) NOT NULL COMMENT '额度类型: GROUP-集团, CUSTOMER-客户, APPROVAL-批复',
    adjustment_type VARCHAR(20) NOT NULL COMMENT '调整类型: TEMPORARY-临时调整, PERMANENT-永久调整, BATCH-批量调整',
    adjustment_amount DECIMAL(20,4) NOT NULL COMMENT '调整金额',
    before_amount DECIMAL(20,4) NOT NULL COMMENT '调整前金额',
    after_amount DECIMAL(20,4) NOT NULL COMMENT '调整后金额',
    reason VARCHAR(500) NOT NULL COMMENT '调整原因',
    effective_time DATETIME COMMENT '生效时间',
    expiry_time DATETIME COMMENT '失效时间',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待审批, APPROVED-已审批, COMPLETED-已完成, REJECTED-已拒绝',
    applicant VARCHAR(50) NOT NULL COMMENT '申请人',
    approver VARCHAR(50) COMMENT '审批人',
    approve_time DATETIME COMMENT '审批时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_adjustment_no (adjustment_no),
    INDEX idx_quota_id (quota_id),
    INDEX idx_quota_type (quota_type),
    INDEX idx_adjustment_type (adjustment_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度调整记录表';

-- ========================================
-- 2. 额度冻结记录表
-- ========================================
CREATE TABLE IF NOT EXISTS quota_freeze (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    freeze_no VARCHAR(50) NOT NULL UNIQUE COMMENT '冻结编号',
    quota_id BIGINT NOT NULL COMMENT '额度ID',
    quota_type VARCHAR(20) NOT NULL COMMENT '额度类型: GROUP-集团, CUSTOMER-客户, APPROVAL-批复',
    freeze_type VARCHAR(20) NOT NULL COMMENT '冻结类型: FULL-全额冻结, PARTIAL-部分冻结, CONDITIONAL-条件冻结',
    freeze_amount DECIMAL(20,4) COMMENT '冻结金额',
    reason VARCHAR(500) NOT NULL COMMENT '冻结原因',
    `condition` VARCHAR(500) COMMENT '冻结条件',
    status VARCHAR(20) NOT NULL DEFAULT 'FROZEN' COMMENT '状态: FROZEN-已冻结, UNFROZEN-已解冻',
    operator VARCHAR(50) NOT NULL COMMENT '操作人',
    freeze_time DATETIME NOT NULL COMMENT '冻结时间',
    unfreeze_time DATETIME COMMENT '解冻时间',
    unfreeze_amount DECIMAL(20,4) COMMENT '解冻金额',
    unfreeze_reason VARCHAR(500) COMMENT '解冻原因',
    unfreeze_operator VARCHAR(50) COMMENT '解冻操作人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_freeze_no (freeze_no),
    INDEX idx_quota_id (quota_id),
    INDEX idx_quota_type (quota_type),
    INDEX idx_freeze_type (freeze_type),
    INDEX idx_status (status),
    INDEX idx_freeze_time (freeze_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度冻结记录表';

-- ========================================
-- 3. 额度终止记录表
-- ========================================
CREATE TABLE IF NOT EXISTS quota_termination (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    termination_no VARCHAR(50) NOT NULL UNIQUE COMMENT '终止编号',
    quota_id BIGINT NOT NULL COMMENT '额度ID',
    quota_type VARCHAR(20) NOT NULL COMMENT '额度类型: GROUP-集团, CUSTOMER-客户, APPROVAL-批复',
    terminate_type VARCHAR(20) NOT NULL COMMENT '终止类型: EXPIRY-到期终止, MANUAL-主动终止, FORCE-强制终止',
    reason VARCHAR(500) NOT NULL COMMENT '终止原因',
    operator VARCHAR(50) NOT NULL COMMENT '操作人',
    terminate_time DATETIME NOT NULL COMMENT '终止时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_termination_no (termination_no),
    INDEX idx_quota_id (quota_id),
    INDEX idx_quota_type (quota_type),
    INDEX idx_terminate_type (terminate_type),
    INDEX idx_terminate_time (terminate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度终止记录表';

-- ========================================
-- 4. 额度生命周期事件表
-- ========================================
CREATE TABLE IF NOT EXISTS quota_lifecycle_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    event_id VARCHAR(50) NOT NULL UNIQUE COMMENT '事件ID',
    quota_id BIGINT NOT NULL COMMENT '额度ID',
    quota_type VARCHAR(20) NOT NULL COMMENT '额度类型: GROUP-集团, CUSTOMER-客户, APPROVAL-批复',
    event_type VARCHAR(20) NOT NULL COMMENT '事件类型: CREATE-创建, ACTIVATE-启用, ADJUST-调整, OCCUPY-占用, RELEASE-释放, FREEZE-冻结, UNFREEZE-解冻, TERMINATE-终止',
    amount DECIMAL(20,4) COMMENT '金额',
    before_status VARCHAR(20) COMMENT '变更前状态',
    after_status VARCHAR(20) COMMENT '变更后状态',
    reason VARCHAR(500) COMMENT '原因',
    operator VARCHAR(50) NOT NULL COMMENT '操作人',
    business_ref_no VARCHAR(50) COMMENT '业务参考号',
    event_time DATETIME NOT NULL COMMENT '事件时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_event_id (event_id),
    INDEX idx_quota_id (quota_id),
    INDEX idx_quota_type (quota_type),
    INDEX idx_event_type (event_type),
    INDEX idx_event_time (event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度生命周期事件表';

-- ========================================
-- 插入示例数据
-- ========================================

-- 插入示例额度调整记录
INSERT INTO quota_adjustment (adjustment_no, quota_id, quota_type, adjustment_type, adjustment_amount, before_amount, after_amount, reason, effective_time, status, applicant, approver, approve_time) VALUES
('ADJ20260213000001', 1, 'GROUP', 'PERMANENT', 10000000.0000, 50000000.0000, 60000000.0000, '业务扩展需要增加集团授信额度', NOW(), 'COMPLETED', 'admin', 'supervisor', NOW()),
('ADJ20260213000002', 2, 'CUSTOMER', 'TEMPORARY', 5000000.0000, 20000000.0000, 25000000.0000, '临时性资金周转需求', NOW(), DATE_ADD(NOW(), INTERVAL 6 MONTH), 'PENDING', 'admin', NULL, NULL);

-- 插入示例额度冻结记录
INSERT INTO quota_freeze (freeze_no, quota_id, quota_type, freeze_type, freeze_amount, reason, status, operator, freeze_time) VALUES
('FRZ20260213000001', 3, 'CUSTOMER', 'PARTIAL', 3000000.0000, '客户风险预警临时冻结', 'FROZEN', 'risk_manager', NOW()),
('FRZ20260213000002', 1, 'GROUP', 'CONDITIONAL', NULL, '集团关联交易调查期间冻结', 'FROZEN', 'compliance_officer', NOW());

-- 插入示例额度终止记录
INSERT INTO quota_termination (termination_no, quota_id, quota_type, terminate_type, reason, operator, terminate_time, remark) VALUES
('TRM20260213000001', 4, 'CUSTOMER', 'EXPIRY', '额度到期自然终止', 'system', NOW(), '客户未申请续期');

-- 插入示例额度生命周期事件
INSERT INTO quota_lifecycle_event (event_id, quota_id, quota_type, event_type, amount, before_status, after_status, reason, operator, business_ref_no, event_time) VALUES
('EVT20260213000001', 1, 'GROUP', 'CREATE', 50000000.0000, NULL, 'ACTIVE', '集团额度初始化创建', 'admin', 'GRP20260201001', NOW()),
('EVT20260213000002', 1, 'GROUP', 'ADJUST', 10000000.0000, 'ACTIVE', 'ACTIVE', '额度调整申请', 'admin', 'ADJ20260213000001', NOW()),
('EVT20260213000003', 2, 'CUSTOMER', 'CREATE', 20000000.0000, NULL, 'ACTIVE', '客户额度初始化创建', 'admin', 'CUST20260201001', NOW()),
('EVT20260213000004', 3, 'CUSTOMER', 'FREEZE', 3000000.0000, 'ACTIVE', 'FROZEN', '风险预警冻结', 'risk_manager', 'FRZ20260213000001', NOW()),
('EVT20260213000005', 4, 'CUSTOMER', 'TERMINATE', NULL, 'ACTIVE', 'TERMINATED', '额度到期终止', 'system', 'TRM20260213000001', NOW());
