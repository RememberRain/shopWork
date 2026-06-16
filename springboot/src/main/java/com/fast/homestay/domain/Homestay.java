package com.fast.homestay.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
public class Homestay
{

    /** 商品ID */
    private String homestayId;

    /** 商品名称 */
    private String title;

    /** 每晚价格 */
    private BigDecimal price;

    /** 地址 */
    private String address;

    /** 标签 */
    private String tags;

    /** 图片 */
    private String image;

    /** 描述 */
    private String description;

    /** 商品商家的用户ID */
    private Long userId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    //商品商家的用户名
    private String userName;

    /** 剩余数量 / 库存 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

}
