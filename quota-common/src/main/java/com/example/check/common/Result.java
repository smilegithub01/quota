package com.example.check.common;

/**
 * 统一返回结果封装类
 *
 * 用于标准化接口返回值，提供成功/失败的统一格式
 *
 * 字段说明：
 *   - success: 是否成功
 *   - msg: 错误信息（失败时）
 *   - data: 返回数据（成功时）
 *
 * 使用示例：
 *   // 成功返回
 *   return Result.ok();
 *   return Result.ok(data);
 *
 *   // 失败返回
 *   return Result.fail("错误信息");
 *
 * @param <T> 数据类型
 */
public class Result<T> {

    private boolean success;

    private String msg;

    private T data;

    /**
     * 创建成功结果（无数据）
     *
     * @param <T> 类型参数
     * @return 成功结果对象
     */
    public static <T> Result<T> ok() {
        Result<T> r = new Result<>();
        r.success = true;
        return r;
    }

    /**
     * 创建成功结果（带数据）
     *
     * @param data 返回数据
     * @param <T> 类型参数
     * @return 成功结果对象
     */
    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.success = true;
        r.data = data;
        return r;
    }

    /**
     * 创建失败结果
     *
     * @param msg 错误信息
     * @param <T> 类型参数
     * @return 失败结果对象
     */
    public static <T> Result<T> fail(String msg) {
        Result<T> r = new Result<>();
        r.success = false;
        r.msg = msg;
        return r;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
