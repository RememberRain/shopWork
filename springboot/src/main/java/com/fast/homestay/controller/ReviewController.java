package com.fast.homestay.controller;

import com.fast.homestay.domain.Review;
import com.fast.homestay.service.ReviewService;
import com.fast.system.constants.RoleIdConstants;
import com.fast.system.controller.BaseController;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.TableDataInfo;
import com.fast.system.service.IUserService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/homestay/review")
public class ReviewController extends BaseController {

    @Resource
    private ReviewService reviewService;

    @Resource
    private IUserService userService;

    /**
     * 查询评价列表（加入角色双向透传）
     */
    @GetMapping("/list")
    public TableDataInfo list(Review review) {
        Long userId = SecurityUtils.getUserId();
        Long roleId = userService.selectUserByUserId(userId).getRoleId();

        // 将角色ID透传给对象，以便前端或Mapper进行动态渲染
        review.setCurrentRoleId(roleId);

        // 数据隔离限制：如果是商家，限制只能看到自己民宿的订单评价
        if (roleId.equals(RoleIdConstants.MERCHANT_ROLE_ID)) {
            review.setHomestayUserId(userId);
        }

        startPage();
        List<Review> list = reviewService.selectReviewList(review);
        return getDataTable(list);
    }

    /**
     * 获取评价详细信息
     */
    @GetMapping(value = "/{reviewId}")
    public AjaxResult getInfo(@PathVariable("reviewId") String reviewId) {
        return success(reviewService.selectReviewByReviewId(reviewId));
    }

    /**
     * 新增评价 (用户前台调用)
     */
    @PostMapping
    public AjaxResult add(@RequestBody Review review) {
        return toAjax(reviewService.insertReview(review));
    }

    /**
     * 修改/回复评价
     */
    @PutMapping
    public AjaxResult edit(@RequestBody Review review) {
        if (review.getReplyContent() != null && !review.getReplyContent().trim().isEmpty() && review.getReplyTime() == null) {
            review.setReplyTime(new java.util.Date());
        }
        return toAjax(reviewService.updateReview(review));
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{reviewIds}")
    public AjaxResult remove(@PathVariable String[] reviewIds) {
        return toAjax(reviewService.deleteReviewByReviewIds(reviewIds));
    }

    /**
     * 根据民宿ID查询评价列表
     */
    @GetMapping("/selectReviewListByHomestayId/{homestayId}")
    public AjaxResult selectReviewListByHomestayId(@PathVariable String homestayId) {
        return success(reviewService.selectReviewListByHomestayId(homestayId));
    }

    /**
     * 查询当前登录用户发表的所有评价记录
     */
    @GetMapping("/myReviewList")
    public TableDataInfo myReviewList() {
        startPage();
        Review review = new Review();

        Long userId = SecurityUtils.getUserId();
        if (userId != null) {
            review.setUserId(userId.intValue());
        }

        List<Review> list = reviewService.selectReviewList(review);
        return getDataTable(list);
    }

    /**
     * 获取当前登录用户的未读商家回复
     */
    @GetMapping("/getUnreadReplies")
    public AjaxResult getUnreadReplies() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return success(new ArrayList<Review>());
        }

        List<Review> list = reviewService.selectUnreadRepliesByUserId(userId);


        if (list == null) {
            return success(new ArrayList<Review>());
        }
        return success(list);
    }

    /**
     * 一键全部忽略/标记已读
     */
    @PostMapping("/readAllReplies")
    public AjaxResult readAllReplies() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return error("当前登录状态已失效，请重新登录");
        }

        // 执行一键已读
        int rows = reviewService.readAllReplies(userId);
        return toAjax(rows);
    }
}
