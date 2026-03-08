import { createApp } from 'vue'
import './style.css' // 这个文件如果报错找不到，可以删掉这一行
import App from './App.vue'
import router from './router' // 引入路由配置
import ElementPlus from 'element-plus' // 引入 Element Plus
import 'element-plus/dist/index.css' // 引入 Element Plus 样式
import * as ElementPlusIconsVue from '@element-plus/icons-vue' // 引入图标

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

app.use(router)      // 👈 关键：必须使用路由
app.use(ElementPlus) // 👈 关键：必须使用 UI 库
app.mount('#app')