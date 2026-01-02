import axios from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data

    // 如果响应的是BaseResponse格式
    if (res.code !== undefined) {
      // code为0表示成功
      if (res.code === 0) {
        return res
      } else {
        // 处理错误
        console.error('请求错误:', res.message)
        return Promise.reject(new Error(res.message || '请求失败'))
      }
    }

    return res
  },
  error => {
    console.error('请求异常:', error.message)
    return Promise.reject(error)
  }
)

export default request
