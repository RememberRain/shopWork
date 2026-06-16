package com.fast.homestay.mapper;

import com.fast.homestay.domain.Homestay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author LJH
 * @version 2.0
 */
@Mapper
public interface HomestayMapper {
    //查询民宿
    public Homestay selectHomestayByHomestayId(String homestayId);

    //查询民宿列表
    public List<Homestay> selectHomestayList(Homestay homestay);

    //新增民宿
    public int insertHomestay(Homestay homestay);

    //修改民宿
    public int updateHomestay(Homestay homestay);

    //批量删除民宿
    public int deleteHomestayByHomestayIds(String[] homestayIds);

    List<Homestay> selectTopSalesList();

    List<Homestay> selectRecommendList(@Param("homestayId") String homestayId, @Param("tags") String[] tags);

    List<Homestay> selectHomestayForAI(@Param("keyword") String keyword);
}
