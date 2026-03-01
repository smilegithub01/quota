package com.bank.quota.common.util;

import com.bank.quota.common.util.MonetaryUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MonetaryUtilsTest {
    
    @Test
    @DisplayName("金额加法测试")
    void testAdd() {
        BigDecimal a = new BigDecimal("100.0000");
        BigDecimal b = new BigDecimal("50.0000");
        BigDecimal result = MonetaryUtils.add(a, b);
        assertEquals(new BigDecimal("150.0000"), result);
    }
    
    @Test
    @DisplayName("金额减法测试")
    void testSubtract() {
        BigDecimal a = new BigDecimal("100.0000");
        BigDecimal b = new BigDecimal("30.0000");
        BigDecimal result = MonetaryUtils.subtract(a, b);
        assertEquals(new BigDecimal("70.0000"), result);
    }
    
    @Test
    @DisplayName("金额乘法测试")
    void testMultiply() {
        BigDecimal a = new BigDecimal("100.0000");
        BigDecimal b = new BigDecimal("2.5");
        BigDecimal result = MonetaryUtils.multiply(a, b);
        assertEquals(new BigDecimal("250.0000"), result);
    }
    
    @Test
    @DisplayName("金额除法测试")
    void testDivide() {
        BigDecimal a = new BigDecimal("100.0000");
        BigDecimal b = new BigDecimal("4.0");
        BigDecimal result = MonetaryUtils.divide(a, b);
        assertEquals(new BigDecimal("25.0000"), result);
    }
    
    @Test
    @DisplayName("百分比计算测试")
    void testPercentage() {
        BigDecimal amount = new BigDecimal("1000.0000");
        BigDecimal rate = new BigDecimal("10.0");
        BigDecimal result = MonetaryUtils.percentage(amount, rate);
        assertEquals(new BigDecimal("100.0000"), result);
    }
    
    @Test
    @DisplayName("使用率计算测试")
    void testUsageRate() {
        BigDecimal used = new BigDecimal("500.0000");
        BigDecimal total = new BigDecimal("1000.0000");
        BigDecimal result = MonetaryUtils.usageRate(used, total);
        assertEquals(new BigDecimal("50.00"), result);
    }
    
    @Test
    @DisplayName("金额是否为正测试")
    void testIsPositive() {
        assertTrue(MonetaryUtils.isPositive(new BigDecimal("100.0000")));
        assertFalse(MonetaryUtils.isPositive(BigDecimal.ZERO));
        assertFalse(MonetaryUtils.isPositive(new BigDecimal("-100.0000")));
    }
    
    @Test
    @DisplayName("金额是否为负测试")
    void testIsNegative() {
        assertTrue(MonetaryUtils.isNegative(new BigDecimal("-100.0000")));
        assertFalse(MonetaryUtils.isNegative(BigDecimal.ZERO));
        assertFalse(MonetaryUtils.isNegative(new BigDecimal("100.0000")));
    }
    
    @Test
    @DisplayName("金额是否为零测试")
    void testIsZero() {
        assertTrue(MonetaryUtils.isZero(BigDecimal.ZERO));
        assertTrue(MonetaryUtils.isZero(null));
        assertFalse(MonetaryUtils.isZero(new BigDecimal("100.0000")));
    }
    
    @Test
    @DisplayName("金额比较测试")
    void testComparison() {
        BigDecimal a = new BigDecimal("100.0000");
        BigDecimal b = new BigDecimal("50.0000");
        
        assertTrue(MonetaryUtils.isGreaterThan(a, b));
        assertTrue(MonetaryUtils.isLessThan(b, a));
        assertTrue(MonetaryUtils.isGreaterThanOrEqual(a, b));
        assertTrue(MonetaryUtils.isLessThanOrEqual(b, a));
    }
    
    @Test
    @DisplayName("范围判断测试")
    void testIsBetween() {
        BigDecimal value = new BigDecimal("50.0000");
        BigDecimal min = new BigDecimal("10.0000");
        BigDecimal max = new BigDecimal("100.0000");
        
        assertTrue(MonetaryUtils.isBetween(value, min, max));
        assertFalse(MonetaryUtils.isBetween(new BigDecimal("150.0000"), min, max));
        assertFalse(MonetaryUtils.isBetween(new BigDecimal("5.0000"), min, max));
    }
    
    @Test
    @DisplayName("绝对值测试")
    void testAbs() {
        assertEquals(new BigDecimal("100.0000"), MonetaryUtils.abs(new BigDecimal("100.0000")));
        assertEquals(new BigDecimal("100.0000"), MonetaryUtils.abs(new BigDecimal("-100.0000")));
    }
    
    @Test
    @DisplayName("取反测试")
    void testNegate() {
        assertEquals(new BigDecimal("-100.0000"), MonetaryUtils.negate(new BigDecimal("100.0000")));
        assertEquals(new BigDecimal("100.0000"), MonetaryUtils.negate(new BigDecimal("-100.0000")));
    }
    
    @Test
    @DisplayName("金额格式化测试")
    void testFormat() {
        String result = MonetaryUtils.format(new BigDecimal("1234567.8900"));
        assertTrue(result.contains("1,234,567.8900"));
    }
    
    @Test
    @DisplayName("字符串转金额测试")
    void testOfString() {
        assertEquals(new BigDecimal("1234.0000"), MonetaryUtils.of("1,234"));
        assertEquals(new BigDecimal("0.0000"), MonetaryUtils.of(""));
    }
    
    @Test
    @DisplayName("最小值测试")
    void testMin() {
        BigDecimal a = new BigDecimal("100.0000");
        BigDecimal b = new BigDecimal("50.0000");
        assertEquals(b, MonetaryUtils.min(a, b));
    }
    
    @Test
    @DisplayName("最大值测试")
    void testMax() {
        BigDecimal a = new BigDecimal("100.0000");
        BigDecimal b = new BigDecimal("50.0000");
        assertEquals(a, MonetaryUtils.max(a, b));
    }
}
