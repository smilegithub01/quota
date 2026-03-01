package com.bank.quota.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QuotaOperation {
    
    String operationType();
    
    String quotaType() default "CUSTOMER";
    
    boolean recordAudit() default true;
}
