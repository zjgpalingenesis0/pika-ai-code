import request from '@/utils/request'

/**
 * 用户注册
 */
export const userRegister = (data) => {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 */
export const userLogin = (data) => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 获取当前登录用户信息
 */
export const getCurrentUser = () => {
  return request({
    url: '/user/current',
    method: 'get'
  })
}

/**
 * 用户登出
 */
export const userLogout = () => {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}
