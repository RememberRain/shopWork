package com.fast.homestay.controller;

import com.fast.homestay.domain.*;
import com.fast.homestay.service.*;
import com.fast.system.controller.BaseController;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.User;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author LJH
 * @version 2.0
 */
//首页数据
@RestController
@RequestMapping("/home")
public class HomeController extends BaseController {

    @Resource
    private IUserService userService;

    @Resource
    private HomestayService homestayService;

    @Resource
    private OrderService orderService;

    @Resource
    private ReviewService reviewService;

    @Resource
    private MerchantService merchantService;

    @Resource
    private SpotService spotService;



    //获取统计数据
    @GetMapping("/selectCount")
    public AjaxResult selectCount() {

        Map<String, Object> result = new HashMap<>();

        //用户统计
        List<Homestay> homestayList = homestayService.selectHomestayList(new Homestay());
        result.put("homestayCount", homestayList.size());

        //民宿统计
        List<User> userList = userService.selectUserList(new User());
        result.put("userCount", userList.size());

        // 订单统计
        List<Order> orderList = orderService.selectOrderList(new Order());
        result.put("orderCount", orderList.size());

        // 评价统计
        List<Review> reviewList = reviewService.selectReviewList(new Review());
        result.put("reviewCount", reviewList.size());

        // 景点统计
        List<Spot> spotList = spotService.selectSpotList(new Spot());
        result.put("spotCount", spotList.size());

        // 商家统计
        List<Merchant> merchantList = merchantService.selectMerchantList(new Merchant());
        result.put("merchantCount", merchantList.size());

        return success(result);
    }

    //查询商家认证状态
    @GetMapping("/merchantAuth")
    public AjaxResult merchantAuth() {
        List<Merchant> merchantList = merchantService.selectMerchantList(new Merchant());

        Map<String, Integer> authCount = new HashMap<>();
        authCount.put("待审核", 0);
        authCount.put("认证通过", 0);
        authCount.put("认证未通过", 0);

        for (Merchant merchant : merchantList) {
            String authStatus = merchant.getAuthStatus();
            if (authStatus != null) {
                authCount.put(authStatus, authCount.getOrDefault(authStatus, 0) + 1);
            }
        }

        return success(authCount);
    }

    //查询订单状态分布统计
    @GetMapping("/orderStatus")
    public AjaxResult orderStatus() {
        List<Order> orderList = orderService.selectOrderList(new Order());

        Map<String, Integer> statusCount = new HashMap<>();
        statusCount.put("待支付", 0);
        statusCount.put("已支付", 0);
        statusCount.put("已完成", 0);
        statusCount.put("已取消", 0);

        for (Order order : orderList) {
            String status = order.getStatus();
            if (status != null) {
                statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
            }
        }

        return success(statusCount);
    }

}
