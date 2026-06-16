package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.RegisterBody;
import com.fast.system.domain.User;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户注册
 */
@RestController
public class RegisterController extends BaseController {

    @Resource
    private IUserService userService;

    /**
     * 注册
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody registerBody) {
        String userName = registerBody.getUserName();
        String password = registerBody.getPassword();
        String phonenumber = registerBody.getPhonenumber();

        // 1. 基础验证
        if (userName == null || userName.trim().isEmpty()) {
            return error("注册失败：用户名不能为空");
        }
        if (phonenumber == null || phonenumber.trim().isEmpty()) {
            return error("注册失败：手机号码不能为空");
        }
        if (phonenumber.length() != 11) {
            return error("注册失败：手机号码格式不正确");
        }
        if (password == null || password.trim().isEmpty()) {
            return error("注册失败：密码不能为空");
        }

        // 2. 校验用户名唯一性
        User userQuery = new User();
        userQuery.setUserName(userName);
        if (userService.selectUserList(userQuery).size() > 0) {
            return error("注册失败：用户名「" + userName + "」已被占用");
        }

        // 3. 校验手机号唯一性（统一使用 selectUserList，避免 TooManyResultsException）
        User phoneQuery = new User();
        phoneQuery.setPhonenumber(phonenumber);
        List<User> existingUsers = userService.selectUserList(phoneQuery);

        if (existingUsers != null && !existingUsers.isEmpty()) {
            return error("注册失败：该手机号码已被其他账号绑定");
        }

        // 4. 创建用户对象
        User newUser = new User();
        newUser.setUserName(userName);
        newUser.setPhonenumber(phonenumber);
        newUser.setPassword(password);

        // 5. 执行注册逻辑
        // 建议在 service 层捕获数据库唯一键冲突异常
        try {
            boolean regFlag = userService.registerUser(newUser);
            if (!regFlag) {
                return error("注册系统异常，请稍后再试");
            }
        } catch (Exception e) {
            // 如果并发导致数据库唯一索引冲突，捕获异常防止页面崩溃
            return error("注册失败：该手机号已被注册，请直接登录");
        }

        return success("注册成功");
    }
}
