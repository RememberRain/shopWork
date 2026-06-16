package com.fast.system.service.impl;

import com.fast.system.constants.RoleIdConstants;
import com.fast.system.domain.User;
import com.fast.system.mapper.UserMapper;
import com.fast.system.mapper.UserRoleMapper;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户 service实现方法
 */
//@Service 注解的作用
//1. 告诉spring这是一个服务层组件(bean)
//2.spring会自动创建这个类的实例并且管理它
@Service
public class UserServiceImpl implements IUserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public User selectUserByUserName(String userName) {
        return userMapper.selectUserByUserName(userName);
    }

    /**
     * 通过用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public User selectUserByUserId(Long userId) {
        return userMapper.selectUserByUserId(userId);
    }

    /**
     * 注册用户
     *
     * @param newUser 要注册的用户信息
     * @return 是否注册成功
     */
    @Override
    public boolean registerUser(User newUser) {
        //根据用户名查询用户信息
        User user = userMapper.selectUserByUserName(newUser.getUserName());

        if (user != null) {
            throw new RuntimeException("用户名已存在, 请更换用户名后再注册");
        }

        int i = userMapper.insertUser(newUser);

        //注册用户后默认为普通用户
        userRoleMapper.insertUserRole(newUser.getUserId(), RoleIdConstants.USER_ROLE_ID);

        return i > 0;
    }

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像访问路径
     * @return 是否更新成功
     */
    @Override
    public int updateUserAvatar(Long userId, String avatar) {
        return userMapper.updateUserAvatar(userId, avatar);
    }

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 是否修改成功
     */
    @Override
    @Transactional
    public int updateUser(User user) {
        if (user.getRoleId() == null) {
            //修改用户信息
            return userMapper.updateUser(user);
        } else {
            //修改用户信息
            userMapper.updateUser(user);

            //先删除用户之前的角色关联 (根据用户ID删除用户和角色关联)
            userRoleMapper.deleteUserRoleByUserId(user.getUserId());

            //新增用户和角色关联
            return userRoleMapper.insertUserRole(user.getUserId(), user.getRoleId());
        }
    }

    /**
     * 重置密码
     *
     * @param userId      用户ID
     * @param newPassword 要修改成的密码
     * @return 是否重置成功
     */
    @Override
    public int resetUserPwd(Long userId, String newPassword) {
        return userMapper.resetUserPwd(userId, newPassword);
    }

    /**
     * 查询用户列表
     *
     * @param user 查询参数
     * @return 用户列表数据
     */
    @Override
    public List<User> selectUserList(User user) {
        return userMapper.selectUserList(user);
    }

    /**
     * 新增用户
     *
     * @param user 表单参数
     * @return 是否新增成功
     */
    @Override
    @Transactional
    public int insertUser(User user) {
        //新增用户
        userMapper.insertUser(user);

        //新增用户和角色关联
        return userRoleMapper.insertUserRole(user.getUserId(), user.getRoleId());
    }

    /**
     * 删除用户
     *
     * @param userIds 用户ID数组
     * @return 是否删除成功
     */
    @Override
    public int deleteUserByUserIds(Long[] userIds) {
        //批量删除用户与角色关联
        userRoleMapper.deleteUserRoles(userIds);

        return userMapper.deleteUserByUserIds(userIds);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPasswordByPhone(String phonenumber, String newPassword) {
        // 1. 根据手机号查询用户（需要在 UserMapper 中编写 selectUserByPhone 方法）
        User user = userMapper.selectUserByPhone(phonenumber);
        if (user == null) {
            return false; // 用户不存在
        }

        // 2. 将新密码进行加密（注意：请务必与你们系统的登录加密方式保持一致，比如SecurityUtils.encryptPassword）
        // String encryptPwd = SecurityUtils.encryptPassword(newPassword);
        user.setPassword(newPassword); // 如果系统目前是明文，则直接 set

        // 3. 更新用户信息
        int rows = userMapper.updateUser(user);
        return rows > 0;
    }

    //通过手机号查询用户信息
    @Override
    public User selectUserByPhone(String phonenumber) {
        // 💡 核心修改：直接调用已经声明好的 userMapper 接口获取数据
        return userMapper.selectUserByPhone(phonenumber);
    }


}
