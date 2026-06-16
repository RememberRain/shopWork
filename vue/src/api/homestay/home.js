import request from '@/utils/request'

// 获取统计数据
export function selectCount() {
    return request({
        url: '/home/selectCount',
        method: 'get',
    })
}

// 查询商家认证状态统计
export function merchantAuth() {
    return request({
        url: '/home/merchantAuth',
        method: 'get',
    })
}

// 查询订单状态分布统计
export function orderStatus() {
    return request({
        url: '/home/orderStatus',
        method: 'get',
    })
}
