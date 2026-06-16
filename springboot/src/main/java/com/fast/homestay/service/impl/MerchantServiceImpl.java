package com.fast.homestay.service.impl;

import com.fast.homestay.domain.Merchant;
import com.fast.homestay.mapper.MerchantMapper;
import com.fast.homestay.service.MerchantService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author LJH
 * @version 2.0
 */
//商家
@Service
public class MerchantServiceImpl implements MerchantService {

    @Resource
    private MerchantMapper merchantMapper;

    //查询商家
    @Override
    public Merchant selectMerchantByMerchantId(String merchantId) {
        return merchantMapper.selectMerchantByMerchantId(merchantId);
    }

    //查询商家列表
    @Override
    public List<Merchant> selectMerchantList(Merchant merchant) {
        return merchantMapper.selectMerchantList(merchant);
    }

    //新增商家
    @Override
    public int insertMerchant(Merchant merchant) {
        merchant.setCreateTime(new Date());
        merchant.setMerchantId(UUID.randomUUID().toString());
        merchant.setUserId(SecurityUtils.getUserId());
        return merchantMapper.insertMerchant(merchant);
    }

     //修改商家
    @Override
    public int updateMerchant(Merchant merchant) {
        return merchantMapper.updateMerchant(merchant);
    }

    //批量删除商家
    @Override
    public int deleteMerchantByMerchantIds(String[] merchantIds) {
        return merchantMapper.deleteMerchantByMerchantIds(merchantIds);
    }

    //查询当前用户的审核状态, 如果没有提交认证, 则为未提交
    @Override
    public String selectUserAuthStatus(Long userId) {
        String authStatus = merchantMapper.selectUserAuthStatus(userId);
        if (authStatus == null) {
            authStatus = "未提交";
        }
        return authStatus;
    }

    //根据当前用户ID删除之前提交的商家信息
    @Override
    public int deleteMerchantByUserId(Long userId) {
        return merchantMapper.deleteMerchantByUserId(userId);
    }
}

