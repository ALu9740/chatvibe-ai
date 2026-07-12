import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from '@/common/router'
import { useAuthStore } from '@/module/auth/stores/auth'
import './style.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

// 路由守卫执行前，从 localStorage 恢复登录态
useAuthStore().restore()

app.mount('#app')
