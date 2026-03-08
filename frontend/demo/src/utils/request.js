import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
    baseURL: '/api', // 这里统一加上前缀，以后页面里写 '/user/login' 即可
    timeout: 5000
})

// 🟢 关键修改 2：请求拦截器 (每次请求前自动加 Token)
request.interceptors.request.use(config => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')

    if (token) {
        // 如果有 token，加到请求头中
        // 后端如果用了 "Bearer " 前缀，这里直接传；如果没有，就写 `Bearer ${token}`
        config.headers['Authorization'] = token
    }
    return config
}, error => {
    return Promise.reject(error)
})

// 🟢 关键修改 3：响应拦截器 (处理 Token 过期)
request.interceptors.response.use(response => {
    return response
}, error => {
    // 如果后端返回 401，说明 Token 过期或无效
    if (error.response && error.response.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        // 强制跳转回登录页 (简单做法：刷新页面让路由守卫处理，或者用 window.location)
        window.location.href = '/'
    }
    return Promise.reject(error)
})

export default request