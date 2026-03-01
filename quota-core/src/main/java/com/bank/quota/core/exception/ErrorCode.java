package com.bank.quota.core.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS("0000", "操作成功"),
    SYSTEM_ERROR("9999", "系统异常"),
    PARAM_ERROR("1001", "参数错误"),
    QUOTA_NOT_FOUND("2001", "额度记录不存在"),
    QUOTA_INSUFFICIENT("2002", "额度不足"),
    QUOTA_FROZEN("2003", "额度已冻结"),
    QUOTA_OCCUPY_FAILED("2004", "额度预占失败"),
    QUOTA_RELEASE_FAILED("2005", "额度释放失败"),
    LOCK_ACQUIRE_FAILED("2006", "获取分布式锁失败"),
    LOCK_NOT_FOUND("2007", "锁定记录不存在"),
    LOCK_ERROR("2008", "锁定操作失败"),
    TRANSACTION_NOT_FOUND("2009", "事务不存在"),
    CONTRACT_NOT_FOUND("2010", "合同不存在"),
    CUSTOMER_NOT_FOUND("2011", "客户不存在"),
    GROUP_NOT_FOUND("2012", "集团不存在"),
    APPROVAL_NOT_FOUND("2013", "批复不存在"),
    CONTRACT_OCCUPY_ERROR("2014", "合同占用错误"),
    DUPLICATE_REQUEST("2015", "重复请求"),
    BUSINESS_RULE_VIOLATION("2016", "业务规则校验失败"),
    DATA_INCONSISTENCY("2017", "数据不一致"),
    CONCURRENT_MODIFICATION("2018", "并发修改冲突"),
    QUOTA_MULTI_OCCUPY_ERROR("2019", "额度多重占用错误"),
    DATA_NOT_FOUND("2020", "数据不存在"),
    DATA_ALREADY_EXISTS("2021", "数据已存在");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
