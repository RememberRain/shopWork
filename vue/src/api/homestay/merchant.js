import request from '@/utils/request'

// 查询商家列表
export function listMerchant(query) {
    return request({
        url: '/homestay/merchant/list',
        method: 'get',
        params: query
    })
}

// 查询商家详细
export function getMerchant(merchantId) {
    return request({
        url: '/homestay/merchant/' + merchantId,
        method: 'get'
    })
}

// 新增商家
export function addMerchant(data) {
    return request({
        url: '/homestay/merchant',
        method: 'post',
        data: data
    })
}

// 修改商家
export function updateMerchant(data) {
    return request({
        url: '/homestay/merchant',
        method: 'put',
        data: data
    })
}

// 删除商家
export function delMerchant(merchantId) {
    return request({
        url: '/homestay/merchant/' + merchantId,
        method: 'delete'
    })
}

// 查询当前用户的审核状态, 如果没有提交认证, 则为未提交
export function selectUserAuthStatus() {
    return request({
        url: '/homestay/merchant/selectUserAuthStatus',
        method: 'get'
    })
}

// 根据当前用户ID删除之前提交的商家信息
export function deleteMerchantByUserId() {
    return request({
        url: '/homestay/merchant/deleteMerchantByUserId',
        method: 'delete'
    })
}

// 认证通过
export function passed(merchantId) {
    return request({
        url: '/homestay/merchant/passed/' + merchantId,
        method: 'put'
    })
}
