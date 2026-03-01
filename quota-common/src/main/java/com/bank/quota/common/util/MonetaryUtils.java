package com.bank.quota.common.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * 金融货币计算工具类
 * 
 * <p>提供银行级金融计算的核心工具方法，确保金额计算的精确性和一致性。
 * 所有金额计算均使用{@link BigDecimal}类型，避免浮点数精度问题。</p>
 * 
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>精度控制：默认使用4位小数精度（SCALE=4）</li>
 *   <li>舍入模式：采用银行家舍入法（HALF_EVEN），符合金融行业标准</li>
 *   <li>空值安全：部分方法支持null值处理，避免NullPointerException</li>
 *   <li>不可变性：所有计算方法返回新的BigDecimal实例</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 加法运算
 * BigDecimal sum = MonetaryUtils.add(amount1, amount2);
 * 
 * // 计算使用率
 * BigDecimal usageRate = MonetaryUtils.usageRate(usedAmount, totalAmount);
 * 
 * // 比较金额大小
 * boolean isGreater = MonetaryUtils.isGreaterThan(amount1, amount2);
 * 
 * // 格式化货币显示
 * String formatted = MonetaryUtils.formatCurrency(amount, "CNY");
 * }</pre>
 * 
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>金额比较应使用本类提供的比较方法，而非BigDecimal的equals方法</li>
 *   <li>除法运算时需确保除数不为零，否则抛出IllegalArgumentException</li>
 *   <li>所有计算结果均会按照SCALE进行精度设置</li>
 * </ul>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-10
 * @see BigDecimal
 * @see RoundingMode
 */
@Slf4j
public final class MonetaryUtils {
    
    /**
     * 默认计算精度（小数位数）
     * <p>金融计算推荐使用4位小数精度，确保计算过程中的精度损失最小化</p>
     */
    public static final int SCALE = 4;
    
    /**
     * 默认舍入模式：银行家舍入法（四舍六入五成双）
     * <p>银行家舍入法是金融行业标准舍入方式，能够更好地平衡舍入误差</p>
     */
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /**
     * 私有构造函数，防止实例化
     * <p>本类为工具类，所有方法均为静态方法，不应被实例化</p>
     */
    private MonetaryUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 金额加法运算
     * 
     * <p>对两个金额进行加法运算，支持null值处理。如果任一参数为null，
     * 则返回另一个参数的值；如果两个参数都为null，则返回null。</p>
     * 
     * <h4>使用示例：</h4>
     * <pre>{@code
     * BigDecimal result1 = MonetaryUtils.add(new BigDecimal("100.50"), new BigDecimal("200.30"));
     * // 结果: 300.8000
     * 
     * BigDecimal result2 = MonetaryUtils.add(new BigDecimal("100.50"), null);
     * // 结果: 100.5000
     * 
     * BigDecimal result3 = MonetaryUtils.add(null, null);
     * // 结果: null
     * }</pre>
     * 
     * @param a 第一个加数，可为null
     * @param b 第二个加数，可为null
     * @return 加法结果，精度为SCALE；如果两个参数都为null则返回null
     */
    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.add(b).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 金额减法运算
     * 
     * <p>对两个金额进行减法运算（a - b），不支持null值。
     * 减法运算要求两个参数都不能为null，否则抛出异常。</p>
     * 
     * <h4>使用示例：</h4>
     * <pre>{@code
     * BigDecimal result = MonetaryUtils.subtract(new BigDecimal("200.50"), new BigDecimal("100.30"));
     * // 结果: 100.2000
     * }</pre>
     * 
     * @param a 被减数，不可为null
     * @param b 减数，不可为null
     * @return 减法结果，精度为SCALE
     * @throws IllegalArgumentException 如果任一参数为null
     */
    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Operands cannot be null for subtraction");
        }
        return a.subtract(b).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 金额乘法运算
     * 
     * <p>对两个金额进行乘法运算，不支持null值。
     * 常用于计算利息、手续费等场景。</p>
     * 
     * <h4>使用示例：</h4>
     * <pre>{@code
     * // 计算利息：本金 × 利率
     * BigDecimal interest = MonetaryUtils.multiply(principal, rate);
     * 
     * // 计算手续费：金额 × 费率
     * BigDecimal fee = MonetaryUtils.multiply(amount, feeRate);
     * }</pre>
     * 
     * @param a 第一个乘数，不可为null
     * @param b 第二个乘数，不可为null
     * @return 乘法结果，精度为SCALE
     * @throws IllegalArgumentException 如果任一参数为null
     */
    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Operands cannot be null for multiplication");
        }
        return a.multiply(b).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 金额除法运算
     * 
     * <p>对两个金额进行除法运算（a / b），不支持null值和零除数。
     * 除法运算会自动进行舍入处理，避免无限小数问题。</p>
     * 
     * <h4>使用示例：</h4>
     * <pre>{@code
     * // 计算平均金额
     * BigDecimal average = MonetaryUtils.divide(totalAmount, count);
     * 
     * // 计算比例
     * BigDecimal ratio = MonetaryUtils.divide(partAmount, totalAmount);
     * }</pre>
     * 
     * @param a 被除数，不可为null
     * @param b 除数，不可为null或零
     * @return 除法结果，精度为SCALE
     * @throws IllegalArgumentException 如果任一参数为null或除数为零
     */
    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (a == null || b == null || b.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Invalid operands for division");
        }
        return a.divide(b, SCALE, ROUNDING_MODE);
    }

    /**
     * 计算百分比金额
     * 
     * <p>根据给定的金额和百分比计算对应的金额值。
     * 百分比以数值形式传入，如12.5表示12.5%。</p>
     * 
     * <h4>使用示例：</h4>
     * <pre>{@code
     * // 计算12.5%的金额
     * BigDecimal result = MonetaryUtils.percentage(new BigDecimal("1000"), new BigDecimal("12.5"));
     * // 结果: 125.0000
     * 
     * // 计算利息（年利率4.5%）
     * BigDecimal interest = MonetaryUtils.percentage(principal, new BigDecimal("4.5"));
     * }</pre>
     * 
     * @param amount 基础金额，不可为null
     * @param rate 百分比数值（如12.5表示12.5%），不可为null
     * @return 计算后的金额，精度为SCALE
     * @throws IllegalArgumentException 如果任一参数为null
     */
    public static BigDecimal percentage(BigDecimal amount, BigDecimal rate) {
        if (amount == null || rate == null) {
            throw new IllegalArgumentException("Amount and rate cannot be null");
        }
        return amount.multiply(rate).divide(BigDecimal.valueOf(100), SCALE, ROUNDING_MODE);
    }

    /**
     * 计算额度使用率
     * 
     * <p>计算已使用额度占总额度的百分比，结果保留2位小数。
     * 这是银行额度管理中的核心计算方法，用于监控额度使用情况。</p>
     * 
     * <h4>使用示例：</h4>
     * <pre>{@code
     * // 计算使用率
     * BigDecimal usageRate = MonetaryUtils.usageRate(
     *     new BigDecimal("750000"),  // 已使用额度
     *     new BigDecimal("1000000")  // 总额度
     * );
     * // 结果: 75.00 (表示75%)
     * 
     * // 判断是否超过预警阈值
     * if (MonetaryUtils.isGreaterThan(usageRate, new BigDecimal("80"))) {
     *     // 触发预警
     * }
     * }</pre>
     * 
     * @param used 已使用额度，可为null（视为零）
     * @param total 总额度，不可为null或零
     * @return 使用率百分比（0-100），精度为2位小数
     * @throws IllegalArgumentException 如果total为null或零
     */
    public static BigDecimal usageRate(BigDecimal used, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Total cannot be null or zero");
        }
        if (used == null) {
            used = BigDecimal.ZERO;
        }
        return used.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取两个金额中的较小值
     * 
     * <p>支持null值处理，如果任一参数为null，则返回另一个参数的值。</p>
     * 
     * @param a 第一个金额，可为null
     * @param b 第二个金额，可为null
     * @return 较小的金额值；如果两个参数都为null则返回null
     */
    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) <= 0 ? a : b;
    }

    /**
     * 获取两个金额中的较大值
     * 
     * <p>支持null值处理，如果任一参数为null，则返回另一个参数的值。</p>
     * 
     * @param a 第一个金额，可为null
     * @param b 第二个金额，可为null
     * @return 较大的金额值；如果两个参数都为null则返回null
     */
    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) >= 0 ? a : b;
    }

    /**
     * 判断金额是否为正数
     * 
     * @param amount 待判断的金额
     * @return 如果金额大于零返回true，否则返回false（包括null情况）
     */
    public static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断金额是否为负数
     * 
     * @param amount 待判断的金额
     * @return 如果金额小于零返回true，否则返回false（包括null情况）
     */
    public static boolean isNegative(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 判断金额是否为零或null
     * 
     * <p>此方法将null值视为零，常用于金额非空校验场景。</p>
     * 
     * @param amount 待判断的金额
     * @return 如果金额为零或null返回true，否则返回false
     */
    public static boolean isZero(BigDecimal amount) {
        return amount == null || amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 判断金额a是否大于金额b
     * 
     * <p>使用compareTo方法进行比较，避免equals方法的精度问题。
     * 两个参数都不能为null。</p>
     * 
     * @param a 第一个金额，不可为null
     * @param b 第二个金额，不可为null
     * @return 如果a > b返回true，否则返回false
     */
    public static boolean isGreaterThan(BigDecimal a, BigDecimal b) {
        return a != null && b != null && a.compareTo(b) > 0;
    }

    /**
     * 判断金额a是否小于金额b
     * 
     * @param a 第一个金额，不可为null
     * @param b 第二个金额，不可为null
     * @return 如果a < b返回true，否则返回false
     */
    public static boolean isLessThan(BigDecimal a, BigDecimal b) {
        return a != null && b != null && a.compareTo(b) < 0;
    }

    /**
     * 判断金额a是否大于或等于金额b
     * 
     * @param a 第一个金额，不可为null
     * @param b 第二个金额，不可为null
     * @return 如果a >= b返回true，否则返回false
     */
    public static boolean isGreaterThanOrEqual(BigDecimal a, BigDecimal b) {
        return a != null && b != null && a.compareTo(b) >= 0;
    }

    /**
     * 判断金额a是否小于或等于金额b
     * 
     * @param a 第一个金额，不可为null
     * @param b 第二个金额，不可为null
     * @return 如果a <= b返回true，否则返回false
     */
    public static boolean isLessThanOrEqual(BigDecimal a, BigDecimal b) {
        return a != null && b != null && a.compareTo(b) <= 0;
    }

    /**
     * 判断金额是否在指定范围内
     * 
     * <p>判断value是否满足：min <= value <= max</p>
     * 
     * @param value 待判断的金额
     * @param min 范围下限（包含）
     * @param max 范围上限（包含）
     * @return 如果在范围内返回true，否则返回false
     */
    public static boolean isBetween(BigDecimal value, BigDecimal min, BigDecimal max) {
        return isGreaterThanOrEqual(value, min) && isLessThanOrEqual(value, max);
    }

    /**
     * 将字符串转换为BigDecimal
     * 
     * <p>支持带逗号的数字格式（如"1,000,000.50"）。
     * 如果字符串为空或null，返回零值。</p>
     * 
     * @param value 数字字符串
     * @return BigDecimal值，精度为SCALE
     * @throws IllegalArgumentException 如果字符串格式无效
     */
    public static BigDecimal of(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        }
        try {
            return new BigDecimal(value.replace(",", "")).setScale(SCALE, ROUNDING_MODE);
        } catch (NumberFormatException e) {
            log.error("Failed to parse BigDecimal from value: {}", value, e);
            throw new IllegalArgumentException("Invalid BigDecimal value: " + value);
        }
    }

    /**
     * 将Long值转换为BigDecimal
     * 
     * @param value Long值
     * @return BigDecimal值，精度为SCALE
     */
    public static BigDecimal of(Long value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 将Double值转换为BigDecimal
     * 
     * <p>注意：由于浮点数精度问题，建议优先使用字符串或Long构造BigDecimal</p>
     * 
     * @param value Double值
     * @return BigDecimal值，精度为SCALE
     */
    public static BigDecimal of(Double value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 格式化金额为字符串
     * 
     * <p>使用千分位分隔符格式化金额，保留4位小数。</p>
     * 
     * @param amount 金额值
     * @return 格式化后的字符串，如"1,000,000.5000"；如果amount为null返回"0.0000"
     */
    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "0.0000";
        }
        return String.format("%,.4f", amount);
    }

    /**
     * 格式化金额为货币字符串
     * 
     * <p>根据货币代码格式化金额显示，保留2位小数。</p>
     * 
     * @param amount 金额值
     * @param currencyCode 货币代码（如"CNY"、"USD"）
     * @return 格式化后的货币字符串，如"¥1,000,000.50"
     */
    public static String formatCurrency(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return "0.00 " + currencyCode;
        }
        try {
            Currency currency = Currency.getInstance(currencyCode);
            java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance();
            format.setCurrency(currency);
            return format.format(amount.setScale(2, RoundingMode.HALF_UP));
        } catch (Exception e) {
            return String.format("%,.2f %s", amount, currencyCode);
        }
    }

    /**
     * 计算金额的绝对值
     * 
     * @param amount 金额值
     * @return 绝对值，精度为SCALE；如果amount为null返回零
     */
    public static BigDecimal abs(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.abs().setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 计算金额的相反数
     * 
     * @param amount 金额值
     * @return 相反数，精度为SCALE；如果amount为null返回零
     */
    public static BigDecimal negate(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.negate().setScale(SCALE, ROUNDING_MODE);
    }
}
