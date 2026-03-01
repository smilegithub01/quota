package com.bank.check.chain.handler;

import com.bank.check.chain.ChainRequest;
import com.bank.check.chain.ChainResponse;
import com.bank.check.chain.CheckHandler;

/**
 * 权限校验处理器
 * 
 * 校验用户权限和角色
 */
public class AuthCheckHandler extends CheckHandler {

    private static final String ERROR_CODE = "AUTH_CHECK_FAILED";
    private static final String DEFAULT_ERROR_MSG = "权限校验失败";

    private static final java.util.Set<String> ADMIN_ROLES = new java.util.HashSet<>();
    static {
        ADMIN_ROLES.add("ADMIN");
        ADMIN_ROLES.add("SUPER_ADMIN");
        ADMIN_ROLES.add("SYSTEM_ADMIN");
    }

    private static final java.util.Set<String> NORMAL_ROLES = new java.util.HashSet<>();
    static {
        NORMAL_ROLES.add("USER");
        NORMAL_ROLES.add("CUSTOMER");
        NORMAL_ROLES.add("OPERATOR");
    }

    private String requiredRole;
    private boolean checkIp = true;

    public AuthCheckHandler() {
        super("AUTH_CHECK", "权限校验");
        this.order = 2;
    }

    public AuthCheckHandler(String requiredRole) {
        super("AUTH_CHECK", "权限校验");
        this.requiredRole = requiredRole;
        this.order = 2;
    }

    @Override
    public boolean handle(ChainRequest request, ChainResponse response) {
        long startTime = System.currentTimeMillis();

        if (request.isSkipAllCheck() || request.isSkipAuthCheck()) {
            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTime));
            return continueChain(request, response);
        }

        if (!response.isPassed()) {
            return false;
        }

        try {
            if (!validateAuth(request)) {
                String errorMsg = getAuthErrorMessage(request);
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

            response.addCheckDetail(ChainResponse.CheckDetail.pass(
                checkItemId, checkItemName, System.currentTimeMillis() - startTime));

        } catch (Exception e) {
            response.addCheckDetail(ChainResponse.CheckDetail.fail(
                checkItemId, checkItemName, ERROR_CODE, "权限校验异常: " + e.getMessage(),
                System.currentTimeMillis() - startTime));
            response.setPassed(false);
            response.setErrorCode(ERROR_CODE);
            response.setErrorMessage("权限校验异常: " + e.getMessage());
            return false;
        }

        return continueChain(request, response);
    }

    private boolean validateAuth(ChainRequest request) {
        String userId = request.getUserId();
        if (userId == null || userId.isEmpty()) {
            return false;
        }

        String username = request.getUsername();
        if (username == null || username.isEmpty()) {
            return false;
        }

        if (requiredRole != null && !requiredRole.isEmpty()) {
            return ADMIN_ROLES.contains(requiredRole.toUpperCase());
        }

        if (checkIp) {
            String ip = request.getIp();
            if (ip == null || ip.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private String getAuthErrorMessage(ChainRequest request) {
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            return "用户ID不能为空";
        }
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return "用户名不能为空";
        }
        if (checkIp && (request.getIp() == null || request.getIp().isEmpty())) {
            return "IP地址不能为空";
        }
        if (requiredRole != null && !requiredRole.isEmpty()) {
            return "当前用户角色[" + requiredRole + "]无权限执行此操作";
        }
        return DEFAULT_ERROR_MSG;
    }

    public AuthCheckHandler setRequiredRole(String requiredRole) {
        this.requiredRole = requiredRole;
        return this;
    }

    public AuthCheckHandler setCheckIp(boolean checkIp) {
        this.checkIp = checkIp;
        return this;
    }
}
