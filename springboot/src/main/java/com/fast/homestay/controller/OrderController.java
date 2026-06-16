package com.fast.homestay.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fast.homestay.domain.Homestay;
import com.fast.homestay.service.HomestayService;
import com.fast.system.constants.RoleIdConstants;
import com.fast.system.mapper.UserMapper;
import com.fast.system.service.IUserService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fast.system.controller.BaseController;
import com.fast.system.domain.AjaxResult;

import com.fast.homestay.domain.Order;
import com.fast.homestay.service.OrderService;
import com.fast.system.domain.TableDataInfo;

/**
 * @author LJH
 * @version 2.0
 */
@RestController
@RequestMapping("/homestay/order")
public class OrderController extends BaseController {

    @Resource
    private OrderService orderService;

    @Resource
    private IUserService userService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private HomestayService homestayService;

    /**
     * 用户支付完成后的状态变更
     */
    @PutMapping("/payment/{orderId}")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult payment(@PathVariable String orderId) {
        Order currentOrder = orderService.selectOrderByOrderId(orderId);
        if (currentOrder == null) {
            return error("订单不存在");
        }

        if (!"待支付".equals(currentOrder.getStatus())) {
            return error("当前订单不可支付");
        }

        // 订单总价
        BigDecimal totalPrice = currentOrder.getTotalPrice();
        // 用户支付前的账户余额
        BigDecimal oldBalance = userService.selectUserByUserId(SecurityUtils.getUserId()).getBalance();

        // 余额充足校验
        if (oldBalance.compareTo(totalPrice) < 0) {
            return error("账户余额不足, 请充值后再进行支付");
        }

        // 扣除用户账户余额
        BigDecimal newBalance = oldBalance.subtract(totalPrice);
        userMapper.updateUserBalance(newBalance, SecurityUtils.getUserId());

        // 状态变更为【未发货】，并记录支付时间
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus("未发货");
        order.setPaymentTime(new Date()); // 记录支付时间

        return toAjax(orderService.updateOrder(order));
    }

    /**
     * 商家点击发货按钮
     */
    @PutMapping("/deliver/{orderId}")
    public AjaxResult deliver(@PathVariable String orderId) {
        Order currentOrder = orderService.selectOrderByOrderId(orderId);
        if (currentOrder == null) {
            return error("订单不存在");
        }
        if (!"未发货".equals(currentOrder.getStatus())) {
            return error("订单不是【未发货】状态，无法进行发货操作！");
        }

        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus("已发货");
        order.setDeliveryTime(new Date()); // 记录发货时间
        return toAjax(orderService.updateOrder(order));
    }

    /**
     * 用户点击确认收货按钮
     */
    /**
     * 用户点击确认收货按钮
     */
    @PutMapping("/receive/{orderId}")
    @Transactional(rollbackFor = Exception.class) // 务必开启事务
    public AjaxResult receive(@PathVariable String orderId) {
        Order currentOrder = orderService.selectOrderByOrderId(orderId);
        if (currentOrder == null) {
            return error("订单不存在");
        }
        if (!"已发货".equals(currentOrder.getStatus())) {
            return error("商家还未发货，无法确认收货！");
        }

        // 1. 更新订单状态为已完成
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus("已完成");
        order.setFinishTime(new Date());
        orderService.updateOrder(order);

        // 2. 扣减库存
        Homestay homestay = homestayService.selectHomestayByHomestayId(currentOrder.getHomestayId());
        if (homestay != null) {
            // 校验库存是否足够（防御性编程）
            if (homestay.getStock() >= currentOrder.getQuantity()) {
                homestay.setStock(homestay.getStock() - currentOrder.getQuantity());
                homestayService.updateHomestay(homestay);
            } else {
                throw new RuntimeException("库存不足，无法完成订单");
            }
        }

        return success("订单已完成，库存已扣减");
    }

    /**
     * 查询订单列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Order order) {
        // 管理员可以看到所有订单数据, 商家能看到自己的民宿归属的订单数据, 用户只能看到自己下单的订单数据
        Long roleId = userService.selectUserByUserId(SecurityUtils.getUserId()).getRoleId();

        // 商家能看到自己的民宿归属的订单数据
        if (RoleIdConstants.MERCHANT_ROLE_ID.equals(roleId)) {
            order.setMerchantUserId(SecurityUtils.getUserId());
        }

        // 用户只能看到自己下单的订单数据
        if (RoleIdConstants.USER_ROLE_ID.equals(roleId)) {
            order.setUserId(SecurityUtils.getUserId());
        }

        startPage();
        List<Order> list = orderService.selectOrderList(order);
        return getDataTable(list);
    }

    /**
     * 获取订单详细信息
     */
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable String orderId) {
        return success(orderService.selectOrderByOrderId(orderId));
    }

    /**
     * 新增订单
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult add(@RequestBody Order order) {
        Homestay homestay = homestayService.selectHomestayByHomestayId(order.getHomestayId());
        if (homestay == null) {
            return error("下单失败，该商品不存在");
        }

        if (homestay.getStock() == null || homestay.getStock() < order.getQuantity()) {
            return error("下单失败，商品库存不足！当前剩余库存: " + homestay.getStock());
        }

        // 计算正确的总价
        BigDecimal correctTotalPrice = homestay.getPrice().multiply(new BigDecimal(order.getQuantity()));
        order.setTotalPrice(correctTotalPrice);

        // 扣减库存
        homestay.setStock(homestay.getStock() - order.getQuantity());
        homestayService.updateHomestay(homestay);

        order.setUserId(SecurityUtils.getUserId());
        order.setStatus("待支付");

        // 兜底防错：预订日期处理
        if (order.getCheckInDate() == null) {
            order.setCheckInDate(new Date());
        }
        if (order.getCheckOutDate() == null) {
            // 默认离店时间为明天
            long tomorrow = System.currentTimeMillis() + 24 * 60 * 60 * 1000;
            order.setCheckOutDate(new Date(tomorrow));
        }
        if (order.getNights() == null) {
            order.setNights(1);
        }

        return toAjax(orderService.insertOrder(order));
    }

    /**
     * 修改订单
     */
    @PutMapping
    public AjaxResult edit(@RequestBody Order order) {
        if (order.getOrderId() == null) {
            return error("订单ID不能为空");
        }

        // 1. 始终先从数据库拿出原本完整的订单数据
        Order checkOrder = orderService.selectOrderByOrderId(order.getOrderId());
        if (checkOrder == null) {
            return error("订单不存在");
        }

        // 2. 如果前端这次请求是为了更改评价状态（刷评防御）
        if ("1".equals(order.getCommentStatus())) {
            // 校验是否属于当前登录人，防止水平越权
            // 校验重复评价
            if ("1".equals(checkOrder.getCommentStatus())) {
                return error("该订单已评价过，请勿重复提交！");
            }

            // 该订单是否已评价
            checkOrder.setCommentStatus("1");
        } else {
            // 如果是其他正常修改（比如管理员后台改状态等），把前端传过来的非空值覆盖进去
            if (order.getStatus() != null) checkOrder.setStatus(order.getStatus());
            if (order.getCommentStatus() != null) checkOrder.setCommentStatus(order.getCommentStatus());

        }

        return toAjax(orderService.updateOrder(checkOrder));
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable String[] orderIds) {
        return toAjax(orderService.deleteOrderByOrderIds(orderIds));
    }

}
