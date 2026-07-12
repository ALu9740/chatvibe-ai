import { useAuthStore } from '@/module/auth/stores/auth'

// 鉴权 composable：暴露 auth store 的常用能力
export function useAuth() {
  return useAuthStore()
}
