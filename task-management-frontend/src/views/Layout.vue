<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon :size="22" color="#fff"><List /></el-icon>
        <span>任务管理系统</span>
      </div>
      <el-menu :default-active="route.path" router class="aside-menu" background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/tasks"><el-icon><Tickets /></el-icon><span>任务管理</span></el-menu-item>
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><span>个人仪表盘</span></el-menu-item>
        <el-menu-item index="/news"><el-icon><Notification /></el-icon><span>实时资讯</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">{{ route.meta.title }}</div>
        <div class="header-right">
          <el-tag :type="userStore.isMentor ? 'danger' : 'success'" effect="plain" size="small">
            {{ userStore.isMentor ? '导师' : '实习生' }}
          </el-tag>
          <el-dropdown @command="onCommand">
            <span class="user-info">
              <el-avatar :size="32" style="background:#409eff">{{ userStore.userInfo?.name?.charAt(0) }}</el-avatar>
              <span>{{ userStore.userInfo?.name }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function onCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #304156; overflow: hidden; }
.logo {
  height: 60px; display: flex; align-items: center; gap: 10px;
  padding: 0 20px; color: #fff; font-size: 16px; font-weight: 600;
  background: #2b3a4d;
}
.aside-menu { border-right: none; }
.header {
  background: #fff; display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
}
.header-left { font-size: 18px; font-weight: 600; }
.header-right { display: flex; align-items: center; gap: 16px; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; color: #303133; }
.main { background: #f0f2f5; padding: 20px; overflow-y: auto; }
</style>
