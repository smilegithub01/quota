# 校验引擎系统说明文档

## 概述

本文档描述了一个基于**责任链模式(Chain of Responsibility)**的接口校验引擎，专门为金融信贷系统设计。该系统实现了灵活可配置的请求校验流程，支持白名单机制，允许特定用户跳过部分或全部校验。

---

## 目录

1. [架构设计](#架构设计)
2. [核心组件](#核心组件)
3. [责任链模式](#责任链模式)
4. [白名单机制](#白名单机制)
5. [执行流程](#执行流程)
6. [配置文件说明](#配置文件说明)
7. [使用示例](#使用示例)

---

## 架构设计

### 设计原则

- **责任链模式**：将校验逻辑分解为独立的处理器，形成链式调用
- **开闭原则**：新增校验只需添加新的Check实现类，无需修改核心代码
- **可配置化**：通过XML配置文件定义接口的执行流程
- **白名单机制**：支持特定用户绕过部分校验

### 系统架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            InterfaceEngine                                   │
│                          (执行引擎/调度中心)                                 │
└─────────────────────────────────────────────────────────────────────────────┘
                                     │
         ┌───────────────────────────┼───────────────────────────┐
         │                           │                           │
         ▼                           ▼                           ▼
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   commonProcess │       │   checkChain    │       │   bizAction     │
│  (通用处理链)   │       │   (校验链)      │       │  (业务动作链)   │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ • logStart      │       │ • PARAM_CHECK   │       │ • insertOrder   │
│ • contextFill   │  ───► │ • AUTH_CHECK    │  ──►  │ • logFinish     │
│ • authCheck     │       │ • RISK_CHECK    │       │                 │
│                 │       │ • STATUS_CHECK  │       │                 │
│                 │       │ • AMOUNT_CHECK  │       │                 │
└─────────────────┘       └─────────────────┘       └─────────────────┘
                                     │
                                     ▼
                          ┌─────────────────┐
                          │  specialProcess  │
                          │ (特殊处理链)     │
                          ├─────────────────┤
                          │ • whiteListBreak │
                          └─────────────────┘
```

---

## 核心组件

### 1. 核心接口

#### Check 接口
校验链的核心接口，每个实现类代表一个具体的校验环节。

```java
public interface Check {
    void check(InvokeContext ctx);
}
```

**主要实现类：**

| 类名 | 层级 | 功能描述 |
|------|------|----------|
| ParamCheck | 1 | 参数校验 |
| AuthCheck | 2 | 权限校验 |
| RiskCheck | 3 | 风控校验 |
| StatusCheck | 4 | 状态校验 |
| AmountCheck | 5 | 金额校验 |

#### Handler 接口
处理器接口，用于执行通用处理和业务动作。

```java
public interface Handler {
    void handle(InvokeContext ctx);
}
```

**主要实现类：**

| 类名 | 类型 | 功能描述 |
|------|------|----------|
| LogStartHandler | commonProcess | 日志开始 |
| LogFinishHandler | bizAction | 日志结束 |
| ContextFillHandler | commonProcess | 上下文填充 |
| AuthCheckHandler | commonProcess | 权限预检 |
| InsertOrderHandler | bizAction | 订单创建 |
| WhiteListBreakHandler | specialProcess | 白名单处理 |

### 2. 抽象基类

#### AbstractCheck
提供了责任链模式和**白名单跳过机制**的核心实现。

关键方法：
- `level()`: 获取校验层级
- `doCheck()`: 执行实际校验逻辑
- `check()`: 模板方法，含白名单逻辑

```java
public abstract class AbstractCheck implements Check {
    public abstract int level();
    protected abstract void doCheck(InvokeContext ctx);
    
    @Override
    public final void check(InvokeContext ctx) {
        if (!ctx.isPass()) return;
        
        WhiteListConfig wl = ctx.getWhiteList();
        // 白名单跳过逻辑
        if (wl != null && wl.getBreakLevel() >= level() && wl.getMaxCount() > 0) {
            ctx.getConsumedLevels().add(level());
            wl.setMaxCount(wl.getMaxCount() - 1);
            return;
        }
        
        doCheck(ctx);
    }
}
```

### 3. 执行引擎

#### InterfaceEngine
整个系统的核心调度器，负责按顺序执行各个处理链。

```java
@Component
public class InterfaceEngine {
    public Result<?> execute(String interfaceName, Object request, WhiteListConfig whiteList) {
        // 1. 获取接口定义
        // 2. 参数校验
        // 3. 通用处理链
        // 4. 校验链（含白名单跳过）
        // 5. 特殊处理链
        // 6. 业务动作链
    }
}
```

---

## 责任链模式

### 模式说明

责任链模式是一种行为设计模式，允许将请求沿着处理者链发送，直到有一个处理者处理它。

在本系统中：
- 每个 `Check` 实现类是一个处理者
- `InterfaceEngine` 负责构建和执行链
- 请求依次经过各个校验环节

### 校验层级

校验层级用于控制校验的执行顺序和优先级：

```
层级越高越后执行

Level 1: PARAM_CHECK    (参数校验，最先执行)
    ↓
Level 2: AUTH_CHECK     (权限校验)
    ↓
Level 3: RISK_CHECK     (风控校验)
    ↓
Level 4: STATUS_CHECK   (状态校验)
    ↓
Level 5: AMOUNT_CHECK   (金额校验，最后执行)
```

---

## 白名单机制

### 概述

白名单机制允许特定用户（如VIP客户、内部员工）跳过部分或全部校验，用于：
- 提高VIP用户的体验
- 内部系统对接时简化流程
- 紧急情况下的快速处理

### WhiteListConfig

```java
public class WhiteListConfig {
    private int breakLevel;  // 允许跳过的最高层级
    private int maxCount;    // 剩余可用次数
}
```

### 工作原理

```
当 AbstractCheck.check() 执行时：

1. 检查白名单是否存在
   └─ 如果 whiteList == null，跳过白名单逻辑

2. 检查是否满足跳过条件
   └─ breakLevel >= this.level()  (白名单允许跳过当前层级)
   └─ maxCount > 0                  (还有可用次数)

3. 如果满足条件
   └─ 将当前层级加入已消耗集合
   └─ 次数减1
   └─ 跳过校验 (return)

4. 如果不满足条件
   └─ 执行实际校验逻辑 doCheck()
```

### 使用示例

```java
// 普通请求（完整校验）
WhiteListConfig whiteList = null;
Result<?> result = engine.execute("submit", request, whiteList);

// 白名单请求（跳过部分校验）
WhiteListConfig whiteList = new WhiteListConfig();
whiteList.setBreakLevel(2);   // 跳过 level <= 2 的校验
whiteList.setMaxCount(10);    // 允许跳过10次
Result<?> result = engine.execute("submit", request, whiteList);
```

---

## 执行流程

### 完整执行流程

```
┌──────────────────────────────────────────────────────────────────────┐
│                         请求入口                                       │
│                   engine.execute(interfaceName, request, whiteList)   │
└──────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────────┐
│  1. 获取接口定义                                                       │
│     InterfaceDefine def = config.get(interfaceName)                  │
└──────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────────┐
│  2. 参数校验                                                          │
│     ParamValidator.validate(paramValidate, request)                   │
│     ↓ 失败则返回 Result.fail()                                         │
└──────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────────┐
│  3. 通用处理链 (commonProcess)                                        │
│     ┌─────────────────────────────────────┐                           │
│     │ logStart      → 记录开始日志        │                           │
│     │ contextFill   → 填充上下文         │                           │
│     │ authCheck    → 权限预检查          │                           │
│     └─────────────────────────────────────┘                           │
└──────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────────┐
│  4. 校验链 (checkChain) 【核心】                                       │
│     ┌─────────────────────────────────────┐                           │
│     │ PARAM_CHECK  → 参数校验 (L:1)      │                           │
│     │    ↓ [白名单检查]                  │                           │
│     │ AUTH_CHECK   → 权限校验 (L:2)      │                           │
│     │    ↓ [白名单检查]                  │                           │
│     │ RISK_CHECK   → 风控校验 (L:3)      │                           │
│     │    ↓ [白名单检查]                  │                           │
│     │ STATUS_CHECK → 状态校验 (L:4)      │                           │
│     │    ↓ [白名单检查]                  │                           │
│     │ AMOUNT_CHECK → 金额校验 (L:5)      │                           │
│     └─────────────────────────────────────┘                           │
│                                  │                                    │
│     任意校验失败 ────────────────┘                                    │
│     (ctx.setPass(false), ctx.setErrMsg())                             │
└──────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────────┐
│  5. 特殊处理链 (specialProcess)                                        │
│     whiteListBreak → 白名单处理                                        │
└──────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────────┐
│  6. 业务动作链 (bizAction)                                            │
│     ┌─────────────────────────────────────┐                           │
│     │ insertOrder → 创建订单              │                           │
│     │ logFinish  → 记录完成日志           │                           │
│     └─────────────────────────────────────┘                           │
└──────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────────┐
│                         返回结果                                       │
│                   Result.ok() 或 Result.fail()                        │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 配置文件说明

### interface-flow.xml

定义了接口的校验流程配置。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<interfaces>
    <!-- 预检查接口 -->
    <interface name="pre_check">
        <param-validate>
            <field name="userId" required="true"/>
            <field name="amount" required="true" type="decimal" min="0.01"/>
        </param-validate>
        <common-process>
            <handler>logStart</handler>
            <handler>contextFill</handler>
        </common-process>
        <check-chain>
            <check>PARAM_CHECK</check>
            <check>AUTH_CHECK</check>
            <check>STATUS_CHECK</check>
        </check-chain>
    </interface>

    <!-- 提交接口（含白名单） -->
    <interface name="submit">
        <param-validate>
            <field name="userId" required="true"/>
            <field name="amount" required="true" type="decimal" min="0.01"/>
            <field name="orderId" required="true" length="10,32"/>
        </param-validate>
        <common-process>
            <handler>logStart</handler>
            <handler>contextFill</handler>
            <handler>authCheck</handler>
        </common-process>
        <check-chain>
            <check>PARAM_CHECK</check>
            <check>AUTH_CHECK</check>
            <check>RISK_CHECK</check>
            <check>STATUS_CHECK</check>
            <check>AMOUNT_CHECK</check>
        </check-chain>
        <special-process>
            <handler>whiteListBreak</handler>
        </special-process>
        <biz-action>
            <handler>insertOrder</handler>
            <handler>logFinish</handler>
        </biz-action>
    </interface>
</interfaces>
```

### check-chain.yml

定义了各类型校验的详细配置。

```yaml
# 校验链配置
check:
  chain:
    default-enabled: true
    simple-enabled: true
    strict-enabled: false

  # 参数校验配置
  param:
    enabled: true
    required-params:
      - amount
      - userId

  # 权限校验配置
  auth:
    enabled: true
    required-role: ADMIN
    check-ip: true
    allowed-ips: 192.168.1.*,10.0.0.*

  # 金额校验配置
  amount:
    enabled: true
    min-amount: 1
    max-amount: 1000000
    single-limit: 50000
    day-limit: 200000

  # 风控校验配置
  risk:
    enabled: true
    max-risk-level: 3

  # 白名单校验配置
  whitelist:
    enabled: true
    default-rule-id: DEFAULT_WHITELIST
```

---

## 使用示例

### 1. 基础调用

```java
@Autowired
private InterfaceEngine engine;

public Result<?> submitOrder(OrderRequest request) {
    // 普通请求
    return engine.execute("submit", request, null);
}
```

### 2. 白名单调用

```java
@Autowired
private InterfaceEngine engine;

public Result<?> submitOrderByWhiteList(OrderRequest request) {
    // 创建白名单配置
    WhiteListConfig whiteList = new WhiteListConfig();
    whiteList.setBreakLevel(2);   // 跳过前两级校验
    whiteList.setMaxCount(10);    // 允许10次
    
    return engine.execute("submit", request, whiteList);
}
```

### 3. 新增自定义校验

```java
@Component("CUSTOM_CHECK")
public class CustomCheck extends AbstractCheck {
    
    @Override
    public int level() {
        return 2;  // 与AUTH_CHECK同级
    }
    
    @Override
    protected void doCheck(InvokeContext ctx) {
        // 自定义校验逻辑
        Object request = ctx.getRequest();
        // ...
        
        if (!validateSuccess) {
            ctx.setPass(false);
            ctx.setErrMsg("自定义校验失败");
        }
    }
}
```

---

## 代码结构

```
quota-common/src/main/java/com/example/check/
├── core/                          # 核心接口和抽象类
│   ├── Check.java                 # 校验接口
│   ├── Handler.java               # 处理器接口
│   ├── AbstractCheck.java         # 校验抽象基类（含白名单逻辑）
│   └── InterfaceEngine.java       # 执行引擎
├── checkimpl/                     # 校验实现类
│   ├── ParamCheck.java            # 参数校验
│   ├── AuthCheck.java             # 权限校验
│   ├── RiskCheck.java             # 风控校验
│   ├── StatusCheck.java           # 状态校验
│   └── AmountCheck.java           # 金额校验
├── handlerimpl/                   # 处理器实现类
│   ├── LogStartHandler.java       # 日志开始
│   ├── LogFinishHandler.java      # 日志结束
│   ├── ContextFillHandler.java    # 上下文填充
│   ├── AuthCheckHandler.java      # 权限预检
│   ├── InsertOrderHandler.java    # 订单创建
│   └── WhiteListBreakHandler.java # 白名单处理
├── common/                        # 通用类
│   ├── Result.java                # 返回结果
│   └── WhiteListConfig.java       # 白名单配置
├── context/                      # 上下文
│   └── InvokeContext.java         # 调用上下文
├── config/                       # 配置类
│   ├── InterfaceConfig.java       # 接口配置管理
│   └── XmlParser.java             # XML解析器
├── define/                        # 定义类
│   ├── InterfaceDefine.java       # 接口定义
│   ├── ParamValidate.java         # 参数校验定义
│   └── FieldRule.java             # 字段规则
├── dto/                          # 数据传输对象
│   └── RequestDTO.java            # 请求DTO
├── validator/                    # 校验器
│   └── ParamValidator.java        # 参数校验器
└── controller/                   # 控制器
    └── UniversalController.java   # 统一控制器
```

---

## 新接口开发流程

### 整体流程概览

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          新接口开发流程                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  步骤1: 分析需求          步骤2: 配置XML        步骤3: 开发/复用Check     │
│  ┌─────────────┐        ┌─────────────┐       ┌─────────────┐            │
│  │ 确定校验规则 │   →    │ 定义接口流程 │  →   │ 复用/新增   │            │
│  └─────────────┘        └─────────────┘       └─────────────┘            │
│                                                            │              │
│                                                            ▼              │
│  步骤5: 测试验证          步骤4: 开发Handler         步骤6: 接入调用     │
│  ┌─────────────┐        ┌─────────────┐       ┌─────────────┐            │
│  │ 单元测试    │   ←    │ 业务处理    │  ←   │ Controller  │            │
│  └─────────────┘        └─────────────┘       └─────────────┘            │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 步骤1: 分析需求

在开发新接口前，需要明确：

```
1. 接口名称是什么？
2. 需要哪些参数？
3. 需要哪些校验？   → 复用现有 Check 还是新增？
4. 需要哪些前置处理？  (日志、上下文填充等)
5. 校验失败后的特殊处理？
6. 业务动作是什么？  (订单创建？额度调整？)
7. 是否需要白名单？
```

### 步骤2: 配置XML接口定义

在 `interface-flow.xml` 中添加新接口配置：

```xml
<!-- 示例：新增一个"额度申请"接口 -->
<interface name="apply_quota">
    <!-- 1. 参数校验规则 -->
    <param-validate>
        <field name="userId" required="true"/>
        <field name="quotaAmount" required="true" type="decimal" min="1000" max="1000000"/>
        <field name="purpose" required="true" length="1,200"/>
        <field name="guaranteeType" required="false"/>
    </param-validate>

    <!-- 2. 通用处理链 (可选) -->
    <common-process>
        <handler>logStart</handler>
        <handler>contextFill</handler>
    </common-process>

    <!-- 3. 校验链 (核心) -->
    <check-chain>
        <check>PARAM_CHECK</check>        <!-- 复用: 参数校验 -->
        <check>AUTH_CHECK</check>         <!-- 复用: 权限校验 -->
        <check>RISK_CHECK</check>         <!-- 复用: 风控校验 -->
        <check>STATUS_CHECK</check>       <!-- 复用: 状态校验 -->
        <check>QUOTA_CHECK</check>        <!-- 新增: 额度校验 -->
    </check-chain>

    <!-- 4. 特殊处理链 (可选) -->
    <special-process>
        <handler>whiteListBreak</handler> <!-- 白名单处理 -->
    </special-process>

    <!-- 5. 业务动作链 -->
    <biz-action>
        <handler>applyQuota</handler>     <!-- 新增: 额度申请 -->
        <handler>logFinish</handler>
    </biz-action>
</interface>
```

### 步骤3: 复用或新增校验 (Check)

#### 3.1 复用现有校验

如果现有校验满足需求，直接在 XML 中引用即可：

```xml
<check>AUTH_CHECK</check>     <!-- 直接复用 -->
<check>RISK_CHECK</check>    <!-- 直接复用 -->
```

#### 3.2 新增自定义校验

如果需要特殊校验，创建新的 Check 实现类：

```java
package com.example.check.checkimpl;

import com.example.check.context.InvokeContext;
import com.example.check.core.AbstractCheck;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * 额度校验器 - 自定义校验示例
 *
 * 校验内容：
 *   - 申请额度是否在允许范围内
 *   - 用户当前额度是否充足
 *   - 额度使用率是否超标
 */
@Component("QUOTA_CHECK")  // XML中引用的名称
public class QuotaCheck extends AbstractCheck {

    @Override
    public int level() {
        return 6;  // 在AMOUNT_CHECK之后执行
    }

    @Override
    protected void doCheck(InvokeContext ctx) {
        // 获取请求对象
        Object request = ctx.getRequest();
        
        // 获取请求中的额度参数
        BigDecimal applyAmount = getApplyAmount(request);
        
        // 获取用户当前额度
        BigDecimal currentQuota = getUserCurrentQuota(request);
        
        // 校验逻辑
        if (applyAmount.compareTo(currentQuota) > 0) {
            ctx.setPass(false);
            ctx.setErrMsg("申请额度超过用户当前额度");
            return;
        }
        
        // 校验通过
        ctx.setPass(true);
    }
    
    private BigDecimal getApplyAmount(Object request) {
        // 通过反射或直接类型转换获取参数
        // TODO: 根据实际请求类型实现
        return BigDecimal.ZERO;
    }
    
    private BigDecimal getUserCurrentQuota(Object request) {
        // TODO: 从数据库或缓存获取用户额度
        return BigDecimal.ZERO;
    }
}
```

### 步骤4: 开发业务Handler

#### 4.1 复用现有Handler

```xml
<handler>logStart</handler>
<handler>logFinish</handler>
```

#### 4.2 新增业务Handler

```java
package com.example.check.handlerimpl;

import com.example.check.context.InvokeContext;
import com.example.check.core.Handler;
import org.springframework.stereotype.Component;

/**
 * 额度申请处理器
 *
 * 功能：
 *   - 创建额度申请记录
 *   - 触发额度审批流程
 *   - 发送通知
 */
@Component("applyQuota")  // XML中引用的名称
public class ApplyQuotaHandler implements Handler {

    @Override
    public void handle(InvokeContext ctx) {
        // 获取请求参数
        Object request = ctx.getRequest();
        
        // TODO: 业务逻辑
        // 1. 创建额度申请记录
        // 2. 初始化申请状态
        // 3. 触发后续审批流程
        
        System.out.println("额度申请已提交");
        
        // 设置结果
        ctx.setPass(true);
    }
}
```

### 步骤5: Controller接入

在Controller中调用校验引擎：

```java
package com.example.check.controller;

import com.example.check.common.Result;
import com.example.check.common.WhiteListConfig;
import com.example.check.core.InterfaceEngine;
import com.example.check.dto.RequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quota")
@RequiredArgsConstructor
public class QuotaController {

    private final InterfaceEngine engine;

    /**
     * 普通额度申请 - 完整校验
     */
    @PostMapping("/apply")
    public Result<?> applyQuota(@RequestBody RequestDTO request) {
        // 普通请求，不使用白名单
        return engine.execute("apply_quota", request, null);
    }

    /**
     * 白名单额度申请 - 跳过部分校验
     */
    @PostMapping("/apply/vip")
    public Result<?> applyQuotaForVip(@RequestBody RequestDTO request) {
        // VIP白名单：跳过前3级校验，共5次机会
        WhiteListConfig whiteList = new WhiteListConfig();
        whiteList.setBreakLevel(3);
        whiteList.setMaxCount(5);
        
        return engine.execute("apply_quota", request, whiteList);
    }
}
```

### 步骤6: 测试验证

#### 6.1 单元测试

```java
package com.example.check;

import com.example.check.common.WhiteListConfig;
import com.example.check.common.Result;
import com.example.check.core.InterfaceEngine;
import com.example.check.dto.RequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QuotaCheckTest {

    @Autowired
    private InterfaceEngine engine;

    @Test
    void testApplyQuota_Success() {
        // 准备请求
        RequestDTO request = new RequestDTO();
        request.setUserId("1001");
        request.setAmount(new BigDecimal("50000"));
        
        // 执行
        Result<?> result = engine.execute("apply_quota", request, null);
        
        // 验证
        assertTrue(result.isSuccess());
    }

    @Test
    void testApplyQuota_WhiteList() {
        // 准备请求
        RequestDTO request = new RequestDTO();
        request.setUserId("1001");
        request.setAmount(new BigDecimal("50000"));
        
        // 白名单配置
        WhiteListConfig whiteList = new WhiteListConfig();
        whiteList.setBreakLevel(2);
        whiteList.setMaxCount(10);
        
        // 执行
        Result<?> result = engine.execute("apply_quota", request, whiteList);
        
        // 验证
        assertTrue(result.isSuccess());
    }

    @Test
    void testApplyQuota_ParamFail() {
        // 准备请求 - 缺少必填参数
        RequestDTO request = new RequestDTO();
        // userId 未设置
        
        // 执行
        Result<?> result = engine.execute("apply_quota", request, null);
        
        // 验证
        assertFalse(result.isSuccess());
        assertEquals("参数校验失败", result.getMsg());
    }
}
```

### 开发 Checklist

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        新接口开发 Checklist                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ ☐ 1. 分析需求，确定接口名称和功能                                       │
│                                                                         │
│ ☐ 2. 确定参数列表                                                       │
│     ☐ 必填参数                                                          │
│     ☐ 可选参数                                                          │
│     ☐ 参数类型和校验规则                                                │
│                                                                         │
│ ☐ 3. 确定校验链                                                         │
│     ☐ 复用: PARAM_CHECK, AUTH_CHECK, RISK_CHECK, STATUS_CHECK         │
│     ☐ 新增: _____________                                               │
│                                                                         │
│ ☐ 4. 确定处理链                                                         │
│     ☐ 通用处理: logStart, contextFill                                   │
│     ☐ 特殊处理: whiteListBreak (如需白名单)                             │
│     ☐ 业务动作: _____________                                           │
│                                                                         │
│ ☐ 5. XML配置                                                            │
│     ☐ 添加 interface 节点                                              │
│     ☐ 配置 param-validate                                              │
│     ☐ 配置 check-chain                                                 │
│     ☐ 配置 handler 引用                                                │
│                                                                         │
│ ☐ 6. 如需新校验                                                         │
│     ☐ 创建 XxxCheck.java                                               │
│     ☐ 继承 AbstractCheck                                               │
│     ☐ 实现 level() 和 doCheck()                                        │
│     ☐ 添加 @Component("XXX_CHECK")                                     │
│                                                                         │
│ ☐ 7. 如需新Handler                                                      │
│     ☐ 创建 XxxHandler.java                                             │
│     ☐ 实现 Handler                                                      │
│     ☐ 添加 @Component("xxx")                                          │
│                                                                         │
│ ☐ 8. Controller接入                                                     │
│     ☐ 注入 InterfaceEngine                                             │
│     ☐ 调用 execute() 方法                                              │
│                                                                         │
│ ☐ 9. 测试验证                                                           │
│     ☐ 正常流程测试                                                      │
│     ☐ 校验失败测试                                                      │
│     ☐ 白名单测试                                                        │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 快速开发模板

#### 模板1: 简单查询接口

```xml
<interface name="query_xxx">
    <param-validate>
        <field name="id" required="true"/>
    </param-validate>
    <common-process>
        <handler>logStart</handler>
    </common-process>
    <check-chain>
        <check>PARAM_CHECK</check>
        <check>AUTH_CHECK</check>
    </check-chain>
    <biz-action>
        <handler>queryXxx</handler>
        <handler>logFinish</handler>
    </biz-action>
</interface>
```

#### 模板2: 重要业务接口 (需白名单)

```xml
<interface name="submit_xxx">
    <param-validate>
        <field name="userId" required="true"/>
        <field name="amount" required="true" type="decimal"/>
    </param-validate>
    <common-process>
        <handler>logStart</handler>
        <handler>contextFill</handler>
    </common-process>
    <check-chain>
        <check>PARAM_CHECK</check>
        <check>AUTH_CHECK</check>
        <check>RISK_CHECK</check>
        <check>STATUS_CHECK</check>
        <check>AMOUNT_CHECK</check>
    </check-chain>
    <special-process>
        <handler>whiteListBreak</handler>
    </special-process>
    <biz-action>
        <handler>submitXxx</handler>
        <handler>logFinish</handler>
    </biz-action>
</interface>
```

### 开发文件清单

| 步骤 | 操作 | 文件位置 |
|------|------|----------|
| 1 | 分析需求 | - |
| 2 | XML配置 | `src/main/resources/interface-flow.xml` |
| 3 | 复用校验 | 无需修改 |
| 3 | 新增校验 | `checkimpl/XxxCheck.java` |
| 4 | 业务处理 | `handlerimpl/XxxHandler.java` |
| 5 | Controller | `controller/XxxController.java` |
| 6 | 测试验证 | `test/...XxxTest.java` |

---

## 总结

该校验引擎是一个设计良好的金融系统请求校验框架，具有以下特点：

1. **灵活性**：通过XML配置文件定义校验流程，无需修改代码即可调整
2. **可扩展性**：新增校验只需实现Check接口
3. **白名单机制**：支持特定用户绕过部分校验
4. **责任链模式**：清晰的职责分离，易于维护
5. **金融级设计**：支持权限、风控、金额等关键业务校验

适用于需要灵活校验流程的金融信贷系统、支付系统等场景。
