package com.bank.quota.common.chain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 责任链模式测试类
 * 
 * 演示责任链模式在白名单与校验系统中的应用
 * 包含多种测试场景，验证责任链的处理流程
 * 
 * 测试场景说明：
 * 场景1：正常请求 - 所有校验都通过
 * 场景2：IP不在白名单 - 被IP白名单处理器拒绝
 * 场景3：用户不在白名单 - 被用户ID白名单处理器拒绝
 * 场景4：参数校验失败 - 被参数校验处理器拒绝
 * 场景5：权限不足 - 被权限校验处理器拒绝
 * 场景6：额度超限 - 被额度校验处理器拒绝
 * 场景7：API不在白名单 - 被API白名单处理器拒绝
 */
public class ChainOfResponsibilityTest {
    
    public static void main(String[] args) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║           责任链模式 - 白名单与校验系统测试                               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // 创建测试处理器
        Handler handlerChain = createFullChain();
        
        // 测试场景1：正常请求 - 所有校验都通过
        testScenario1(handlerChain);
        
        // 测试场景2：IP不在白名单
        testScenario2(handlerChain);
        
        // 测试场景3：用户不在白名单
        testScenario3(handlerChain);
        
        // 测试场景4：参数校验失败
        testScenario4(handlerChain);
        
        // 测试场景5：权限不足
        testScenario5(handlerChain);
        
        // 测试场景6：额度超限
        testScenario6(handlerChain);
        
        // 测试场景7：API不在白名单
        testScenario7(handlerChain);
        
        // 测试场景8：使用自定义链
        testScenario8();
        
        // 测试场景9：链终止情况
        testScenario9();
        
        // 测试场景10：组合多个校验场景
        testScenario10();
    }
    
    /**
     * 创建完整的责任链
     */
    private static Handler createFullChain() {
        // IP白名单：允许内网IP
        Set<String> ipWhitelist = new HashSet<>();
        ipWhitelist.add("192.168.1.100");
        ipWhitelist.add("192.168.1.101");
        ipWhitelist.add("10.0.0.1");
        ipWhitelist.add("127.0.0.1");
        IpWhitelistHandler ipHandler = new IpWhitelistHandler(ipWhitelist, true);
        
        // 用户ID白名单：允许特定用户
        Set<Long> userIdWhitelist = new HashSet<>();
        userIdWhitelist.add(1001L);
        userIdWhitelist.add(1002L);
        userIdWhitelist.add(1003L);
        Set<String> usernameWhitelist = new HashSet<>();
        usernameWhitelist.add("admin");
        usernameWhitelist.add("system");
        UserIdWhitelistHandler userHandler = new UserIdWhitelistHandler(userIdWhitelist, usernameWhitelist);
        
        // API白名单：允许特定的API
        ApiWhitelistHandler apiHandler = new ApiWhitelistHandler(true);
        apiHandler.addGlobalApi("quota_query");
        apiHandler.addGlobalApi("quota_apply");
        apiHandler.addGlobalApi("quota_adjust");
        apiHandler.addGlobalApi("risk_query");
        apiHandler.addUserApi(1001L, "admin_query");
        apiHandler.addUserApi(1002L, "quota_approve");
        apiHandler.addRoleApi("ROLE_ADMIN", "*");
        
        // 参数校验
        ParameterValidationHandler paramHandler = new ParameterValidationHandler();
        paramHandler.addRequiredParam("amount");
        paramHandler.addRequiredParam("currency");
        paramHandler.addValidationRule("amount", ParameterValidationHandler.ValidationRule.positive("金额必须为正数"));
        paramHandler.addValidationRule("amount", ParameterValidationHandler.ValidationRule.range(
                new BigDecimal("0.01"), new BigDecimal("1000000"), "金额超出允许范围"));
        paramHandler.addValidationRule("currency", ParameterValidationHandler.ValidationRule.notNull("货币不能为空"));
        
        // 权限校验
        PermissionValidationHandler permHandler = new PermissionValidationHandler();
        permHandler.addCommonPermission("api:login");
        permHandler.addRolePermission("ROLE_USER", "quota_query");
        permHandler.addRolePermission("ROLE_USER", "quota_apply");
        permHandler.addRolePermission("ROLE_AUDITOR", "quota_query");
        permHandler.addRolePermission("ROLE_AUDITOR", "risk_query");
        permHandler.addRolePermission("ROLE_ADMIN", "quota_query");
        permHandler.addRolePermission("ROLE_ADMIN", "quota_apply");
        permHandler.addRolePermission("ROLE_ADMIN", "quota_adjust");
        permHandler.addRolePermission("ROLE_ADMIN", "quota_approve");
        permHandler.addRolePermission("ROLE_ADMIN", "risk_query");
        permHandler.addUserPermission(1001L, "admin_query");
        permHandler.addUserPermission(1002L, "quota_approve");
        
        // 额度校验
        QuotaValidationHandler quotaHandler = new QuotaValidationHandler();
        quotaHandler.setDefaultLimits(
                new BigDecimal("100000.00"),  // 单笔限额
                new BigDecimal("500000.00"),  // 日限额
                new BigDecimal("2000000.00")   // 月限额
        );
        quotaHandler.setUserQuotaLimit(1001L, new QuotaValidationHandler.QuotaLimit(
                new BigDecimal("500000.00"),
                new BigDecimal("1000000.00"),
                new BigDecimal("5000000.00")
        ));
        
        // 使用ChainBuilder组装责任链
        ChainBuilder builder = new ChainBuilder("FullValidationChain");
        builder.addHandler(ipHandler)
                .addHandler(userHandler)
                .addHandler(apiHandler)
                .addHandler(paramHandler)
                .addHandler(permHandler)
                .addHandler(quotaHandler);
        
        return builder.build();
    }
    
    /**
     * 测试场景1：正常请求 - 所有校验都通过
     */
    private static void testScenario1(Handler handlerChain) {
        printScenarioHeader("场景1：正常请求 - 所有校验都通过");
        
        RequestContext context = new RequestContext("REQ-001", "quota_query");
        context.setClientIp("192.168.1.100");
        context.setUserId(1001L);
        context.setUsername("admin");
        context.setRoles(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
        context.addParam("amount", "50000");
        context.addParam("currency", "CNY");
        
        System.out.println("请求信息: " + context);
        System.out.println();
        
        HandleResult result = handlerChain.handleRequest(context);
        
        printResult(result, context);
    }
    
    /**
     * 测试场景2：IP不在白名单
     */
    private static void testScenario2(Handler handlerChain) {
        printScenarioHeader("场景2：IP不在白名单");
        
        RequestContext context = new RequestContext("REQ-002", "quota_query");
        context.setClientIp("202.106.1.1");  // 不在白名单的IP
        context.setUserId(1001L);
        context.setUsername("admin");
        context.setRoles(Arrays.asList("ROLE_ADMIN"));
        context.addParam("amount", "50000");
        context.addParam("currency", "CNY");
        
        System.out.println("请求信息: " + context);
        System.out.println();
        
        HandleResult result = handlerChain.handleRequest(context);
        
        printResult(result, context);
    }
    
    /**
     * 测试场景3：用户不在白名单
     */
    private static void testScenario3(Handler handlerChain) {
        printScenarioHeader("场景3：用户不在白名单");
        
        RequestContext context = new RequestContext("REQ-003", "quota_query");
        context.setClientIp("192.168.1.100");
        context.setUserId(9999L);  // 不在白名单的用户
        context.setUsername("unknown");
        context.setRoles(Arrays.asList("ROLE_USER"));
        context.addParam("amount", "50000");
        context.addParam("currency", "CNY");
        
        System.out.println("请求信息: " + context);
        System.out.println();
        
        HandleResult result = handlerChain.handleRequest(context);
        
        printResult(result, context);
    }
    
    /**
     * 测试场景4：参数校验失败
     */
    private static void testScenario4(Handler handlerChain) {
        printScenarioHeader("场景4：参数校验失败 - 金额为负数");
        
        RequestContext context = new RequestContext("REQ-004", "quota_apply");
        context.setClientIp("192.168.1.100");
        context.setUserId(1002L);
        context.setUsername("user1002");
        context.setRoles(Arrays.asList("ROLE_USER"));
        context.addParam("amount", "-1000");  // 负数金额
        context.addParam("currency", "CNY");
        
        System.out.println("请求信息: " + context);
        System.out.println();
        
        HandleResult result = handlerChain.handleRequest(context);
        
        printResult(result, context);
    }
    
    /**
     * 测试场景5：权限不足
     */
    private static void testScenario5(Handler handlerChain) {
        printScenarioHeader("场景5：权限不足 - 用户无API权限");
        
        RequestContext context = new RequestContext("REQ-005", "quota_adjust");  // 需要特殊权限
        context.setClientIp("192.168.1.101");
        context.setUserId(1003L);
        context.setUsername("user1003");
        context.setRoles(Arrays.asList("ROLE_USER"));  // 只有ROLE_USER，没有quota_adjust权限
        context.addParam("amount", "50000");
        context.addParam("currency", "CNY");
        
        System.out.println("请求信息: " + context);
        System.out.println();
        
        HandleResult result = handlerChain.handleRequest(context);
        
        printResult(result, context);
    }
    
    /**
     * 测试场景6：额度超限
     */
    private static void testScenario6(Handler handlerChain) {
        printScenarioHeader("场景6：额度超限 - 单笔超过限额");
        
        RequestContext context = new RequestContext("REQ-006", "quota_apply");
        context.setClientIp("192.168.1.100");
        context.setUserId(1002L);
        context.setUsername("user1002");
        context.setRoles(Arrays.asList("ROLE_USER"));
        context.addParam("amount", "200000");  // 超过默认单笔限额10万
        context.addParam("currency", "CNY");
        
        System.out.println("请求信息: " + context);
        System.out.println();
        
        HandleResult result = handlerChain.handleRequest(context);
        
        printResult(result, context);
    }
    
    /**
     * 测试场景7：API不在白名单
     */
    private static void testScenario7(Handler handlerChain) {
        printScenarioHeader("场景7：API不在白名单");
        
        RequestContext context = new RequestContext("REQ-007", "forbidden_api");  // 不在白名单的API
        context.setClientIp("192.168.1.100");
        context.setUserId(1002L);
        context.setUsername("user1002");
        context.setRoles(Arrays.asList("ROLE_USER"));
        context.addParam("amount", "50000");
        context.addParam("currency", "CNY");
        
        System.out.println("请求信息: " + context);
        System.out.println();
        
        HandleResult result = handlerChain.handleRequest(context);
        
        printResult(result, context);
    }
    
    /**
     * 测试场景8：使用自定义链
     */
    private static void testScenario8() {
        printScenarioHeader("场景8：使用自定义责任链");
        
        // 创建自定义链：只包含IP白名单、用户白名单和参数校验
        Set<String> ipSet = new HashSet<>();
        ipSet.add("192.168.0.0/16");
        ipSet.add("10.0.0.0/8");
        
        Set<Long> userSet = new HashSet<>();
        userSet.add(100L);
        userSet.add(200L);
        
        ChainBuilder customChain = new ChainBuilder("CustomLightChain");
        customChain.addHandler(new IpWhitelistHandler(ipSet))
                .addHandler(new UserIdWhitelistHandler(userSet))
                .addHandler(new ParameterValidationHandler());
        
        Handler handler = customChain.build();
        
        RequestContext context = new RequestContext("REQ-008", "simple_query");
        context.setClientIp("192.168.1.50");
        context.setUserId(100L);
        context.addParam("id", "12345");
        
        System.out.println("请求信息: " + context);
        System.out.println();
        
        HandleResult result = handler.handleRequest(context);
        
        printResult(result, context);
    }
    
    /**
     * 测试场景9：链终止情况 - TERMINATED
     */
    private static void testScenario9() {
        printScenarioHeader("场景9：链终止情况 - 到达链尾自然结束");
        
        // 创建一个简单的链
        ChainBuilder simpleChain = new ChainBuilder("SimpleChain");
        simpleChain.addHandler(new IpWhitelistHandler(new HashSet<>(Arrays.asList("127.0.0.1")), false))
                .addHandler(new UserIdWhitelistHandler(new HashSet<>(Arrays.asList(1L))));
        
        Handler handler = simpleChain.build();
        
        RequestContext context = new RequestContext("REQ-009", "test");
        context.setClientIp("127.0.0.1");
        context.setUserId(1L);
        
        System.out.println("请求信息: " + context);
        System.out.println();
        
        HandleResult result = handler.handleRequest(context);
        
        printResult(result, context);
    }
    
    /**
     * 测试场景10：组合多个校验场景 - 前面校验通过，最后校验失败
     */
    private static void testScenario10() {
        printScenarioHeader("场景10：组合校验 - 前8项通过，第9项失败");
        
        // 创建新的handlerChain以重置状态
        Handler handlerChain = createFullChain();
        
        RequestContext context = new RequestContext("REQ-010", "quota_apply");
        context.setClientIp("192.168.1.100");
        context.setUserId(1002L);
        context.setUsername("user1002");
        context.setRoles(Arrays.asList("ROLE_USER"));
        context.addParam("amount", "800000");  // 超过日限额50万
        context.addParam("currency", "CNY");
        
        System.out.println("请求信息: " + context);
        System.out.println("该请求将依次通过：IP白名单、用户白名单、API白名单、参数校验、权限校验");
        System.out.println("但在额度校验时失败（日限额50万，已使用50万，新申请80万）");
        System.out.println();
        
        HandleResult result = handlerChain.handleRequest(context);
        
        printResult(result, context);
    }
    
    /**
     * 打印场景标题
     */
    private static void printScenarioHeader(String title) {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ " + title);
        System.out.println("└─────────────────────────────────────────────────────────────────────────┘");
    }
    
    /**
     * 打印处理结果
     */
    private static void printResult(HandleResult result, RequestContext context) {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("最终处理结果: " + result);
        System.out.println("处理消息: " + context.getMessage());
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println();
    }
}
