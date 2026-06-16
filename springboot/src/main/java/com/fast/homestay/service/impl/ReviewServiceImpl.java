package com.fast.homestay.service.impl;

import com.fast.homestay.domain.Review;
import com.fast.homestay.mapper.ReviewMapper;
import com.fast.homestay.service.ReviewService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author LJH
 * @version 2.0
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewMapper reviewMapper;

    //查询评价
    @Override
    public Review selectReviewByReviewId(String reviewId) {
        return reviewMapper.selectReviewByReviewId(reviewId);
    }

    //查询评价列表
    @Override
    public List<Review> selectReviewList(Review review) {
        return reviewMapper.selectReviewList(review);
    }

    //新增评价
    @Override
    public int insertReview(Review review) {
        review.setCreateTime(new Date());
        review.setReviewId(UUID.randomUUID().toString());
        review.setUserId(Math.toIntExact(SecurityUtils.getUserId()));
        return reviewMapper.insertReview(review);
    }

    //修改评价
    @Override
    public int updateReview(Review review) {
        return reviewMapper.updateReview(review);
    }

    //批量删除评价
    @Override
    public int deleteReviewByReviewIds(String[] reviewIds) {
        return reviewMapper.deleteReviewByReviewIds(reviewIds);
    }

    //根据民宿ID查询评价列表
    @Override
    public List<Review> selectReviewListByHomestayId(String homestayId) {
        return reviewMapper.selectReviewListByHomestayId(homestayId);
    }

    @Override
    public List<Review> selectUnreadRepliesByUserId(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return reviewMapper.selectUnreadRepliesByUserId(userId.intValue());
    }

    @Override
    public int readAllReplies(Long userId) {
        if (userId == null) {
            return 0;
        }
        return reviewMapper.updateReplyReadState(userId.intValue());
    }

}
