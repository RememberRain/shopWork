package com.fast.homestay.controller;

import com.fast.homestay.domain.Homestay;
import com.fast.homestay.service.HomestayService;
import com.fast.system.constants.RoleIdConstants;
import com.fast.system.controller.BaseController;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.TableDataInfo;
import com.fast.system.service.IUserService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author LJH
 * @version 2.0
 */
@RestController
@RequestMapping("/homestay/homestay")
public class HomestayController extends BaseController {
    @Resource
    private HomestayService homestayService;

    @Resource
    private IUserService userService;


    //查询商品列表
    @GetMapping("/list")
    public TableDataInfo list(Homestay homestay) {
        //根据当前用户ID查询角色ID
        Long roleId = userService.selectUserByUserId(SecurityUtils.getUserId()).getRoleId();

        //超级管理员和普通用户可以看到所有商品数据, 商家只能看到自己的商品数据
        if (roleId.equals(RoleIdConstants.MERCHANT_ROLE_ID)) {
            homestay.setUserId(SecurityUtils.getUserId());
        }

        startPage();
        List<Homestay> list = homestayService.selectHomestayList(homestay);
        return getDataTable(list);
    }

    //获取商品详细信息
    @GetMapping(value = "/{homestayId}")
    public AjaxResult getInfo(@PathVariable("homestayId") String homestayId) {
        return success(homestayService.selectHomestayByHomestayId(homestayId));
    }

    //新增商品
    @PostMapping
    public AjaxResult add(@RequestBody Homestay homestay) {
        return toAjax(homestayService.insertHomestay(homestay));
    }

    //修改商品
    @PutMapping
    public AjaxResult edit(@RequestBody Homestay homestay) {
        return toAjax(homestayService.updateHomestay(homestay));
    }

    //删除商品
    @DeleteMapping("/{homestayIds}")
    public AjaxResult remove(@PathVariable String[] homestayIds) {
        return toAjax(homestayService.deleteHomestayByHomestayIds(homestayIds));
    }

    //获取排行榜
    @GetMapping("/topSales")
    public AjaxResult getTopSales() {
        return success(homestayService.selectTopSalesList());
    }

    //相似推荐
    @GetMapping("/recommend/{homestayId}")
    public AjaxResult getRecommend(@PathVariable("homestayId") String homestayId) {
        return success(homestayService.selectRecommendList(homestayId));
    }


}
