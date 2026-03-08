<template>
  <div class="score-manage">
    <h2>📊 成绩录入管理</h2>

    <el-card class="box-card" style="margin-bottom: 20px;">
      <div style="margin-bottom: 10px; font-weight: bold;">请选择要录入的考试科目：</div>
      <el-select v-model="currentExamId" placeholder="请选择考试" @change="handleExamChange" style="width: 300px">
        <el-option
            v-for="item in examList"
            :key="item.id"
            :label="item.name + ' (¥' + item.fee + ')'"
            :value="item.id">
        </el-option>
      </el-select>
    </el-card>

    <el-card v-if="currentExamId">
      <template #header>
        <div class="clearfix">
          <span>考生列表 (共 {{ studentList.length }} 人)</span>
        </div>
      </template>

      <el-table :data="studentList" border stripe style="width: 100%">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="userId" label="学号/ID" width="100" />

        <el-table-column prop="username" label="学生姓名" width="150" />
        <el-table-column prop="email" label="邮箱" width="220" />

        <el-table-column label="考试成绩" width="250">
          <template #default="scope">
            <el-input-number
                v-model="scope.row.score"
                :min="0" :max="100"
                controls-position="right"
                placeholder="未录入"
            />
            <el-button
                type="primary"
                size="small"
                style="margin-left: 10px;"
                @click="saveScore(scope.row)"
            >
              保存
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-empty v-else description="请先在上方选择一个考试科目 👆"></el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request' // 注意检查这里的路径是否正确
import { ElMessage } from 'element-plus'

const examList = ref([])      // 所有考试科目
const currentExamId = ref(null) // 当前选中的考试ID
const studentList = ref([])   // 当前考试的学生列表

// A. 初始化：获取所有考试列表 (供下拉框选择)
const loadExams = async () => {
  try {
    // 假设你有这个接口获取所有考试，如果没有，可以用之前的 /exam/list
    const res = await request.get('/exam/list')
    if (res.data.code === 200) {
      examList.value = res.data.data
    }
  } catch (error) {
    console.error(error)
  }
}

// B. 切换考试时，加载学生列表
const handleExamChange = async (val) => {
  if (!val) return
  try {
    const res = await request.get('/admin/score/student-list', {
      params: { examId: val }
    })
    if (res.data.code === 200) {
      studentList.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '加载考生失败')
    }
  } catch (error) {
    console.error(error)
  }
}

// C. 保存单个学生的成绩
const saveScore = async (row) => {
  if (row.score === undefined || row.score === null) {
    return ElMessage.warning('请输入分数')
  }
  try {
    const res = await request.post('/admin/score/update', {
      userId: row.userId,
      examId: currentExamId.value,
      score: row.score
    })

    if (res.data.code === 200) {
      ElMessage.success(`学生 ${row.username} 成绩保存成功！`)
    } else {
      ElMessage.error(res.data.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('网络异常')
  }
}

onMounted(() => {
  loadExams()
})
</script>

<style scoped>
.score-manage {
  padding: 20px;
}
</style>