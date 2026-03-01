package com.bank.check.chain;

import com.bank.check.chain.handler.*;
import com.bank.check.chain.service.CheckChainService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 校验责任链功能测试
 */
public class CheckChainTest {

    public static void main(String[] args) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   校验责任链系统 - 功能演示                             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        CheckChainService service = new CheckChainService();

        testBasicCheck(service);

        testSkipCheck(service);

        testCustomChain(service);

        testPenetrateCheck(service);

        System.out.println();
        System.out.println("══════════════════════════════════════════════════════════════════════════");
        System.out.println("测试完成！");
        System.out.println("══════════════════════════════════════════════════════════════════════════");
    }

    private static void testBasicCheck(CheckChainService service) {
        System.out.println("【测试1】基础校验 - 正常流程");
        System.out.println("──────────────────────────────────────────────────────────────────────────");

        CheckChain chain = CheckChain.createDefaultChain();

        ChainRequest request = ChainRequest.create(UUID.randomUUID().toString(), "TRANSFER")
                .withParams(createParams(1000L))
                .withUser("1001", "张三")
                .withIp("192.168.1.100")
                .withAmount(1000L);

        ChainResponse response = chain.execute(request);

        System.out.println("校验结果: " + (response.isPassed() ? "通过" : "拒绝"));
        System.out.println("错误码: " + response.getErrorCode());
        System.out.println("错误信息: " + response.getErrorMessage());
        System.out.println("耗时: " + response.getTotalDurationMs() + "ms");
        System.out.println("校验详情: ");
        for (ChainResponse.CheckDetail detail : response.getCheckDetails()) {
            System.out.println("  - " + detail.getCheckItemName() + ": " + 
                (detail.isPassed() ? "通过" : "拒绝(" + detail.getErrorMessage() + ")"));
        }
        System.out.println();
    }

    private static void testSkipCheck(CheckChainService service) {
        System.out.println("【测试2】跳过校验 - 突破特定校验");
        System.out.println("──────────────────────────────────────────────────────────────────────────");

        CheckChain chain = CheckChain.createDefaultChain();

        ChainRequest request = ChainRequest.create(UUID.randomUUID().toString(), "TRANSFER")
                .withParams(createParams(1000L))
                .withUser("1001", "张三")
                .withIp("192.168.1.100")
                .withAmount(1000L)
                .skipRiskCheck()
                .skipFrequencyCheck();

        ChainResponse response = chain.execute(request);

        System.out.println("校验结果: " + (response.isPassed() ? "通过" : "拒绝"));
        System.out.println("跳过的校验: 风控校验、频率校验");
        System.out.println("校验详情: ");
        for (ChainResponse.CheckDetail detail : response.getCheckDetails()) {
            System.out.println("  - " + detail.getCheckItemName() + ": " + 
                (detail.isPassed() ? "通过" : "拒绝(" + detail.getErrorMessage() + ")"));
        }
        System.out.println();
    }

    private static void testCustomChain(CheckChainService service) {
        System.out.println("【测试3】自定义校验链");
        System.out.println("──────────────────────────────────────────────────────────────────────────");

        CheckChain chain = new CheckChain("CUSTOM_CHAIN");
        chain.addHandler(new ParamCheckHandler())
             .addHandler(new AuthCheckHandler())
             .addHandler(new AmountCheckHandler(100L, 50000L));

        ChainRequest request = ChainRequest.create(UUID.randomUUID().toString(), "TRANSFER")
                .withParams(createParams(1000L))
                .withUser("1001", "张三")
                .withAmount(60000L);

        ChainResponse response = chain.execute(request);

        System.out.println("校验结果: " + (response.isPassed() ? "通过" : "拒绝"));
        System.out.println("错误信息: " + response.getErrorMessage());
        System.out.println("校验详情: ");
        for (ChainResponse.CheckDetail detail : response.getCheckDetails()) {
            System.out.println("  - " + detail.getCheckItemName() + ": " + 
                (detail.isPassed() ? "通过" : "拒绝(" + detail.getErrorMessage() + ")"));
        }
        System.out.println();
    }

    private static void testPenetrateCheck(CheckChainService service) {
        System.out.println("【测试4】突破全部校验 - 管理员特殊权限");
        System.out.println("──────────────────────────────────────────────────────────────────────────");

        CheckChain chain = CheckChain.createDefaultChain();

        ChainRequest request = ChainRequest.create(UUID.randomUUID().toString(), "TRANSFER")
                .withParams(createParams(1000L))
                .withUser("admin", "管理员")
                .withIp("192.168.1.1")
                .withAmount(1000L)
                .skipAllCheck();

        ChainResponse response = chain.execute(request);

        System.out.println("校验结果: " + (response.isPassed() ? "通过" : "拒绝"));
        System.out.println("突破方式: 跳过全部校验");
        System.out.println("校验详情: ");
        for (ChainResponse.CheckDetail detail : response.getCheckDetails()) {
            System.out.println("  - " + detail.getCheckItemName() + ": " + 
                (detail.isPassed() ? "通过(已跳过)" : "拒绝(" + detail.getErrorMessage() + ")"));
        }
        System.out.println();
    }

    private static Map<String, Object> createParams(Long amount) {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currency", "CNY");
        params.put("remark", "测试转账");
        return params;
    }
}
