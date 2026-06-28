<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>内部任务管理系统</h1>
        <p>实习生 & 导师 · 任务协作平台</p>
      </div>
      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @keyup.enter="handleLogin">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="用户名" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password />
            </el-form-item>
            <el-button type="primary" size="large" :loading="loading" style="width:100%" @click="handleLogin">登 录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form ref="regFormRef" :model="regForm" :rules="regRules" @keyup.enter="handleRegister">
            <el-form-item prop="username">
              <el-input v-model="regForm.username" placeholder="用户名" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="name">
              <el-input v-model="regForm.name" placeholder="姓名" :prefix-icon="UserFilled" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="regForm.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password />
            </el-form-item>
            <el-form-item prop="role">
              <el-select v-model="regForm.role" size="large" style="width:100%">
                <el-option label="实习生" value="INTERN" />
                <el-option label="导师" value="MENTOR" />
              </el-select>
            </el-form-item>
            <el-button type="primary" size="large" :loading="loading" style="width:100%" @click="handleRegister">注 册</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <el-alert type="info" :closable="false" class="demo-tip">
        <template #title>演示账号：mentor / 123456（导师） · intern01 / 123456（实习生）</template>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { authApi } from '../api'
import { useUserStore } from '../store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref()
const regFormRef = ref()

const loginForm = reactive({ username: 'mentor', password: '123456' })
const regForm = reactive({ username: '', name: '', password: '', role: 'INTERN' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const regRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }]
}

async function handleLogin() {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    const data = await authApi.login(loginForm)
    userStore.setLogin(data)
    ElMessage.success(`欢迎回来，${data.name}`)
    router.push(route.query.redirect || '/tasks')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  await regFormRef.value.validate()
  loading.value = true
  try {
    const data = await authApi.register(regForm)
    userStore.setLogin(data)
    ElMessage.success('注册成功')
    router.push('/tasks')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  background: #fff;
  border-radius: 12px;
  padding: 36px 40px 28px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.2);
}
.login-header { text-align: center; margin-bottom: 20px; }
.login-header h1 { font-size: 22px; color: #303133; }
.login-header p { font-size: 13px; color: #909399; margin-top: 6px; }
.login-tabs { margin-bottom: 12px; }
.demo-tip { margin-top: 10px; }
</style>
