<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2 style="text-align: center">网上报名查分系统</h2>
      </template>

      <el-tabs v-model="activeTab" stretch>

        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" @keyup.enter="handleLogin">
            <el-form-item>
              <el-input v-model="loginForm.username" placeholder="用户名/学号" prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item>
              <el-button
                  type="primary"
                  style="width: 100%"
                  :loading="isLoading"
                  @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
            <div style="text-align: right">
              <el-link type="primary" @click="openResetDialog">忘记密码？</el-link>
            </div>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册新账号" name="register">
          <el-form :model="regForm">
            <el-form-item>
              <el-input v-model="regForm.username" placeholder="设置用户名" prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="regForm.email" placeholder="绑定邮箱 (用于找回密码)" prefix-icon="Message" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="regForm.password" type="password" placeholder="设置密码" prefix-icon="Lock" />
            </el-form-item>
            <el-form-item>
              <el-button
                  type="success"
                  style="width: 100%"
                  :loading="isLoading"
                  @click="handleRegister"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" title="安全找回密码" width="30%" :before-close="handleCloseDialog">
      <el-form :model="resetForm" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="resetForm.username" placeholder="请输入您的账号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="resetForm.email" placeholder="请输入注册时的邮箱" />
        </el-form-item>
        <el-form-item label="验证码">
          <el-row :gutter="10">
            <el-col :span="14">
              <el-input v-model="resetForm.code" placeholder="6位验证码" />
            </el-col>
            <el-col :span="10">
              <el-button
                  plain
                  :disabled="timer > 0"
                  @click="handleSendCode"
                  style="width: 100%"
              >
                {{ timer > 0 ? `${timer}s后重发` : '发送验证码' }}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="resetForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取消</el-button>
          <el-button type="primary" :loading="resetLoading" @click="handleResetPassword">重置密码</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()

// 状态变量
const activeTab = ref('login')
const dialogVisible = ref(false)
const isLoading = ref(false)

// 表单数据
const loginForm = ref({ username: '', password: '' })
const regForm = ref({ username: '', email: '', password: '' })

// 找回密码相关
const resetLoading = ref(false)
const timer = ref(0)
const resetForm = ref({
  username: '',
  email: '',
  code: '',
  newPassword: ''
})

// ================= 核心：登录逻辑 =================
const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    return ElMessage.warning('请输入用户名和密码')
  }

  isLoading.value = true
  try {
    // 发起登录请求
    const res = await axios.post('/api/user/login', loginForm.value)

    if (res.data.code === 200) {
      const resultData = res.data.data

      // 🟢 1. 提取 Token 和 用户信息
      // 假设后端返回结构是: { token: 'xxx', user: { role: 'admin', ... } }
      // 如果后端直接返回扁平结构: { token: 'xxx', role: 'admin', ... }
      // 下面这行代码做了兼容处理
      const token = resultData.token
      const userInfo = resultData.user || resultData

      if (token) {
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(userInfo))

        ElMessage.success('登录成功')

        // ✨✨✨ 2. 核心修改：根据角色跳转 ✨✨✨
        // 判断 role 字段 (请确保你数据库里管理员的 role 确实是 'admin')
        if (userInfo.role === 'admin') {
          console.log('检测到管理员，跳转后台...')
          router.push('/admin/dashboard')
        } else {
          console.log('检测到学生，跳转前台...')
          router.push('/dashboard')
        }

      } else {
        ElMessage.error('系统异常：未获取到 Token')
      }
    } else {
      ElMessage.error(res.data.msg || '登录失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('无法连接服务器，请检查后端是否启动')
  } finally {
    isLoading.value = false
  }
}

// ================= 注册逻辑 =================
const handleRegister = async () => {
  if (!regForm.value.username || !regForm.value.password || !regForm.value.email) {
    return ElMessage.warning('请填写完整注册信息')
  }

  isLoading.value = true
  try {
    const res = await axios.post('/api/user/register', regForm.value)

    if (res.data.code === 200) {
      ElMessage.success('注册成功，请登录')
      activeTab.value = 'login'
      // 自动填入刚才注册的用户名
      loginForm.value.username = regForm.value.username
    } else {
      ElMessage.error(res.data.msg || '注册失败')
    }
  } catch (error) {
    ElMessage.error('注册请求超时或失败')
  } finally {
    isLoading.value = false
  }
}

// ================= 找回密码逻辑 =================

// 打开弹窗
const openResetDialog = () => {
  resetForm.value = { username: '', email: '', code: '', newPassword: '' }
  dialogVisible.value = true
}

// 关闭弹窗
const handleCloseDialog = () => {
  dialogVisible.value = false
}

// 1. 发送验证码
const handleSendCode = async () => {
  if (!resetForm.value.email) return ElMessage.warning('请填写邮箱')

  try {
    const res = await axios.post('/api/user/send-code', {
      email: resetForm.value.email,
      username: resetForm.value.username
    })

    if (res.data.code === 200) {
      ElMessage.success('验证码已发送')
      // 倒计时逻辑
      timer.value = 60
      const interval = setInterval(() => {
        timer.value--
        if (timer.value <= 0) clearInterval(interval)
      }, 1000)
    } else {
      ElMessage.error(res.data.msg || '发送失败')
    }
  } catch (error) {
    ElMessage.error('请求发送失败')
  }
}

// 2. 提交重置
const handleResetPassword = async () => {
  const { username, email, code, newPassword } = resetForm.value
  if (!username || !email || !code || !newPassword) {
    return ElMessage.warning('请填写完整信息')
  }

  resetLoading.value = true
  try {
    const res = await axios.post('/api/user/reset-password', {
      username, email, code, newPassword
    })

    if (res.data.code === 200) {
      ElMessage.success('密码重置成功，请重新登录')
      dialogVisible.value = false
      activeTab.value = 'login'
    } else {
      ElMessage.error(res.data.msg || '重置失败')
    }
  } catch (error) {
    ElMessage.error('重置请求异常')
  } finally {
    resetLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f0f2f5;
  background-image: linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%); /* 加个简单的背景色 */
}
.login-card {
  width: 450px;
  border-radius: 10px;
}
</style>