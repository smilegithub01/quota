-- 额度管理系统优化数据库变更脚本
-- 版本: V2.0
-- 日期: 2026-02-12
-- 说明: 包含额度校验规则库、TCC事务支持、多币种管理等新增表结构

USE quota_system;

-- ========================================
-- 1. 额度校验规则表
-- ========================================
CREATE TABLE IF NOT EXISTS quota_validation_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    rule_code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则编码',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(30) NOT NULL COMMENT '规则类型: LIMIT-限额, STATUS-状态, COMPLIANCE-合规, BUSINESS-业务',
    validation_level INT NOT NULL COMMENT '校验层级: 1-集团, 2-客户, 3-客户细项, 4-批复, 5-合规, 6-业务',
    validation_object VARCHAR(30) NOT NULL COMMENT '校验对象: GROUP/CUSTOMER/APPROVAL/CONTRACT',
    rule_expression TEXT NOT NULL COMMENT '规则表达式(SpEL格式)',
    error_code VARCHAR(20) NOT NULL COMMENT '错误编码',
    error_message VARCHAR(500) NOT NULL COMMENT '错误信息模板',
    business_types VARCHAR(200) COMMENT '适用业务类型(逗号分隔): WORKING_CAPITAL_LOAN,PROJECT_LOAN,TRADE_FINANCE等',
    risk_levels VARCHAR(50) COMMENT '适用风险等级(逗号分隔): R1,R2,R3,R4,R5',
    priority INT NOT NULL DEFAULT 100 COMMENT '优先级(数值越小优先级越高)',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED-启用, DISABLED-禁用',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号',
    effective_date DATETIME COMMENT '生效时间',
    expire_date DATETIME COMMENT '失效时间',
    description VARCHAR(500) COMMENT '规则描述',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_rule_code (rule_code),
    INDEX idx_rule_type (rule_type),
    INDEX idx_validation_level (validation_level),
    INDEX idx_status (status),
    INDEX idx_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度校验规则表';

-- ========================================
-- 2. 规则版本历史表
-- ========================================
CREATE TABLE IF NOT EXISTS quota_validation_rule_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史ID',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    rule_code VARCHAR(50) NOT NULL COMMENT '规则编码',
    version INT NOT NULL COMMENT '版本号',
    rule_content TEXT NOT NULL COMMENT '规则内容快照(JSON格式)',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型: CREATE-创建, UPDATE-更新, DELETE-删除',
    change_reason VARCHAR(500) COMMENT '变更原因',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_rule_id (rule_id),
    INDEX idx_rule_code (rule_code),
    INDEX idx_version (version),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则版本历史表';

-- ========================================
-- 3. 规则灰度发布表
-- ========================================
CREATE TABLE IF NOT EXISTS rule_gray_release (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '发布ID',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    release_no VARCHAR(50) NOT NULL UNIQUE COMMENT '发布编号',
    from_version INT NOT NULL COMMENT '原版本',
    to_version INT NOT NULL COMMENT '目标版本',
    gray_ratio DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '灰度比例(%)',
    gray_scope VARCHAR(500) COMMENT '灰度范围(客户ID列表/部门列表)',
    release_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '发布状态: PENDING-待发布, GRAY-灰度中, FULL-全量, ROLLBACK-已回滚',
    release_strategy VARCHAR(20) NOT NULL DEFAULT 'RATIO' COMMENT '发布策略: RATIO-比例, SCOPE-范围, TIME-时间',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_rule_id (rule_id),
    INDEX idx_release_no (release_no),
    INDEX idx_release_status (release_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则灰度发布表';

-- ========================================
-- 4. TCC事务日志表
-- ========================================
CREATE TABLE IF NOT EXISTS tcc_transaction_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    xid VARCHAR(128) NOT NULL UNIQUE COMMENT '全局事务ID',
    transaction_name VARCHAR(100) NOT NULL COMMENT '事务名称',
    status VARCHAR(20) NOT NULL COMMENT '事务状态: TRY-尝试中, CONFIRMED-已确认, CANCELLED-已取消',
    lock_results TEXT COMMENT '锁定结果快照(JSON格式)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_xid (xid),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TCC事务日志表';

-- ========================================
-- 5. 汇率配置表
-- ========================================
CREATE TABLE IF NOT EXISTS exchange_rate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '汇率ID',
    currency_code VARCHAR(10) NOT NULL COMMENT '货币代码: CNY/USD/EUR/JPY等',
    currency_name VARCHAR(50) NOT NULL COMMENT '货币名称',
    rate_to_cny DECIMAL(20,8) NOT NULL COMMENT '对人民币汇率',
    rate_source VARCHAR(50) NOT NULL COMMENT '汇率来源: REUTERS-路透, PBOC-央行, MANUAL-手工',
    effective_date DATE NOT NULL COMMENT '生效日期',
    effective_time DATETIME NOT NULL COMMENT '生效时间',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-有效, INACTIVE-无效',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_currency_date (currency_code, effective_date),
    INDEX idx_currency_code (currency_code),
    INDEX idx_effective_date (effective_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='汇率配置表';

-- ========================================
-- 6. 额度占用链路追踪表
-- ========================================
CREATE TABLE IF NOT EXISTS quota_occupy_trace (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '追踪ID',
    trace_id VARCHAR(64) NOT NULL UNIQUE COMMENT '链路追踪ID',
    contract_no VARCHAR(50) NOT NULL COMMENT '合同编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    group_id BIGINT COMMENT '集团ID',
    occupy_amount DECIMAL(20,4) NOT NULL COMMENT '占用金额',
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    trace_data TEXT NOT NULL COMMENT '链路数据(JSON格式)',
    status VARCHAR(20) NOT NULL COMMENT '状态: SUCCESS-成功, FAILED-失败, ROLLBACK-已回滚',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_trace_id (trace_id),
    INDEX idx_contract_no (contract_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_group_id (group_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度占用链路追踪表';

-- ========================================
-- 7. 本地消息表（最终一致性保障）
-- ========================================
CREATE TABLE IF NOT EXISTS local_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    message_id VARCHAR(64) NOT NULL UNIQUE COMMENT '消息ID',
    message_type VARCHAR(50) NOT NULL COMMENT '消息类型',
    message_key VARCHAR(200) COMMENT '消息Key',
    message_body TEXT NOT NULL COMMENT '消息内容',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待发送, SENT-已发送, CONSUMED-已消费, FAILED-失败',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retry INT NOT NULL DEFAULT 5 COMMENT '最大重试次数',
    next_retry_time DATETIME COMMENT '下次重试时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_message_id (message_id),
    INDEX idx_status (status),
    INDEX idx_message_type (message_type),
    INDEX idx_next_retry_time (next_retry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地消息表';

-- ========================================
-- 8. 额度预警配置表
-- ========================================
CREATE TABLE IF NOT EXISTS quota_warning_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_code VARCHAR(50) NOT NULL UNIQUE COMMENT '配置编码',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    quota_type VARCHAR(30) NOT NULL COMMENT '额度类型: GROUP-集团, CUSTOMER-客户, APPROVAL-批复',
    warning_level VARCHAR(20) NOT NULL COMMENT '预警级别: YELLOW-黄色, ORANGE-橙色, RED-红色, BLOCK-阻断',
    threshold_value DECIMAL(5,2) NOT NULL COMMENT '阈值(%)',
    notify_channels VARCHAR(200) NOT NULL COMMENT '通知渠道: SMS,EMAIL,SYSTEM,WEWORK',
    notify_recipients VARCHAR(500) COMMENT '通知接收人',
    auto_block TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否自动阻断: 0-否, 1-是',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED-启用, DISABLED-禁用',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_code (config_code),
    INDEX idx_quota_type (quota_type),
    INDEX idx_warning_level (warning_level),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度预警配置表';

-- ========================================
-- 9. 修改现有表结构 - 添加多币种支持
-- ========================================

-- 为客户额度表添加多币种字段
ALTER TABLE customer_quota 
ADD COLUMN currency VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种' AFTER available_quota,
ADD COLUMN original_amount DECIMAL(20,4) COMMENT '原币金额' AFTER currency,
ADD COLUMN cny_equivalent DECIMAL(20,4) COMMENT '人民币等值金额' AFTER original_amount,
ADD COLUMN locked_quota DECIMAL(20,4) NOT NULL DEFAULT 0.0000 COMMENT '锁定额度' AFTER used_quota;

-- 为批复额度表添加锁定额度字段
ALTER TABLE approval_quota 
ADD COLUMN locked_quota DECIMAL(20,4) NOT NULL DEFAULT 0.0000 COMMENT '锁定额度' AFTER used_quota;

-- 为集团额度表添加锁定额度字段
ALTER TABLE group_quota 
ADD COLUMN locked_quota DECIMAL(20,4) NOT NULL DEFAULT 0.0000 COMMENT '锁定额度' AFTER used_quota;

-- 为合同占用表添加多币种和状态扩展
ALTER TABLE contract_occupancy 
ADD COLUMN currency VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种' AFTER occupancy_amount,
ADD COLUMN original_amount DECIMAL(20,4) COMMENT '原币金额' AFTER currency,
ADD COLUMN cny_equivalent DECIMAL(20,4) COMMENT '人民币等值金额' AFTER original_amount,
ADD COLUMN exchange_rate DECIMAL(20,8) COMMENT '占用时汇率' AFTER cny_equivalent,
ADD COLUMN transaction_id VARCHAR(128) COMMENT '事务ID' AFTER exchange_rate,
MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PRE_OCCUPIED' COMMENT '状态: PRE_OCCUPIED-预占, OCCUPIED-已占用, RELEASED-已释放, CANCELLED-已取消, EXPIRED-已过期';

-- 为客户额度细项表添加锁定额度字段
ALTER TABLE customer_quota_sub 
ADD COLUMN locked_quota DECIMAL(19,2) NOT NULL DEFAULT 0.00 COMMENT '锁定额度' AFTER used_quota;

-- 为集团额度细项表添加锁定额度字段
ALTER TABLE group_quota_sub 
ADD COLUMN locked_quota DECIMAL(19,2) NOT NULL DEFAULT 0.00 COMMENT '锁定额度' AFTER used_quota;

-- ========================================
-- 10. 插入初始校验规则数据
-- ========================================
INSERT INTO quota_validation_rule (rule_code, rule_name, rule_type, validation_level, validation_object, rule_expression, error_code, error_message, priority, status) VALUES
-- 第一级：集团限额校验
('RULE_GROUP_001', '集团总额度可用性校验', 'LIMIT', 1, 'GROUP', 
 '#groupQuota.availableQuota >= #amount && #groupQuota.status == T(com.bank.quota.core.domain.GroupQuota.QuotaStatus).ENABLED', 
 'GROUP_QUOTA_INSUFFICIENT', '集团额度不足，当前可用: {available}，申请金额: {amount}', 10, 'ENABLED'),
('RULE_GROUP_002', '集团细项额度可用性校验', 'LIMIT', 1, 'GROUP',
 '#groupQuotaSub.availableQuota >= #amount && #groupQuotaSub.status == T(com.bank.quota.core.domain.GroupQuotaSub.QuotaStatus).ENABLED',
 'GROUP_SUB_QUOTA_INSUFFICIENT', '集团细项额度不足，细项类型: {subType}，当前可用: {available}', 11, 'ENABLED'),

-- 第二级：客户额度校验
('RULE_CUSTOMER_001', '客户总额度可用性校验', 'LIMIT', 2, 'CUSTOMER',
 '#customerQuota.availableQuota >= #amount && #customerQuota.status == T(com.bank.quota.core.domain.CustomerQuota.QuotaStatus).ENABLED',
 'CUSTOMER_QUOTA_INSUFFICIENT', '客户额度不足，当前可用: {available}，申请金额: {amount}', 20, 'ENABLED'),
('RULE_CUSTOMER_002', '客户状态有效性校验', 'STATUS', 2, 'CUSTOMER',
 '#customer.status == T(com.bank.quota.core.domain.Customer.CustomerStatus).ACTIVE && #customerQuota.status == T(com.bank.quota.core.domain.CustomerQuota.QuotaStatus).ENABLED',
 'CUSTOMER_STATUS_INVALID', '客户状态异常，当前状态: {status}', 21, 'ENABLED'),
('RULE_CUSTOMER_003', '客户额度有效期校验', 'STATUS', 2, 'CUSTOMER',
 '#customerQuota.expiryDate == null || #customerQuota.expiryDate >= T(java.time.LocalDateTime).now()',
 'CUSTOMER_QUOTA_EXPIRED', '客户额度已过期，过期日期: {expiryDate}', 22, 'ENABLED'),

-- 第三级：客户细项额度校验
('RULE_CUSTOMER_SUB_001', '客户细项额度可用性校验', 'LIMIT', 3, 'CUSTOMER',
 '#customerQuotaSub.availableQuota >= #amount && #customerQuotaSub.status == T(com.bank.quota.core.domain.CustomerQuotaSub.QuotaStatus).ENABLED',
 'CUSTOMER_SUB_QUOTA_INSUFFICIENT', '客户细项额度不足，细项类型: {subType}，当前可用: {available}', 30, 'ENABLED'),

-- 第四级：批复额度校验
('RULE_APPROVAL_001', '批复额度可用性校验', 'LIMIT', 4, 'APPROVAL',
 '#approvalQuota.availableQuota >= #amount && #approvalQuota.status == T(com.bank.quota.core.domain.ApprovalQuota.QuotaStatus).ENABLED',
 'APPROVAL_QUOTA_INSUFFICIENT', '批复额度不足，批复编号: {approvalNo}，当前可用: {available}', 40, 'ENABLED'),
('RULE_APPROVAL_002', '批复有效期校验', 'STATUS', 4, 'APPROVAL',
 '#approvalQuota.expiryDate == null || #approvalQuota.expiryDate >= T(java.time.LocalDateTime).now()',
 'APPROVAL_EXPIRED', '批复已过期，批复编号: {approvalNo}，过期日期: {expiryDate}', 41, 'ENABLED'),

-- 第五级：监管合规校验
('RULE_COMPLIANCE_001', '单一客户集中度校验', 'COMPLIANCE', 5, 'CUSTOMER',
 'calculateSingleCustomerConcentration(#customerId) <= 0.10',
 'SINGLE_CUSTOMER_CONCENTRATION_EXCEED', '单一客户集中度超标，当前: {concentration}%，监管上限: 10%', 50, 'ENABLED'),
('RULE_COMPLIANCE_002', '集团客户集中度校验', 'COMPLIANCE', 5, 'GROUP',
 'calculateGroupCustomerConcentration(#groupId) <= 0.15',
 'GROUP_CUSTOMER_CONCENTRATION_EXCEED', '集团客户集中度超标，当前: {concentration}%，监管上限: 15%', 51, 'ENABLED'),
('RULE_COMPLIANCE_003', '关联方授信余额校验', 'COMPLIANCE', 5, 'CUSTOMER',
 'calculateAffiliateCreditBalance(#customerId) <= getNetCapital() * 0.20',
 'AFFILIATE_CREDIT_EXCEED', '关联方授信余额超标，当前: {balance}，监管上限: 净资本20%', 52, 'ENABLED'),

-- 第六级：业务规则校验
('RULE_BUSINESS_001', '风险等级限制校验', 'BUSINESS', 6, 'CUSTOMER',
 '#customerQuota.riskLevel in {T(com.bank.quota.core.domain.CustomerQuota.RiskLevel).R1, T(com.bank.quota.core.domain.CustomerQuota.RiskLevel).R2, T(com.bank.quota.core.domain.CustomerQuota.RiskLevel).R3} || #needExtraApproval == true',
 'RISK_LEVEL_RESTRICTION', '客户风险等级为{riskLevel}，需要额外审批', 60, 'ENABLED'),
('RULE_BUSINESS_002', '流动资金贷款额度上限校验', 'BUSINESS', 6, 'CONTRACT',
 '#businessType == ''WORKING_CAPITAL_LOAN'' ? #amount <= #customerQuota.totalQuota * 0.7 : true',
 'WORKING_CAPITAL_LOAN_EXCEED', '流动资金贷款金额超过客户总额度70%', 61, 'ENABLED');

-- ========================================
-- 11. 插入预警配置数据
-- ========================================
INSERT INTO quota_warning_config (config_code, config_name, quota_type, warning_level, threshold_value, notify_channels, auto_block, status, create_by) VALUES
('WARN_YELLOW_80', '额度使用率黄色预警', 'CUSTOMER', 'YELLOW', 80.00, 'SYSTEM,EMAIL', 0, 'ENABLED', 'SYSTEM'),
('WARN_ORANGE_90', '额度使用率橙色预警', 'CUSTOMER', 'ORANGE', 90.00, 'SYSTEM,EMAIL,SMS', 0, 'ENABLED', 'SYSTEM'),
('WARN_RED_95', '额度使用率红色预警', 'CUSTOMER', 'RED', 95.00, 'SYSTEM,EMAIL,SMS', 0, 'ENABLED', 'SYSTEM'),
('WARN_BLOCK_100', '额度使用率阻断', 'CUSTOMER', 'BLOCK', 100.00, 'SYSTEM,EMAIL,SMS', 1, 'ENABLED', 'SYSTEM'),
('WARN_GROUP_YELLOW', '集团额度使用率黄色预警', 'GROUP', 'YELLOW', 80.00, 'SYSTEM,EMAIL', 0, 'ENABLED', 'SYSTEM'),
('WARN_GROUP_ORANGE', '集团额度使用率橙色预警', 'GROUP', 'ORANGE', 90.00, 'SYSTEM,EMAIL,SMS', 0, 'ENABLED', 'SYSTEM'),
('WARN_GROUP_RED', '集团额度使用率红色预警', 'GROUP', 'RED', 95.00, 'SYSTEM,EMAIL,SMS', 0, 'ENABLED', 'SYSTEM');

-- ========================================
-- 12. 插入初始汇率数据
-- ========================================
INSERT INTO exchange_rate (currency_code, currency_name, rate_to_cny, rate_source, effective_date, effective_time, status, create_by) VALUES
('CNY', '人民币', 1.00000000, 'MANUAL', CURDATE(), NOW(), 'ACTIVE', 'SYSTEM'),
('USD', '美元', 7.20000000, 'PBOC', CURDATE(), NOW(), 'ACTIVE', 'SYSTEM'),
('EUR', '欧元', 7.80000000, 'PBOC', CURDATE(), NOW(), 'ACTIVE', 'SYSTEM'),
('JPY', '日元', 0.04800000, 'PBOC', CURDATE(), NOW(), 'ACTIVE', 'SYSTEM'),
('GBP', '英镑', 9.10000000, 'PBOC', CURDATE(), NOW(), 'ACTIVE', 'SYSTEM'),
('HKD', '港币', 0.92000000, 'PBOC', CURDATE(), NOW(), 'ACTIVE', 'SYSTEM');

-- ========================================
-- 13. 插入系统配置数据
-- ========================================
INSERT INTO system_config (config_key, config_value, config_name, category, description, status, create_by) VALUES
('validation.rule.enabled', 'true', '校验规则开关', 'VALIDATION', '是否启用可配置化校验规则', 'ENABLED', 'SYSTEM'),
('validation.rule.cache.seconds', '300', '规则缓存时间', 'VALIDATION', '校验规则缓存时间（秒）', 'ENABLED', 'SYSTEM'),
('tcc.transaction.timeout.seconds', '60', 'TCC事务超时时间', 'TRANSACTION', 'TCC分布式事务超时时间（秒）', 'ENABLED', 'SYSTEM'),
('occupy.pre.expire.hours', '24', '预占过期时间', 'QUOTA', '额度预占默认过期时间（小时）', 'ENABLED', 'SYSTEM'),
('exchange.rate.update.cron', '0 0 9 * * ?', '汇率更新时间', 'EXCHANGE', '汇率自动更新Cron表达式', 'ENABLED', 'SYSTEM'),
('sync.delay.max.seconds', '5', '同步最大延迟', 'SYNC', '统一授信数据同步最大延迟（秒）', 'ENABLED', 'SYSTEM'),
('trace.enabled', 'true', '链路追踪开关', 'TRACE', '是否启用额度占用链路追踪', 'ENABLED', 'SYSTEM'),
('audit.log.retention.years', '7', '审计日志保留年限', 'AUDIT', '审计日志保留年限', 'ENABLED', 'SYSTEM');
