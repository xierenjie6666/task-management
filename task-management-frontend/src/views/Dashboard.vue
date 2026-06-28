<template>
  <div class="dashboard">
    <div class="stat-cards">
      <div class="stat-card" style="border-left-color:#909399">
        <div class="stat-num">{{ stats.total }}</div>
        <div class="stat-label">任务总数</div>
      </div>
      <div class="stat-card" style="border-left-color:#409eff">
        <div class="stat-num">{{ stats.inProgress }}</div>
        <div class="stat-label">进行中</div>
      </div>
      <div class="stat-card" style="border-left-color:#67c23a">
        <div class="stat-num">{{ stats.done }}</div>
        <div class="stat-label">已完成</div>
      </div>
      <div class="stat-card" style="border-left-color:#e6a23c">
        <div class="stat-num">{{ stats.completionRate }}%</div>
        <div class="stat-label">完成率</div>
      </div>
    </div>

    <div class="charts">
      <div class="chart-box">
        <div class="chart-title">任务状态分布</div>
        <div ref="pieRef" class="chart"></div>
      </div>
      <div class="chart-box">
        <div class="chart-title">任务优先级分布</div>
        <div ref="barRef" class="chart"></div>
      </div>
    </div>

    <el-card class="my-tasks">
      <template #header>
        <span>我的待办任务</span>
        <el-button link type="primary" @click="$router.push('/tasks')">查看全部</el-button>
      </template>
      <el-table :data="todoTasks" stripe>
        <el-table-column prop="title" label="任务" min-width="200" />
        <el-table-column label="优先级" width="90">
          <template #default="{ row }"><el-tag :type="priorityMeta(row.priority).type" size="small">{{ priorityMeta(row.priority).label }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="100" />
        <el-table-column label="倒计时" width="140">
          <template #default="{ row }">
            <span :style="{ color: isOverdue(row.dueDate, row.status) ? '#f56c6c' : '#909399' }">{{ dueCountdown(row.dueDate) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { taskApi } from '../api'
import { useUserStore } from '../store/user'
import { priorityMeta, dueCountdown, isOverdue } from '../utils/constants'

const userStore = useUserStore()
const stats = ref({ total: 0, todo: 0, inProgress: 0, done: 0, completionRate: 0 })
const todoTasks = ref([])
const pieRef = ref()
const barRef = ref()
let pieChart, barChart

onMounted(async () => {
  const data = await taskApi.dashboard()
  stats.value = data
  todoTasks.value = (data.tasks || []).filter((t) => t.status !== 'DONE')
  await nextTick()
  renderCharts(data.tasks || [])
  window.addEventListener('resize', resize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  pieChart?.dispose()
  barChart?.dispose()
})

function renderCharts(tasks) {
  // 饼图：状态分布
  pieChart = echarts.init(pieRef.value)
  const statusCount = { TODO: 0, IN_PROGRESS: 0, DONE: 0 }
  tasks.forEach((t) => { statusCount[t.status] = (statusCount[t.status] || 0) + 1 })
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['40%', '70%'], avoidLabelOverlap: false,
      label: { show: true, formatter: '{b}: {c}' },
      data: [
        { value: statusCount.TODO, name: '待办', itemStyle: { color: '#909399' } },
        { value: statusCount.IN_PROGRESS, name: '进行中', itemStyle: { color: '#409eff' } },
        { value: statusCount.DONE, name: '已完成', itemStyle: { color: '#67c23a' } }
      ]
    }]
  })

  // 柱状图：优先级分布
  barChart = echarts.init(barRef.value)
  const priCount = { HIGH: 0, MEDIUM: 0, LOW: 0 }
  tasks.forEach((t) => { priCount[t.priority] = (priCount[t.priority] || 0) + 1 })
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['高', '中', '低'] },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar', barWidth: '40%',
      data: [
        { value: priCount.HIGH, itemStyle: { color: '#f56c6c' } },
        { value: priCount.MEDIUM, itemStyle: { color: '#e6a23c' } },
        { value: priCount.LOW, itemStyle: { color: '#909399' } }
      ]
    }]
  })
}

function resize() { pieChart?.resize(); barChart?.resize() }
</script>

<style scoped>
.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 8px; padding: 24px; border-left: 4px solid #409eff; text-align: center; }
.stat-num { font-size: 32px; font-weight: 700; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 6px; }
.charts { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px; }
.chart-box { background: #fff; border-radius: 8px; padding: 16px; }
.chart-title { font-size: 15px; font-weight: 600; margin-bottom: 10px; }
.chart { height: 300px; }
.my-tards { }
</style>
