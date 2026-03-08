<template>
  <div>
    <h2>📊 报名情况统计 (管理员端)</h2>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="always" style="background-color: #409EFF; color: white;">
          <div class="stat-title">总报名人数</div>
          <div class="stat-num">{{ totalStudents.toLocaleString() }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="always" style="background-color: #67C23A; color: white;">
          <div class="stat-title">实收报名费</div>
          <div class="stat-num">¥ {{ totalIncome.toLocaleString() }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-loading="loading">
      <template #header>
        <span>各科目报名热度</span>
      </template>

      <el-empty v-if="statsData.length === 0" description="暂无统计数据" />

      <div v-else v-for="item in statsData" :key="item.name" style="margin-bottom: 20px;">
        <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
          <span>{{ item.name }}</span>
          <span>{{ item.count }} 人 ({{ item.percent }}%)</span>
        </div>
        <el-progress
            :text-inside="true"
            :stroke-width="20"
            :percentage="item.percent"
            :status="item.status"
        />
      </div>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
// ✅ 1. 引入封装好的 request (如果你没配 @ 别名，就用 ../utils/request)
import request from '../utils/request'
// 定义响应式数据
const totalStudents = ref(0)
const totalIncome = ref(0)
const statsData = ref([])
const loading = ref(false)

// 加载统计数据
const loadStats = async () => {
  loading.value = true
  try {
    // ✅ 2. 修改点：
    //    - 使用 request.get
    //    - 去掉 '/api' 前缀 (因为 request.js 已配置 baseURL: '/api')
    const res = await request.get('/admin/stats')

    if (res.data.code === 200) {
      const data = res.data.data

      // 1. 更新顶部卡片数据
      totalStudents.value = data.totalStudents || 0
      totalIncome.value = data.totalIncome || 0

      // 2. 更新列表数据
      if (data.popularExams && Array.isArray(data.popularExams)) {
        statsData.value = data.popularExams
      } else {
        statsData.value = []
      }
    } else {
      ElMessage.error(res.data.msg || '获取统计数据失败')
    }
  } catch (error) {
    console.error(error)
    // request.js 会自动处理 401 等错误，这里只处理业务逻辑错误即可
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.stat-title { font-size: 14px; opacity: 0.8; }
.stat-num { font-size: 24px; font-weight: bold; margin-top: 5px; }
</style>