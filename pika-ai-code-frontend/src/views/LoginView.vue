<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="title">Pika AI Code</h1>

      <!-- 登录/注册切换 -->
      <div class="tab-container">
        <div
          class="tab"
          :class="{ active: activeTab === 'login' }"
          @click="activeTab = 'login'"
        >
          登录
        </div>
        <div
          class="tab"
          :class="{ active: activeTab === 'register' }"
          @click="activeTab = 'register'"
        >
          注册
        </div>
      </div>

      <!-- 登录表单 -->
      <div v-if="activeTab === 'login'" class="form-container">
        <form @submit.prevent="handleLogin">
          <div class="form-item">
            <label>账号</label>
            <input
              v-model="loginForm.userAccount"
              type="text"
              placeholder="请输入账号"
              required
            />
          </div>
          <div class="form-item">
            <label>密码</label>
            <input
              v-model="loginForm.userPassword"
              type="password"
              placeholder="请输入密码"
              required
            />
          </div>
          <button type="submit" class="submit-btn">登录</button>
        </form>
      </div>

      <!-- 注册表单 -->
      <div v-else class="form-container">
        <form @submit.prevent="handleRegister">
          <div class="form-item">
            <label>账号</label>
            <input
              v-model="registerForm.userAccount"
              type="text"
              placeholder="请输入账号"
              required
            />
          </div>
          <div class="form-item">
            <label>密码</label>
            <input
              v-model="registerForm.userPassword"
              type="password"
              placeholder="请输入密码"
              required
            />
          </div>
          <div class="form-item">
            <label>确认密码</label>
            <input
              v-model="registerForm.checkPassword"
              type="password"
              placeholder="请再次输入密码"
              required
            />
          </div>
          <button type="submit" class="submit-btn">注册</button>
        </form>
      </div>

      <!-- 错误提示 -->
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { userLogin, userRegister } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const errorMessage = ref('')

// 登录表单
const loginForm = ref({
  userAccount: '',
  userPassword: ''
})

// 注册表单
const registerForm = ref({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

// 处理登录
const handleLogin = async () => {
  try {
    errorMessage.value = ''
    const response = await userLogin(loginForm.value)

    if (response.code === 0 && response.data) {
      // 登录成功，保存用户信息
      userStore.setUserInfo(response.data)

      // 保存token（如果后端返回了token）
      // 这里假设后端使用cookie管理session，不需要手动保存token

      // 跳转到主页
      router.push('/home')
    } else {
      errorMessage.value = response.message || '登录失败，请检查账号和密码'
    }
  } catch (error) {
    console.error('登录错误:', error)
    errorMessage.value = error.message || '登录失败，请稍后重试'
  }
}

// 处理注册
const handleRegister = async () => {
  // 验证密码
  if (registerForm.value.userPassword !== registerForm.value.checkPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }

  if (registerForm.value.userPassword.length < 6) {
    errorMessage.value = '密码长度不能少于6位'
    return
  }

  try {
    errorMessage.value = ''
    const response = await userRegister(registerForm.value)

    if (response.code === 0 && response.data) {
      // 注册成功，切换到登录页
      activeTab.value = 'login'
      errorMessage.value = ''
      // 清空注册表单
      registerForm.value = {
        userAccount: '',
        userPassword: '',
        checkPassword: ''
      }
      alert('注册成功，请登录')
    } else {
      errorMessage.value = response.message || '注册失败'
    }
  } catch (error) {
    console.error('注册错误:', error)
    errorMessage.value = error.message || '注册失败，请稍后重试'
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 28px;
}

.tab-container {
  display: flex;
  margin-bottom: 30px;
  border-bottom: 2px solid #eee;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 10px;
  cursor: pointer;
  color: #666;
  transition: all 0.3s;
}

.tab.active {
  color: #667eea;
  border-bottom: 2px solid #667eea;
  margin-bottom: -2px;
}

.form-container {
  margin-top: 20px;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.form-item input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.form-item input:focus {
  outline: none;
  border-color: #667eea;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.2s;
}

.submit-btn:hover {
  transform: translateY(-2px);
}

.submit-btn:active {
  transform: translateY(0);
}

.error-message {
  margin-top: 20px;
  padding: 10px;
  background: #fee;
  color: #c33;
  border-radius: 5px;
  text-align: center;
  font-size: 14px;
}
</style>
