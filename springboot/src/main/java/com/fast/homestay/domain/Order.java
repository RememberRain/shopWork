package com.fast.homestay.domain;

import java.math.BigDecimal;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * @author LJH
 * @version 2.1
 */
@Data
public class Order {

    /** 订单ID(订单号) */
    private String orderId;

    /** 商品ID */
    private String homestayId;

    /** 购买数量 */
    private Integer quantity;

    /** 收货地址 */
    private String shippingAddress;

    /** 入住日期 */
    private Date checkInDate;

    /** 退房日期 */
    private Date checkOutDate;

    /** 入住天数 */
    private Integer nights;

    /** 订单总价 */
    private BigDecimal totalPrice;

    /** 联系电话 */
    private String contactPhone;

    /** 订单状态 */
    private String status;

    // 🌟 核心新增：评价状态 (0-未评价，1-已评价)
    private String commentStatus;

    /** 下单用户的用户ID */
    private Long userId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 民宿名称 */
    private String title;

    /** 民宿图片 */
    private String image;

    /** 民宿地址 */
    private String address;

    /** 下单用户名 */
    private String userName;

    //商家的用户ID
    private Long merchantUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date paymentTime; // 支付时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deliveryTime;// 发货时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;  // 完成时间
}
