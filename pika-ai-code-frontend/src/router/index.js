import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/HomeView.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 初始化用户信息
  userStore.initUserInfo()

  // 需要登录的页面
  if (to.meta.requiresAuth) {
    if (!userStore.isLogin) {
      next('/login')
    } else {
      next()
    }
  } else {
    // 已登录用户访问登录页，重定向到首页
    if (to.path === '/login' && userStore.isLogin) {
      next('/home')
    } else {
      next()
    }
  }
})

export default router
