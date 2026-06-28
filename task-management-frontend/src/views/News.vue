<template>
  <div class="news-page">
    <div class="news-toolbar">
      <el-input v-model="keyword" placeholder="搜索资讯关键词（如 Java、Vue、AI）" clearable style="width:320px" @keyup.enter="loadNews" @clear="loadNews">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" :loading="loading" @click="loadNews"><el-icon><Refresh /></el-icon>刷新资讯</el-button>
      <span class="source-tip">数据来源：Hacker News API + HNRSS（5 分钟缓存）</span>
    </div>

    <el-alert type="info" :closable="false" class="info-tip">
      本模块调用第三方公开 API（Hacker News Firebase）与 RSS（hnrss.org/frontpage）获取最新科技/编程资讯，并在任务详情页按标签自动关联展示。
    </el-alert>

    <div v-loading="loading" class="news-grid">
      <a v-for="(n, i) in news" :key="i" :href="n.url" target="_blank" class="news-card">
        <div class="news-card-title">
          <el-tag v-if="n.matchedTag" size="small" type="success">{{ n.matchedTag }}</el-tag>
          {{ n.title }}
        </div>
        <div class="news-card-summary" v-if="n.summary">{{ stripHtml(n.summary).slice(0, 120) }}...</div>
        <div class="news-card-meta">
          <el-tag size="small" :type="n.source === 'Hacker News' ? 'primary' : 'warning'">{{ n.source }}</el-tag>
          <span>{{ n.publishedAt }}</span>
        </div>
      </a>
      <el-empty v-if="!loading && !news.length" description="暂无资讯，请检查网络或点击刷新" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { newsApi } from '../api'

const news = ref([])
const keyword = ref('')
const loading = ref(false)

onMounted(() => loadNews())

async function loadNews() {
  loading.value = true
  try {
    news.value = await newsApi.latest({ keyword: keyword.value, size: 20 })
  } catch {
    ElMessage.warning('资讯获取失败，可能是网络问题，可稍后重试')
  } finally {
    loading.value = false
  }
}

function stripHtml(html) {
  return html.replace(/<[^>]+>/g, '')
}
</script>

<style scoped>
.news-toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.source-tip { font-size: 12px; color: #909399; }
.info-tip { margin-bottom: 16px; }
.news-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px; }
.news-card { background: #fff; border: 1px solid #ebeef5; border-radius: 8px; padding: 16px; transition: all 0.2s; display: flex; flex-direction: column; }
.news-card:hover { box-shadow: 0 6px 16px rgba(0,0,0,0.1); transform: translateY(-2px); }
.news-card-title { font-size: 15px; font-weight: 600; line-height: 1.5; margin-bottom: 8px; display: flex; gap: 6px; align-items: flex-start; }
.news-card-summary { font-size: 13px; color: #606266; line-height: 1.5; margin-bottom: 10px; flex: 1; }
.news-card-meta { display: flex; align-items: center; gap: 8px; font-size: 12px; color: #909399; }
</style>
