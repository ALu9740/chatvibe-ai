import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, fetchMe as apiFetchMe } from '@/module/auth/api/auth'

export interface AuthUser {
  username: string
  role: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null)
  const user = ref<AuthUser | null>(null)

  const isAuthed = computed(() => !!token.value)

  // 登录并持久化 token
  async function login(username: string, password: string) {
    const res = await apiLogin(username, password)
    token.value = res.token
    localStorage.setItem('cv_token', res.token)
    await fetchMe()
  }

  // 拉取当前用户信息
  async function fetchMe() {
    try {
      user.value = await apiFetchMe()
    } catch {
      user.value = null
    }
  }

  // 登出
  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('cv_token')
  }

  // 从 localStorage 恢复 token
  function restore() {
    const t = localStorage.getItem('cv_token')
    if (t) token.value = t
  }

  return { token, user, isAuthed, login, fetchMe, logout, restore }
})
