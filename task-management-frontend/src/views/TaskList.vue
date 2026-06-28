<template>
  <div class="task-list">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="filters.keyword" placeholder="搜索任务标题/描述" clearable style="width:220px" @keyup.enter="loadTasks" @clear="loadTasks">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="filters.status" placeholder="状态" clearable style="width:120px" @change="loadTasks">
          <el-option v-for="s in STATUS_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-select v-model="filters.priority" placeholder="优先级" clearable style="width:120px" @change="loadTasks">
          <el-option v-for="p in PRIORITY_OPTIONS" :key="p.value" :label="p.label" :value="p.value" />
        </el-select>
        <el-select v-model="filters.assigneeId" placeholder="负责人" clearable filterable style="width:150px" @change="loadTasks">
          <el-option v-for="u in users" :key="u.id" :label="u.name" :value="u.id" />
        </el-select>
        <el-button @click="loadTasks"><el-icon><Refresh /></el-icon>刷新</el-button>
      </div>
      <div class="toolbar-right">
        <el-button-group>
          <el-button :type="viewMode === 'card' ? 'primary' : ''" @click="viewMode = 'card'"><el-icon><Grid /></el-icon>看板</el-button>
          <el-button :type="viewMode === 'table' ? 'primary' : ''" @click="viewMode = 'table'"><el-icon><Menu /></el-icon>表格</el-button>
        </el-button-group>
        <el-button @click="exportCsv"><el-icon><Download /></el-icon>导出CSV</el-button>
        <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新建任务</el-button>
      </div>
    </div>

    <el-alert v-if="!userStore.isMentor" type="info" :closable="false" class="role-tip">
      当前为实习生视图，仅显示分配给你或你创建的任务。
    </el-alert>

    <!-- 看板视图（拖拽） -->
    <div v-if="viewMode === 'card'" class="kanban">
      <div v-for="col in kanbanCols" :key="col.value" class="kanban-col"
           @dragover.prevent="onDragOver(col.value)" @dragleave="onDragLeave(col.value)" @drop="onDrop(col.value)">
        <div class="kanban-head" :style="{ borderTopColor: col.color }">
          <span>{{ col.label }}</span>
          <el-badge :value="grouped[col.value].length" type="primary" />
        </div>
        <div class="kanban-body" :class="{ 'drag-over': dragOverCol === col.value }">
          <div v-for="task in grouped[col.value]" :key="task.id" class="task-card"
               :style="{ borderLeftColor: priorityMeta(task.priority).color }"
               draggable="true" @dragstart="onDragStart(task)" @click="openDetail(task)">
            <div class="card-title">{{ task.title }}</div>
            <div class="card-tags">
              <el-tag size="small" :type="priorityMeta(task.priority).type">{{ priorityMeta(task.priority).label }}</el-tag>
              <el-tag size="small" :type="isOverdue(task.dueDate, task.status) ? 'danger' : 'info'">
                {{ dueCountdown(task.dueDate) || '无截止' }}
              </el-tag>
            </div>
            <div class="card-footer">
              <span class="assignee">
                <el-avatar :size="22" style="background:#909399;font-size:12px">{{ task.assigneeName?.charAt(0) || '?' }}</el-avatar>
                {{ task.assigneeName || '未分配' }}
              </span>
              <el-icon class="drag-icon"><Rank /></el-icon>
            </div>
          </div>
          <div v-if="grouped[col.value].length === 0" class="empty-col">拖拽任务到此列</div>
        </div>
      </div>
    </div>

    <!-- 表格视图 -->
    <el-table v-else :data="tasks" stripe @row-click="openDetail">
      <el-table-column prop="title" label="任务标题" min-width="200" />
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          <el-select v-model="row.status" size="small" @click.stop @change="quickChangeStatus(row)">
            <el-option v-for="s in STATUS_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="优先级" width="90">
        <template #default="{ row }">
          <el-tag :type="priorityMeta(row.priority).type" size="small">{{ priorityMeta(row.priority).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="assigneeName" label="负责人" width="100" />
      <el-table-column prop="creatorName" label="创建人" width="100" />
      <el-table-column label="截止日期" width="180">
        <template #default="{ row }">
          <span :style="{ color: isOverdue(row.dueDate, row.status) ? '#f56c6c' : '' }">
            {{ row.dueDate || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="tags" label="标签" width="150" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click.stop="removeTask(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑弹窗 -->
    <TaskDialog v-model="dialogVisible" :task-id="editingId" @success="loadTasks" />

    <!-- 任务详情抽屉（含关联资讯） -->
    <el-drawer v-model="detailVisible" title="任务详情" size="480px">
      <div v-if="detail" class="detail">
        <h2>{{ detail.title }}</h2>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="状态">
            <el-tag :type="statusMeta(detail.status).type">{{ statusMeta(detail.status).label }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="priorityMeta(detail.priority).type">{{ priorityMeta(detail.priority).label }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">{{ detail.assigneeName || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ detail.creatorName }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">
            <span :style="{ color: isOverdue(detail.dueDate, detail.status) ? '#f56c6c' : '' }">
              {{ detail.dueDate || '-' }} ({{ dueCountdown(detail.dueDate) }})
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="标签">{{ detail.tags || '-' }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ detail.description || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-actions">
          <el-button type="primary" @click="openEdit(detail)">编辑</el-button>
          <el-button type="danger" @click="removeTask(detail)">删除</el-button>
        </div>

        <!-- 关联资讯 -->
        <div class="related-news" v-if="detail.tags">
          <h3>关联资讯（基于标签：{{ detail.tags }}）</h3>
          <el-button size="small" :loading="newsLoading" @click="loadRelatedNews"><el-icon><Refresh /></el-icon>刷新资讯</el-button>
          <div v-if="relatedNews.length" class="news-list">
            <a v-for="(n, i) in relatedNews" :key="i" :href="n.url" target="_blank" class="news-item">
              <div class="news-title">
                <el-tag v-if="n.matchedTag" size="small" type="success">{{ n.matchedTag }}</el-tag>
                {{ n.title }}
              </div>
              <div class="news-meta">{{ n.source }} · {{ n.publishedAt }}</div>
            </a>
          </div>
          <el-empty v-else-if="!newsLoading" description="暂无关联资讯，点击上方刷新获取" />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { taskApi, userApi, newsApi } from '../api'
import { useUserStore } from '../store/user'
import { STATUS_OPTIONS, PRIORITY_OPTIONS, statusMeta, priorityMeta, dueCountdown, isOverdue } from '../utils/constants'
import TaskDialog from '../components/TaskDialog.vue'

const userStore = useUserStore()
const tasks = ref([])
const users = ref([])
const viewMode = ref('card')
const dialogVisible = ref(false)
const editingId = ref(null)
const detailVisible = ref(false)
const detail = ref(null)
const relatedNews = ref([])
const newsLoading = ref(false)

const filters = reactive({ keyword: '', status: '', priority: '', assigneeId: null })

const kanbanCols = STATUS_OPTIONS
const grouped = computed(() => {
  const g = { TODO: [], IN_PROGRESS: [], DONE: [] }
  tasks.value.forEach((t) => { if (g[t.status]) g[t.status].push(t) })
  return g
})

onMounted(async () => {
  await loadTasks()
  users.value = await userApi.list()
})

async function loadTasks() {
  tasks.value = await taskApi.list(filters)
}

function openCreate() { editingId.value = null; dialogVisible.value = true }
function openEdit(row) { editingId.value = row.id; detailVisible.value = false; dialogVisible.value = true }

async function openDetail(row) {
  detail.value = await taskApi.getById(row.id)
  detailVisible.value = true
  relatedNews.value = []
  if (detail.value.tags) loadRelatedNews()
}

async function loadRelatedNews() {
  newsLoading.value = true
  try {
    relatedNews.value = await newsApi.byTags({ tags: detail.value.tags, size: 5 })
  } finally { newsLoading.value = false }
}

async function quickChangeStatus(row) {
  try {
    await taskApi.changeStatus(row.id, row.status)
    ElMessage.success('状态已更新')
  } catch {
    await loadTasks()
  }
}

async function removeTask(row) {
  await ElMessageBox.confirm(`确认删除任务「${row.title}」？`, '提示', { type: 'warning' })
  await taskApi.remove(row.id)
  ElMessage.success('删除成功')
  detailVisible.value = false
  await loadTasks()
}

// ===== 拖拽 =====
const draggingTask = ref(null)
const dragOverCol = ref(null)
function onDragStart(task) { draggingTask.value = task }
function onDragOver(col) { dragOverCol.value = col }
function onDragLeave(col) { if (dragOverCol.value === col) dragOverCol.value = null }
async function onDrop(col) {
  dragOverCol.value = null
  const task = draggingTask.value
  if (!task || task.status === col) { draggingTask.value = null; return }
  task.status = col
  await quickChangeStatus(task)
  draggingTask.value = null
}

// ===== CSV 导出 =====
function exportCsv() {
  if (!tasks.value.length) { ElMessage.warning('暂无数据'); return }
  const headers = ['ID', '标题', '状态', '优先级', '负责人', '创建人', '截止日期', '标签', '描述']
  const rows = tasks.value.map((t) => [
    t.id, t.title, statusMeta(t.status).label, priorityMeta(t.priority).label,
    t.assigneeName || '', t.creatorName || '', t.dueDate || '', t.tags || '', (t.description || '').replace(/[\r\n,]/g, ' ')
  ])
  const csv = [headers, ...rows].map((r) => r.map((c) => `"${String(c).replace(/"/g, '""')}"`).join(',')).join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `任务列表_${new Date().toLocaleDateString()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('已导出 CSV')
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 8px; }
.toolbar-left, .toolbar-right { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.role-tip { margin-bottom: 16px; }
.kanban { display: flex; gap: 16px; min-height: 500px; }
.kanban-col { flex: 1; background: #fff; border-radius: 8px; display: flex; flex-direction: column; min-width: 280px; }
.kanban-head { padding: 12px 16px; font-weight: 600; border-top: 3px solid #409eff; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.kanban-body { padding: 10px; flex: 1; overflow-y: auto; min-height: 200px; transition: background 0.2s; }
.kanban-body.drag-over { background: #ecf5ff; }
.task-card { background: #fff; border: 1px solid #ebeef5; border-left: 4px solid #409eff; border-radius: 6px; padding: 12px; margin-bottom: 10px; cursor: grab; transition: box-shadow 0.2s; }
.task-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.task-card:active { cursor: grabbing; }
.card-title { font-size: 14px; font-weight: 500; margin-bottom: 8px; line-height: 1.4; }
.card-tags { display: flex; gap: 6px; margin-bottom: 8px; flex-wrap: wrap; }
.card-footer { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #909399; }
.assignee { display: flex; align-items: center; gap: 4px; }
.drag-icon { color: #c0c4cc; }
.empty-col { text-align: center; color: #c0c4cc; padding: 40px 0; font-size: 13px; }
.detail h2 { font-size: 18px; margin-bottom: 16px; }
.detail-actions { margin: 16px 0; display: flex; gap: 8px; }
.related-news { border-top: 1px solid #eee; padding-top: 16px; }
.related-news h3 { font-size: 15px; margin-bottom: 10px; }
.news-list { margin-top: 10px; }
.news-item { display: block; padding: 10px; border: 1px solid #ebeef5; border-radius: 6px; margin-bottom: 8px; transition: background 0.2s; }
.news-item:hover { background: #f5f7fa; }
.news-title { font-size: 13px; line-height: 1.5; display: flex; gap: 6px; align-items: flex-start; }
.news-meta { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
