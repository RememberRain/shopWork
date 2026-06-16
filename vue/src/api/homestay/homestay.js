import request from '@/utils/request'

// 查询民宿列表
export function listHomestay(query) {
  return request({
    url: '/homestay/homestay/list',
    method: 'get',
    params: query
  })
}

// 查询民宿详细
export function getHomestay(homestayId) {
  return request({
    url: '/homestay/homestay/' + homestayId,
    method: 'get'
  })
}

// 新增民宿
export function addHomestay(data) {
  return request({
    url: '/homestay/homestay',
    method: 'post',
    data: data
  })
}

// 修改民宿
export function updateHomestay(data) {
  return request({
    url: '/homestay/homestay',
    method: 'put',
    data: data
  })
}

// 删除民宿
export function delHomestay(homestayId) {
  return request({
    url: '/homestay/homestay/' + homestayId,
    method: 'delete'
  })
}

//拿到销量前十的商品
export function getTopSalesList() {
  return request({
    url: '/homestay/homestay/topSales',
    method: 'get'
  })
}

// 查询同标签推荐列表
export function getRecommendList(homestayId) {
  return request({
    url: '/homestay/homestay/recommend/' + homestayId,
    method: 'get'
  })
}
