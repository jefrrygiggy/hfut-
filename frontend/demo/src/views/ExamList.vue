<template>
  <div class="exam-list-container">
    <div class="header-box">
      <h2>📋 考试报名列表</h2>
      <p class="sub-title">请选择科目进行报名，支付完成后系统将自动更新状态</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="8" v-for="exam in examList" :key="exam.id" style="margin-bottom: 20px;">
        <el-card shadow="hover" class="exam-card">
          <template #header>
            <div class="card-header">
              <span class="exam-name">{{ exam.name }}</span>
              <el-tag type="danger" effect="light" size="large">
                ¥ {{ exam.fee }}
              </el-tag>
            </div>
          </template>

          <div class="card-content">
            <div class="info-row">
              <el-icon><Calendar /></el-icon>
              <span>考试时间: {{ formatTime(exam.examTime) }}</span>
            </div>
            <div class="info-row">
              <el-icon><Document /></el-icon>
              <span>说明: {{ exam.description || '暂无说明' }}</span>
            </div>
          </div>

          <div class="card-footer">
            <el-button
                v-if="exam.isRegistered"
                type="success"
                disabled
                plain
            >
              ✅ 您已报名
            </el-button>

            <el-button
                v-else
                type="primary"
                :loading="loadingMap[exam.id]"
                @click="handleAlipayPay(exam)"
            >
              <img
                  src="https://img.alicdn.com/tfs/TB1eZOFxXP7gK0jSZFjXXc5aXXa-32-32.svg"
                  style="width: 16px; margin-right: 5px; vertical-align: middle;"
              />
              支付宝报名
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="examList.length === 0" description="暂无可以报名的考试" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Document } from '@element-plus/icons-vue'
import request from '../utils/request' // 请确保路径正确

// --- 状态变量 ---
const examList = ref([]) // 考试列表数据
const loadingMap = ref({}) // 控制每个按钮的 Loading 状态 { 101: true, 102: false }
const currentUser = JSON.parse(localStorage.getItem('user') || 'null') // 当前登录用户

// --- 工具函数: 格式化时间 ---
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

// --- 核心业务逻辑 ---

// 1. 获取考试列表
const loadExamList = async () => {
  try {
    const res = await request.get('/exam/list', {
      params: { userId: currentUser?.id }
    })
    if (res.data.code === 200) {
      examList.value = res.data.data
    } else {
      ElMessage.warning(res.data.msg || '列表加载失败')
    }
  } catch (error) {
    console.error('加载列表错误:', error)
    ElMessage.error('网络连接异常')
  }
}

// 2. 处理支付宝支付点击事件
const handleAlipayPay = async (exam) => {
  // 基础校验
  if (!currentUser) {
    return ElMessage.warning('请先登录系统')
  }

  // 二次确认框
  try {
    await ElMessageBox.confirm(
        `确认报名【${exam.name}】吗？\n需支付金额：¥${exam.fee}`,
        '报名确认',
        {
          confirmButtonText: '去支付',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
  } catch {
    return // 用户点击了取消
  }

  // 开启 Loading 防止重复点击
  loadingMap.value[exam.id] = true

  try {
    // 步骤 A: 调用 apply 接口，确保数据库里生成了“未支付”的订单记录
    const applyRes = await request.post('/enrollment/apply', {
      userId: currentUser.id,
      examId: exam.id
    })

    if (applyRes.data.code !== 200) {
      ElMessage.error(applyRes.data.msg || '订单创建失败')
      loadingMap.value[exam.id] = false
      return
    }

    // 步骤 B: 请求后端获取支付宝的支付表单 HTML
    const payRes = await request.post('/enrollment/alipay/pay', {
      userId: currentUser.id,
      examId: exam.id
    })

    if (payRes.data.code === 200) {
      const formHtml = payRes.data.data // 后端返回的 HTML 字符串

      // ✨✨✨ 关键逻辑：将 HTML 渲染并提交 ✨✨✨
      // 1. 创建一个临时的 div
      const div = document.createElement('div')
      // 2. 将后端返回的 form 表单代码放入 div
      div.innerHTML = formHtml
      // 3. 将 div 放入 body 中
      document.body.appendChild(div)

      // 4. 找到这个表单并提交
      const form = div.getElementsByTagName('form')[0]
      if (form) {
        form.setAttribute('target', '_blank') // 可选：在新标签页打开支付
        form.submit()
        ElMessage.success('正在跳转至支付宝...')
      } else {
        ElMessage.error('支付跳转失败：未找到表单')
      }

    } else {
      // 对应 if (payRes.data.code === 200) 的 else
      ElMessage.error(payRes.data.msg || '发起支付请求失败')
    }

  } catch (e) {
    console.error(e)
    ElMessage.error('系统异常，请稍后重试')
  } finally {
    // 如果是跳转页面，其实这里执行不执行无所谓
    // 但如果出错了，需要把 loading 状态改回来
    loadingMap.value[exam.id] = false
  }
}

// --- 生命周期钩子 ---
onMounted(() => {
  if (currentUser && currentUser.id) {
    loadExamList()
  } else {
    ElMessage.warning('未登录，无法获取报名信息')
  }
})
</script>

<style scoped>
.exam-list-container {
  padding: 20px;
  background-color: #f5f7fa; /* 浅灰背景 */
  min-height: 100vh;
}

.header-box {
  margin-bottom: 20px;
  border-left: 5px solid #409eff;
  padding-left: 15px;
}

.sub-title {
  color: #909399;
  font-size: 14px;
  margin-top: 5px;
}

.exam-card {
  transition: all 0.3s;
  border-radius: 8px;
}

.exam-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.exam-name {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}

.card-content {
  color: #606266;
  font-size: 14px;
  min-height: 80px;
  padding: 10px 0;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  gap: 8px;
}

.card-footer {
  margin-top: 15px;
  text-align: right;
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}
</style>