import { createRouter, createWebHistory } from 'vue-router'

// 建议都使用动态导入 (箭头函数 import)，这样可以路由懒加载，提高性能
const routes = [
    // 1. 根路径重定向到登录
    {
        path: '/',
        redirect: '/login'
    },

    // 2. 登录页
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },

    // ================== 🎓 学生端路由 ==================
    {
        path: '/dashboard',
        component: () => import('../layout/Layout.vue'), // 学生共用布局
        redirect: '/dashboard/exam',
        children: [
            {
                path: 'exam',
                name: 'StudentExam',
                component: () => import('../views/ExamList.vue'),
                meta: { title: '在线报名' }
            },
            {
                path: 'score',
                name: 'StudentScore',
                component: () => import('../views/ScoreList.vue'),
                meta: { title: '我的成绩' }
            }
        ]
    },

    // ================== 👮 管理员端路由 ==================
    {
        path: '/admin',
        component: () => import('../layout/Layout.vue'), // 管理员也可以复用同一个布局（只要侧边栏动态渲染）
        redirect: '/admin/dashboard',
        children: [
            {
                // 对应 router.push('/admin/dashboard')
                path: 'dashboard',
                name: 'AdminDashboard',
                // 这里引用我们刚才写的 Dashboard.vue (含图表)
                component: () => import('../views/admin/Dashboard.vue'),
                meta: { title: '运营看板' }
            },
            {
                // 对应成绩录入页面
                path: 'score',
                name: 'AdminScore',
                // 这里引用我们刚才写的 ScoreManage.vue
                component: () => import('../views/admin/ScoreManage.vue'),
                meta: { title: '成绩录入' }
            },
            {
                path: 'exam',
                component: () => import('../views/admin/ExamManage.vue'),
                meta: { title: '科目管理' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router