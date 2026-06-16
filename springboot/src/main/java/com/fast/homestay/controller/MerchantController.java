package com.fast.homestay.controller;

import com.fast.homestay.domain.Merchant;
import com.fast.homestay.service.MerchantService;
import com.fast.system.constants.RoleIdConstants;
import com.fast.system.controller.BaseController;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.TableDataInfo;
import com.fast.system.domain.User;
import com.fast.system.service.IUserService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author LJH
 * @version 2.0
 */
@RestController
@RequestMapping("/homestay/merchant")
public class MerchantController extends BaseController {

    @Resource
    private MerchantService merchantService;

    @Resource
    private IUserService userService;

    //查询商家列表
    @GetMapping("/list")
    public TableDataInfo list(Merchant merchant) {
        startPage();
        List<Merchant> list = merchantService.selectMerchantList(merchant);
        return getDataTable(list);
    }

    //获取商家详细信息
    @GetMapping(value = "/{merchantId}")
    public AjaxResult getInfo(@PathVariable("merchantId") String merchantId) {
        return success(merchantService.selectMerchantByMerchantId(merchantId));
    }

    //新增商家
    @PostMapping
    public AjaxResult add(@RequestBody Merchant merchant) {
        return toAjax(merchantService.insertMerchant(merchant));
    }

    //修改商家
    @PutMapping
    public AjaxResult edit(@RequestBody Merchant merchant) {
        return toAjax(merchantService.updateMerchant(merchant));
    }

    //删除商家
    @DeleteMapping("/{merchantIds}")
    public AjaxResult remove(@PathVariable String[] merchantIds) {
        return toAjax(merchantService.deleteMerchantByMerchantIds(merchantIds));
    }

    //查询当前用户的审核状态, 如果没有提交认证, 则为未提交
    @GetMapping("/selectUserAuthStatus")
    public AjaxResult selectUserAuthStatus() {
        //当前用户的用户ID
        Long userId = SecurityUtils.getUserId();
        return success(merchantService.selectUserAuthStatus(userId));
    }

    //根据当前用户ID删除之前提交的商家信息
    @DeleteMapping("/deleteMerchantByUserId")
    public AjaxResult deleteMerchantByUserId() {
        //当前用户的用户ID
        Long userId = SecurityUtils.getUserId();
        return toAjax(merchantService.deleteMerchantByUserId(userId));
    }

    //认证通过
    @PutMapping("/passed/{merchantId}")
    @Transactional
    public AjaxResult passed(@PathVariable String merchantId) {
        //根据商家ID查询对应的商家用户ID
        Long merchantUserId = merchantService.selectMerchantByMerchantId(merchantId).getUserId();

        //将该用户的角色修改为商家
        User user = userService.selectUserByUserId(merchantUserId);
        user.setRoleId(RoleIdConstants.MERCHANT_ROLE_ID);
        userService.updateUser(user);

        //将该商家的审核状态修改为认证通过
        Merchant merchant = new Merchant();
        merchant.setMerchantId(merchantId);
        merchant.setAuthStatus("认证通过");

        return toAjax(merchantService.updateMerchant(merchant));
    }

}
