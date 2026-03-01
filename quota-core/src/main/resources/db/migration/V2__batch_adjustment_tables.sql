-- 额度批量调整表
CREATE TABLE quota_batch_adjustment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    batch_no VARCHAR(32) NOT NULL UNIQUE COMMENT '批量号',
    batch_name VARCHAR(100) NOT NULL COMMENT '批量调整名称',
    adjustment_type VARCHAR(20) NOT NULL COMMENT '调整类型(INCREASE,DECREASE)',
    total_amount DECIMAL(24,6) COMMENT '总金额',
    item_count INT DEFAULT 0 COMMENT '项目数量',
    processed_count INT DEFAULT 0 COMMENT '已处理数量',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    failed_count INT DEFAULT 0 COMMENT '失败数量',
    status VARCHAR(20) NOT NULL COMMENT '状态(PENDING,UNDER_REVIEW,APPROVED,REJECTED,CANCELLED,COMPLETED,PARTIAL_SUCCESS,FAILED)',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    executor_id VARCHAR(50) COMMENT '执行者ID',
    executor_name VARCHAR(100) COMMENT '执行者姓名',
    description VARCHAR(500) COMMENT '描述',
    error_log TEXT COMMENT '错误日志',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_batch_no (batch_no),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度批量调整表';

-- 额度批量调整明细表
CREATE TABLE quota_batch_adjustment_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    batch_id BIGINT NOT NULL COMMENT '批量ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户姓名',
    quota_type VARCHAR(20) COMMENT '额度类型(CUSTOMER,GROUP,ITEM)',
    adjustment_type VARCHAR(20) NOT NULL COMMENT '调整类型(INCREASE,DECREASE)',
    adjustment_amount DECIMAL(24,6) COMMENT '调整金额',
    before_amount DECIMAL(24,6) COMMENT '调整前金额',
    after_amount DECIMAL(24,6) COMMENT '调整后金额',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币类型',
    status VARCHAR(20) NOT NULL COMMENT '状态(PENDING,UNDER_REVIEW,APPROVED,REJECTED,CANCELLED,COMPLETED)',
    error_message VARCHAR(500) COMMENT '错误信息',
    process_time DATETIME COMMENT '处理时间',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_batch_id (batch_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='额度批量调整明细表';