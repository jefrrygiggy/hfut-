<template>
  <div class="layout-container">
    <div class="header">
      <div class="logo">
        🎓 网上报名查分系统
      </div>
      <div class="user-info">
        <span>欢迎, {{ user.username }} ({{ userRole === 'admin' ? '管理员' : '学生' }})</span>
        <el-button type="danger" link size="small" @click="handleLogout" style="margin-left: 15px">
          退出登录
        </el-button>
      </div>
    </div>

    <div class="main-body">

      <div class="aside">
        <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical"
            router
            unique-opened
            background-color="#fff"
            text-color="#303133"
            active-text-color="#409EFF"
        >
          <template v-if="userRole === 'admin'">
            <el-menu-item index="/admin/dashboard">
              <el-icon><DataLine /></el-icon>
              <span>运营数据看板</span>
            </el-menu-item>

            <el-menu-item index="/admin/exam">
              <el-icon><Setting /></el-icon>
              <span>科目管理</span>
            </el-menu-item>

            <el-menu-item index="/admin/score">
              <el-icon><Edit /></el-icon>
              <span>成绩录入管理</span>
            </el-menu-item>
          </template>

          <template v-else>
            <el-menu-item index="/dashboard/exam">
              <el-icon><List /></el-icon>
              <span>报名考试列表</span>
            </el-menu-item>

            <el-menu-item index="/dashboard/score">
              <el-icon><Trophy /></el-icon>
              <span>我的成绩查询</span>
            </el-menu-item>
          </template>

        </el-menu>
      </div>

      <div class="main-content">
        <router-view />
      </div>

    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataLine, Edit, List, Trophy, Setting } from '@element-plus/icons-vue'
const route = useRoute()
const router = useRouter()

// 获取当前用户
const userStr = localStorage.getItem('user') || '{}'
const user = JSON.parse(userStr)
// 假如没有 role 字段，默认就是 student
const userRole = user.role || 'student'

// 当前高亮的菜单
const activeMenu = computed(() => route.path)

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
/* 整体容器 */
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部栏 */
.header {
  height: 60px;
  background-color: #409EFF; /* 主题蓝 */
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  z-index: 10;
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.user-info {
  font-size: 14px;
}

/* 主体区域 (flex布局：左边定宽，右边自适应) */
.main-body {
  flex: 1;
  display: flex;
  overflow: hidden; /* 防止双滚动条 */
}

/* 左侧边栏 */
.aside {
  width: 220px;
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
  overflow-y: auto;
}

/* 菜单样式微调 */
.el-menu-vertical {
  border-right: none; /* 去掉 Element 菜单自带的边框 */
  margin-top: 10px;
}

/* 右侧内容区 */
.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto; /* 内容多了只有这里滚动 */
  background-color: #f5f7fa; /* 淡淡的灰色背景，让卡片更立体 */
}
</style>