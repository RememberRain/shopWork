package com.fast.homestay.service;

import com.fast.homestay.domain.Spot;
import java.util.List;

/**
 * @author LJH
 * @version 3.0
 * 尾货商品业务层接口
 */
public interface SpotService {

    /**
     * 查询尾货商品列表
     */
    List<Spot> selectSpotList(Spot spot);

    /**
     * 根据尾货商品id查询尾货信息
     */
    Spot selectSpotBySpotId(String spotId);

    /**
     * 新增尾货商品
     */
    int insertSpot(Spot spot);

    /**
     * 修改尾货商品
     */
    int updateSpot(Spot spot);

    /**
     * 批量删除尾货商品
     */
    int deleteSpotByIds(String[] spotIds);
}
