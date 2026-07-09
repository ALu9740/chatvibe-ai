import http, { API_BASE, SSE_BASE } from './http'
import { streamSse } from './sse'

export interface PdfSession {
  id: number
  fileName: string
}

// 上传 PDF：POST /api/pdf/upload/{chatId}
export function uploadPdf(chatId: number, file: File) {
  const fd = new FormData()
  fd.append('file', file)
  return http
    .post<{ ok: boolean; fileName: string; sessionId: number }>(`/pdf/upload/${chatId}`, fd)
    .then((r) => r.data)
}

// PDF 问答：GET /api/pdf/chat?prompt=&chatId=（text/event-stream）
export function pdfChat(prompt: string, chatId: number, onChunk: (chunk: string) => void) {
  const url = `${SSE_BASE}/pdf/chat?prompt=${encodeURIComponent(prompt)}&chatId=${chatId}`
  return streamSse(url, { method: 'GET' }, onChunk)
}

// PDF 会话列表：GET /api/pdf/sessions
export function getPdfSessions() {
  return http.get<PdfSession[]>('/pdf/sessions').then((r) => r.data)
}

// 下载 PDF 文件为 Blob URL（携带 JWT，供 iframe 预览）
export async function fetchPdfBlobUrl(chatId: number): Promise<string> {
  const token = localStorage.getItem('cv_token')
  const res = await fetch(`${API_BASE}/pdf/file/${chatId}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  if (!res.ok) throw new Error('PDF 加载失败')
  const blob = await res.blob()
  return URL.createObjectURL(blob)
}
