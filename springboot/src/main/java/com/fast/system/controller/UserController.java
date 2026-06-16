package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.TableDataInfo;
import com.fast.system.domain.User;
import com.fast.system.mapper.UserMapper;
import com.fast.system.service.IUserService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户信息
 */
@RestController
@RequestMapping("/system/user")
public class UserController extends BaseController{
    @Resource
    private IUserService userService;

    @Resource
    private UserMapper userMapper;

    //查询用户列表
    @GetMapping("/selectUserList")
    public TableDataInfo selectUserList(User user) {
        //启动分页功能
        startPage();
        //查询用户列表
        List<User> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    //根据用户ID查询用户信息
    @GetMapping("/selectUserByUserId/{userId}")
    public AjaxResult selectUserByUserId(@PathVariable Long userId) {
        User user = userService.selectUserByUserId(userId);
        return success(user);
    }

    //新增用户
    @PostMapping("/insertUser")
    public AjaxResult insertUser(@RequestBody User user) {
        return toAjax(userService.insertUser(user));
    }

    // 修改用户
    @PutMapping("/updateUser")
    public AjaxResult updateUser(@RequestBody User user) {
        return toAjax(userService.updateUser(user));
    }

    // 删除用户
    @DeleteMapping("/deleteUserByUserIds/{userIds}")
    public AjaxResult deleteUserByUserIds(@PathVariable Long[] userIds) {
        return toAjax(userService.deleteUserByUserIds(userIds));
    }

    //查询当前用户余额
    @GetMapping("/selectMyBalance")
    public AjaxResult selectMyBalance() {
        BigDecimal balance = userService.selectUserByUserId(SecurityUtils.getUserId()).getBalance();
        return success(balance);
    }

    //账户充值
    @PutMapping("/recharge/{amount}")
    public AjaxResult recharge(@PathVariable BigDecimal amount){

        //拿到当前用户的账户余额
        BigDecimal oldBalance = userService.selectUserByUserId(SecurityUtils.getUserId()).getBalance();

        //充值后的账户余额
        BigDecimal newBalance = oldBalance.add(amount);

        //更新账户余额
        return toAjax(userMapper.updateUserBalance(newBalance, SecurityUtils.getUserId()));
    }

    //登录后忘记密码
    @PutMapping("/forgetPassword")
    public AjaxResult forgetPassword(@RequestBody User user) {
        String phonenumber = user.getPhonenumber();
        String newPassword = user.getPassword();

        // 1. 基础数据校验
        if (phonenumber == null || phonenumber.trim().isEmpty()) {
            return error("手机号码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return error("新密码不能为空");
        }

        // 2. 调用 Service 层处理重置密码业务逻辑
        boolean success = userService.resetPasswordByPhone(phonenumber, newPassword);

        if (success) {
            return success("密码重置成功，请使用新密码登录");
        } else {
            return error("密码重置失败，未找到该手机号对应的用户");
        }
    }


    @PostMapping("/resetPwdByPhone")
    public AjaxResult resetPwdByPhone(@RequestBody Map<String, String> params) {
        String userName = params.get("userName");
        String phonenumber = params.get("phonenumber");
        String newPassword = params.get("newPassword");

        // 1. 核心核实逻辑：首先根据手机号找出数据库中的用户
        User user = userService.selectUserByPhone(phonenumber);

        // 2.  共同匹配校验：如果用户不存在，或者数据库里的用户名与输入的用户名不一致，直接拦截
        if (user == null || !user.getUserName().equals(userName.trim())) {
            return error("凭证核实失败：用户名与绑定的手机号码不匹配");
        }

        // 3. 通过双重核实，调用 Service 层去更新该手机号对应用户的密码
        boolean success = userService.resetPasswordByPhone(phonenumber, newPassword);

        return success ? success("密码重置成功，请使用新密码登录") : error("系统异常，密码重置失败");
    }
}
