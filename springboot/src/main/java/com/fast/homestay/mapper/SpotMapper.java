package com.fast.homestay.mapper;

import com.fast.homestay.domain.Spot;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * @author LJH
 * @version 3.0
 * 尾货商品数据持久层接口
 */
@Mapper
public interface SpotMapper {

    /**
     * 查询尾货商品列表
     */
    List<Spot> selectSpotList(Spot spot);

    /**
     * 根据尾货商品id查询详细信息
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
