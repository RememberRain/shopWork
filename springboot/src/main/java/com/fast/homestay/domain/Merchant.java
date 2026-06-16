package com.fast.homestay.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author LJH
 * @version 2.0
 */
@Data
public class Merchant
{

    /** 商家ID */
    private String merchantId;

    /** 真实姓名 */
    private String realName;

    /** 身份证号 */
    private String idCard;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 身份证正面 */
    private String idCardFront;

    /** 身份证反面 */
    private String idCardBack;

    /** 营业执照 */
    private String license;

    /** 认证状态 */
    private String authStatus;

    /** 民宿商家的用户ID */
    private Long userId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    //商家的用户名
    private String userName;


}
