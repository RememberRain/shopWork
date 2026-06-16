package com.fast.homestay.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
 
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.fast.homestay.mapper.OrderMapper;
import com.fast.homestay.domain.Order;
import com.fast.homestay.service.OrderService;

/**
 * @author LJH
 * @version 2.0
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderMapper orderMapper;

    /**
     * 1. 用户支付成功
     */
    @Override
    public int payOrder(String orderId) {
        // 💡 修正：接口调用变更为 selectOrderByOrderId
        Order order = orderMapper.selectOrderByOrderId(orderId);
        // 💡 修正：状态使用字符串进行比对与赋值，单引号改为双引号
        if (order != null && "待支付".equals(order.getStatus())) {
            order.setStatus("未发货"); // 支付成功后变为未发货状态
            order.setPaymentTime(new Date());
            return orderMapper.updateOrder(order);
        }
        return 0;
    }

    /**
     * 2. 商家执行发货
     */
    @Override
    public int deliverOrder(String orderId) {
        Order order = orderMapper.selectOrderByOrderId(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        //
        if (!"未发货".equals(order.getStatus())) {
            throw new RuntimeException("当前订单状态不是【未发货】，无法执行发货操作！");
        }

        order.setStatus("已发货");
        order.setDeliveryTime(new Date());
        return orderMapper.updateOrder(order);
    }

    /**
     * 3. 用户确认收货
     */
    @Override
    public int receiveOrder(String orderId) {
        Order order = orderMapper.selectOrderByOrderId(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        //
        if (!"已发货".equals(order.getStatus())) {
            throw new RuntimeException("商家还未发货或订单已完成，无法确认收货！");
        }

        order.setStatus("已完成"); // 变更为：已完成
        order.setFinishTime(new Date());
        return orderMapper.updateOrder(order);
    }


    // 查询订单
    @Override
    public Order selectOrderByOrderId(String orderId) {
        return orderMapper.selectOrderByOrderId(orderId);
    }

    // 查询订单列表
    @Override
    public List<Order> selectOrderList(Order order) {
        return orderMapper.selectOrderList(order);
    }

    // 新增订单
    @Override
    public int insertOrder(Order order) {
        // 1. 设置创建时间
        order.setCreateTime(new Date());

        // 2. 兜底设置当前用户 ID
        if (order.getUserId() == null) {
            order.setUserId(SecurityUtils.getUserId());
        }

        // 3. 动态生成电商标准的唯一订单号
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formatDateTime = now.format(formatter);

        // 订单号: OR + 当前日期时间 + 当前用户ID
        order.setOrderId("OR" + formatDateTime + SecurityUtils.getUserId());

        return orderMapper.insertOrder(order);
    }

    // 修改订单
    @Override
    public int updateOrder(Order order) {
        return orderMapper.updateOrder(order);
    }

    // 批量删除订单
    @Override
    public int deleteOrderByOrderIds(String[] orderIds) {
        return orderMapper.deleteOrderByOrderIds(orderIds);
    }
}
