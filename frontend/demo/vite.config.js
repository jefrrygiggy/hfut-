// vite.config.js
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    // 👇👇👇 重点是添加这块 server 配置 👇👇👇
    server: {
        port: 5173, // 前端端口
        proxy: {
            '/api': {
                target: 'http://localhost:8080', // 后端地址
                changeOrigin: true, // 允许跨域
                // rewrite: (path) => path.replace(/^\/api/, '') // ❌ 注意！不要加这行，因为你的后端Controller明确写了 /api/user
            }
        }
    }
})