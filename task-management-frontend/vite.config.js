import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// AI 辅助说明：前端由 AI 工具协助生成骨架与页面实现
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    open: true,
    // 代理后端接口，避免跨域（开发环境）
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
