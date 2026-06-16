package com.fast.homestay.mapper;

import com.fast.homestay.domain.Review;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author LJH
 * @version 2.0
 */
@Mapper
public interface ReviewMapper {

    public Review selectReviewByReviewId(String reviewId);

    // 查询评价列表
    public List<Review> selectReviewList(Review review);

    // 新增评价
    public int insertReview(Review review);

    //修改评价
    public int updateReview(Review review);

    //批量删除评价
    public int deleteReviewByReviewIds(String[] reviewIds);

    //根据民宿ID查询评价列表
    List<Review> selectReviewListByHomestayId(String homestayId);

    //查询用户未读的商家回复
    public List<Review> selectUnreadRepliesByUserId(Integer userId);

    //批量更新已读状态
    public int updateReplyReadState(Integer userId);
}
