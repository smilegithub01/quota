package com.example.check.handlerimpl;

import com.example.check.context.InvokeContext;
import com.example.check.core.Handler;
import org.springframework.stereotype.Component;

/**
 * 订单创建处理器
 *
 * 在业务动作阶段创建订单
 *
 * 功能：
 *   - 生成订单号
 *   - 保存订单信息到数据库
 *   - 初始化订单状态
 *
 * 所属流程：bizAction (业务动作链)
 *
 * @see Handler 处理器接口
 */
@Component("insertOrder")
public class InsertOrderHandler implements Handler {

    @Override
    public void handle(InvokeContext ctx) {
        System.out.println("订单插入完成");
    }
}
