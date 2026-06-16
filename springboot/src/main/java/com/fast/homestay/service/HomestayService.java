package com.fast.homestay.service;

import com.fast.homestay.domain.Homestay;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author LJH
 * @version 2.0
 */

public interface HomestayService {
    //查询商品
    public Homestay selectHomestayByHomestayId(String homestayId);

    //查询商品列表
    public List<Homestay> selectHomestayList(Homestay homestay);

    //新增商品
    public int insertHomestay(Homestay homestay);

    //修改商品
    public int updateHomestay(Homestay homestay);

    //批量删除商品
    public int deleteHomestayByHomestayIds(String[] homestayIds);

    List<Homestay> selectTopSalesList();

    List selectRecommendList(String homestayId);

    /**
     * AI 导购专用：根据用户描述搜索商品
     */
    List<Homestay> searchHomestayForAI(String query);
}
