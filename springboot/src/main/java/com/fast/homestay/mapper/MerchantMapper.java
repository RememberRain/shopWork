package com.fast.homestay.mapper;

import com.fast.homestay.domain.Merchant;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author LJH
 * @version 2.0
 */
@Mapper
public interface MerchantMapper {
    //查询商家
    public Merchant selectMerchantByMerchantId(String merchantId);

    //查询商家列表
    public List<Merchant> selectMerchantList(Merchant merchant);

    //新增商家
    public int insertMerchant(Merchant merchant);

    //修改商家
    public int updateMerchant(Merchant merchant);

    //批量删除商家
    public int deleteMerchantByMerchantIds(String[] merchantIds);

    //查询当前用户的审核状态, 如果没有提交认证, 则为未提交
    String selectUserAuthStatus(Long userId);

    //根据当前用户ID删除之前提交的商家信息
    int deleteMerchantByUserId(Long userId);
}
