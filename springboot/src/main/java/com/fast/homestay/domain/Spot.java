package com.fast.homestay.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * @author LJH
 * @version 3.0
 * 尾货商品实体类（原景点表重构）
 */
@Data
public class Spot {

    // 尾货商品ID
    private String spotId;

    // 关联的主商品ID（用于建立两者的业务联系）
    private String homestayId;

    // 尾货商品名称
    private String name;

    // 仓库位置 / 发货地址
    private String location;

    // 抢购截止时间（或原开放时间字段的复用）
    private String openingHours;

    // 尾货图片
    private String image;

    // 尾货特卖介绍
    private String description;

    // 关联商品的实时库存（核心：用于前端或后端的动态排序）
    private Integer stock;

    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
