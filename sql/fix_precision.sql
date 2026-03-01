-- 数据库精度修复脚本
-- 将所有金额字段统一修改为DECIMAL(24,6)以满足银行业务精度要求

USE quota_system;

-- ========================================
-- 1. 修复item_quota表精度
-- ========================================
ALTER TABLE item_quota 
    MODIFY COLUMN total_quota DECIMAL(24,6) NOT NULL COMMENT '细项总额度',
    MODIFY COLUMN used_quota DECIMAL(24,6) NOT NULL DEFAULT 0.000000 COMMENT '细项已用额度',
    MODIFY COLUMN available_quota DECIMAL(24,6) NOT NULL COMMENT '细项可用额度';

-- ========================================
-- 2. 修复approval_quota表精度
-- ========================================
ALTER TABLE approval_quota 
    MODIFY COLUMN approval_quota DECIMAL(24,6) NOT NULL COMMENT '批复额度',
    MODIFY COLUMN used_quota DECIMAL(24,6) NOT NULL DEFAULT 0.000000 COMMENT '已用额度',
    MODIFY COLUMN available_quota DECIMAL(24,6) NOT NULL COMMENT '可用额度';

-- ========================================
-- 3. 修复quota_lock表精度
-- ========================================
ALTER TABLE quota_lock 
    MODIFY COLUMN lock_amount DECIMAL(24,6) NOT NULL COMMENT '锁定金额';

-- ========================================
-- 4. 修复contract_occupancy表精度
-- ========================================
ALTER TABLE contract_occupancy 
    MODIFY COLUMN occupancy_amount DECIMAL(24,6) NOT NULL COMMENT '占用金额';

-- ========================================
-- 5. 修复group_quota表锁定额度精度
-- ========================================
ALTER TABLE group_quota 
    MODIFY COLUMN locked_quota DECIMAL(24,6) NOT NULL DEFAULT 0.000000 COMMENT '锁定额度';

-- ========================================
-- 6. 修复customer_quota表锁定额度精度
-- ========================================
ALTER TABLE customer_quota 
    MODIFY COLUMN locked_quota DECIMAL(24,6) NOT NULL DEFAULT 0.000000 COMMENT '锁定额度';

-- ========================================
-- 7. 修复group_quota表其他金额字段精度
-- ========================================
ALTER TABLE group_quota 
    MODIFY COLUMN total_quota DECIMAL(24,6) NOT NULL COMMENT '总额度',
    MODIFY COLUMN used_quota DECIMAL(24,6) NOT NULL DEFAULT 0.000000 COMMENT '已用额度',
    MODIFY COLUMN available_quota DECIMAL(24,6) NOT NULL COMMENT '可用额度',
    MODIFY COLUMN frozen_quota DECIMAL(24,6) DEFAULT 0.000000 COMMENT '冻结额度';

-- ========================================
-- 8. 修复customer_quota表其他金额字段精度
-- ========================================
ALTER TABLE customer_quota 
    MODIFY COLUMN total_quota DECIMAL(24,6) NOT NULL COMMENT '总额度',
    MODIFY COLUMN used_quota DECIMAL(24,6) NOT NULL DEFAULT 0.000000 COMMENT '已用额度',
    MODIFY COLUMN available_quota DECIMAL(24,6) NOT NULL COMMENT '可用额度';

-- ========================================
-- 9. 修复quota_usage_detail表金额字段精度
-- ========================================
ALTER TABLE quota_usage_detail 
    MODIFY COLUMN transaction_amount DECIMAL(24,6) NOT NULL COMMENT '交易金额',
    MODIFY COLUMN before_balance DECIMAL(24,6) NOT NULL COMMENT '交易前余额',
    MODIFY COLUMN after_balance DECIMAL(24,6) NOT NULL COMMENT '交易后余额',
    MODIFY COLUMN before_used DECIMAL(24,6) COMMENT '交易前已用额度',
    MODIFY COLUMN after_used DECIMAL(24,6) COMMENT '交易后已用额度',
    MODIFY COLUMN before_locked DECIMAL(24,6) COMMENT '交易前锁定额度',
    MODIFY COLUMN after_locked DECIMAL(24,6) COMMENT '交易后锁定额度';

-- ========================================
-- 10. 修复quota_adjustment表金额字段精度
-- ========================================
ALTER TABLE quota_adjustment 
    MODIFY COLUMN adjustment_amount DECIMAL(24,6) NOT NULL COMMENT '调整金额',
    MODIFY COLUMN before_amount DECIMAL(24,6) NOT NULL COMMENT '调整前金额',
    MODIFY COLUMN after_amount DECIMAL(24,6) NOT NULL COMMENT '调整后金额';

-- ========================================
-- 11. 修复quota_freeze表金额字段精度
-- ========================================
ALTER TABLE quota_freeze 
    MODIFY COLUMN freeze_amount DECIMAL(24,6) COMMENT '冻结金额',
    MODIFY COLUMN unfreeze_amount DECIMAL(24,6) COMMENT '解冻金额';

-- ========================================
-- 12. 修复quota_lifecycle_event表金额字段精度
-- ========================================
ALTER TABLE quota_lifecycle_event 
    MODIFY COLUMN amount DECIMAL(24,6) COMMENT '金额';

-- ========================================
-- 验证修复结果
-- ========================================
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    COLUMN_TYPE,
    NUMERIC_PRECISION,
    NUMERIC_SCALE
FROM 
    INFORMATION_SCHEMA.COLUMNS 
WHERE 
    TABLE_SCHEMA = 'quota_system' 
    AND DATA_TYPE = 'decimal'
    AND (COLUMN_NAME LIKE '%quota%' OR COLUMN_NAME LIKE '%amount%' OR COLUMN_NAME LIKE '%balance%')
ORDER BY 
    TABLE_NAME, COLUMN_NAME;
