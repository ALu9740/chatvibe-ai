import http, { SSE_BASE } from './http'
import { streamSse } from './sse'

export interface ChatSession {
  id: number
  title: string
  createdAt: string
}

export interface ChatMessageItem {
  role: string
  content: string
}

// 会话列表：GET /api/chat/sessions
export function getSessions() {
  return http.get<ChatSession[]>('/chat/sessions').then((r) => r.data)
}

// 会话消息：GET /api/chat/sessions/{id}/messages
export function getMessages(id: number) {
  return http.get<ChatMessageItem[]>(`/chat/sessions/${id}/messages`).then((r) => r.data)
}

// 删除会话：DELETE /api/chat/sessions/{id}
export function deleteSession(id: number) {
  return http.delete<{ ok: boolean }>(`/chat/sessions/${id}`).then((r) => r.data)
}

// 发送聊天：POST /api/chat（FormData，text/event-stream）
export function sendChat(
  data: { prompt: string; chatId: number | null; files?: File[] },
  onChunk: (chunk: string) => void,
): Promise<void> {
  const fd = new FormData()
  fd.append('prompt', data.prompt)
  if (data.chatId != null) fd.append('chatId', String(data.chatId))
  data.files?.forEach((f) => fd.append('files', f))
  return streamSse(`${SSE_BASE}/chat`, { method: 'POST', body: fd }, onChunk)
}
