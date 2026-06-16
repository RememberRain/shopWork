package com.fast.homestay.service.impl;

import com.fast.homestay.domain.Spot;
import com.fast.homestay.mapper.SpotMapper;
import com.fast.homestay.service.SpotService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author LJH
 * @version 3.0
 * 尾货商品业务层实现类
 */
@Service
public class SpotServiceImpl implements SpotService {

    @Resource
    private SpotMapper spotMapper;

    /**
     * 查询尾货商品列表
     */
    @Override
    public List<Spot> selectSpotList(Spot spot) {
        return spotMapper.selectSpotList(spot);
    }

    /**
     * 根据尾货id查询详细信息
     */
    @Override
    public Spot selectSpotBySpotId(String spotId) {
        return spotMapper.selectSpotBySpotId(spotId);
    }

    /**
     * 新增尾货商品
     */
    @Override
    public int insertSpot(Spot spot) {
        // 生成分布式唯一UUID字符串主键
        spot.setSpotId(UUID.randomUUID().toString());
        // 自动记录创建时间
        spot.setCreateTime(new Date());
        return spotMapper.insertSpot(spot);
    }

    /**
     * 修改尾货商品
     */
    @Override
    public int updateSpot(Spot spot) {
        return spotMapper.updateSpot(spot);
    }

    /**
     * 批量删除尾货商品
     */
    @Override
    public int deleteSpotByIds(String[] spotIds) {
        return spotMapper.deleteSpotByIds(spotIds);
    }
}
