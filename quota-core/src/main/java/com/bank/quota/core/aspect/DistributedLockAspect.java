package com.bank.quota.core.aspect;

import com.bank.quota.core.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class DistributedLockAspect {
    
    private final RedissonClient redissonClient;
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    
    @Pointcut("@annotation(com.bank.quota.core.annotation.DistributedLock)")
    public void distributedLockPointcut() {}
    
    @Around("distributedLockPointcut() && @annotation(distributedLock)")
    public Object around(ProceedingJoinPoint point, DistributedLock distributedLock) throws Throwable {
        String lockKey = parseLockKey(point, distributedLock.key());
        String className = point.getTarget().getClass().getSimpleName();
        String methodName = point.getSignature().getName();
        
        log.debug("[分布式锁] 尝试获取锁 - Key: {}, 类: {}, 方法: {}", lockKey, className, methodName);
        
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), TimeUnit.MILLISECONDS);
        
        if (!acquired) {
            log.warn("[分布式锁] 获取锁失败 - Key: {}, 类: {}, 方法: {}", lockKey, className, methodName);
            throw new RuntimeException("获取分布式锁失败，请稍后重试");
        }
        
        log.debug("[分布式锁] 获取锁成功 - Key: {}, 类: {}, 方法: {}", lockKey, className, methodName);
        
        try {
            return point.proceed();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[分布式锁] 释放锁成功 - Key: {}, 类: {}, 方法: {}", lockKey, className, methodName);
            }
        }
    }
    
    private String parseLockKey(ProceedingJoinPoint point, String keyExpression) {
        if (keyExpression == null || keyExpression.isEmpty()) {
            return "quota:lock:" + point.getSignature().toLongString();
        }
        
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = point.getArgs();
        
        EvaluationContext context = new StandardEvaluationContext();
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        
        Expression expression = PARSER.parseExpression(keyExpression);
        Object value = expression.getValue(context);
        
        return "quota:lock:" + (value != null ? value.toString() : keyExpression);
    }
}
