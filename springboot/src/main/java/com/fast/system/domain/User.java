package com.fast.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户对象 user
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    //用户ID
    private Long userId;

    //用户名
    private String userName;

    //手机号码
    private String phonenumber;

    //用户性别
    private Integer sex;

    //用户头像
    private String avatar;

    //密码
    private String password;

    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    //角色ID (该字段用于关联角色信息)
    private Long roleId;

    //角色名称 (该字段用于关联查询)
    private String roleName;

    //账户余额
    private BigDecimal balance;

    private String realName;  //真实姓名

    private String idCard;    //身份证号

}
