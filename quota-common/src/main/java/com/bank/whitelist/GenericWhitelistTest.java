package com.bank.whitelist;

import com.bank.whitelist.common.CheckItem;
import com.bank.whitelist.common.CheckResult;
import com.bank.whitelist.common.WhitelistRule;
import com.bank.whitelist.common.WhitelistRule.RuleType;
import com.bank.whitelist.core.CheckContext;
import com.bank.whitelist.core.WhitelistManager;
import com.bank.whitelist.service.WhitelistService;

/**
 * 通用白名单校验系统测试
 * 
 * 测试场景：
 * 1. 基础配置 - 初始化白名单规则和校验项
 * 2. 单项校验 - 校验单个校验项
 * 3. 批量校验 - 一次校验多个校验项
 * 4. 动态更新 - 运行时更新白名单
 * 5. 多种规则类型 - 测试不同类型的白名单规则
 * 6. 规则匹配模式 - 测试精确匹配、前缀匹配、正则匹配
 * 7. 校验项匹配模式 - 测试ALL和ANY模式
 * 8. 变更监听 - 测试规则变更通知
 */
public class GenericWhitelistTest {
    
    public static void main(String[] args) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║              通用白名单校验系统 - 功能演示                                ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // 使用门面服务简化操作
        WhitelistService service = new WhitelistService();
        
        // ==================== 初始化配置 ====================
        initWhitelistRules(service);
        
        // ==================== 测试场景 ====================
        
        // 场景1：基础校验 - IP白名单
        testBasicCheck(service);
        
        // 场景2：多规则关联校验
        testMultiRuleCheck(service);
        
        // 场景3：批量校验
        testBatchCheck(service);
        
        // 场景4：动态更新白名单
        testDynamicUpdate(service);
        
        // 场景5：前缀匹配规则
        testPrefixMatch(service);
        
        // 场景6：正则匹配规则
        testRegexMatch(service);
        
        // 场景7：ALL匹配模式
        testAllMatchMode(service);
        
        // 场景8：统计功能
        testStatistics(service);
        
        // 场景9：变更监听
        testChangeListener(service);
        
        System.out.println();
        System.out.println("══════════════════════════════════════════════════════════════════════════");
        System.out.println("测试完成！");
        System.out.println("══════════════════════════════════════════════════════════════════════════");
    }
    
    /**
     * 初始化白名单规则和校验项
     */
    private static void initWhitelistRules(WhitelistService service) {
        System.out.println("【初始化】配置白名单规则和校验项");
        System.out.println("──────────────────────────────────────────────────────────────────────────");
        
        // 1. 添加IP白名单规则
        service.addIpRule("IP_WHITELIST", "可信IP白名单",
                "192.168.1.100", "192.168.1.101", "10.0.0.1", "127.0.0.1");
        
        // 2. 添加用户ID白名单规则
        service.addUserIdRule("USER_ID_WHITELIST", "可信用户ID白名单",
                "1001", "1002", "1003", "admin");
        
        // 3. 添加用户名白名单规则
        service.addUsernameRule("USERNAME_WHITELIST", "可信用户名白名单",
                "admin", "system", "operator");
        
        // 4. 添加设备ID白名单规则
        service.addDeviceIdRule("DEVICE_WHITELIST", "可信设备白名单",
                "DEVICE_001", "DEVICE_002", "DEVICE_003");
        
        // 5. 添加手机号白名单规则
        service.addPhoneRule("PHONE_WHITELIST", "可信手机号白名单",
                "13800138000", "13900139000", "15000150000");
        
        // 6. 添加API编码白名单规则
        service.addApiCodeRule("API_WHITELIST", "可信API白名单",
                "quota_query", "quota_apply", "quota_adjust", "risk_query");
        
        // 7. 添加业务编码白名单规则
        service.addBizCodeRule("BIZ_CODE_WHITELIST", "可信业务编码白名单",
                "LOAN_APPLY", "CREDIT_QUERY", "RISK_ASSESS");
        
        // 8. 添加带前缀匹配的规则
        service.addPrefixRule("IP_PREFIX_WHITELIST", "IP段白名单", RuleType.IP,
                "192.168.", "10.", "172.16.");
        
        // 9. 添加带正则匹配的规则
        service.addRegexRule("PHONE_REGEX_WHITELIST", "手机号正则白名单", RuleType.PHONE,
                "138\\d{8}", "139\\d{8}");
        
        // 注册校验项
        // 校验项1：IP校验（关联IP白名单和IP段白名单）
        service.registerCheckItem("CHECK_IP", "IP地址校验", 
                CheckItem.MatchMode.ANY, "IP_WHITELIST", "IP_PREFIX_WHITELIST");
        
        // 校验项2：用户ID校验（关联用户ID白名单）
        service.registerCheckItem("CHECK_USER_ID", "用户ID校验", 
                "USER_ID_WHITELIST");
        
        // 校验项3：用户名校验（关联用户名白名单）
        service.registerCheckItem("CHECK_USERNAME", "用户名校验", 
                "USERNAME_WHITELIST");
        
        // 校验项4：设备校验（关联设备白名单）
        service.registerCheckItem("CHECK_DEVICE", "设备ID校验", 
                "DEVICE_WHITELIST");
        
        // 校验项5：手机号校验（关联手机号白名单和正则白名单）
        service.registerCheckItem("CHECK_PHONE", "手机号校验",
                CheckItem.MatchMode.ANY, "PHONE_WHITELIST", "PHONE_REGEX_WHITELIST");
        
        // 校验项6：API校验（关联API白名单）
        service.registerCheckItem("CHECK_API", "API接口校验",
                "API_WHITELIST");
        
        // 校验项7：业务编码校验（关联业务编码白名单）
        service.registerCheckItem("CHECK_BIZ_CODE", "业务编码校验",
                "BIZ_CODE_WHITELIST");
        
        // 校验项8：综合校验（需要同时满足多个规则）
        service.registerCheckItem("CHECK_COMPREHENSIVE", "综合校验",
                CheckItem.MatchMode.ALL, "IP_WHITELIST", "USER_ID_WHITELIST");
        
        System.out.println();
    }
    
    /**
     * 测试场景1：基础校验
     */
    private static void testBasicCheck(WhitelistService service) {
        printSection("场景1：基础校验 - IP白名单");
        
        // 通过的请求
        CheckContext context1 = new CheckContext()
                .setIp("192.168.1.100")
                .setUserId("1001");
        
        CheckResult result1 = service.check("CHECK_IP", context1);
        System.out.println("校验IP [192.168.1.100]: " + (result1.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        
        // 拒绝的请求
        CheckContext context2 = new CheckContext()
                .setIp("202.106.1.1")
                .setUserId("1001");
        
        CheckResult result2 = service.check("CHECK_IP", context2);
        System.out.println("校验IP [202.106.1.1]: " + (result2.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        System.out.println("  原因: " + result2.getErrorMessage());
        System.out.println();
    }
    
    /**
     * 测试场景2：多规则关联校验
     */
    private static void testMultiRuleCheck(WhitelistService service) {
        printSection("场景2：多规则关联校验");
        
        // 校验用户ID
        CheckContext context = new CheckContext()
                .setIp("192.168.1.100")
                .setUserId("1002")
                .setUsername("testuser");
        
        CheckResult result1 = service.check("CHECK_USER_ID", context);
        System.out.println("校验用户ID [1002]: " + (result1.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        
        CheckResult result2 = service.check("CHECK_USERNAME", context);
        System.out.println("校验用户名 [testuser]: " + (result2.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        System.out.println();
    }
    
    /**
     * 测试场景3：批量校验
     */
    private static void testBatchCheck(WhitelistService service) {
        printSection("场景3：批量校验");
        
        CheckContext context = new CheckContext()
                .setIp("192.168.1.100")
                .setUserId("1001")
                .setDeviceId("DEVICE_001")
                .setPhone("13800138000")
                .setApiCode("quota_query");
        
        java.util.List<String> checkItemIds = java.util.Arrays.asList(
                "CHECK_IP", "CHECK_USER_ID", "CHECK_DEVICE", "CHECK_PHONE", "CHECK_API"
        );
        
        System.out.println("发起批量校验，校验项: " + checkItemIds);
        
        java.util.List<CheckResult> results = service.checkBatch(checkItemIds, context);
        
        for (CheckResult result : results) {
            String status = result.isPassed() ? "✓" : "✗";
            System.out.println("  " + status + " " + result.getCheckItemId() + ": " + 
                    (result.isPassed() ? "通过" : result.getErrorMessage()));
        }
        System.out.println();
    }
    
    /**
     * 测试场景4：动态更新白名单
     */
    private static void testDynamicUpdate(WhitelistService service) {
        printSection("场景4：动态更新白名单");
        
        // 初始状态：IP不在白名单
        CheckContext context1 = new CheckContext().setIp("202.106.1.1");
        CheckResult result1 = service.check("CHECK_IP", context1);
        System.out.println("更新前 - 校验IP [202.106.1.1]: " + (result1.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        
        // 动态添加IP到白名单
        System.out.println("执行动态添加: updateWhitelist('IP_WHITELIST', '202.106.1.1')");
        service.updateWhitelist("IP_WHITELIST", "202.106.1.1");
        
        // 再次校验
        CheckResult result2 = service.check("CHECK_IP", context1);
        System.out.println("更新后 - 校验IP [202.106.1.1]: " + (result2.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        
        // 移除IP
        System.out.println("执行动态移除: removeFromWhitelist('IP_WHITELIST', '202.106.1.1')");
        service.removeFromWhitelist("IP_WHITELIST", "202.106.1.1");
        
        CheckResult result3 = service.check("CHECK_IP", context1);
        System.out.println("移除后 - 校验IP [202.106.1.1]: " + (result3.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        System.out.println();
    }
    
    /**
     * 测试场景5：前缀匹配规则
     */
    private static void testPrefixMatch(WhitelistService service) {
        printSection("场景5：前缀匹配规则");
        
        CheckContext context1 = new CheckContext().setIp("192.168.1.50");
        CheckResult result1 = service.check("CHECK_IP", context1);
        System.out.println("校验IP [192.168.1.50] (匹配192.168.): " + 
                (result1.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        
        CheckContext context2 = new CheckContext().setIp("172.16.0.1");
        CheckResult result2 = service.check("CHECK_IP", context2);
        System.out.println("校验IP [172.16.0.1] (匹配172.16.): " + 
                (result2.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        
        CheckContext context3 = new CheckContext().setIp("8.8.8.8");
        CheckResult result3 = service.check("CHECK_IP", context3);
        System.out.println("校验IP [8.8.8.8] (无匹配): " + 
                (result3.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        System.out.println();
    }
    
    /**
     * 测试场景6：正则匹配规则
     */
    private static void testRegexMatch(WhitelistService service) {
        printSection("场景6：正则匹配规则");
        
        CheckContext context1 = new CheckContext().setPhone("13812345678");
        CheckResult result1 = service.check("CHECK_PHONE", context1);
        System.out.println("校验手机号 [13812345678] (匹配138\\d{8}): " + 
                (result1.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        
        CheckContext context2 = new CheckContext().setPhone("15812345678");
        CheckResult result2 = service.check("CHECK_PHONE", context2);
        System.out.println("校验手机号 [15812345678] (不匹配): " + 
                (result2.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        
        CheckContext context3 = new CheckContext().setPhone("13800138000");
        CheckResult result3 = service.check("CHECK_PHONE", context3);
        System.out.println("校验手机号 [13800138000] (精确匹配): " + 
                (result3.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        System.out.println();
    }
    
    /**
     * 测试场景7：ALL匹配模式
     */
    private static void testAllMatchMode(WhitelistService service) {
        printSection("场景7：ALL匹配模式 - 综合校验");
        
        // 两个都在白名单 - 应该通过
        CheckContext context1 = new CheckContext()
                .setIp("192.168.1.100")  // 在IP白名单中
                .setUserId("1001");       // 在用户ID白名单中
        
        CheckResult result1 = service.check("CHECK_COMPREHENSIVE", context1);
        System.out.println("校验 (IP在白名单 + 用户ID在白名单): " + 
                (result1.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        
        // 只有一个在白名单 - 应该拒绝
        CheckContext context2 = new CheckContext()
                .setIp("192.168.1.100")  // 在IP白名单中
                .setUserId("9999");       // 不在用户ID白名单中
        
        CheckResult result2 = service.check("CHECK_COMPREHENSIVE", context2);
        System.out.println("校验 (IP在白名单 + 用户ID不在白名单): " + 
                (result2.isPassed() ? "✓ 通过" : "✗ 拒绝"));
        System.out.println("  原因: " + result2.getErrorMessage());
        System.out.println();
    }
    
    /**
     * 测试场景8：统计功能
     */
    private static void testStatistics(WhitelistService service) {
        printSection("场景8：统计功能");
        
        // 执行一些校验
        CheckContext context = new CheckContext().setIp("192.168.1.100");
        for (int i = 0; i < 5; i++) {
            service.check("CHECK_IP", context);
        }
        
        context = new CheckContext().setIp("202.106.1.1");
        for (int i = 0; i < 3; i++) {
            service.check("CHECK_IP", context);
        }
        
        System.out.println("CHECK_IP 统计:");
        System.out.println("  总校验次数: " + service.getCheckCount("CHECK_IP"));
        System.out.println("  拒绝次数: " + service.getRejectCount("CHECK_IP"));
        System.out.println("  通过率: " + 
                String.format("%.1f%%", (service.getCheckCount("CHECK_IP") - service.getRejectCount("CHECK_IP")) 
                * 100.0 / service.getCheckCount("CHECK_IP")));
        System.out.println();
    }
    
    /**
     * 测试场景9：变更监听
     */
    private static void testChangeListener(WhitelistService service) {
        printSection("场景9：变更监听");
        
        // 注册监听器
        service.addRuleChangeListener(event -> {
            System.out.println("  [监听器] 规则变更: " + event.getEventType() + 
                    " - " + event.getRule().getRuleId());
        });
        
        service.addCheckItemChangeListener(event -> {
            System.out.println("  [监听器] 校验项变更: " + event.getEventType() + 
                    " - " + event.getCheckItem().getCheckItemId());
        });
        
        // 触发变更
        System.out.println("执行操作: 添加新规则");
        service.addIpRule("NEW_IP_RULE", "新IP规则", "1.1.1.1");
        
        System.out.println("执行操作: 更新校验项");
        service.setCheckItemEnabled("CHECK_IP", false);
        
        System.out.println("执行操作: 启用校验项");
        service.setCheckItemEnabled("CHECK_IP", true);
        System.out.println();
    }
    
    /**
     * 打印场景标题
     */
    private static void printSection(String title) {
        System.out.println();
        System.out.println("══════════════════════════════════════════════════════════════════════════");
        System.out.println(title);
        System.out.println("══════════════════════════════════════════════════════════════════════════");
    }
}
