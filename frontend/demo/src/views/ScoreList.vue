<template>
  <div>
    <h2>我的考试成绩</h2>
    <el-table :data="scoreList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="examName" label="考试科目" />

      <el-table-column prop="examTime" label="考试时间" width="180">
        <template #default="scope">
          {{ formatTime(scope.row.examTime) }}
        </template>
      </el-table-column>

      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.status === '已出分' ? 'success' : 'info'">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="score" label="成绩" width="120">
        <template #default="scope">
          <span v-if="scope.row.score != null" style="font-weight: bold; color: #409EFF">
            {{ scope.row.score }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
// ✅ 1. 引入封装好的 request
import request from '../utils/request'
const scoreList = ref([])
const loading = ref(false)

// 获取当前用户 ID
const userStr = localStorage.getItem('user')
const currentUser = userStr ? JSON.parse(userStr) : null

// 时间格式化工具 (去掉 T)
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

// 加载数据函数
const loadScores = async () => {
  if (!currentUser) {
    ElMessage.warning('请先登录查看成绩')
    return
  }

  loading.value = true
  try {
    // ✅ 2. 修改点：
    //    - 使用 request.get
    //    - 去掉 '/api' 前缀
    const res = await request.get('/enrollment/my-scores', {
      params: { userId: currentUser.id }
    })

    if (res.data.code === 200) {
      scoreList.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '查询成绩失败')
    }
  } catch (error) {
    console.error(error)
    // request.js 会处理网络异常，这里可以只打印日志
  } finally {
    loading.value = false
  }
}

// 页面加载时触发
onMounted(() => {
  loadScores()
})
</script>