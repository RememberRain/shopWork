import request from '@/utils/request'

// 查询评价列表
export function listReview(query) {
  return request({
    url: '/homestay/review/list',
    method: 'get',
    params: query
  })
}

// 查询评价详细
export function getReview(reviewId) {
  return request({
    url: '/homestay/review/' + reviewId,
    method: 'get'
  })
}

// 新增评价
export function addReview(data) {
  return request({
    url: '/homestay/review',
    method: 'post',
    data: data
  })
}

// 修改评价
export function updateReview(data) {
  return request({
    url: '/homestay/review',
    method: 'put',
    data: data
  })
}

// 删除评价
export function delReview(reviewId) {
  return request({
    url: '/homestay/review/' + reviewId,
    method: 'delete'
  })
}

// 根据民宿ID查询评价列表
export function selectReviewListByHomestayId(homestayId) {
  return request({
    url: '/homestay/review/selectReviewListByHomestayId/' + homestayId,
    method: 'get'
  })
}

// 查询当前用户的历史评价列表
export function getMyReviewList(query) {
  return request({
    url: '/homestay/review/myReviewList', // 更正为 homestay
    method: 'get',
    params: query
  })
}

// 获取未读商家回复
export function getUnreadReplies() {
  return request({
    url: '/homestay/review/getUnreadReplies',
    method: 'get'
  })
}

// 全部忽略标记已读
export function readAllReplies() {
  return request({
    url: '/homestay/review/readAllReplies',
    method: 'post'
  })
}
