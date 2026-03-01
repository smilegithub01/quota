package com.bank.check.chain.handler;

import com.bank.check.chain.ChainRequest;
import com.bank.check.chain.ChainResponse;
import com.bank.check.chain.CheckHandler;

/**
 * 风控校验处理器
 * 
 * 校验交易风险等级和风控规则
 */
public class RiskCheckHandler extends CheckHandler {

    private static final String ERROR_CODE = "RISK_CHECK_FAILED";
    private static final String DEFAULT_ERROR_MSG = "风控校验失败";

    private int maxRiskLevel = 3;
    private boolean enableRealTimeCheck = true;
    private java.util.Set<String> highRiskAreas = new java.util.HashSet<>();

    public RiskCheckHandler() {
        super("RISK_CHECK", "风控校验");
        this.order = 5;
        initHighRiskAreas();
    }

    private void initHighRiskAreas() {
        highRiskAreas.add("RU");
        highRiskAreas.add("IR");
        highRiskAreas.add("KP");
        highRiskAreas.add("SY");
    }

    @Override
    public boolean handle(ChainRequest request, ChainResponse response) {
        long startTime = System.currentTimeMillis();

        if (request.isSkipAllCheck() || request.isSkipRiskCheck()) {
            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTime));
            return continueChain(request, response);
        }

        if (!response.isPassed()) {
            return false;
        }

        try {
            int riskLevel = evaluateRiskLevel(request);
            
            if (riskLevel > maxRiskLevel) {
                String errorMsg = "风险等级过高，拒绝交易";
                response.addCheckDetail(ChainResponse.CheckDetail.fail(
                    checkItemId, checkItemName, ERROR_CODE, errorMsg,
                    System.currentTimeMillis() - startTime));
                response.setPassed(false);
                response.setFailedCheckItemId(checkItemId);
                response.setFailedCheckItemName(checkItemName);
                response.setErrorCode(ERROR_CODE);
                response.setErrorMessage(errorMsg);
                return false;
            }

            if (!checkRiskRules(request)) {
                String errorMsg = "触犯风控规则";
                response.addCheckDetail(ChainResponse.CheckDetail.fail(
                    checkItemId, checkItemName, ERROR_CODE, errorMsg,
                    System.currentTimeMillis() - startTime));
                response.setPassed(false);
                response.setFailedCheckItemId(checkItemId);
                response.setFailedCheckItemName(checkItemName);
                response.setErrorCode(ERROR_CODE);
                response.setErrorMessage(errorMsg);
                return false;
            }

            request.putExtData("riskLevel", riskLevel);
            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTime));

        } catch (Exception e) {
            response.addCheckDetail(ChainResponse.CheckDetail.fail(
                checkItemId, checkItemName, ERROR_CODE, "风控校验异常: " + e.getMessage(),
                System.currentTimeMillis() - startTime));
            response.setPassed(false);
            response.setErrorCode(ERROR_CODE);
            response.setErrorMessage("风控校验异常: " + e.getMessage());
            return false;
        }

        return continueChain(request, response);
    }

    private int evaluateRiskLevel(ChainRequest request) {
        int level = 0;

        Long amount = request.getAmount();
        if (amount != null) {
            if (amount > 100000) {
                level += 2;
            } else if (amount > 50000) {
                level += 1;
            }
        }

        String ip = request.getIp();
        if (ip != null && ip.startsWith("10.")) {
            level -= 1;
        }

        Object riskLevel = request.getExtData("externalRiskLevel");
        if (riskLevel != null) {
            try {
                level += Integer.parseInt(riskLevel.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        return Math.max(0, Math.min(5, level));
    }

    private boolean checkRiskRules(ChainRequest request) {
        return true;
    }

    public RiskCheckHandler setMaxRiskLevel(int maxRiskLevel) {
        this.maxRiskLevel = maxRiskLevel;
        return this;
    }

    public RiskCheckHandler setEnableRealTimeCheck(boolean enableRealTimeCheck) {
        this.enableRealTimeCheck = enableRealTimeCheck;
        return this;
    }

    public RiskCheckHandler addHighRiskArea(String areaCode) {
        this.highRiskAreas.add(areaCode);
        return this;
    }
}
