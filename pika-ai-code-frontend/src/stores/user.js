import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const isLogin = ref(false)

  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = info
    isLogin.value = true
    // 保存到localStorage
    localStorage.setItem('userInfo', JSON.stringify(info))
    localStorage.setItem('isLogin', 'true')
  }

  // 清除用户信息
  const clearUserInfo = () => {
    userInfo.value = null
    isLogin.value = false
    localStorage.removeItem('userInfo')
    localStorage.removeItem('isLogin')
    localStorage.removeItem('token')
  }

  // 初始化：从localStorage恢复用户信息
  const initUserInfo = () => {
    const savedUserInfo = localStorage.getItem('userInfo')
    const savedIsLogin = localStorage.getItem('isLogin')
    if (savedUserInfo && savedIsLogin === 'true') {
      userInfo.value = JSON.parse(savedUserInfo)
      isLogin.value = true
    }
  }

  return {
    userInfo,
    isLogin,
    setUserInfo,
    clearUserInfo,
    initUserInfo
  }
})
