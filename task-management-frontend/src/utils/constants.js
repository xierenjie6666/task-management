// 任务状态 / 优先级常量映射（含颜色、标签文案）

export const STATUS_OPTIONS = [
  { value: 'TODO', label: '待办', color: '#909399', type: 'info' },
  { value: 'IN_PROGRESS', label: '进行中', color: '#409eff', type: 'primary' },
  { value: 'DONE', label: '已完成', color: '#67c23a', type: 'success' }
]

export const PRIORITY_OPTIONS = [
  { value: 'HIGH', label: '高', color: '#f56c6c', type: 'danger' },
  { value: 'MEDIUM', label: '中', color: '#e6a23c', type: 'warning' },
  { value: 'LOW', label: '低', color: '#909399', type: 'info' }
]

export function statusMeta(value) {
  return STATUS_OPTIONS.find((s) => s.value === value) || STATUS_OPTIONS[0]
}
export function priorityMeta(value) {
  return PRIORITY_OPTIONS.find((p) => p.value === value) || PRIORITY_OPTIONS[1]
}

/**
 * 截止日期倒计时文案。
 */
export function dueCountdown(dueDate) {
  if (!dueDate) return ''
  const due = new Date(dueDate).getTime()
  const now = Date.now()
  const diff = due - now
  if (diff <= 0) return '已逾期'
  const days = Math.floor(diff / 86400000)
  const hours = Math.floor((diff % 86400000) / 3600000)
  if (days > 0) return `剩余 ${days} 天 ${hours} 小时`
  return `剩余 ${hours} 小时`
}

export function isOverdue(dueDate, status) {
  if (!dueDate || status === 'DONE') return false
  return new Date(dueDate).getTime() < Date.now()
}
