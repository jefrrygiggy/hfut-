<template>
  <div class="dashboard-container">
    <h2>📊 管理员运营看板</h2>

    <el-row :gutter="20" class="data-row">
      <el-col :span="8">
        <el-card shadow="hover" class="data-card user-card">
          <div class="card-content">
            <div class="icon-wrapper">
              <el-icon><User /></el-icon>
            </div>
            <div class="text-info">
              <div class="label">注册学生总数</div>
              <div class="num">{{ statsData.totalStudents }} <span class="unit">人</span></div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="data-card money-card">
          <div class="card-content">
            <div class="icon-wrapper">
              <el-icon><Money /></el-icon>
            </div>
            <div class="text-info">
              <div class="label">累计报名收入</div>
              <div class="num">¥ {{ statsData.totalIncome }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="data-card exam-card">
          <div class="card-content">
            <div class="icon-wrapper">
              <el-icon><Trophy /></el-icon>
            </div>
            <div class="text-info">
              <div class="label">热门科目数量</div>
              <div class="num">{{ statsData.popularExams.length }} <span class="unit">门</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>📈 科目报名热度排行</span>
            </div>
          </template>
          <div id="mainChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" style="height: 450px; overflow-y: auto;">
          <template #header>
            <span>🔥 热门科目详情</span>
          </template>
          <div v-for="(item, index) in statsData.popularExams" :key="index" class="exam-item">
            <div class="exam-info">
              <span class="exam-name">{{ index + 1 }}. {{ item.name }}</span>
              <span class="exam-count">{{ item.count }} 人报名</span>
            </div>
            <el-progress
                :percentage="item.percent"
                :status="item.status"
                :stroke-width="10"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { User, Money, Trophy } from '@element-plus/icons-vue' // 引入图标
import request from '../../utils/request'
import * as echarts from 'echarts'

// 定义数据结构，与后端 Result Map 对应
const statsData = ref({
  totalStudents: 0,
  totalIncome: 0,
  popularExams: []
})

// 加载数据
const loadStats = async () => {
  try {
    // 调用 AdminController 的 /api/admin/stats 接口
    const res = await request.get('/admin/stats')
    if (res.data.code === 200) {
      statsData.value = res.data.data

      // 数据回来后，初始化图表
      // nextTick 确保 DOM 已经渲染完毕
      nextTick(() => {
        initChart()
      })
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 初始化 ECharts
const initChart = () => {
  const chartDom = document.getElementById('mainChart')
  if (!chartDom) return

  // 避免重复初始化
  let myChart = echarts.getInstanceByDom(chartDom)
  if (!myChart) {
    myChart = echarts.init(chartDom)
  }

  // 提取数据供图表使用
  const xData = statsData.value.popularExams.map(item => item.name)
  const yData = statsData.value.popularExams.map(item => item.count)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        data: xData,
        axisTick: { alignWithLabel: true },
        axisLabel: { interval: 0, rotate: 30 } // 防止标签重叠
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '报名人数'
      }
    ],
    series: [
      {
        name: '报名人数',
        type: 'bar',
        barWidth: '40%',
        data: yData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }

  myChart.setOption(option)

  // 监听窗口大小变化，自适应图表
  window.addEventListener('resize', () => {
    myChart.resize()
  })
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

/* 卡片样式 */
.data-card {
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.data-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.15);
}

.card-content {
  display: flex;
  align-items: center;
  height: 80px;
}

.icon-wrapper {
  font-size: 48px;
  opacity: 0.8;
  margin-right: 20px;
}

.text-info .label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 5px;
}

.text-info .num {
  font-size: 28px;
  font-weight: bold;
}

.text-info .unit {
  font-size: 14px;
  font-weight: normal;
}

/* 不同卡片的背景色 */
.user-card {
  background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
  color: #2c3e50;
}

.money-card {
  background: linear-gradient(135deg, #fad961 0%, #f76b1c 100%);
}

.exam-card {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
}

/* 列表样式 */
.exam-item {
  margin-bottom: 20px;
}

.exam-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 14px;
  color: #606266;
}

.exam-name {
  font-weight: bold;
}
</style>