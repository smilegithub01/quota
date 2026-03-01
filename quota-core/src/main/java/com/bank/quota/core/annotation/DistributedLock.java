package com.bank.quota.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    
    String key() default "";
    
    long waitTime() default 3000;
    
    long leaseTime() default 5000;
}
