<template>
  <div class="exam-manage">
    <div class="header-actions">
      <h2>📚 考试科目管理</h2>
      <el-button type="primary" @click="openAddDialog">
        + 发布新考试
      </el-button>
    </div>

    <el-table :data="examList" border stripe style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="考试名称" width="200" />

      <el-table-column label="报名费" width="120">
        <template #default="scope">
          <el-tag type="warning">¥ {{ scope.row.fee }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="考试时间" width="200">
        <template #default="scope">
          {{ formatTime(scope.row.examTime) }}
        </template>
      </el-table-column>

      <el-table-column prop="description" label="说明/备注" />

      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-popconfirm title="确定要删除这个考试吗？" @confirm="handleDelete(scope.row.id)">
            <template #reference>
              <el-button type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="发布新考试" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="考试名称">
          <el-input v-model="form.name" placeholder="例如：英语六级 (CET-6)" />
        </el-form-item>

        <el-form-item label="报名费用">
          <el-input-number v-model="form.fee" :min="0" :precision="0" />
        </el-form-item>

        <el-form-item label="考试时间">
          <el-date-picker
              v-model="form.examTime"
              type="datetime"
              placeholder="选择考试日期和时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注说明">
          <el-input type="textarea" v-model="form.description" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'

const examList = ref([])
const dialogVisible = ref(false)
const form = ref({
  name: '',
  fee: 0,
  examTime: '',
  description: ''
})

// 时间格式化
const formatTime = (str) => {
  return str ? str.replace('T', ' ') : ''
}

// 加载列表
const loadList = async () => {
  const res = await request.get('/exam/list')
  if (res.data.code === 200) {
    examList.value = res.data.data
  }
}

// 打开弹窗
const openAddDialog = () => {
  form.value = { name: '', fee: 0, examTime: '', description: '' }
  dialogVisible.value = true
}

// 提交保存
const submitForm = async () => {
  if (!form.value.name || !form.value.examTime) {
    return ElMessage.warning('请填写完整信息')
  }

  try {
    const res = await request.post('/exam/add', form.value)
    if (res.data.code === 200) {
      ElMessage.success('发布成功')
      dialogVisible.value = false
      loadList() // 刷新列表
    } else {
      ElMessage.error(res.data.msg || '发布失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  }
}

// 删除
const handleDelete = async (id) => {
  try {
    const res = await request.delete(`/exam/delete/${id}`)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      loadList()
    } else {
      ElMessage.error(res.data.msg)
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.exam-manage {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>