import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import fs from 'fs'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    https: {
      // 使用后端相同的SSL证书
      cert: fs.readFileSync(path.resolve(__dirname, '../backend/src/main/resources/keystore/chatvibe.icu+3.pem')),
      key: fs.readFileSync(path.resolve(__dirname, '../backend/src/main/resources/keystore/chatvibe.icu+3-key.pem')),
    },
    port: 5173,
    proxy: {
      '/api': {
        target: 'https://localhost:8080',
        changeOrigin: true,
        secure: false, // 开发环境忽略证书验证
      },
    },
  },
})
