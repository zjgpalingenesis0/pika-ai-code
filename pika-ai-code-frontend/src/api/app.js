import request from '@/utils/request'

/**
 * 分页查询用户的应用列表
 */
export const listAppByPage = (data) => {
  return request({
    url: '/app/list/page/vo',
    method: 'post',
    data
  })
}

/**
 * 分页查询精选应用列表
 */
export const listFeaturedAppByPage = (data) => {
  return request({
    url: '/app/list/featured/page/vo',
    method: 'post',
    data
  })
}

/**
 * 根据id获取应用详情
 */
export const getAppById = (data) => {
  return request({
    url: '/app/get',
    method: 'post',
    data
  })
}

/**
 * 创建应用
 */
export const addApp = (data) => {
  return request({
    url: '/app/add',
    method: 'post',
    data
  })
}

/**
 * 更新应用
 */
export const updateApp = (data) => {
  return request({
    url: '/app/update',
    method: 'post',
    data
  })
}

/**
 * 删除应用
 */
export const deleteApp = (data) => {
  return request({
    url: '/app/delete',
    method: 'post',
    data
  })
}
