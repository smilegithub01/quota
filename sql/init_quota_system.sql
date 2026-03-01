-- 额度管理系统数据库初始化脚本
-- Database: quota_system
-- Character Set: utf8mb4
-- Collation: utf8mb4_unicode_ci

-- 创建数据库
CREATE DATABASE IF NOT EXISTS quota_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quota_system;

-- 集团额度表
CREATE TABLE IF NOT EXISTS group_quota (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    group_id BIGINT NOT NULL UNIQUE COMMENT '集团ID',
    group_name VARCHAR(100) NOT NULL COMMENT '集团名称',
    total_quota DECIMAL(20,4) NOT NULL COMMENT '总额度',
    used_quota DECIMAL(20,4) NOT NULL DEFAULT 0.0000 COMMENT '已用额度',
    locked_quota DECIMAL(20,4) NOT NULL DEFAULT 0.0000 COMMENT '锁定额度',
    available_quota DECIMAL(20,4) NOT NULL COMMENT '可用额度',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED-有效, FROZEN-冻结, DISABLED-停用',
    description VARCHAR(500) COMMENT '描述',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_group_id (group_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='集团额度表';

-- 客户表（独立客户基础信息表）
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    customer_no VARCHAR(32) NOT NULL UNIQUE COMMENT '客户编号',
    customer_name VARCHAR(200) NOT NULL COMMENT '客户名称',
    customer_type VARCHAR(20) NOT NULL COMMENT '客户类型: CORPORATE-对公客户, INDIVIDUAL-个人客户, INTERBANK-同业客户',
    category VARCHAR(20) NOT NULL COMMENT '客户分类: GROUP_CUSTOMER-集团客户, SINGLE_CUSTOMER-单一客户',
    group_id BIGINT COMMENT '所属集团ID（集团客户必填）',
    group_name VARCHAR(200) COMMENT '集团名称',
    id_type VARCHAR(20) COMMENT '证件类型: USCC-统一社会信用代码, ID_CARD-身份证, PASSPORT-护照等',
    id_number VARCHAR(50) COMMENT '证件号码',
    legal_person VARCHAR(100) COMMENT '法定代表人',
    registered_address VARCHAR(500) COMMENT '注册地址',
    business_scope VARCHAR(1000) COMMENT '经营范围',
    registered_capital DECIMAL(20,4) COMMENT '注册资本',
    establish_date DATE COMMENT '成立日期',
    industry_code VARCHAR(20) COMMENT '行业代码',
    industry_name VARCHAR(100) COMMENT '行业名称',
    risk_level VARCHAR(5) NOT NULL DEFAULT 'R3' COMMENT '风险等级: R1-低风险, R2-较低风险, R3-中等风险, R4-较高风险, R5-高风险',
    credit_rating VARCHAR(10) COMMENT '信用评级',
    credit_score INT COMMENT '信用评分',
    contact_person VARCHAR(100) COMMENT '联系人',
    contact_phone VARCHAR(50) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '客户状态: ACTIVE-正常, FROZEN-冻结, DISABLED-停用, CANCELLED-注销',
    effective_date DATE COMMENT '生效日期',
    expiry_date DATE COMMENT '失效日期',
    description VARCHAR(500) COMMENT '描述',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_customer_no (customer_no),
    INDEX idx_customer_type (customer_type),
    INDEX idx_category (category),
    INDEX idx_group_id (group_id),
    INDEX idx_risk_level (risk_level),
    INDEX idx_status (status),
    INDEX idx_id_number (id_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- 客户额度表
CREATE TABLE IF NOT EXISTS customer_quota (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    customer_id BIGINT NOT NULL UNIQUE COMMENT '客户ID',
    customer_no VARCHAR(32) COMMENT '客户编号',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户名称',
    customer_type VARCHAR(20) NOT NULL COMMENT '客户类型: CORPORATE-对公客户, INDIVIDUAL-个人客户, INTERBANK-同业客户',
    category VARCHAR(20) COMMENT '客户分类: GROUP_CUSTOMER-集团客户, SINGLE_CUSTOMER-单一客户',
    group_id BIGINT COMMENT '所属集团ID',
    total_quota DECIMAL(20,4) NOT NULL COMMENT '总额度',
    used_quota DECIMAL(20,4) NOT NULL DEFAULT 0.0000 COMMENT '已用额度',
    locked_quota DECIMAL(20,4) NOT NULL DEFAULT 0.0000 COMMENT '锁定额度',
    available_quota DECIMAL(20,4) NOT NULL COMMENT '可用额度',
    risk_level VARCHAR(5) COMMENT '风险等级',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED-有效, FROZEN-冻结, DISABLED-停用',
    effective_date DATETIME COMMENT '生效日期',
    expiry_date DATETIME COMMENT '失效日期',
    description VARCHAR(500) COMMENT '描述',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_customer_id (customer_id),
    INDEX idx_customer_no (customer_no),
    INDEX idx_group_id (group_id),
    INDEX idx_status (status),
    INDEX idx_customer_type (customer_type),
    INDEX idx_category (category),
    INDEX idx_risk_level (risk_level),
    CONSTRAINT fk_customer_quota_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_customer_quota_group FOREIGN KEY (group_id) REFERENCES group_quota(group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户额度表';

-- 客户细项额度表
CREATE TABLE IF NOT EXISTS customer_quota_sub (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    customer_quota_id BIGINT NOT NULL COMMENT '客户额度ID',
    sub_item_type VARCHAR(50) NOT NULL COMMENT '细项类型: APPROVAL-批复额度, ITEM-项目额度, CONTRACT-合同额度',
    sub_item_id BIGINT NOT NULL COMMENT '细项ID',
    total_quota DECIMAL(19,2) NOT NULL COMMENT '细项总额度',
    used_quota DECIMAL(19,2) NOT NULL DEFAULT 0.00 COMMENT '细项已用额度',
    available_quota DECIMAL(19,2) NOT NULL COMMENT '细项可用额度',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED-有效, FROZEN-冻结, DISABLED-停用',
    description VARCHAR(500) COMMENT '描述',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_customer_quota_id (customer_quota_id),
    INDEX idx_sub_item (sub_item_type, sub_item_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户细项额度表';

-- 批复额度表
CREATE TABLE IF NOT EXISTS approval_quota (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    customer_quota_sub_id BIGINT NOT NULL COMMENT '客户细项额度ID',
    approval_no VARCHAR(50) NOT NULL UNIQUE COMMENT '批复编号',
    approval_type VARCHAR(50) NOT NULL COMMENT '批复类型',
    approval_quota DECIMAL(19,2) NOT NULL COMMENT '批复额度',
    used_quota DECIMAL(19,2) NOT NULL DEFAULT 0.00 COMMENT '已用额度',
    available_quota DECIMAL(19,2) NOT NULL COMMENT '可用额度',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED-有效, FROZEN-冻结, DISABLED-停用',
    description VARCHAR(500) COMMENT '描述',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_approval_no (approval_no),
    INDEX idx_customer_quota_sub_id (customer_quota_sub_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批复额度表';

-- 白名单表
CREATE TABLE IF NOT EXISTS whitelist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    whitelist_no VARCHAR(50) NOT NULL UNIQUE COMMENT '白名单编号',
    whitelist_type VARCHAR(50) NOT NULL COMMENT '白名单类型: CUSTOMER-客户豁免, BUSINESS-业务豁免, AMOUNT-金额豁免',
    customer_id BIGINT COMMENT '客户ID',
    customer_name VARCHAR(100) COMMENT '客户名称',
    business_type VARCHAR(50) COMMENT '业务类型',
    exempt_rule VARCHAR(50) NOT NULL COMMENT '豁免规则: FULL-完全豁免, PARTIAL-部分豁免, THRESHOLD-阈值豁免',
    exempt_amount DECIMAL(20,4) COMMENT '豁免金额',
    effective_time DATETIME NOT NULL COMMENT '生效时间',
    expiry_time DATETIME NOT NULL COMMENT '失效时间',
    status VARCHAR(20) NOT NULL COMMENT '状态: PENDING-待审批, ACTIVE-已激活, INVALID-已失效',
    apply_reason VARCHAR(500) NOT NULL COMMENT '申请原因',
    approve_remark VARCHAR(500) COMMENT '审批备注',
    applicant VARCHAR(50) NOT NULL COMMENT '申请人',
    approver VARCHAR(50) COMMENT '审批人',
    apply_time DATETIME NOT NULL COMMENT '申请时间',
    approve_time DATETIME COMMENT '审批时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_whitelist_no (whitelist_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_whitelist_type (whitelist_type),
    INDEX idx_status (status),
    INDEX idx_effective_time (effective_time),
    INDEX idx_expiry_time (expiry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='白名单表';

-- 白名单豁免项配置表
CREATE TABLE IF NOT EXISTS whitelist_exempt_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    whitelist_no VARCHAR(50) NOT NULL COMMENT '白名单编号',
    check_point VARCHAR(50) NOT NULL COMMENT '校验点编码',
    check_point_name VARCHAR(100) NOT NULL COMMENT '校验点名称',
    exempt_type VARCHAR(30) NOT NULL COMMENT '豁免类型: FULL-完全豁免, PARTIAL-部分豁免, THRESHOLD-阈值豁免, CONDITIONAL-条件豁免',
    exempt_value DECIMAL(20,4) COMMENT '豁免值(部分豁免金额/阈值)',
    exempt_ratio DECIMAL(5,4) COMMENT '豁免比例(0-1)',
    exempt_condition VARCHAR(500) COMMENT '豁免条件表达式(SpEL)',
    override_threshold DECIMAL(20,4) COMMENT '覆盖阈值(替换原校验阈值)',
    effective_time DATETIME NOT NULL COMMENT '生效时间',
    expiry_time DATETIME NOT NULL COMMENT '失效时间',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT '状态: ENABLED-启用, DISABLED-禁用',
    remark VARCHAR(500) COMMENT '备注说明',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    UNIQUE KEY uk_whitelist_check_point (whitelist_no, check_point),
    INDEX idx_whitelist_no (whitelist_no),
    INDEX idx_check_point (check_point),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='白名单豁免项配置表';

-- 白名单使用记录表
CREATE TABLE IF NOT EXISTS whitelist_usage_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    log_id VARCHAR(50) NOT NULL UNIQUE COMMENT '日志ID',
    whitelist_no VARCHAR(50) NOT NULL COMMENT '白名单编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    business_type VARCHAR(50) COMMENT '业务类型',
    check_point VARCHAR(50) COMMENT '校验点编码',
    exempt_type VARCHAR(30) COMMENT '豁免类型',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型: OCCUPY-占用, RELEASE-释放, ADJUST-调整',
    original_amount DECIMAL(20,4) COMMENT '原始金额',
    exempt_amount DECIMAL(20,4) COMMENT '豁免金额',
    actual_amount DECIMAL(20,4) COMMENT '实际金额',
    operation_time DATETIME NOT NULL COMMENT '操作时间',
    operator VARCHAR(50) COMMENT '操作人',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_log_id (log_id),
    INDEX idx_whitelist_no (whitelist_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_business_type (business_type),
    INDEX idx_operation_type (operation_type),
    INDEX idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='白名单使用记录表';

-- 额度锁定表
CREATE TABLE IF NOT EXISTS quota_lock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    lock_no VARCHAR(50) NOT NULL UNIQUE COMMENT '锁定编号',
    object_type VARCHAR(20) NOT NULL COMMENT '对象类型: GROUP-集团, CUSTOMER-客户, APPROVAL-批复',
    object_id BIGINT NOT NULL COMMENT '对象ID',
    lock_amount DECIMAL(19,2) NOT NULL COMMENT '锁定金额',
    lock_reason VARCHAR(500) COMMENT '锁定原因',
    expiry_time DATETIME COMMENT '过期时间',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-活跃, RELEASED-已释放, EXPIRED-已过期',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_lock_no (lock_no),
    INDEX idx_object (object_type, object_id),
    INDEX idx_status (status),
    INDEX idx_expiry_time (expiry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度锁定表';

-- 合同占用表
CREATE TABLE IF NOT EXISTS contract_occupancy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    contract_no VARCHAR(50) NOT NULL UNIQUE COMMENT '合同编号',
    approval_quota_sub_id BIGINT NOT NULL COMMENT '批复细项ID',
    occupancy_amount DECIMAL(19,2) NOT NULL COMMENT '占用金额',
    status VARCHAR(20) NOT NULL DEFAULT 'OCCUPIED' COMMENT '状态: OCCUPIED-占用中, RELEASED-已释放',
    description VARCHAR(500) COMMENT '描述',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_contract_no (contract_no),
    INDEX idx_approval_quota_sub_id (approval_quota_sub_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同占用表';

-- 审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    object_type VARCHAR(50) NOT NULL COMMENT '对象类型',
    object_id VARCHAR(100) NOT NULL COMMENT '对象ID',
    operation_desc VARCHAR(1000) COMMENT '操作描述',
    before_value TEXT COMMENT '变更前值',
    after_value TEXT COMMENT '变更后值',
    operator VARCHAR(50) COMMENT '操作人',
    operator_ip VARCHAR(50) COMMENT '操作人IP',
    status VARCHAR(20) NOT NULL COMMENT '操作状态: SUCCESS, FAILED',
    error_message TEXT COMMENT '错误信息',
    duration_ms BIGINT COMMENT '执行耗时(ms)',
    request_id VARCHAR(50) COMMENT '请求ID',
    trace_id VARCHAR(50) COMMENT '链路追踪ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_operation_type (operation_type),
    INDEX idx_object (object_type, object_id),
    INDEX idx_operator (operator),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    INDEX idx_trace_id (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- 风险预警规则表
CREATE TABLE IF NOT EXISTS risk_warning_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    rule_code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则编码',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型: USAGE_RATE-使用率阈值, BALANCE_LOW-余额不足',
    warning_level VARCHAR(20) NOT NULL COMMENT '预警级别: LOW, MEDIUM, HIGH, CRITICAL',
    threshold_type VARCHAR(20) NOT NULL COMMENT '阈值类型: GREATER_THAN, LESS_THAN, EQUAL, BETWEEN',
    threshold_value DECIMAL(19,4) NOT NULL COMMENT '阈值',
    object_type VARCHAR(20) NOT NULL COMMENT '对象类型: GROUP, CUSTOMER, APPROVAL',
    notify_channels VARCHAR(200) COMMENT '通知渠道: EMAIL,SMS,WEBHOOK',
    notify_recipients VARCHAR(500) COMMENT '通知接收者',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED, DISABLED',
    description VARCHAR(500) COMMENT '描述',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_rule_code (rule_code),
    INDEX idx_rule_type (rule_type),
    INDEX idx_warning_level (warning_level),
    INDEX idx_object_type (object_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风险预警规则表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置Key',
    config_value VARCHAR(500) NOT NULL COMMENT '配置Value',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    category VARCHAR(50) NOT NULL COMMENT '配置分类',
    description VARCHAR(500) COMMENT '描述',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED, DISABLED',
    create_by VARCHAR NULL COMMENT '创建(50) NOT人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入初始系统配置数据
INSERT INTO system_config (config_key, config_value, config_name, category, description, create_by) VALUES
('quota.default.currency', 'CNY', '默认货币', 'QUOTA', '系统默认货币类型', 'SYSTEM'),
('quota.default.usage.warning.threshold', '0.8', '默认额度使用预警阈值', 'QUOTA', '当额度使用率超过此值时触发预警', 'SYSTEM'),
('quota.lock.timeout.seconds', '30', '额度锁定超时时间', 'QUOTA', '额度锁定的默认超时时间（秒）', 'SYSTEM'),
('audit.enabled', 'true', '审计日志开关', 'SYSTEM', '是否启用审计日志记录', 'SYSTEM'),
('risk.warning.enabled', 'true', '风险预警开关', 'SYSTEM', '是否启用风险预警功能', 'SYSTEM'),
('cache.local.enabled', 'true', '本地缓存开关', 'CACHE', '是否启用本地缓存', 'SYSTEM'),
('cache.local.expire.seconds', '300', '本地缓存过期时间', 'CACHE', '本地缓存默认过期时间（秒）', 'SYSTEM'),
('cache.redis.expire.seconds', '600', 'Redis缓存过期时间', 'CACHE', 'Redis缓存默认过期时间（秒）', 'SYSTEM'),
('async.core.pool.size', '5', '异步核心线程数', 'ASYNC', '异步处理核心线程池大小', 'SYSTEM'),
('async.max.pool.size', '10', '异步最大线程数', 'ASYNC', '异步处理最大线程池大小', 'SYSTEM'),
('rate.limit.enabled', 'true', '限流开关', 'SECURITY', '是否启用API限流', 'SYSTEM'),
('rate.limit.default.requests', '100', '默认限流阈值', 'SECURITY', '默认限流请求数', 'SYSTEM'),
('encryption.enabled', 'true', '加密开关', 'SECURITY', '是否启用数据加密', 'SYSTEM'),
('signature.enabled', 'true', '签名验证开关', 'SECURITY', '是否启用请求签名验证', 'SYSTEM');

-- 插入示例集团数据
INSERT INTO group_quota (group_id, group_name, total_quota, used_quota, locked_quota, available_quota, status, create_by) VALUES
(1001, '测试集团A', 100000000.0000, 30000000.0000, 5000000.0000, 65000000.0000, 'ENABLED', 'admin'),
(1002, '测试集团B', 50000000.0000, 10000000.0000, 2000000.0000, 38000000.0000, 'ENABLED', 'admin');

-- 插入示例客户数据
INSERT INTO customer (customer_no, customer_name, customer_type, category, group_id, group_name, id_type, id_number, legal_person, registered_address, business_scope, registered_capital, establish_date, industry_code, industry_name, risk_level, credit_rating, credit_score, contact_person, contact_phone, contact_email, status, create_by) VALUES
('CUS20260212001', '测试企业有限公司', 'CORPORATE', 'GROUP_CUSTOMER', 1001, '测试集团A', 'USCC', '91110000000000001X', '张三', '北京市朝阳区', '技术开发、技术咨询', 10000000.0000, '2020-01-01', 'J66', '货币金融服务', 'R2', 'AA', 780, '李四', '13800138001', 'contact1@example.com', 'ACTIVE', 'admin'),
('CUS20260212002', '测试贸易公司', 'CORPORATE', 'GROUP_CUSTOMER', 1001, '测试集团A', 'USCC', '91110000000000002Y', '王五', '北京市海淀区', '国内贸易、进出口业务', 5000000.0000, '2019-06-15', 'F51', '批发业', 'R3', 'A', 720, '赵六', '13800138002', 'contact2@example.com', 'ACTIVE', 'admin'),
('CUS20260212003', '独立科技公司', 'CORPORATE', 'SINGLE_CUSTOMER', NULL, NULL, 'USCC', '91110000000000003Z', '钱七', '上海市浦东新区', '软件开发、技术服务', 20000000.0000, '2018-03-20', 'I65', '软件和信息技术服务业', 'R1', 'AAA', 850, '孙八', '13800138003', 'contact3@example.com', 'ACTIVE', 'admin'),
('CUS20260212004', '测试个人客户', 'INDIVIDUAL', 'SINGLE_CUSTOMER', NULL, NULL, 'ID_CARD', '110101199001011234', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'R3', NULL, 700, '周九', '13800138004', 'contact4@example.com', 'ACTIVE', 'admin'),
('CUS20260212005', '测试同业银行', 'INTERBANK', 'GROUP_CUSTOMER', 1002, '测试集团B', 'USCC', '91110000000000005A', '吴十', '北京市西城区', '货币银行服务', 500000000.0000, '2010-01-01', 'J66', '货币金融服务', 'R2', 'AA', 800, '郑十一', '13800138005', 'contact5@example.com', 'ACTIVE', 'admin');

-- 插入示例客户额度数据
INSERT INTO customer_quota (customer_id, customer_no, customer_name, customer_type, category, group_id, total_quota, used_quota, locked_quota, available_quota, risk_level, status, create_by) VALUES
(1, 'CUS20260212001', '测试企业有限公司', 'CORPORATE', 'GROUP_CUSTOMER', 1001, 20000000.0000, 5000000.0000, 1000000.0000, 14000000.0000, 'R2', 'ENABLED', 'admin'),
(2, 'CUS20260212002', '测试贸易公司', 'CORPORATE', 'GROUP_CUSTOMER', 1001, 15000000.0000, 3000000.0000, 500000.0000, 11500000.0000, 'R3', 'ENABLED', 'admin'),
(3, 'CUS20260212003', '独立科技公司', 'CORPORATE', 'SINGLE_CUSTOMER', NULL, 30000000.0000, 8000000.0000, 2000000.0000, 20000000.0000, 'R1', 'ENABLED', 'admin'),
(4, 'CUS20260212004', '测试个人客户', 'INDIVIDUAL', 'SINGLE_CUSTOMER', NULL, 500000.0000, 100000.0000, 0.0000, 400000.0000, 'R3', 'ENABLED', 'admin'),
(5, 'CUS20260212005', '测试同业银行', 'INTERBANK', 'GROUP_CUSTOMER', 1002, 100000000.0000, 20000000.0000, 5000000.0000, 75000000.0000, 'R2', 'ENABLED', 'admin');

-- 插入示例白名单数据
INSERT INTO whitelist (whitelist_no, whitelist_type, customer_id, customer_name, exempt_rule, effective_time, expiry_time, status, apply_reason, applicant, apply_time) VALUES
('WL20260213001', 'CUSTOMER', 1, '测试企业有限公司', 'FULL', NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 'ACTIVE', 'VIP客户，需豁免额度校验', 'admin', NOW()),
('WL20260213002', 'CUSTOMER', 3, '独立科技公司', 'PARTIAL', NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 'ACTIVE', '战略合作伙伴，部分豁免', 'admin', NOW());

-- 插入白名单豁免项配置示例
INSERT INTO whitelist_exempt_item (whitelist_no, check_point, check_point_name, exempt_type, exempt_value, effective_time, expiry_time, status, remark, create_by) VALUES
('WL20260213001', 'GROUP_TOTAL_LIMIT', '集团总限额校验', 'FULL', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 'ENABLED', 'VIP客户完全豁免集团限额校验', 'admin'),
('WL20260213001', 'CUSTOMER_TOTAL_QUOTA', '客户总额度校验', 'FULL', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 'ENABLED', 'VIP客户完全豁免客户额度校验', 'admin'),
('WL20260213001', 'RISK_EXPOSURE', '风险敞口校验', 'FULL', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 'ENABLED', 'VIP客户完全豁免风险敞口校验', 'admin'),
('WL20260213002', 'RISK_EXPOSURE', '风险敞口校验', 'PARTIAL', 5000000.0000, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 'ENABLED', '战略合作伙伴风险敞口豁免500万', 'admin'),
('WL20260213002', 'COMPLIANCE_CHECK', '合规性校验', 'THRESHOLD', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 'ENABLED', '合规校验阈值提高', 'admin');

-- 更新override_threshold for THRESHOLD type
UPDATE whitelist_exempt_item SET override_threshold = 15000000.0000 WHERE whitelist_no = 'WL20260213002' AND check_point = 'COMPLIANCE_CHECK';

-- 插入示例风险预警规则
INSERT INTO risk_warning_rule (rule_code, rule_name, rule_type, warning_level, threshold_type, threshold_value, object_type, notify_channels, notify_recipients, status, create_by) VALUES
('RULE_USAGE_HIGH', '额度使用率过高预警', 'USAGE_RATE', 'HIGH', 'GREATER_THAN', 0.8000, 'CUSTOMER', 'EMAIL,SMS', 'risk@example.com', 'ENABLED', 'admin'),
('RULE_USAGE_CRITICAL', '额度使用率严重预警', 'USAGE_RATE', 'CRITICAL', 'GREATER_THAN', 0.9000, 'CUSTOMER', 'EMAIL,SMS,WEBHOOK', 'risk@example.com,ops@example.com', 'ENABLED', 'admin'),
('RULE_BALANCE_LOW', '可用余额不足预警', 'BALANCE_LOW', 'MEDIUM', 'LESS_THAN', 1000000.0000, 'CUSTOMER', 'EMAIL', 'risk@example.com', 'ENABLED', 'admin'),
('RULE_GROUP_USAGE_HIGH', '集团额度使用率过高预警', 'USAGE_RATE', 'HIGH', 'GREATER_THAN', 0.7500, 'GROUP', 'EMAIL,SMS', 'risk@example.com', 'ENABLED', 'admin');

-- =====================================================
-- 汇率管理相关表
-- =====================================================

-- 汇率数据源配置表
CREATE TABLE IF NOT EXISTS exchange_rate_source (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    source_code VARCHAR(20) NOT NULL UNIQUE COMMENT '数据源编码',
    source_name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    api_url VARCHAR(500) COMMENT 'API地址',
    auth_type VARCHAR(20) COMMENT '认证类型: API_KEY, OAUTH2, CERT',
    auth_config TEXT COMMENT '认证配置(JSON)',
    priority INT DEFAULT 1 COMMENT '优先级',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT '状态: ENABLED, DISABLED',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    INDEX idx_priority (priority),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='汇率数据源配置表';

-- 汇率主表
CREATE TABLE IF NOT EXISTS exchange_rate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    currency_pair VARCHAR(20) NOT NULL COMMENT '货币对，如USD/CNY',
    base_currency VARCHAR(10) NOT NULL COMMENT '基础货币',
    quote_currency VARCHAR(10) NOT NULL COMMENT '报价货币',
    rate DECIMAL(18,6) NOT NULL COMMENT '汇率值',
    source_code VARCHAR(20) NOT NULL COMMENT '数据源编码',
    effective_date DATE NOT NULL COMMENT '生效日期',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, INACTIVE',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    UNIQUE KEY uk_currency_pair_effective_date (currency_pair, effective_date),
    INDEX idx_currency_pair (currency_pair),
    INDEX idx_base_currency (base_currency),
    INDEX idx_effective_date (effective_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='汇率主表';

-- 汇率历史表
CREATE TABLE IF NOT EXISTS exchange_rate_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    currency_pair VARCHAR(20) NOT NULL COMMENT '货币对',
    rate DECIMAL(18,6) NOT NULL COMMENT '汇率值',
    source_code VARCHAR(20) NOT NULL COMMENT '数据源编码',
    effective_date DATE NOT NULL COMMENT '生效日期',
    validation_result TEXT COMMENT '验证结果(JSON)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_currency_pair (currency_pair),
    INDEX idx_effective_date (effective_date),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='汇率历史表';

-- =====================================================
-- 业务数据重算相关表
-- =====================================================

-- 重算任务表
CREATE TABLE IF NOT EXISTS recalculate_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    task_id VARCHAR(50) NOT NULL UNIQUE COMMENT '任务ID',
    task_type VARCHAR(30) NOT NULL COMMENT '任务类型: EXCHANGE_RATE, BUSINESS_TYPE, CUSTOMER, TIME_RANGE, BATCH',
    trigger_source VARCHAR(30) NOT NULL COMMENT '触发来源: EXCHANGE_RATE_UPDATE, MANUAL, SCHEDULED, API',
    priority VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '优先级: CRITICAL, HIGH, MEDIUM, LOW',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, RUNNING, COMPLETED, FAILED, CANCELLED',
    currency_pair VARCHAR(20) COMMENT '货币对(汇率重算)',
    business_type VARCHAR(50) COMMENT '业务类型',
    customer_id BIGINT COMMENT '客户ID',
    group_id BIGINT COMMENT '集团ID',
    time_range_start DATETIME COMMENT '时间范围开始',
    time_range_end DATETIME COMMENT '时间范围结束',
    total_records INT DEFAULT 0 COMMENT '总记录数',
    processed_records INT DEFAULT 0 COMMENT '已处理记录数',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    fail_count INT DEFAULT 0 COMMENT '失败数量',
    progress DECIMAL(5,2) DEFAULT 0.00 COMMENT '进度百分比',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    error_message TEXT COMMENT '错误信息',
    operator VARCHAR(50) COMMENT '操作人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_type (task_type),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='重算任务表';

-- 重算差异记录表
CREATE TABLE IF NOT EXISTS recalculate_diff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    task_id VARCHAR(50) NOT NULL COMMENT '任务ID',
    quota_id BIGINT NOT NULL COMMENT '额度ID',
    quota_type VARCHAR(20) NOT NULL COMMENT '额度类型: GROUP, CUSTOMER, APPROVAL, CONTRACT',
    field_name VARCHAR(50) NOT NULL COMMENT '字段名称',
    before_value DECIMAL(20,4) COMMENT '变更前值',
    after_value DECIMAL(20,4) COMMENT '变更后值',
    difference DECIMAL(20,4) COMMENT '差异值',
    currency VARCHAR(10) COMMENT '货币',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_id (task_id),
    INDEX idx_quota_id (quota_id),
    INDEX idx_quota_type (quota_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='重算差异记录表';

-- =====================================================
-- 晚间业务同步相关表
-- =====================================================

-- 同步任务表
CREATE TABLE IF NOT EXISTS sync_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    sync_task_id VARCHAR(50) NOT NULL UNIQUE COMMENT '同步任务ID',
    sync_type VARCHAR(30) NOT NULL COMMENT '同步类型: DAILY_BATCH, REALTIME, MANUAL',
    data_source VARCHAR(50) NOT NULL COMMENT '数据源: CORE_BUSINESS, CREDIT_SYSTEM',
    data_types VARCHAR(200) COMMENT '数据类型列表(JSON)',
    sync_mode VARCHAR(20) DEFAULT 'INCREMENTAL' COMMENT '同步模式: FULL, INCREMENTAL',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, RUNNING, COMPLETED, FAILED',
    total_records INT DEFAULT 0 COMMENT '总记录数',
    processed_records INT DEFAULT 0 COMMENT '已处理记录数',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    fail_count INT DEFAULT 0 COMMENT '失败数量',
    progress DECIMAL(5,2) DEFAULT 0.00 COMMENT '进度百分比',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    error_message TEXT COMMENT '错误信息',
    operator VARCHAR(50) COMMENT '操作人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sync_type (sync_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步任务表';

-- 同步检查点表
CREATE TABLE IF NOT EXISTS sync_checkpoint (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    sync_task_id VARCHAR(50) NOT NULL COMMENT '同步任务ID',
    data_type VARCHAR(50) NOT NULL COMMENT '数据类型',
    last_processed_id BIGINT COMMENT '最后处理ID',
    last_processed_time DATETIME COMMENT '最后处理时间',
    processed_count INT DEFAULT 0 COMMENT '已处理数量',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, COMPLETED',
    checkpoint_time DATETIME NOT NULL COMMENT '检查点时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_sync_task_id (sync_task_id),
    INDEX idx_data_type (data_type),
    INDEX idx_checkpoint_time (checkpoint_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步检查点表';

-- 同步错误记录表
CREATE TABLE IF NOT EXISTS sync_error_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    sync_task_id VARCHAR(50) NOT NULL COMMENT '同步任务ID',
    data_type VARCHAR(50) NOT NULL COMMENT '数据类型',
    record_id VARCHAR(100) COMMENT '记录ID',
    error_code VARCHAR(20) COMMENT '错误码',
    error_message TEXT COMMENT '错误信息',
    raw_data TEXT COMMENT '原始数据',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, RETRY, SUCCESS, FAILED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sync_task_id (sync_task_id),
    INDEX idx_data_type (data_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步错误记录表';

-- =====================================================
-- 授信额度同步相关表
-- =====================================================

-- 额度版本控制表
CREATE TABLE IF NOT EXISTS quota_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    quota_id BIGINT NOT NULL COMMENT '额度ID',
    quota_type VARCHAR(20) NOT NULL COMMENT '额度类型: GROUP, CUSTOMER, APPROVAL, CONTRACT',
    version_no INT NOT NULL COMMENT '版本号',
    snapshot_data JSON NOT NULL COMMENT '快照数据',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型: CREATE, UPDATE, FREEZE, UNFREEZE, TERMINATE',
    change_reason VARCHAR(500) COMMENT '变更原因',
    source_system VARCHAR(50) COMMENT '来源系统',
    source_tx_id VARCHAR(100) COMMENT '来源事务ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_quota_id (quota_id),
    INDEX idx_version_no (quota_id, version_no),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度版本控制表';

-- 系统接入白名单表
CREATE TABLE IF NOT EXISTS system_whitelist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    system_code VARCHAR(50) NOT NULL UNIQUE COMMENT '系统编码',
    system_name VARCHAR(100) NOT NULL COMMENT '系统名称',
    api_key VARCHAR(100) NOT NULL COMMENT 'API密钥(加密存储)',
    allowed_ips VARCHAR(500) COMMENT '允许的IP列表',
    allowed_apis VARCHAR(1000) COMMENT '允许的API列表',
    rate_limit INT DEFAULT 1000 COMMENT '限流阈值(次/分钟)',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT '状态: ENABLED, DISABLED',
    effective_date DATETIME COMMENT '生效日期',
    expiry_date DATETIME COMMENT '失效日期',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统接入白名单表';

-- 额度同步记录表
CREATE TABLE IF NOT EXISTS quota_sync_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    sync_id VARCHAR(50) NOT NULL UNIQUE COMMENT '同步ID',
    quota_type VARCHAR(20) NOT NULL COMMENT '额度类型: GROUP, CUSTOMER, APPROVAL, CONTRACT',
    operation VARCHAR(20) NOT NULL COMMENT '操作类型: CREATE, UPDATE, DELETE',
    source_system VARCHAR(50) NOT NULL COMMENT '来源系统',
    source_tx_id VARCHAR(100) COMMENT '来源事务ID',
    request_id VARCHAR(100) COMMENT '请求ID',
    quota_id BIGINT COMMENT '额度ID',
    version_no INT COMMENT '版本号',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, SUCCESS, FAILED',
    error_message TEXT COMMENT '错误信息',
    process_time DATETIME COMMENT '处理时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_quota_type (quota_type),
    INDEX idx_source_system (source_system),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度同步记录表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入汇率数据源配置
INSERT INTO exchange_rate_source (source_code, source_name, api_url, auth_type, priority, status, description, create_by) VALUES
('PBOC', '中国人民银行', 'https://api.pbc.gov.cn/rates', 'API_KEY', 1, 'ENABLED', '央行官方汇率数据源', 'admin'),
('REUTERS', '路透社', 'https://api.reuters.com/rates', 'OAUTH2', 2, 'ENABLED', '路透社实时汇率数据源', 'admin'),
('BLOOMBERG', '彭博社', 'https://api.bloomberg.com/rates', 'CERT', 3, 'ENABLED', '彭博社实时汇率数据源', 'admin'),
('CFETS', '外汇交易中心', 'https://api.cfets.cn/rates', 'API_KEY', 4, 'ENABLED', '外汇交易中心汇率数据源', 'admin');

-- 插入初始汇率数据
INSERT INTO exchange_rate (currency_pair, base_currency, quote_currency, rate, source_code, effective_date, status, create_by) VALUES
('USD/CNY', 'USD', 'CNY', 7.245600, 'PBOC', CURDATE(), 'ACTIVE', 'admin'),
('EUR/CNY', 'EUR', 'CNY', 7.823400, 'PBOC', CURDATE(), 'ACTIVE', 'admin'),
('GBP/CNY', 'GBP', 'CNY', 9.156700, 'PBOC', CURDATE(), 'ACTIVE', 'admin'),
('JPY/CNY', 'JPY', 'CNY', 0.048500, 'PBOC', CURDATE(), 'ACTIVE', 'admin'),
('HKD/CNY', 'HKD', 'CNY', 0.928700, 'PBOC', CURDATE(), 'ACTIVE', 'admin');

-- 插入系统接入白名单
INSERT INTO system_whitelist (system_code, system_name, api_key, allowed_ips, allowed_apis, rate_limit, status, description, create_by) VALUES
('CREDIT_SYSTEM', '授信系统', 'credit_api_key_xxx', '10.0.1.0/24', '/api/v1/sync/*', 2000, 'ENABLED', '核心授信系统接入', 'admin'),
('CORE_BUSINESS', '核心业务系统', 'core_api_key_xxx', '10.0.2.0/24', '/api/v1/sync/*', 3000, 'ENABLED', '核心业务系统接入', 'admin'),
('RISK_SYSTEM', '风险管理系统', 'risk_api_key_xxx', '10.0.3.0/24', '/api/v1/quota/*', 1000, 'ENABLED', '风险管理系统接入', 'admin');
