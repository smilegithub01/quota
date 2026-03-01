# 银行级信贷额度管控平台API接口文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 文档名称 | API接口文档 |
| 文档版本 | V1.0 |
| 编制日期 | 2026年2月12日 |
| 编制人 | 系统架构师 |
| 项目编号 | QUOTA-2026-001 |

---

## 目录

- [1. 接口概述](#1-接口概述)
- [2. 通用说明](#2-通用说明)
- [3. 额度生命周期管控接口](#3-额度生命周期管控接口)
- [4. 集团额度管理接口](#4-集团额度管理接口)
- [5. 客户额度管理接口](#5-客户额度管理接口)
- [6. 批复额度管理接口](#6-批复额度管理接口)
- [7. 白名单管理接口](#7-白名单管理接口)
- [8. 额度控制接口](#8-额度控制接口)
- [9. 风险预警接口](#9-风险预警接口)
- [10. 审计日志接口](#10-审计日志接口)
- [11. 系统配置接口](#11-系统配置接口)
- [12. 客户管理接口](#12-客户管理接口)
- [13. 授信申请接口](#13-授信申请接口)
- [14. 用信申请接口](#14-用信申请接口)
- [15. 审批流程接口](#15-审批流程接口)
- [16. 额度使用明细接口](#16-额度使用明细接口)
- [17. 风险监控接口](#17-风险监控接口)

---

## 1. 接口概述

### 1.1 接口基础路径

```
http://{host}:{port}/api/v1
```

### 1.2 认证方式

所有接口需要在请求头中携带认证Token：

```
Authorization: Bearer {token}
```

### 1.3 请求格式

- Content-Type: application/json
- 字符编码: UTF-8

---

## 2. 通用说明

### 2.1 响应格式

所有接口统一返回以下格式：

```json
{
    "code": "000000",
    "message": "成功",
    "data": { ... }
}
```

### 2.2 错误码说明

| 错误码 | 说明 |
|--------|------|
| 000000 | 成功 |
| 100001 | 系统异常 |
| 100002 | 参数异常 |
| 100003 | 数据不存在 |
| 100004 | 数据已存在 |
| 200001 | 额度不存在 |
| 200002 | 额度不足 |
| 200003 | 额度已冻结 |
| 300001 | 客户不存在 |
| 400001 | 集团不存在 |
| 500001 | 批复不存在 |
| 800001 | 白名单不存在 |

---

## 3. 额度生命周期管控接口

### 3.1 创建额度

**接口地址**: `POST /quota/lifecycle/create`

**接口描述**: 根据授信批复结果创建额度记录，建立六级额度管控体系的初始数据

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| approvalNo | String | 是 | 授信批复编号 |
| customerId | Long | 是 | 客户ID |
| quotaType | String | 是 | 额度类型：GROUP/CUSTOMER/APPROVAL |
| quotaAmount | BigDecimal | 是 | 额度金额 |
| currency | String | 否 | 币种，默认CNY |
| effectiveTime | DateTime | 是 | 生效日期 |
| expiryTime | DateTime | 是 | 到期日期 |
| guaranteeInfo | String | 否 | 担保信息 |
| createBy | String | 是 | 创建人 |

**请求示例**:

```json
{
    "approvalNo": "AP202602130001",
    "customerId": 2001,
    "quotaType": "CUSTOMER",
    "quotaAmount": 50000000.00,
    "currency": "CNY",
    "effectiveTime": "2026-02-13T00:00:00",
    "expiryTime": "2027-02-12T23:59:59",
    "guaranteeInfo": "房产抵押",
    "createBy": "admin"
}
```

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "quotaId": 10001,
        "quotaNo": "QT20260213000001",
        "quotaType": "CUSTOMER",
        "status": "PENDING",
        "totalQuota": 50000000.00,
        "usedQuota": 0.00,
        "availableQuota": 50000000.00,
        "frozenQuota": 0.00,
        "effectiveTime": "2026-02-13T00:00:00",
        "expiryTime": "2027-02-12T23:59:59"
    }
}
```

### 3.2 调整额度

**接口地址**: `POST /quota/lifecycle/adjust`

**接口描述**: 调整额度金额，支持临时调整、永久调整、批量调整

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| quotaId | Long | 是 | 额度ID |
| adjustmentType | String | 是 | 调整类型：TEMPORARY/PERMANENT/BATCH |
| adjustmentAmount | BigDecimal | 是 | 调整金额（正数为增加，负数为减少） |
| reason | String | 是 | 调整原因 |
| expiryTime | DateTime | 否 | 有效期（临时调整必填） |
| applicant | String | 是 | 申请人 |

**请求示例**:

```json
{
    "quotaId": 10001,
    "adjustmentType": "PERMANENT",
    "adjustmentAmount": 10000000.00,
    "reason": "客户信用等级提升",
    "applicant": "admin"
}
```

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "adjustmentId": 20001,
        "quotaId": 10001,
        "adjustmentType": "PERMANENT",
        "adjustmentAmount": 10000000.00,
        "beforeAmount": 50000000.00,
        "afterAmount": 60000000.00,
        "status": "COMPLETED",
        "createTime": "2026-02-13T10:30:00"
    }
}
```

### 3.3 冻结额度

**接口地址**: `POST /quota/lifecycle/freeze`

**接口描述**: 冻结额度，支持全额冻结、部分冻结、条件冻结

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| quotaId | Long | 是 | 额度ID |
| freezeType | String | 是 | 冻结类型：FULL/PARTIAL/CONDITIONAL |
| freezeAmount | BigDecimal | 否 | 冻结金额（部分冻结必填） |
| reason | String | 是 | 冻结原因 |
| condition | String | 否 | 冻结条件（条件冻结必填） |
| operator | String | 是 | 操作人 |

**请求示例**:

```json
{
    "quotaId": 10001,
    "freezeType": "PARTIAL",
    "freezeAmount": 10000000.00,
    "reason": "客户风险等级下降",
    "operator": "admin"
}
```

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "freezeId": 30001,
        "quotaId": 10001,
        "freezeType": "PARTIAL",
        "freezeAmount": 10000000.00,
        "status": "FROZEN",
        "availableQuota": 40000000.00,
        "freezeTime": "2026-02-13T10:30:00"
    }
}
```

### 3.4 解冻额度

**接口地址**: `POST /quota/lifecycle/unfreeze`

**接口描述**: 解冻额度，恢复额度可用性

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| freezeId | Long | 是 | 冻结记录ID |
| unfreezeAmount | BigDecimal | 否 | 解冻金额（部分解冻时填写） |
| reason | String | 是 | 解冻原因 |
| operator | String | 是 | 操作人 |

**请求示例**:

```json
{
    "freezeId": 30001,
    "unfreezeAmount": 5000000.00,
    "reason": "风险已解除",
    "operator": "admin"
}
```

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "unfreezeId": 40001,
        "freezeId": 30001,
        "unfreezeAmount": 5000000.00,
        "status": "ACTIVE",
        "availableQuota": 45000000.00,
        "unfreezeTime": "2026-02-13T11:00:00"
    }
}
```

### 3.5 终止额度

**接口地址**: `POST /quota/lifecycle/terminate`

**接口描述**: 终止额度，支持到期终止、主动终止、强制终止

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| quotaId | Long | 是 | 额度ID |
| terminateType | String | 是 | 终止类型：EXPIRY/MANUAL/FORCE |
| reason | String | 是 | 终止原因 |
| operator | String | 是 | 操作人 |

**请求示例**:

```json
{
    "quotaId": 10001,
    "terminateType": "MANUAL",
    "reason": "客户申请终止授信",
    "operator": "admin"
}
```

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "terminateId": 50001,
        "quotaId": 10001,
        "terminateType": "MANUAL",
        "status": "TERMINATED",
        "terminateTime": "2026-02-13T10:30:00"
    }
}
```

### 3.6 查询额度生命周期

**接口地址**: `GET /quota/lifecycle/{quotaId}`

**接口描述**: 查询额度生命周期详情

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| quotaId | Long | 是 | 额度ID |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "quotaId": 10001,
        "quotaNo": "QT20260213000001",
        "quotaType": "CUSTOMER",
        "status": "ACTIVE",
        "totalQuota": 60000000.00,
        "usedQuota": 15000000.00,
        "availableQuota": 45000000.00,
        "frozenQuota": 0.00,
        "effectiveTime": "2026-02-13T00:00:00",
        "expiryTime": "2027-02-12T23:59:59",
        "createTime": "2026-02-13T09:00:00",
        "updateTime": "2026-02-13T10:30:00"
    }
}
```

### 3.7 查询额度生命周期事件

**接口地址**: `GET /quota/lifecycle/{quotaId}/events`

**接口描述**: 查询额度生命周期事件列表

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| quotaId | Long | 是 | 额度ID |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": [
        {
            "eventId": "EVT001",
            "quotaId": 10001,
            "eventType": "CREATE",
            "amount": 50000000.00,
            "reason": "授信批复创建",
            "operator": "admin",
            "eventTime": "2026-02-13T09:00:00"
        },
        {
            "eventId": "EVT002",
            "quotaId": 10001,
            "eventType": "ADJUST",
            "amount": 10000000.00,
            "reason": "客户信用等级提升",
            "operator": "admin",
            "eventTime": "2026-02-13T10:30:00"
        }
    ]
}
```

---

## 4. 集团额度管理接口

### 3.1 创建集团额度

**接口地址**: `POST /group-quota`

**接口描述**: 创建新的集团额度记录

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| groupId | Long | 是 | 集团ID |
| groupName | String | 是 | 集团名称 |
| totalQuota | BigDecimal | 是 | 总额度 |
| description | String | 否 | 描述 |
| createBy | String | 是 | 创建人 |

**请求示例**:

```json
{
    "groupId": 1001,
    "groupName": "测试集团",
    "totalQuota": 10000000.00,
    "description": "测试集团额度",
    "createBy": "admin"
}
```

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "id": 1,
        "groupId": 1001,
        "groupName": "测试集团",
        "totalQuota": 10000000.00,
        "usedQuota": 0.00,
        "availableQuota": 10000000.00,
        "status": "ENABLED",
        "createTime": "2026-02-12T10:30:00"
    }
}
```

### 3.2 查询集团额度

**接口地址**: `GET /group-quota/{id}`

**接口描述**: 根据ID查询集团额度详情

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 集团额度ID |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "id": 1,
        "groupId": 1001,
        "groupName": "测试集团",
        "totalQuota": 10000000.00,
        "usedQuota": 2000000.00,
        "availableQuota": 8000000.00,
        "usageRate": 0.20,
        "status": "ENABLED",
        "description": "测试集团额度",
        "createBy": "admin",
        "createTime": "2026-02-12T10:30:00"
    }
}
```

### 3.3 冻结集团额度

**接口地址**: `POST /group-quota/{id}/freeze`

**接口描述**: 冻结指定的集团额度

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 是 | 冻结原因 |
| operator | String | 是 | 操作人 |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "id": 1,
        "groupId": 1001,
        "status": "FROZEN"
    }
}
```

### 3.4 解冻集团额度

**接口地址**: `POST /group-quota/{id}/unfreeze`

**接口描述**: 解冻指定的集团额度

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| operator | String | 是 | 操作人 |

### 3.5 停用集团额度

**接口地址**: `POST /group-quota/{id}/disable`

**接口描述**: 停用指定的集团额度

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 是 | 停用原因 |
| operator | String | 是 | 操作人 |

### 3.6 调整集团额度

**接口地址**: `POST /group-quota/{id}/adjust`

**接口描述**: 调整集团额度金额

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| adjustmentAmount | BigDecimal | 是 | 调整金额（正数为增加，负数为减少） |
| reason | String | 是 | 调整原因 |
| operator | String | 是 | 操作人 |

### 3.7 查询集团额度使用量

**接口地址**: `GET /group-quota/{id}/usage`

**接口描述**: 查询集团额度使用情况

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "id": 1,
        "groupId": 1001,
        "totalQuota": 10000000.00,
        "usedQuota": 2000000.00,
        "availableQuota": 8000000.00,
        "usageRate": 20.00,
        "customerUsages": [
            {
                "customerId": 2001,
                "customerName": "客户A",
                "usedQuota": 1000000.00
            }
        ]
    }
}
```

---

## 4. 客户额度管理接口

### 4.1 创建客户额度

**接口地址**: `POST /customer-quota`

**接口描述**: 创建新的客户额度记录

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerId | Long | 是 | 客户ID |
| customerName | String | 是 | 客户名称 |
| customerType | String | 是 | 客户类型：INDIVIDUAL-个人, CORPORATE-企业 |
| groupId | Long | 否 | 所属集团ID |
| totalQuota | BigDecimal | 是 | 总额度 |
| description | String | 否 | 描述 |
| createBy | String | 是 | 创建人 |

**请求示例**:

```json
{
    "customerId": 2001,
    "customerName": "测试客户",
    "customerType": "CORPORATE",
    "groupId": 1001,
    "totalQuota": 500000.00,
    "description": "企业客户额度",
    "createBy": "admin"
}
```

### 4.2 查询客户额度

**接口地址**: `GET /customer-quota/{customerId}`

**接口描述**: 根据客户ID查询客户额度详情

### 4.3 冻结客户额度

**接口地址**: `POST /customer-quota/{customerId}/freeze`

**接口描述**: 冻结指定的客户额度

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 是 | 冻结原因 |
| operator | String | 是 | 操作人 |

### 4.4 解冻客户额度

**接口地址**: `POST /customer-quota/{customerId}/unfreeze`

**接口描述**: 解冻指定的客户额度

### 4.5 停用客户额度

**接口地址**: `POST /customer-quota/{customerId}/disable`

**接口描述**: 停用指定的客户额度

### 4.6 调整客户额度

**接口地址**: `POST /customer-quota/{customerId}/adjust`

**接口描述**: 调整客户额度金额

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| adjustmentAmount | BigDecimal | 是 | 调整金额 |
| reason | String | 是 | 调整原因 |
| operator | String | 是 | 操作人 |

### 4.7 额度转移

**接口地址**: `POST /customer-quota/transfer`

**接口描述**: 在客户之间转移额度

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fromCustomerId | Long | 是 | 源客户ID |
| toCustomerId | Long | 是 | 目标客户ID |
| amount | BigDecimal | 是 | 转移金额 |
| reason | String | 是 | 转移原因 |
| operator | String | 是 | 操作人 |

### 4.8 查询集团下客户额度

**接口地址**: `GET /customer-quota/group/{groupId}`

**接口描述**: 查询指定集团下所有客户额度

### 4.9 按类型查询客户额度

**接口地址**: `GET /customer-quota/type/{customerType}`

**接口描述**: 根据客户类型查询客户额度

---

## 5. 批复额度管理接口

### 5.1 创建批复额度

**接口地址**: `POST /approval-quota`

**接口描述**: 创建新的批复额度记录

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerQuotaSubId | Long | 是 | 客户细项额度ID |
| approvalNo | String | 是 | 批复编号 |
| approvalType | String | 是 | 批复类型 |
| approvalQuota | BigDecimal | 是 | 批复额度 |
| description | String | 否 | 描述 |
| createBy | String | 是 | 创建人 |

### 5.2 查询批复额度

**接口地址**: `GET /approval-quota/{approvalId}`

**接口描述**: 根据ID查询批复额度详情

### 5.3 根据批复编号查询

**接口地址**: `GET /approval-quota/no/{approvalNo}`

**接口描述**: 根据批复编号查询批复额度

### 5.4 冻结批复额度

**接口地址**: `POST /approval-quota/{approvalId}/freeze`

**接口描述**: 冻结指定的批复额度

### 5.5 解冻批复额度

**接口地址**: `POST /approval-quota/{approvalId}/unfreeze`

**接口描述**: 解冻指定的批复额度

### 5.6 撤回批复额度

**接口地址**: `POST /approval-quota/{approvalId}/withdraw`

**接口描述**: 撤回已使用的批复额度

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| amount | BigDecimal | 是 | 撤回金额 |
| reason | String | 是 | 撤回原因 |
| operator | String | 是 | 操作人 |

---

## 6. 白名单管理接口

### 6.1 添加白名单

**接口地址**: `POST /whitelist`

**接口描述**: 将客户添加到白名单，创建后需审批才能生效

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerId | Long | 是 | 客户ID |
| customerName | String | 是 | 客户名称 |
| whitelistType | String | 是 | 白名单类型：VIP_CUSTOMER, BUSINESS_PARTNER |
| businessType | String | 否 | 业务类型 |
| description | String | 否 | 描述 |
| createBy | String | 是 | 创建人 |

**请求示例**:

```json
{
    "customerId": 1001,
    "customerName": "测试客户",
    "whitelistType": "VIP_CUSTOMER",
    "businessType": "LOAN",
    "description": "VIP客户白名单",
    "createBy": "admin"
}
```

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "id": 1,
        "whitelistNo": "WL20260212001",
        "whitelistType": "VIP_CUSTOMER",
        "customerId": 1001,
        "customerName": "测试客户",
        "status": "PENDING",
        "effectiveTime": "2026-02-12T10:30:00",
        "expiryTime": "2027-02-12T10:30:00"
    }
}
```

### 6.2 申请白名单（完整版）

**接口地址**: `POST /whitelist/apply`

**接口描述**: 提交白名单申请，支持配置豁免规则、有效期等详细信息

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| whitelistType | String | 是 | 白名单类型：VIP_CUSTOMER, BUSINESS_PARTNER |
| customerId | Long | 是 | 客户ID |
| customerName | String | 是 | 客户名称 |
| businessType | String | 否 | 业务类型 |
| exemptRule | String | 是 | 豁免规则：FULL-全额豁免, PARTIAL-部分豁免, THRESHOLD-阈值豁免 |
| exemptAmount | BigDecimal | 否 | 豁免金额（部分豁免或阈值豁免时必填） |
| effectiveTime | DateTime | 是 | 生效时间 |
| expiryTime | DateTime | 是 | 失效时间 |
| applyReason | String | 是 | 申请原因 |
| applicant | String | 是 | 申请人 |

**请求示例**:

```json
{
    "whitelistType": "VIP_CUSTOMER",
    "customerId": 1001,
    "customerName": "测试客户",
    "businessType": "LOAN",
    "exemptRule": "FULL",
    "effectiveTime": "2026-02-12T00:00:00",
    "expiryTime": "2027-02-12T00:00:00",
    "applyReason": "VIP客户额度豁免",
    "applicant": "admin"
}
```

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "whitelistNo": "WL20260212001",
        "status": "PENDING",
        "message": "白名单申请已提交，等待审批"
    }
}
```

### 6.3 审批白名单

**接口地址**: `POST /whitelist/approve`

**接口描述**: 审批已提交的白名单申请

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| whitelistNo | String | 是 | 白名单编号 |
| approved | Boolean | 是 | 是否通过 |
| approveRemark | String | 否 | 审批备注 |
| approver | String | 是 | 审批人 |

### 6.4 查询白名单

**接口地址**: `GET /whitelist/{id}`

**接口描述**: 根据ID查询白名单详情

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 白名单ID |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "id": 1,
        "whitelistNo": "WL20260212001",
        "whitelistType": "VIP_CUSTOMER",
        "customerId": 1001,
        "customerName": "测试客户",
        "businessType": "LOAN",
        "exemptRule": "FULL",
        "exemptAmount": null,
        "effectiveTime": "2026-02-12T00:00:00",
        "expiryTime": "2027-02-12T00:00:00",
        "status": "ACTIVE",
        "applyReason": "VIP客户额度豁免",
        "applicant": "admin",
        "approver": "manager",
        "applyTime": "2026-02-12T10:00:00",
        "approveTime": "2026-02-12T11:00:00"
    }
}
```

### 6.5 根据白名单编号查询

**接口地址**: `GET /whitelist/no/{whitelistNo}`

**接口描述**: 根据白名单编号查询详情

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| whitelistNo | String | 是 | 白名单编号 |

### 6.6 检查客户是否在白名单

**接口地址**: `GET /whitelist/check/{customerId}`

**接口描述**: 检查客户是否在白名单中，返回白名单状态和类型

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerId | Long | 是 | 客户ID |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "customerId": 1001,
        "inWhitelist": true,
        "whitelistType": "VIP_CUSTOMER",
        "whitelistNo": "WL20260212001",
        "exemptRule": "FULL",
        "exemptAmount": null,
        "effectiveTime": "2026-02-12T00:00:00",
        "expiryTime": "2027-02-12T00:00:00"
    }
}
```

### 6.7 根据客户ID查询白名单

**接口地址**: `GET /whitelist/customer/{customerId}`

**接口描述**: 根据客户ID查询该客户的所有白名单记录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerId | Long | 是 | 客户ID |

### 6.8 查询客户有效白名单

**接口地址**: `GET /whitelist/customer/{customerId}/active`

**接口描述**: 查询指定客户当前有效的白名单列表

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerId | Long | 是 | 客户ID |

### 6.9 按状态查询白名单

**接口地址**: `GET /whitelist/status/{status}`

**接口描述**: 根据状态查询白名单列表

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | 状态：PENDING-待审批, ACTIVE-生效, INVALID-失效 |

### 6.10 删除白名单

**接口地址**: `DELETE /whitelist/{id}`

**接口描述**: 从白名单中移除客户（逻辑删除，保留审计记录）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 白名单ID |

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| operator | String | 是 | 操作人 |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": null
}
```

### 6.11 批量导入白名单

**接口地址**: `POST /whitelist/batch-import`

**接口描述**: 批量导入白名单记录，导入后需审批才能生效

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| whitelists | List | 是 | 白名单列表 |
| createBy | String | 是 | 创建人 |

**请求示例**:

```json
{
    "whitelists": [
        {
            "customerId": 1001,
            "customerName": "客户A",
            "whitelistType": "VIP_CUSTOMER",
            "businessType": "LOAN",
            "description": "VIP客户"
        },
        {
            "customerId": 1002,
            "customerName": "客户B",
            "whitelistType": "BUSINESS_PARTNER",
            "businessType": "TRADE",
            "description": "合作伙伴"
        }
    ],
    "createBy": "admin"
}
```

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "totalCount": 2,
        "successCount": 2,
        "failCount": 0,
        "errorMessages": []
    }
}
```

### 6.12 查询所有有效白名单

**接口地址**: `GET /whitelist/active`

**接口描述**: 查询当前所有有效的白名单

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": [
        {
            "id": 1,
            "whitelistNo": "WL20260212001",
            "whitelistType": "VIP_CUSTOMER",
            "customerId": 1001,
            "customerName": "测试客户",
            "status": "ACTIVE"
        }
    ]
}
```

---

## 7. 额度控制接口

### 7.1 锁定额度

**接口地址**: `POST /quota-control/lock`

**接口描述**: 锁定指定金额的额度

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| objectType | String | 是 | 对象类型：GROUP, CUSTOMER, APPROVAL |
| objectId | Long | 是 | 对象ID |
| lockAmount | BigDecimal | 是 | 锁定金额 |
| lockReason | String | 是 | 锁定原因 |
| expirySeconds | Integer | 否 | 过期时间（秒） |
| operator | String | 是 | 操作人 |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "lockNo": "LK20260212001",
        "status": "ACTIVE",
        "lockedAmount": 100000.00
    }
}
```

### 7.2 占用额度

**接口地址**: `POST /quota-control/occupy`

**接口描述**: 占用指定金额的额度

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| contractNo | String | 是 | 合同编号 |
| approvalQuotaSubId | Long | 是 | 批复细项ID |
| occupyAmount | BigDecimal | 是 | 占用金额 |
| description | String | 否 | 描述 |
| operator | String | 是 | 操作人 |

### 7.3 释放额度

**接口地址**: `POST /quota-control/release`

**接口描述**: 释放锁定的额度

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| lockNo | String | 是 | 锁定编号 |
| operator | String | 是 | 操作人 |

### 7.4 校验额度充足性

**接口地址**: `POST /quota-control/validate`

**接口描述**: 校验额度是否充足

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerId | Long | 是 | 客户ID |
| amount | BigDecimal | 是 | 校验金额 |
| businessType | String | 是 | 业务类型 |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "sufficient": true,
        "availableAmount": 500000.00,
        "shortfallAmount": 0.00
    }
}
```

---

## 8. 风险预警接口

### 8.1 创建预警规则

**接口地址**: `POST /risk-warning/rules`

**接口描述**: 创建风险预警规则

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleCode | String | 是 | 规则编码 |
| ruleName | String | 是 | 规则名称 |
| ruleType | String | 是 | 规则类型：USAGE_RATE, BALANCE_LOW |
| warningLevel | String | 是 | 预警级别：LOW, MEDIUM, HIGH, CRITICAL |
| thresholdType | String | 是 | 阈值类型：GREATER_THAN, LESS_THAN, EQUAL |
| thresholdValue | BigDecimal | 是 | 阈值 |
| objectType | String | 是 | 对象类型：GROUP, CUSTOMER, APPROVAL |
| notifyChannels | String | 否 | 通知渠道：EMAIL,SMS,WEBHOOK |
| notifyRecipients | String | 否 | 通知接收者 |
| description | String | 否 | 描述 |
| createBy | String | 是 | 创建人 |

### 8.2 查询预警规则

**接口地址**: `GET /risk-warning/rules/{ruleId}`

**接口描述**: 根据ID查询预警规则

### 8.3 启用预警规则

**接口地址**: `POST /risk-warning/rules/{ruleId}/enable`

**接口描述**: 启用预警规则

### 8.4 停用预警规则

**接口地址**: `POST /risk-warning/rules/{ruleId}/disable`

**接口描述**: 停用预警规则

### 8.5 手动触发预警检查

**接口地址**: `POST /risk-warning/check`

**接口描述**: 手动触发预警检查

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| objectType | String | 是 | 对象类型 |
| objectId | Long | 是 | 对象ID |

---

## 9. 审计日志接口

### 9.1 分页查询审计日志

**接口地址**: `GET /audit/logs`

**接口描述**: 分页查询审计日志

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| operationType | String | 否 | 操作类型 |
| objectType | String | 否 | 对象类型 |
| objectId | String | 否 | 对象ID |
| operator | String | 否 | 操作人 |
| status | String | 否 | 操作状态 |
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认20 |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "total": 100,
        "pageNum": 1,
        "pageSize": 20,
        "pages": 5,
        "list": [
            {
                "id": 1,
                "operationType": "QUOTA_CREATE",
                "objectType": "GROUP_QUOTA",
                "objectId": "1001",
                "operationDesc": "创建集团额度",
                "operator": "admin",
                "status": "SUCCESS",
                "createTime": "2026-02-12T10:30:00"
            }
        ]
    }
}
```

### 9.2 查询对象操作日志

**接口地址**: `GET /audit/logs/object/{objectId}`

**接口描述**: 查询指定对象的所有操作日志

---

## 10. 系统配置接口

### 10.1 创建系统配置

**接口地址**: `POST /system/configs`

**接口描述**: 创建系统配置项

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| configKey | String | 是 | 配置Key |
| configValue | String | 是 | 配置Value |
| configName | String | 是 | 配置名称 |
| category | String | 是 | 配置分类 |
| description | String | 否 | 描述 |
| createBy | String | 是 | 创建人 |

### 10.2 查询系统配置

**接口地址**: `GET /system/configs/{id}`

**接口描述**: 根据ID查询系统配置

### 10.3 根据Key查询配置

**接口地址**: `GET /system/configs/key/{configKey}`

**接口描述**: 根据配置Key查询系统配置

### 10.4 按分类查询配置

**接口地址**: `GET /system/configs/category/{category}`

**接口描述**: 根据分类查询系统配置列表

### 10.5 更新系统配置

**接口地址**: `PUT /system/configs/{id}`

**接口描述**: 更新系统配置项

### 10.6 删除系统配置

**接口地址**: `DELETE /system/configs/{id}`

**接口描述**: 删除系统配置项

---

## 附录A：枚举值说明

### A.1 额度状态 (QuotaStatus)

| 值 | 说明 |
|----|------|
| ENABLED | 有效 |
| FROZEN | 冻结 |
| DISABLED | 停用 |

### A.2 客户类型 (CustomerType)

| 值 | 说明 |
|----|------|
| INDIVIDUAL | 个人客户 |
| CORPORATE | 企业客户 |

### A.3 白名单类型 (WhitelistType)

| 值 | 说明 |
|----|------|
| VIP_CUSTOMER | VIP客户 |
| BUSINESS_PARTNER | 合作伙伴 |

### A.4 预警级别 (WarningLevel)

| 值 | 说明 |
|----|------|
| LOW | 低 |
| MEDIUM | 中 |
| HIGH | 高 |
| CRITICAL | 严重 |

### A.5 操作类型 (OperationType)

| 值 | 说明 |
|----|------|
| QUOTA_CREATE | 创建额度 |
| QUOTA_UPDATE | 更新额度 |
| QUOTA_FREEZE | 冻结额度 |
| QUOTA_UNFREEZE | 解冻额度 |
| QUOTA_DISABLE | 停用额度 |
| QUOTA_LOCK | 锁定额度 |
| QUOTA_OCCUPY | 占用额度 |
| QUOTA_RELEASE | 释放额度 |
| SYSTEM_CONFIG_UPDATE | 系统配置更新 |

---

## 附录B：错误码完整列表

| 错误码 | 错误消息 | 说明 |
|--------|----------|------|
| 000000 | 成功 | 操作成功 |
| 100001 | 系统异常 | 未预期的系统错误 |
| 100002 | 参数异常 | 请求参数校验失败 |
| 100003 | 数据不存在 | 查询数据未找到 |
| 100004 | 数据已存在 | 数据重复 |
| 200001 | 额度不存在 | 额度记录未找到 |
| 200002 | 额度不足 | 可用额度不足 |
| 200003 | 额度已冻结 | 额度处于冻结状态 |
| 200004 | 额度已过期 | 额度已超过有效期 |
| 300001 | 客户不存在 | 客户记录未找到 |
| 300002 | 客户类型错误 | 客户类型不匹配 |
| 400001 | 集团不存在 | 集团记录未找到 |
| 500001 | 批复不存在 | 批复记录未找到 |
| 500002 | 批复已过期 | 批复已超过有效期 |
| 600001 | 合同不存在 | 合同记录未找到 |
| 600002 | 合同占用异常 | 合同占用失败 |
| 700001 | 映射关系不存在 | 业务映射未配置 |
| 700002 | 映射关系错误 | 映射配置错误 |
| 800001 | 白名单不存在 | 白名单记录未找到 |
| 800002 | 白名单已过期 | 白名单已过期 |
| 800003 | 白名单无效 | 白名单状态无效 |
| 900001 | 获取锁失败 | 分布式锁获取失败 |
| 900002 | 事务执行失败 | 分布式事务失败 |
| A00001 | 认证失败 | 身份认证失败 |
| A00002 | 权限不足 | 无操作权限 |
| B00001 | 授信申请不存在 | 授信申请记录未找到 |
| B00002 | 用信申请不存在 | 用信申请记录未找到 |
| B00003 | 审批流程不存在 | 审批流程记录未找到 |
| B00004 | 审批节点不存在 | 审批节点记录未找到 |
| C00001 | 风险指标异常 | 风险指标计算异常 |

---

## 11. 客户管理接口

### 11.1 创建客户

**接口地址**: `POST /customers`

**接口描述**: 创建新的客户信息

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerNo | String | 是 | 客户编号，唯一标识 |
| customerName | String | 是 | 客户名称 |
| customerType | String | 是 | 客户类型: CORPORATE-对公客户, INDIVIDUAL-个人客户, INTERBANK-同业客户 |
| category | String | 是 | 客户分类: GROUP_CUSTOMER-集团客户, SINGLE_CUSTOMER-单一客户 |
| groupId | Long | 否 | 所属集团ID（集团客户必填） |
| idType | String | 否 | 证件类型: USCC-统一社会信用代码, ID_CARD-身份证 |
| idNumber | String | 否 | 证件号码 |
| legalPerson | String | 否 | 法定代表人 |
| industryCode | String | 否 | 行业代码 |
| riskLevel | String | 否 | 风险等级: R1-R5 |
| contactPerson | String | 否 | 联系人 |
| contactPhone | String | 否 | 联系电话 |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "id": 1,
        "customerNo": "CUST202602120001",
        "customerName": "示例企业有限公司",
        "customerType": "CORPORATE",
        "category": "SINGLE_CUSTOMER",
        "status": "ACTIVE",
        "createTime": "2026-02-12T10:00:00"
    }
}
```

### 11.2 更新客户

**接口地址**: `PUT /customers/{id}`

**接口描述**: 更新客户信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 客户ID |

**请求参数**: 同创建客户接口

### 11.3 获取客户详情

**接口地址**: `GET /customers/{id}`

**接口描述**: 获取客户详细信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 客户ID |

### 11.4 查询客户列表

**接口地址**: `GET /customers`

**接口描述**: 分页查询客户列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerNo | String | 否 | 客户编号 |
| customerName | String | 否 | 客户名称（模糊查询） |
| customerType | String | 否 | 客户类型 |
| category | String | 否 | 客户分类 |
| groupId | Long | 否 | 集团ID |
| status | String | 否 | 客户状态 |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

### 11.5 冻结客户

**接口地址**: `PUT /customers/{id}/freeze`

**接口描述**: 冻结客户

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 是 | 冻结原因 |

### 11.6 解冻客户

**接口地址**: `PUT /customers/{id}/unfreeze`

**接口描述**: 解冻客户

### 11.7 获取统一客户视图

**接口地址**: `GET /customers/{id}/unified-view`

**接口描述**: 获取统一客户视图，整合展示客户基本信息、额度信息、风险信息等

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "customerInfo": {
            "customerId": 1,
            "customerNo": "CUST202602120001",
            "customerName": "示例企业有限公司",
            "customerType": "CORPORATE",
            "category": "SINGLE_CUSTOMER",
            "riskLevel": "R3",
            "status": "ACTIVE"
        },
        "quotaOverview": {
            "totalQuota": 10000000.00,
            "usedQuota": 3000000.00,
            "availableQuota": 7000000.00,
            "lockedQuota": 500000.00,
            "usageRate": 30.00
        },
        "riskInfo": {
            "riskLevel": "R3",
            "creditScore": 75,
            "warningCount": 2
        },
        "whitelistStatus": {
            "inWhitelist": true,
            "whitelistType": "SPECIAL_SUPPORT",
            "expireDate": "2026-12-31"
        }
    }
}
```

### 11.8 获取集团成员

**接口地址**: `GET /customers/{id}/members`

**接口描述**: 获取集团下的所有成员客户

---

## 12. 授信申请接口

### 12.1 创建授信申请

**接口地址**: `POST /credit-applications`

**接口描述**: 创建新的授信申请

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerId | Long | 是 | 客户ID |
| applicationType | String | 是 | 申请类型: NEW-新增, RENEWAL-续授信, ADJUSTMENT-调整 |
| applicationAmount | BigDecimal | 是 | 申请金额 |
| currency | String | 否 | 币种，默认CNY |
| applicationPurpose | String | 否 | 申请用途 |
| guaranteeMethod | String | 否 | 担保方式 |
| termMonths | Integer | 否 | 期限(月) |
| applicant | String | 否 | 申请人 |
| department | String | 否 | 申请部门 |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "id": 1,
        "applicationId": "CA202602120001",
        "customerId": 1,
        "applicationType": "NEW",
        "applicationAmount": 5000000.00,
        "status": "DRAFT",
        "createTime": "2026-02-12T10:00:00"
    }
}
```

### 12.2 更新授信申请

**接口地址**: `PUT /credit-applications/{applicationId}`

**接口描述**: 更新授信申请信息

### 12.3 获取授信申请详情

**接口地址**: `GET /credit-applications/{applicationId}`

**接口描述**: 获取授信申请详细信息

### 12.4 查询授信申请列表

**接口地址**: `GET /credit-applications`

**接口描述**: 分页查询授信申请列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerId | Long | 否 | 客户ID |
| applicationType | String | 否 | 申请类型 |
| status | String | 否 | 申请状态 |
| applicationDateStart | String | 否 | 申请日期开始 |
| applicationDateEnd | String | 否 | 申请日期结束 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

### 12.5 提交授信申请

**接口地址**: `POST /credit-applications/{applicationId}/submit`

**接口描述**: 提交授信申请，进入审批流程

### 12.6 审批通过

**接口地址**: `POST /credit-applications/{applicationId}/approve`

**接口描述**: 审批通过授信申请

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| approveOpinion | String | 否 | 审批意见 |
| approvedAmount | BigDecimal | 否 | 批准金额（可调整） |

### 12.7 审批拒绝

**接口地址**: `POST /credit-applications/{applicationId}/reject`

**接口描述**: 审批拒绝授信申请

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| rejectReason | String | 是 | 拒绝原因 |

### 12.8 取消授信申请

**接口地址**: `POST /credit-applications/{applicationId}/cancel`

**接口描述**: 取消授信申请

---

## 13. 用信申请接口

### 13.1 创建用信申请

**接口地址**: `POST /usage-applications`

**接口描述**: 创建新的用信申请

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerId | Long | 是 | 客户ID |
| quotaId | Long | 是 | 额度ID |
| productType | String | 是 | 产品类型 |
| usageAmount | BigDecimal | 是 | 用信金额 |
| currency | String | 否 | 币种，默认CNY |
| usageTerm | Integer | 否 | 用信期限(天) |
| interestRate | BigDecimal | 否 | 利率(%) |
| repaymentMethod | String | 否 | 还款方式 |
| fundUsage | String | 否 | 资金用途 |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "id": 1,
        "usageId": "UA202602120001",
        "customerId": 1,
        "quotaId": 1,
        "productType": "流动资金贷款",
        "usageAmount": 1000000.00,
        "status": "DRAFT",
        "createTime": "2026-02-12T10:00:00"
    }
}
```

### 13.2 更新用信申请

**接口地址**: `PUT /usage-applications/{usageId}`

**接口描述**: 更新用信申请信息

### 13.3 获取用信申请详情

**接口地址**: `GET /usage-applications/{usageId}`

**接口描述**: 获取用信申请详细信息

### 13.4 查询用信申请列表

**接口地址**: `GET /usage-applications`

**接口描述**: 分页查询用信申请列表

### 13.5 提交用信申请

**接口地址**: `POST /usage-applications/{usageId}/submit`

**接口描述**: 提交用信申请，进入审批流程

### 13.6 审批通过

**接口地址**: `POST /usage-applications/{usageId}/approve`

**接口描述**: 审批通过用信申请

### 13.7 执行放款

**接口地址**: `POST /usage-applications/{usageId}/execute`

**接口描述**: 执行放款操作，占用额度

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| contractNo | String | 是 | 合同编号 |
| loanNoteNo | String | 是 | 借据编号 |
| disbursementDate | String | 是 | 放款日期 |

### 13.8 取消用信申请

**接口地址**: `POST /usage-applications/{usageId}/cancel`

**接口描述**: 取消用信申请

---

## 14. 审批流程接口

### 14.1 发起审批流程

**接口地址**: `POST /approval-processes`

**接口描述**: 发起新的审批流程

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| businessType | String | 是 | 业务类型: CREDIT_APPLICATION, USAGE_APPLICATION, QUOTA_ADJUSTMENT |
| businessId | String | 是 | 业务ID |
| processDefinitionId | String | 是 | 流程定义ID |
| priority | String | 否 | 优先级: LOW, NORMAL, HIGH, URGENT |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "id": 1,
        "processId": "AP202602120001",
        "businessType": "CREDIT_APPLICATION",
        "businessId": "CA202602120001",
        "processStatus": "RUNNING",
        "currentNode": "部门审批",
        "currentAssignee": "user001"
    }
}
```

### 14.2 获取审批流程详情

**接口地址**: `GET /approval-processes/{processId}`

**接口描述**: 获取审批流程详细信息

### 14.3 获取流程节点列表

**接口地址**: `GET /approval-processes/{processId}/nodes`

**接口描述**: 获取审批流程的所有节点信息

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": [
        {
            "nodeName": "开始",
            "nodeType": "START",
            "nodeOrder": 1,
            "nodeStatus": "COMPLETED"
        },
        {
            "nodeName": "部门审批",
            "nodeType": "APPROVAL",
            "nodeOrder": 2,
            "nodeStatus": "PROCESSING",
            "assigneeName": "张三"
        },
        {
            "nodeName": "风险审批",
            "nodeType": "APPROVAL",
            "nodeOrder": 3,
            "nodeStatus": "PENDING"
        }
    ]
}
```

### 14.4 审批通过

**接口地址**: `POST /approval-processes/{processId}/approve`

**接口描述**: 审批通过当前节点

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| approveOpinion | String | 否 | 审批意见 |

### 14.5 审批拒绝

**接口地址**: `POST /approval-processes/{processId}/reject`

**接口描述**: 审批拒绝当前节点

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| rejectReason | String | 是 | 拒绝原因 |

### 14.6 转办

**接口地址**: `POST /approval-processes/{processId}/transfer`

**接口描述**: 转办当前节点给其他人

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| transferToUserId | String | 是 | 转办目标用户ID |
| transferReason | String | 否 | 转办原因 |

### 14.7 终止流程

**接口地址**: `POST /approval-processes/{processId}/terminate`

**接口描述**: 终止审批流程

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| terminateReason | String | 是 | 终止原因 |

### 14.8 获取我的待办

**接口地址**: `GET /approval-processes/my-pending`

**接口描述**: 获取当前用户的待办任务列表

### 14.9 获取我的已办

**接口地址**: `GET /approval-processes/my-completed`

**接口描述**: 获取当前用户的已办任务列表

---

## 15. 额度使用明细接口

### 15.1 查询使用明细

**接口地址**: `GET /quota-usage-details`

**接口描述**: 分页查询额度使用明细

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| quotaId | Long | 否 | 额度ID |
| customerId | Long | 否 | 客户ID |
| transactionType | String | 否 | 交易类型 |
| transactionTimeStart | String | 否 | 交易时间开始 |
| transactionTimeEnd | String | 否 | 交易时间结束 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "total": 100,
        "list": [
            {
                "detailNo": "UD202602120001",
                "quotaId": 1,
                "customerId": 1,
                "transactionType": "OCCUPY",
                "transactionAmount": 1000000.00,
                "beforeBalance": 10000000.00,
                "afterBalance": 9000000.00,
                "transactionTime": "2026-02-12T10:00:00",
                "operator": "admin"
            }
        ]
    }
}
```

### 15.2 获取额度明细

**接口地址**: `GET /quota-usage-details/quota/{quotaId}`

**接口描述**: 获取指定额度的使用明细

### 15.3 获取客户明细

**接口地址**: `GET /quota-usage-details/customer/{customerId}`

**接口描述**: 获取指定客户的使用明细

### 15.4 使用统计

**接口地址**: `GET /quota-usage-details/statistics`

**接口描述**: 获取额度使用统计

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerId | Long | 否 | 客户ID |
| startDate | String | 否 | 统计开始日期 |
| endDate | String | 否 | 统计结束日期 |
| groupBy | String | 否 | 分组方式: DAY, WEEK, MONTH |

### 15.5 导出明细

**接口地址**: `GET /quota-usage-details/export`

**接口描述**: 导出额度使用明细为Excel文件

---

## 16. 风险监控接口

### 16.1 创建/更新风险指标

**接口地址**: `POST /risk-indexes`

**接口描述**: 创建或更新风险监控指标

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerId | Long | 是 | 客户ID |
| indexType | String | 是 | 指标类型 |
| indexCode | String | 是 | 指标代码 |
| indexName | String | 是 | 指标名称 |
| indexValue | BigDecimal | 是 | 指标值 |
| thresholdValue | BigDecimal | 否 | 阈值 |
| calcDate | String | 是 | 计算日期 |

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "id": 1,
        "customerId": 1,
        "indexCode": "QUOTA_USAGE_RATE",
        "indexName": "额度使用率",
        "indexValue": 75.50,
        "thresholdValue": 80.00,
        "riskLevel": "MEDIUM",
        "calcDate": "2026-02-12"
    }
}
```

### 16.2 获取风险指标详情

**接口地址**: `GET /risk-indexes/{id}`

**接口描述**: 获取风险指标详情

### 16.3 获取客户风险指标

**接口地址**: `GET /risk-indexes/customer/{customerId}`

**接口描述**: 获取客户的所有风险指标列表

### 16.4 查询风险指标列表

**接口地址**: `GET /risk-indexes`

**接口描述**: 分页查询风险指标

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| customerId | Long | 否 | 客户ID |
| indexType | String | 否 | 指标类型 |
| riskLevel | String | 否 | 风险等级 |
| calcDate | String | 否 | 计算日期 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

### 16.5 计算风险指标

**接口地址**: `POST /risk-indexes/calculate/{customerId}`

**接口描述**: 重新计算客户的风险指标

### 16.6 风险评估

**接口地址**: `GET /risk-indexes/assess/{customerId}`

**接口描述**: 对客户进行综合风险评估

**响应示例**:

```json
{
    "code": "000000",
    "message": "成功",
    "data": {
        "customerId": 1,
        "overallRiskLevel": "MEDIUM",
        "riskScore": 65,
        "indexResults": [
            {
                "indexCode": "QUOTA_USAGE_RATE",
                "indexValue": 75.50,
                "riskLevel": "MEDIUM",
                "assessment": "额度使用率较高，建议关注"
            }
        ],
        "suggestions": [
            "建议适当控制用信规模",
            "关注客户经营状况变化"
        ]
    }
}
```

### 16.7 获取高风险客户

**接口地址**: `GET /risk-indexes/high-risk`

**接口描述**: 获取高风险客户列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| riskLevel | String | 否 | 风险等级: HIGH, CRITICAL |
| limit | Integer | 否 | 返回数量限制 |

---

**文档版本**: V1.1  
**更新日期**: 2026年2月12日  
**更新内容**: 新增客户管理、授信申请、用信申请、审批流程、额度使用明细、风险监控等接口文档
