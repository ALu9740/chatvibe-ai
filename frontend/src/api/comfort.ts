import http, { SSE_BASE } from './http'
import { streamSse, type SseMeta } from './sse'

export interface ComfortScenario {
  key: string
  label: string
  desc: string
}

// 场景列表：GET /api/comfort/scenarios
export function getScenarios() {
  return http.get<ComfortScenario[]>('/comfort/scenarios').then((r) => r.data)
}

// 学会哄人对话：POST /api/comfort/chat（JSON，text/event-stream）
// [DONE] 前可能发送一次 {score, tip} 元信息
export function comfortChat(
  data: { scenario: string; chatId: number | null; prompt: string; context?: string },
  onChunk: (chunk: string) => void,
  onMeta?: (meta: SseMeta) => void,
) {
  return streamSse(
    `${SSE_BASE}/comfort/chat`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    },
    onChunk,
    onMeta,
  )
}
