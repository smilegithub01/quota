-- 额度管理系统补充表结构脚本
-- 本脚本补充DB_SCHEMA.md中定义但init_quota_system.sql中缺失的表结构

USE quota_system;

-- ========================================
-- 1. 集团客户关系表
-- ========================================
CREATE TABLE IF NOT EXISTS group_relationship (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    parent_customer_id BIGINT NOT NULL COMMENT '父客户ID',
    child_customer_id BIGINT NOT NULL COMMENT '子客户ID',
    relationship_type VARCHAR(20) NOT NULL COMMENT '关系类型: CONTROL-控股, INFLUENCE-参股, AFFILIATE-关联',
    control_ratio DECIMAL(5,2) COMMENT '控制比例(%)',
    relationship_desc VARCHAR(500) COMMENT '关系描述',
    effective_date DATE NOT NULL COMMENT '生效日期',
    expire_date DATE COMMENT '失效日期',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '关系状态: ACTIVE-有效, INACTIVE-无效',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_parent_child (parent_customer_id, child_customer_id),
    INDEX idx_parent_customer (parent_customer_id),
    INDEX idx_child_customer (child_customer_id),
    INDEX idx_relationship_type (relationship_type),
    INDEX idx_status (status),
    CONSTRAINT fk_group_rel_parent FOREIGN KEY (parent_customer_id) REFERENCES customer(id),
    CONSTRAINT fk_group_rel_child FOREIGN KEY (child_customer_id) REFERENCES customer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='集团客户关系表';

-- ========================================
-- 2. 客户关联方表
-- ========================================
CREATE TABLE IF NOT EXISTS customer_affiliate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    affiliate_type VARCHAR(30) NOT NULL COMMENT '关联方类型: SHAREHOLDER-股东, CONTROLLER-实际控制人, GUARANTOR-担保人, RELATED_PERSON-关联人',
    affiliate_id BIGINT COMMENT '关联方客户ID',
    affiliate_name VARCHAR(200) NOT NULL COMMENT '关联方名称',
    affiliate_identity VARCHAR(100) COMMENT '关联方身份/证件号码',
    relationship_desc VARCHAR(500) COMMENT '关系描述',
    relationship_ratio DECIMAL(5,2) COMMENT '关联比例(%)',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-有效, INACTIVE-无效',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_customer_id (customer_id),
    INDEX idx_affiliate_id (affiliate_id),
    INDEX idx_affiliate_type (affiliate_type),
    INDEX idx_status (status),
    CONSTRAINT fk_affiliate_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户关联方表';

-- ========================================
-- 3. 授信申请表
-- ========================================
CREATE TABLE IF NOT EXISTS credit_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    application_id VARCHAR(64) NOT NULL UNIQUE COMMENT '申请ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    application_type VARCHAR(20) NOT NULL COMMENT '申请类型: NEW-新增, RENEWAL-续授信, ADJUSTMENT-调整',
    application_amount DECIMAL(20,4) NOT NULL COMMENT '申请金额',
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    application_purpose VARCHAR(500) COMMENT '申请用途',
    guarantee_method VARCHAR(100) COMMENT '担保方式',
    term_months INT COMMENT '期限(月)',
    application_date DATE NOT NULL COMMENT '申请日期',
    applicant VARCHAR(100) COMMENT '申请人',
    department VARCHAR(100) COMMENT '申请部门',
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' COMMENT '申请状态: DRAFT-草稿, SUBMITTED-已提交, IN_REVIEW-审核中, APPROVED-已批准, REJECTED-已拒绝, CANCELLED-已取消',
    current_approver VARCHAR(64) COMMENT '当前审批人',
    current_approve_node VARCHAR(100) COMMENT '当前审批节点',
    approve_result VARCHAR(20) DEFAULT 'PENDING' COMMENT '审批结果: PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝',
    approve_advice TEXT COMMENT '审批意见',
    approve_date DATETIME COMMENT '审批完成日期',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_application_id (application_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_application_date (application_date),
    INDEX idx_current_approver (current_approver),
    CONSTRAINT fk_credit_app_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='授信申请表';

-- ========================================
-- 4. 用信申请表
-- ========================================
CREATE TABLE IF NOT EXISTS usage_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    usage_id VARCHAR(64) NOT NULL UNIQUE COMMENT '用信申请ID',
    application_id VARCHAR(64) COMMENT '授信申请ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    quota_id BIGINT NOT NULL COMMENT '额度ID',
    product_type VARCHAR(50) NOT NULL COMMENT '产品类型',
    usage_amount DECIMAL(20,4) NOT NULL COMMENT '用信金额',
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    usage_term INT COMMENT '用信期限(天)',
    interest_rate DECIMAL(8,4) COMMENT '利率(%)',
    repayment_method VARCHAR(50) COMMENT '还款方式',
    guarantee_method VARCHAR(100) COMMENT '担保方式',
    fund_usage VARCHAR(500) COMMENT '资金用途',
    application_date DATE NOT NULL COMMENT '申请日期',
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态: DRAFT-草稿, SUBMITTED-已提交, IN_REVIEW-审核中, APPROVED-已批准, REJECTED-已拒绝, CANCELLED-已取消, EXECUTED-已执行',
    contract_no VARCHAR(100) COMMENT '合同编号',
    loan_note_no VARCHAR(100) COMMENT '借据编号',
    disbursement_date DATE COMMENT '放款日期',
    maturity_date DATE COMMENT '到期日期',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_usage_id (usage_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_quota_id (quota_id),
    INDEX idx_product_type (product_type),
    INDEX idx_status (status),
    INDEX idx_disbursement_date (disbursement_date),
    INDEX idx_contract_no (contract_no),
    CONSTRAINT fk_usage_app_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_usage_app_quota FOREIGN KEY (quota_id) REFERENCES customer_quota(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用信申请表';

-- ========================================
-- 5. 额度使用明细表
-- ========================================
CREATE TABLE IF NOT EXISTS quota_usage_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    detail_no VARCHAR(64) NOT NULL UNIQUE COMMENT '明细编号',
    quota_id BIGINT NOT NULL COMMENT '额度ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    usage_id VARCHAR(64) COMMENT '用信申请ID',
    transaction_type VARCHAR(20) NOT NULL COMMENT '交易类型: OCCUPY-占用, RELEASE-释放, FREEZE-冻结, UNFREEZE-解冻, ADJUST-调整, LOCK-锁定, UNLOCK-解锁',
    transaction_amount DECIMAL(20,4) NOT NULL COMMENT '交易金额',
    before_balance DECIMAL(20,4) NOT NULL COMMENT '交易前余额',
    after_balance DECIMAL(20,4) NOT NULL COMMENT '交易后余额',
    before_used DECIMAL(20,4) COMMENT '交易前已用额度',
    after_used DECIMAL(20,4) COMMENT '交易后已用额度',
    before_locked DECIMAL(20,4) COMMENT '交易前锁定额度',
    after_locked DECIMAL(20,4) COMMENT '交易后锁定额度',
    business_type VARCHAR(50) COMMENT '业务类型',
    business_ref_no VARCHAR(100) COMMENT '业务参考号',
    operator VARCHAR(64) COMMENT '操作员',
    transaction_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    remark TEXT COMMENT '备注',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_detail_no (detail_no),
    INDEX idx_quota_id (quota_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_usage_id (usage_id),
    INDEX idx_transaction_time (transaction_time),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_business_ref_no (business_ref_no),
    CONSTRAINT fk_usage_detail_quota FOREIGN KEY (quota_id) REFERENCES customer_quota(id),
    CONSTRAINT fk_usage_detail_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度使用明细表';

-- ========================================
-- 6. 风险监控指标表
-- ========================================
CREATE TABLE IF NOT EXISTS risk_monitoring_index (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '指标ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    index_type VARCHAR(50) NOT NULL COMMENT '指标类型',
    index_code VARCHAR(50) NOT NULL COMMENT '指标代码',
    index_name VARCHAR(200) NOT NULL COMMENT '指标名称',
    index_value DECIMAL(20,4) COMMENT '指标值',
    index_unit VARCHAR(20) COMMENT '指标单位',
    threshold_value DECIMAL(20,4) COMMENT '阈值',
    threshold_min DECIMAL(20,4) COMMENT '阈值下限',
    threshold_max DECIMAL(20,4) COMMENT '阈值上限',
    risk_level VARCHAR(10) COMMENT '风险等级: LOW-低, MEDIUM-中, HIGH-高, CRITICAL-严重',
    calc_date DATE NOT NULL COMMENT '计算日期',
    calc_method VARCHAR(200) COMMENT '计算方法',
    data_source VARCHAR(100) COMMENT '数据来源',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-有效, INACTIVE-无效',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_customer_id (customer_id),
    INDEX idx_index_code (index_code),
    INDEX idx_calc_date (calc_date),
    INDEX idx_risk_level (risk_level),
    INDEX idx_index_type (index_type),
    CONSTRAINT fk_risk_index_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风险监控指标表';

-- ========================================
-- 7. 风险预警事件表
-- ========================================
CREATE TABLE IF NOT EXISTS risk_warning_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预警ID',
    warning_no VARCHAR(64) NOT NULL UNIQUE COMMENT '预警编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    rule_id BIGINT COMMENT '触发规则ID',
    warning_type VARCHAR(50) NOT NULL COMMENT '预警类型',
    warning_code VARCHAR(50) NOT NULL COMMENT '预警代码',
    warning_title VARCHAR(200) NOT NULL COMMENT '预警标题',
    warning_content TEXT NOT NULL COMMENT '预警内容',
    risk_level VARCHAR(10) NOT NULL COMMENT '风险等级: LOW-低, MEDIUM-中, HIGH-高, CRITICAL-严重',
    warning_status VARCHAR(20) NOT NULL DEFAULT 'UNHANDLED' COMMENT '预警状态: UNHANDLED-未处理, HANDLING-处理中, HANDLED-已处理, IGNORED-已忽略',
    handler VARCHAR(64) COMMENT '处理人',
    handle_time DATETIME COMMENT '处理时间',
    handle_result TEXT COMMENT '处理结果',
    warning_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预警日期',
    resolve_date DATETIME COMMENT '解除日期',
    notify_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '通知状态: PENDING-待发送, SENT-已发送, FAILED-发送失败',
    notify_time DATETIME COMMENT '通知时间',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_warning_no (warning_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_warning_type (warning_type),
    INDEX idx_warning_status (warning_status),
    INDEX idx_warning_date (warning_date),
    INDEX idx_risk_level (risk_level),
    INDEX idx_rule_id (rule_id),
    CONSTRAINT fk_warning_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_warning_rule FOREIGN KEY (rule_id) REFERENCES risk_warning_rule(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风险预警事件表';

-- ========================================
-- 8. 审批流程表
-- ========================================
CREATE TABLE IF NOT EXISTS approval_process (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    process_id VARCHAR(64) NOT NULL UNIQUE COMMENT '流程ID',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型: CREDIT_APPLICATION-授信申请, USAGE_APPLICATION-用信申请, QUOTA_ADJUSTMENT-额度调整',
    business_id VARCHAR(64) NOT NULL COMMENT '业务ID',
    process_definition_id VARCHAR(100) NOT NULL COMMENT '流程定义ID',
    process_name VARCHAR(200) NOT NULL COMMENT '流程名称',
    current_node VARCHAR(100) COMMENT '当前节点',
    current_assignee VARCHAR(64) COMMENT '当前处理人',
    process_status VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '流程状态: RUNNING-运行中, COMPLETED-已完成, TERMINATED-已终止, REJECTED-已拒绝',
    priority VARCHAR(10) DEFAULT 'NORMAL' COMMENT '优先级: LOW-低, NORMAL-普通, HIGH-高, URGENT-紧急',
    start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    initiator VARCHAR(64) COMMENT '发起人',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_process_id (process_id),
    INDEX idx_business_type (business_type),
    INDEX idx_business_id (business_id),
    INDEX idx_process_status (process_status),
    INDEX idx_current_assignee (current_assignee),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批流程表';

-- ========================================
-- 9. 审批节点表
-- ========================================
CREATE TABLE IF NOT EXISTS approval_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '节点ID',
    process_id VARCHAR(64) NOT NULL COMMENT '流程ID',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    node_type VARCHAR(50) NOT NULL COMMENT '节点类型: START-开始, APPROVAL-审批, COUNTERSIGN-会签, TRANSFER-转办, END-结束',
    node_order INT NOT NULL COMMENT '节点顺序',
    assignee_type VARCHAR(20) COMMENT '处理人类型: USER-用户, ROLE-角色, DEPARTMENT-部门',
    assignee_id VARCHAR(64) COMMENT '处理人ID',
    assignee_name VARCHAR(100) COMMENT '处理人姓名',
    node_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '节点状态: PENDING-待处理, PROCESSING-处理中, COMPLETED-已完成, REJECTED-已拒绝, SKIPPED-已跳过',
    approve_result VARCHAR(20) DEFAULT 'PENDING' COMMENT '审批结果: PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝, TRANSFERRED-已转办',
    approve_opinion TEXT COMMENT '审批意见',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_minutes INT COMMENT '处理时长(分钟)',
    timeout_hours INT COMMENT '超时时间(小时)',
    is_timeout TINYINT(1) DEFAULT 0 COMMENT '是否超时: 0-否, 1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_process_id (process_id),
    INDEX idx_node_status (node_status),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_approve_result (approve_result),
    INDEX idx_node_order (node_order),
    CONSTRAINT fk_approval_node_process FOREIGN KEY (process_id) REFERENCES approval_process(process_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批节点表';

-- ========================================
-- 10. 流程定义表
-- ========================================
CREATE TABLE IF NOT EXISTS process_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    definition_id VARCHAR(100) NOT NULL UNIQUE COMMENT '定义ID',
    definition_name VARCHAR(200) NOT NULL COMMENT '定义名称',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号',
    node_config TEXT NOT NULL COMMENT '节点配置(JSON格式)',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-有效, INACTIVE-无效',
    description VARCHAR(500) COMMENT '描述',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_definition_id (definition_id),
    INDEX idx_business_type (business_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程定义表';

-- ========================================
-- 11. 担保信息表
-- ========================================
CREATE TABLE IF NOT EXISTS guarantee_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    guarantee_no VARCHAR(64) NOT NULL UNIQUE COMMENT '担保编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    application_id VARCHAR(64) COMMENT '授信申请ID',
    guarantee_type VARCHAR(30) NOT NULL COMMENT '担保方式: MORTGAGE-抵押, PLEDGE-质押, GUARANTEE-保证, CREDIT-信用',
    guarantee_amount DECIMAL(20,4) NOT NULL COMMENT '担保金额',
    guarantor_name VARCHAR(200) COMMENT '担保人/抵押人名称',
    guarantor_id_type VARCHAR(20) COMMENT '担保人证件类型',
    guarantor_id_number VARCHAR(50) COMMENT '担保人证件号码',
    collateral_type VARCHAR(50) COMMENT '抵押物/质押物类型',
    collateral_desc VARCHAR(500) COMMENT '抵押物/质押物描述',
    collateral_value DECIMAL(20,4) COMMENT '抵押物/质押物评估价值',
    valuation_date DATE COMMENT '评估日期',
    valuation_agency VARCHAR(200) COMMENT '评估机构',
    register_date DATE COMMENT '登记日期',
    register_no VARCHAR(100) COMMENT '登记编号',
    effective_date DATE COMMENT '生效日期',
    expire_date DATE COMMENT '到期日期',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-有效, RELEASED-已释放, CANCELLED-已取消',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_guarantee_no (guarantee_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_application_id (application_id),
    INDEX idx_guarantee_type (guarantee_type),
    INDEX idx_status (status),
    CONSTRAINT fk_guarantee_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='担保信息表';

-- ========================================
-- 12. 合同信息表
-- ========================================
CREATE TABLE IF NOT EXISTS contract_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_no VARCHAR(100) NOT NULL UNIQUE COMMENT '合同编号',
    contract_name VARCHAR(200) NOT NULL COMMENT '合同名称',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    usage_id VARCHAR(64) COMMENT '用信申请ID',
    contract_type VARCHAR(50) NOT NULL COMMENT '合同类型',
    contract_amount DECIMAL(20,4) NOT NULL COMMENT '合同金额',
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    sign_date DATE COMMENT '签订日期',
    effective_date DATE COMMENT '生效日期',
    expire_date DATE COMMENT '到期日期',
    contract_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '合同状态: DRAFT-草稿, SIGNED-已签订, EFFECTIVE-生效中, EXPIRED-已到期, TERMINATED-已终止',
    contract_file_url VARCHAR(500) COMMENT '合同文件URL',
    remark VARCHAR(500) COMMENT '备注',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_contract_no (contract_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_usage_id (usage_id),
    INDEX idx_contract_status (contract_status),
    INDEX idx_expire_date (expire_date),
    CONSTRAINT fk_contract_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同信息表';

-- ========================================
-- 13. 利率配置表
-- ========================================
CREATE TABLE IF NOT EXISTS interest_rate_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    rate_code VARCHAR(50) NOT NULL UNIQUE COMMENT '利率代码',
    rate_name VARCHAR(100) NOT NULL COMMENT '利率名称',
    rate_type VARCHAR(30) NOT NULL COMMENT '利率类型: FIXED-固定利率, FLOATING-浮动利率, BASE_RATE-基准利率',
    base_rate DECIMAL(8,4) COMMENT '基准利率(%)',
    spread_rate DECIMAL(8,4) COMMENT '利差(%)',
    total_rate DECIMAL(8,4) NOT NULL COMMENT '总利率(%)',
    effective_date DATE NOT NULL COMMENT '生效日期',
    expire_date DATE COMMENT '失效日期',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-有效, INACTIVE-无效',
    description VARCHAR(500) COMMENT '描述',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_rate_code (rate_code),
    INDEX idx_rate_type (rate_type),
    INDEX idx_effective_date (effective_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='利率配置表';

-- ========================================
-- 14. 额度调整记录表
-- ========================================
CREATE TABLE IF NOT EXISTS quota_adjustment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    adjustment_no VARCHAR(64) NOT NULL UNIQUE COMMENT '调整编号',
    quota_type VARCHAR(20) NOT NULL COMMENT '额度类型: GROUP-集团额度, CUSTOMER-客户额度, APPROVAL-批复额度',
    quota_id BIGINT NOT NULL COMMENT '额度ID',
    adjustment_type VARCHAR(20) NOT NULL COMMENT '调整类型: INCREASE-调增, DECREASE-调减, FREEZE-冻结, UNFREEZE-解冻',
    before_amount DECIMAL(20,4) NOT NULL COMMENT '调整前金额',
    adjustment_amount DECIMAL(20,4) NOT NULL COMMENT '调整金额',
    after_amount DECIMAL(20,4) NOT NULL COMMENT '调整后金额',
    adjustment_reason VARCHAR(500) COMMENT '调整原因',
    application_id VARCHAR(64) COMMENT '关联申请ID',
    approval_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审批状态: PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝',
    approver VARCHAR(64) COMMENT '审批人',
    approve_time DATETIME COMMENT '审批时间',
    approve_opinion VARCHAR(500) COMMENT '审批意见',
    operator VARCHAR(64) COMMENT '操作人',
    operate_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_adjustment_no (adjustment_no),
    INDEX idx_quota_type (quota_type),
    INDEX idx_quota_id (quota_id),
    INDEX idx_adjustment_type (adjustment_type),
    INDEX idx_operate_time (operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度调整记录表';

-- ========================================
-- 插入示例数据
-- ========================================

-- 插入示例集团客户关系数据
INSERT INTO group_relationship (parent_customer_id, child_customer_id, relationship_type, control_ratio, relationship_desc, effective_date, status, create_by) VALUES
(1, 2, 'CONTROL', 60.00, '集团控股子公司', '2020-01-01', 'ACTIVE', 'admin');

-- 插入示例客户关联方数据
INSERT INTO customer_affiliate (customer_id, affiliate_type, affiliate_name, affiliate_identity, relationship_desc, relationship_ratio, status, create_by) VALUES
(1, 'SHAREHOLDER', '测试投资集团', '91110000000000010A', '持股60%', 60.00, 'ACTIVE', 'admin'),
(1, 'CONTROLLER', '张三', '110101198001011234', '实际控制人', 100.00, 'ACTIVE', 'admin'),
(1, 'GUARANTOR', '李四', '110101198501015678', '连带责任担保', NULL, 'ACTIVE', 'admin');

-- 插入示例流程定义数据
INSERT INTO process_definition (definition_id, definition_name, business_type, version, node_config, status, description, create_by) VALUES
('PROC_CREDIT_001', '授信申请审批流程', 'CREDIT_APPLICATION', 1, '[{"nodeOrder":1,"nodeType":"START","nodeName":"发起申请"},{"nodeOrder":2,"nodeType":"APPROVAL","nodeName":"客户经理初审","assigneeType":"ROLE","assigneeId":"ROLE_CUSTOMER_MANAGER"},{"nodeOrder":3,"nodeType":"APPROVAL","nodeName":"部门主管审批","assigneeType":"ROLE","assigneeId":"ROLE_DEPT_MANAGER"},{"nodeOrder":4,"nodeType":"APPROVAL","nodeName":"风控审批","assigneeType":"ROLE","assigneeId":"ROLE_RISK_MANAGER"},{"nodeOrder":5,"nodeType":"APPROVAL","nodeName":"分管领导审批","assigneeType":"ROLE","assigneeId":"ROLE_VICE_PRESIDENT"},{"nodeOrder":6,"nodeType":"END","nodeName":"流程结束"}]', 'ACTIVE', '标准授信申请审批流程', 'admin'),
('PROC_USAGE_001', '用信申请审批流程', 'USAGE_APPLICATION', 1, '[{"nodeOrder":1,"nodeType":"START","nodeName":"发起申请"},{"nodeOrder":2,"nodeType":"APPROVAL","nodeName":"客户经理审核","assigneeType":"ROLE","assigneeId":"ROLE_CUSTOMER_MANAGER"},{"nodeOrder":3,"nodeType":"APPROVAL","nodeName":"部门主管审批","assigneeType":"ROLE","assigneeId":"ROLE_DEPT_MANAGER"},{"nodeOrder":4,"nodeType":"END","nodeName":"流程结束"}]', 'ACTIVE', '标准用信申请审批流程', 'admin');

-- 插入示例利率配置数据
INSERT INTO interest_rate_config (rate_code, rate_name, rate_type, base_rate, spread_rate, total_rate, effective_date, status, description, create_by) VALUES
('RATE_LPR_1Y', 'LPR一年期利率', 'BASE_RATE', 3.4500, 0.0000, 3.4500, '2024-01-01', 'ACTIVE', '贷款市场报价利率一年期', 'admin'),
('RATE_LPR_5Y', 'LPR五年期利率', 'BASE_RATE', 3.9500, 0.0000, 3.9500, '2024-01-01', 'ACTIVE', '贷款市场报价利率五年期', 'admin'),
('RATE_CORP_LOAN', '对公贷款利率', 'FLOATING', 3.4500, 1.5000, 4.9500, '2024-01-01', 'ACTIVE', '对公贷款执行利率', 'admin'),
('RATE_INDIVIDUAL', '个人贷款利率', 'FLOATING', 3.4500, 2.0000, 5.4500, '2024-01-01', 'ACTIVE', '个人贷款执行利率', 'admin');

-- 插入示例风险监控指标数据
INSERT INTO risk_monitoring_index (customer_id, index_type, index_code, index_name, index_value, index_unit, threshold_value, risk_level, calc_date, data_source, status, create_by) VALUES
(1, 'QUOTA_USAGE', 'QUOTA_USAGE_RATE', '额度使用率', 25.0000, '%', 80.0000, 'LOW', '2026-02-12', 'SYSTEM', 'ACTIVE', 'admin'),
(1, 'FINANCIAL', 'DEBT_RATIO', '资产负债率', 45.5000, '%', 70.0000, 'LOW', '2026-02-12', 'EXTERNAL', 'ACTIVE', 'admin'),
(1, 'FINANCIAL', 'CURRENT_RATIO', '流动比率', 1.8500, '', 1.0000, 'LOW', '2026-02-12', 'EXTERNAL', 'ACTIVE', 'admin'),
(3, 'QUOTA_USAGE', 'QUOTA_USAGE_RATE', '额度使用率', 26.6667, '%', 80.0000, 'LOW', '2026-02-12', 'SYSTEM', 'ACTIVE', 'admin');
