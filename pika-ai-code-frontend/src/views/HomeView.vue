<template>
  <div class="home-container">
    <header class="header">
      <div class="header-content">
        <h1 class="logo">Pika AI Code</h1>
        <div class="user-info">
          <span class="username">{{ userInfo?.userName || userInfo?.userAccount }}</span>
          <button @click="handleLogout" class="logout-btn">退出登录</button>
        </div>
      </div>
    </header>

    <main class="main-content">
      <!-- 应用列表 -->
      <div class="app-list">
        <h2>我的应用</h2>

        <div v-if="loading" class="loading">
          加载中...
        </div>

        <div v-else-if="appList.length === 0" class="empty-state">
          <p>暂无应用，快去创建一个吧！</p>
          <button @click="showCreateDialog = true" class="create-btn">
            + 创建应用
          </button>
        </div>

        <div v-else class="app-grid">
          <div
            v-for="app in appList"
            :key="app.id"
            class="app-card"
            @click="handleAppClick(app)"
          >
            <div class="app-cover">
              <img v-if="app.appCover" :src="app.appCover" :alt="app.appName" />
              <div v-else class="default-cover">{{ app.appName?.charAt(0) }}</div>
            </div>
            <div class="app-info">
              <h3>{{ app.appName }}</h3>
              <p class="app-desc">{{ app.initPrompt }}</p>
              <div class="app-meta">
                <span class="app-type">{{ app.codeGenType }}</span>
                <span class="app-date">{{ formatDate(app.createTime) }}</span>
              </div>
            </div>
          </div>

          <!-- 创建新应用卡片 -->
          <div class="app-card create-card" @click="showCreateDialog = true">
            <div class="create-icon">+</div>
            <p>创建新应用</p>
          </div>
        </div>
      </div>
    </main>

    <!-- 创建应用对话框 -->
    <div v-if="showCreateDialog" class="modal-overlay" @click="closeCreateDialog">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h2>创建新应用</h2>
          <button @click="closeCreateDialog" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleCreateApp">
            <div class="form-item">
              <label>应用名称 *</label>
              <input
                v-model="newApp.appName"
                type="text"
                placeholder="请输入应用名称"
                required
              />
            </div>
            <div class="form-item">
              <label>初始提示词 *</label>
              <textarea
                v-model="newApp.initPrompt"
                placeholder="请输入应用的初始提示词"
                rows="5"
                required
              ></textarea>
            </div>
            <div class="form-item">
              <label>应用封面（可选）</label>
              <input
                v-model="newApp.appCover"
                type="text"
                placeholder="请输入封面图片URL"
              />
            </div>
            <div class="modal-footer">
              <button type="button" @click="closeCreateDialog" class="cancel-btn">
                取消
              </button>
              <button type="submit" class="confirm-btn" :disabled="creating">
                {{ creating ? '创建中...' : '创建' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listAppByPage, addApp } from '@/api/app'
import { userLogout } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const userInfo = ref(userStore.userInfo)
const appList = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const creating = ref(false)

const newApp = ref({
  appName: '',
  initPrompt: '',
  appCover: ''
})

// 获取应用列表
const fetchAppList = async () => {
  try {
    loading.value = true
    const response = await listAppByPage({
      current: 1,
      pageSize: 20
    })

    if (response.code === 0 && response.data) {
      appList.value = response.data.records || []
    }
  } catch (error) {
    console.error('获取应用列表失败:', error)
    alert('获取应用列表失败')
  } finally {
    loading.value = false
  }
}

// 处理应用卡片点击
const handleAppClick = (app) => {
  console.log('点击应用:', app)
  // TODO: 跳转到应用详情页或编辑页
  alert(`点击了应用: ${app.appName}`)
}

// 创建应用
const handleCreateApp = async () => {
  try {
    creating.value = true
    const response = await addApp(newApp.value)

    if (response.code === 0) {
      alert('创建成功')
      closeCreateDialog()
      // 刷新应用列表
      await fetchAppList()
    } else {
      alert(response.message || '创建失败')
    }
  } catch (error) {
    console.error('创建应用失败:', error)
    alert('创建应用失败')
  } finally {
    creating.value = false
  }
}

// 关闭创建对话框
const closeCreateDialog = () => {
  showCreateDialog.value = false
  newApp.value = {
    appName: '',
    initPrompt: '',
    appCover: ''
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await userLogout()
    userStore.clearUserInfo()
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
    // 即使接口失败也清除本地状态
    userStore.clearUserInfo()
    router.push('/login')
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  fetchAppList()
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  margin: 0;
  font-size: 24px;
  color: #667eea;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  color: #333;
  font-weight: 500;
}

.logout-btn {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: #f5f5f5;
  border-color: #667eea;
  color: #667eea;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.app-list h2 {
  margin-bottom: 20px;
  color: #333;
}

.loading {
  text-align: center;
  padding: 60px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 60px;
  color: #999;
}

.create-btn {
  margin-top: 20px;
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  transition: transform 0.2s;
}

.create-btn:hover {
  transform: translateY(-2px);
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.app-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.app-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.app-cover {
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  color: white;
  font-weight: bold;
}

.app-info {
  padding: 15px;
}

.app-info h3 {
  margin: 0 0 10px;
  color: #333;
  font-size: 18px;
}

.app-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.app-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.create-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 250px;
  border: 2px dashed #ddd;
  background: #fafafa;
}

.create-card:hover {
  border-color: #667eea;
}

.create-icon {
  font-size: 48px;
  color: #667eea;
  margin-bottom: 10px;
}

.create-card p {
  color: #666;
  margin: 0;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 10px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
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

.form-item input,
.form-item textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  box-sizing: border-box;
  font-family: inherit;
}

.form-item input:focus,
.form-item textarea:focus {
  outline: none;
  border-color: #667eea;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.cancel-btn,
.confirm-btn {
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn {
  background: white;
  border: 1px solid #ddd;
}

.cancel-btn:hover {
  background: #f5f5f5;
}

.confirm-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.confirm-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
