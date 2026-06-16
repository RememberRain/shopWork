import request from '@/utils/request.js'


//查询景点列表
export function selectSpotList(query){
    return request({
        url: '/homestay/spot/selectSpotList',
        method: 'get',
        params: query
    })
}

//根据景点id查询景点信息
export function selectSpotBySpotId(spotId){
    return request({
        url: '/homestay/spot/selectSpotBySpotId/' + spotId,
        method: 'get',
    })
}

//新增景点
export function insertSpot(data){
    return request({
        url: '/homestay/spot/insertSpot',
        method: 'post',
        data: data
    })
}

//修改景点
export function updateSpot(data){
    return request({
        url: '/homestay/spot/updateSpot',
        method: 'put',
        data: data
    })
}

//删除景点
export function deleteSpotByIds(spotIds){
    return request({
        url: '/homestay/spot/deleteSpotByIds/'+ spotIds,
        method: 'delete',
    })
}


