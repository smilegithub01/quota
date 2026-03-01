package com.bank.quota.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    String operation() default "";
    
    String module() default "";
    
    String description() default "";
}
