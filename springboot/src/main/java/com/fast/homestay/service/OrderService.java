package com.fast.homestay.service;

import java.util.List;
import com.fast.homestay.domain.Order;

/**
 * @author LJH
 * @version 2.0
 */
public interface OrderService
{
    //查询订单
    public Order selectOrderByOrderId(String orderId);

    //查询订单列表
    public List<Order> selectOrderList(Order order);

    //新增订单
    public int insertOrder(Order order);

    // 修改订单
    public int updateOrder(Order order);

    // 批量删除订单
    public int deleteOrderByOrderIds(String[] orderIds);

    // 1. 用户支付成功
    int payOrder(String orderId);

    // 2. 商家发货
    int deliverOrder(String orderId);

    // 3. 用户确认收货
    int receiveOrder(String orderId);
}
