package com.fast.homestay.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * @author LJH
 * @version 3.0
 */
@Data
public class Review {
    /** 评价ID */
    private String reviewId;

    /** 民宿ID */
    private String homestayId;

    /** 评分 */
    private Integer score;

    /** 评价内容 */
    private String content;

    /** 评价用户的用户ID */
    private Integer userId;

    /** 评价时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


    /** 商家回复内容 */
    private String replyContent;

    /** 商家回复时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date replyTime;


    // 民宿名称
    private String title;

    // 评价用户
    private String userName;

    // 用户头像
    private String avatar;

    // 民宿所属商家的用户ID
    private Long homestayUserId;

    // 当前登录用户的角色ID（选填，用于在 Mybatis 映射文件中精细控制过滤）
    private Long currentRoleId;
}
