import { useChatStore } from '../stores/chat'

// 聊天/PDF/学会哄人 统一 composable
export function useChat() {
  return useChatStore()
}
