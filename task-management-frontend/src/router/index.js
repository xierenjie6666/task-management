import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/tasks',
    children: [
      { path: 'tasks', name: 'Tasks', component: () => import('../views/TaskList.vue'), meta: { title: '任务管理' } },
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '个人仪表盘' } },
      { path: 'news', name: 'News', component: () => import('../views/News.vue'), meta: { title: '实时资讯' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：未登录跳转登录页
router.beforeEach((to) => {
  const userStore = useUserStore()
  if (!to.meta.public && !userStore.token) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'Login' && userStore.token) {
    return { name: 'Tasks' }
  }
})

export default router
