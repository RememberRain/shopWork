import request from '@/utils/request'

// 查询订单列表
export function listOrder(query) {
    return request({
        url: '/homestay/order/list',
        method: 'get',
        params: query
    })
}

// 查询订单详细
export function getOrder(orderId) {
    return request({
        url: '/homestay/order/' + orderId,
        method: 'get'
    })
}

// 新增订单
export function addOrder(data) {
    return request({
        url: '/homestay/order',
        method: 'post',
        data: data
    })
}

// 修改订单
export function updateOrder(data) {
    return request({
        url: '/homestay/order',
        method: 'put',
        data: data
    })
}

// 删除订单
export function delOrder(orderId) {
    return request({
        url: '/homestay/order/' + orderId,
        method: 'delete'
    })
}

//支付订单
export function payment(orderId) {
    return request({
        url: '/homestay/order/payment/' + orderId,
        method: 'put'
    })
}

// 发货
export function deliverOrder(orderId) {
    return request({
        url: '/homestay/order/deliver/' + orderId,
        method: 'put'
    })
}
// 收货
export function receiveOrder(orderId) {
    return request({
        url: '/homestay/order/receive/' + orderId,
        method: 'put'
    })
}
