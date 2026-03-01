package com.bank.quota.core.aspect;

import com.bank.quota.core.annotation.OperationLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {
    
    @Pointcut("@annotation(com.bank.quota.core.annotation.OperationLog)")
    public void operationLogPointcut() {}
    
    @Around("operationLogPointcut() && @annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = point.getTarget().getClass().getSimpleName();
        String methodName = point.getSignature().getName();
        
        log.info("[操作日志] 开始执行 - 模块: {}, 操作: {}, 类: {}, 方法: {}",
                operationLog.module(), operationLog.operation(), className, methodName);
        
        try {
            Object result = point.proceed();
            long elapsedTime = System.currentTimeMillis() - startTime;
            
            log.info("[操作日志] 执行成功 - 模块: {}, 操作: {}, 耗时: {}ms",
                    operationLog.module(), operationLog.operation(), elapsedTime);
            
            return result;
        } catch (Throwable e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error("[操作日志] 执行失败 - 模块: {}, 操作: {}, 耗时: {}ms, 异常: {}",
                    operationLog.module(), operationLog.operation(), elapsedTime, e.getMessage());
            throw e;
        }
    }
}
