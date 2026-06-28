import request from './request'

// ===== 认证 =====
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data)
}

// ===== 任务 =====
export const taskApi = {
  list: (params) => request.get('/tasks', { params }),
  getById: (id) => request.get(`/tasks/${id}`),
  create: (data) => request.post('/tasks', data),
  update: (id, data) => request.put(`/tasks/${id}`, data),
  changeStatus: (id, status) => request.patch(`/tasks/${id}/status`, { status }),
  remove: (id) => request.delete(`/tasks/${id}`),
  dashboard: () => request.get('/tasks/dashboard')
}

// ===== 用户 =====
export const userApi = {
  list: () => request.get('/users')
}

// ===== 资讯 =====
export const newsApi = {
  latest: (params) => request.get('/news', { params }),
  byTags: (params) => request.get('/news/by-tags', { params })
}
