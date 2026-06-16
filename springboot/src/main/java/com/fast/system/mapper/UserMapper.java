package com.fast.system.mapper;

import com.fast.system.domain.User;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户 Mapper
 */

@Mapper
public interface UserMapper {


    //根据用户名查询用户
    public User selectUserByUserName(String userName);

    //通过用户ID查询用户信息
    User selectUserByUserId(Long userId);

    //新增用户
    int insertUser(User user);

    //更新用户头像
    int updateUserAvatar(Long userId, String avatar);

    //修改用户信息
    int updateUser(User user);

    // 重置密码
    int resetUserPwd(Long userId, String newPassword);

    // 查询用户列表
    List<User> selectUserList(User user);

    //删除用户
    int deleteUserByUserIds(Long[] userIds);

    //更新账户余额
    int updateUserBalance(BigDecimal newBalance, Long userId);

    //通过手机号查询用户（忘记密码）
    User selectUserByPhone(String phonenumber);
}
