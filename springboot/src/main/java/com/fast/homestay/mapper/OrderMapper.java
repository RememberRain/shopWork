package com.fast.homestay.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.fast.homestay.domain.Order;

/**
 * @author LJH
 * @version 2.0
 */
@Mapper
public interface OrderMapper {


    //查询订单
    public Order selectOrderByOrderId(String orderId);

    //查询订单列表
    public List<Order> selectOrderList(Order order);

    //新增订单
    public int insertOrder(Order order);

    //修改订单
    public int updateOrder(Order order);

    //批量删除订单
    public int deleteOrderByOrderIds(String[] orderIds);
}
