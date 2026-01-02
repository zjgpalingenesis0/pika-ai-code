# Pika AI Code 前端项目

## 项目介绍
这是Pika AI Code的前端项目，基于Vue3 + Vite + Axios开发。

## 功能特性
- 用户登录/注册
- 应用列表展示
- 创建新应用
- 用户信息管理

## 技术栈
- Vue 3
- Vue Router 4
- Pinia (状态管理)
- Axios (HTTP请求)
- Vite (构建工具)

## 项目结构
```
pika-ai-code-frontend/
├── src/
│   ├── api/              # API接口
│   │   ├── user.js       # 用户相关接口
│   │   └── app.js        # 应用相关接口
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── stores/           # 状态管理
│   │   └── user.js       # 用户状态
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios封装
│   ├── views/            # 页面组件
│   │   ├── LoginView.vue # 登录/注册页
│   │   └── HomeView.vue  # 主页
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML模板
├── vite.config.js        # Vite配置
└── package.json          # 项目依赖
```

## 快速开始

### 安装依赖
```bash
npm install
```

### 启动开发服务器
```bash
npm run dev
```

项目将在 http://localhost:5173 启动

### 构建生产版本
```bash
npm run build
```

## 环境配置
后端接口地址配置在 `vite.config.js` 中：
```javascript
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8123',
      changeOrigin: true
    }
  }
}
```

确保后端服务运行在 http://localhost:8123

## 使用说明

1. 启动后端服务
2. 启动前端开发服务器：`npm run dev`
3. 访问 http://localhost:5173
4. 注册新账号或使用已有账号登录
5. 登录后可以查看和创建应用

## API接口说明

### 用户相关
- POST `/api/user/register` - 用户注册
- POST `/api/user/login` - 用户登录
- GET `/api/user/current` - 获取当前用户信息
- POST `/api/user/logout` - 用户登出

### 应用相关
- POST `/api/app/add` - 创建应用
- POST `/api/app/update` - 更新应用
- POST `/api/app/delete` - 删除应用
- POST `/api/app/get` - 获取应用详情
- POST `/api/app/list/page/vo` - 分页查询用户应用列表

## 注意事项
1. 确保后端服务正常运行
2. 前端使用代理转发API请求到后端
3. 用户登录状态保存在localStorage中
