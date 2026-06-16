package com.fast.homestay.service.impl;

import com.fast.homestay.domain.Homestay;
import com.fast.homestay.mapper.HomestayMapper;
import com.fast.homestay.service.HomestayService;
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
public class HomestayServiceImpl implements HomestayService {
    @Resource
    private HomestayMapper homestayMapper;

    //查询民宿
    @Override
    public Homestay selectHomestayByHomestayId(String homestayId) {
        return homestayMapper.selectHomestayByHomestayId(homestayId);
    }

    //查询民宿列表
    @Override
    public List<Homestay> selectHomestayList(Homestay homestay) {
        return homestayMapper.selectHomestayList(homestay);
    }

    //新增民宿
    @Override
    public int insertHomestay(Homestay homestay) {
        homestay.setCreateTime(new Date());
        homestay.setHomestayId(UUID.randomUUID().toString());
        homestay.setUserId(SecurityUtils.getUserId());
        return homestayMapper.insertHomestay(homestay);
    }

    //修改民宿
    @Override
    public int updateHomestay(Homestay homestay) {
        return homestayMapper.updateHomestay(homestay);
    }

    //批量删除民宿
    @Override
    public int deleteHomestayByHomestayIds(String[] homestayIds) {
        return homestayMapper.deleteHomestayByHomestayIds(homestayIds);
    }

    @Override
    public List<Homestay> selectTopSalesList() {
        return homestayMapper.selectTopSalesList();
    }

    @Override
    public List<Homestay> selectRecommendList(String homestayId) {
        Homestay homestay = homestayMapper.selectHomestayByHomestayId(homestayId);
        if (homestay == null || homestay.getTags() == null) {
            return new ArrayList<>();
        }

        String[] tags = homestay.getTags().split(",");
        List<Homestay> list = homestayMapper.selectRecommendList(homestayId, tags);

        // 如果推荐数量不足7个，补全一些销量高的商品，提升用户体验
        if (list.size() < 7) {
            List<Homestay> topSales = homestayMapper.selectTopSalesList();
            for (Homestay item : topSales) {
                if (list.size() < 7 && !list.contains(item) && !item.getHomestayId().equals(homestayId)) {
                    list.add(item);
                }
            }
        }
        return list;
    }

    //AI导购
    @Override
    public List<Homestay> searchHomestayForAI(String keyword) {
        // 简单防御：如果关键词为空，返回热销榜前 5 条作为推荐兜底
        if (keyword == null || keyword.trim().isEmpty()) {
            return homestayMapper.selectTopSalesList();
        }
        return homestayMapper.selectHomestayForAI(keyword);
    }


}
