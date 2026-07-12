import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as chatApi from '@/module/chat/api/chat'
import * as pdfApi from '@/module/pdf/api/pdf'
import { comfortChat as apiComfortChat } from '@/module/comfort/api/comfort'
import type { SseMeta } from '@/common/api/sse'

export interface Session {
  id: number
  title?: string
  fileName?: string
  createdAt?: string
}

export interface Message {
  role: 'user' | 'assistant'
  content: string
  streaming?: boolean
  meta?: { score?: number; tip?: string }
}

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<Session[]>([])
  const currentId = ref<number | null>(null)
  const messages = ref<Message[]>([])
  const streaming = ref(false)

  // 加载普通聊天会话列表
  async function loadSessions() {
    sessions.value = await chatApi.getSessions()
  }

  // 选中某个会话并加载历史消息
  async function selectSession(id: number) {
    currentId.value = id
    const list = await chatApi.getMessages(id)
    messages.value = list.map((m) => ({
      role: (m.role === 'user' ? 'user' : 'assistant') as 'user' | 'assistant',
      content: m.content,
    }))
  }

  // 新建会话（本地占位，待首条消息后由后端落库）
  function newSession() {
    currentId.value = null
    messages.value = []
  }

  // 删除会话
  async function deleteSession(id: number) {
    await chatApi.deleteSession(id)
    if (currentId.value === id) {
      currentId.value = null
      messages.value = []
    }
    await loadSessions()
  }

  // 发送聊天消息（SSE 流式）
  async function sendPrompt(text: string, files?: File[]) {
    if (streaming.value || !text.trim()) return
    streaming.value = true
    const wasNew = currentId.value == null
    messages.value.push({ role: 'user', content: text })
    messages.value.push({ role: 'assistant', content: '', streaming: true })
    // 通过响应式数组访问代理对象，确保修改能触发 UI 更新
    const aiIndex = messages.value.length - 1
    try {
      await chatApi.sendChat({ prompt: text, chatId: currentId.value, files }, (chunk) => {
        messages.value[aiIndex].content += chunk
      })
    } catch {
      messages.value[aiIndex].content += '\n\n_（连接中断，请稍后重试）_'
    } finally {
      messages.value[aiIndex].streaming = false
      streaming.value = false
    }
    // 新会话首条消息后，刷新列表并锁定到最新会话
    if (wasNew) {
      await loadSessions()
      const last = sessions.value[sessions.value.length - 1]
      if (last) currentId.value = last.id
    }
  }

  // 加载 PDF 会话列表
  async function loadPdfSessions() {
    sessions.value = await pdfApi.getPdfSessions()
  }

  // PDF 问答（SSE 流式）；新会话由客户端生成 chatId
  async function sendPdfPrompt(prompt: string) {
    if (streaming.value || !prompt.trim()) return
    if (currentId.value == null) currentId.value = Date.now()
    const chatId = currentId.value
    streaming.value = true
    messages.value.push({ role: 'user', content: prompt })
    messages.value.push({ role: 'assistant', content: '', streaming: true })
    const aiIndex = messages.value.length - 1
    try {
      await pdfApi.pdfChat(prompt, chatId, (chunk) => {
        messages.value[aiIndex].content += chunk
      })
    } catch {
      messages.value[aiIndex].content += '\n\n_（连接中断，请稍后重试）_'
    } finally {
      messages.value[aiIndex].streaming = false
      streaming.value = false
    }
  }

  // 学会哄人对话（SSE 流式），可能在结束时收到 {score, tip} 元信息
  async function sendComfort(scenario: string, prompt: string, context?: string) {
    if (streaming.value || !prompt.trim()) return
    if (currentId.value == null) currentId.value = Date.now()
    const chatId = currentId.value
    streaming.value = true
    messages.value.push({ role: 'user', content: prompt })
    messages.value.push({ role: 'assistant', content: '', streaming: true })
    const aiIndex = messages.value.length - 1
    try {
      await apiComfortChat(
        { scenario, chatId, prompt, context },
        (chunk) => {
          messages.value[aiIndex].content += chunk
        },
        (meta: SseMeta) => {
          const score = typeof meta.score === 'number' ? meta.score : undefined
          const tip = typeof meta.tip === 'string' ? meta.tip : undefined
          if (score !== undefined || tip !== undefined) {
            messages.value[aiIndex].meta = { score, tip }
          }
        },
      )
    } catch {
      messages.value[aiIndex].content += '\n\n_（连接中断，请稍后重试）_'
    } finally {
      messages.value[aiIndex].streaming = false
      streaming.value = false
    }
  }

  return {
    sessions,
    currentId,
    messages,
    streaming,
    loadSessions,
    selectSession,
    newSession,
    deleteSession,
    sendPrompt,
    loadPdfSessions,
    sendPdfPrompt,
    sendComfort,
  }
})
