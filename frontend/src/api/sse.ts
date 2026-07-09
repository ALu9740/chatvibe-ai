// 基于 fetch ReadableStream 的 SSE 封装
// 约定：data: <html-chunk>\n\n 流式输出，data: [DONE]\n\n 终止
// 若 data 内容为 JSON 对象，则视为元信息（如共情评分）

export type SseMeta = Record<string, unknown>
export type SseChunkHandler = (chunk: string) => void
export type SseMetaHandler = (meta: SseMeta) => void

/**
 * 发起 SSE 流式请求
 * @param url 完整请求地址
 * @param init fetch 初始化参数（method/body 等）
 * @param onChunk 接收 HTML 文本块的回调
 * @param onMeta 可选，接收 JSON 元信息的回调
 */
export async function streamSse(
  url: string,
  init: RequestInit,
  onChunk: SseChunkHandler,
  onMeta?: SseMetaHandler,
): Promise<void> {
  const token = localStorage.getItem('cv_token')
  const headers = new Headers(init.headers)
  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (!headers.has('Accept')) headers.set('Accept', 'text/event-stream')

  const res = await fetch(url, { ...init, headers })
  if (!res.ok || !res.body) {
    throw new Error(`SSE 连接失败：HTTP ${res.status}`)
  }

  const reader = res.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let stopped = false

  const handleEvent = (raw: string): void => {
    const dataLines = raw
      .split('\n')
      .map((line) => line.trim())
      .filter((line) => line.startsWith('data:'))
      .map((line) => line.slice(5).replace(/^\s/, ''))
    if (dataLines.length === 0) return
    const data = dataLines.join('\n')
    if (data === '[DONE]') {
      stopped = true
      return
    }
    // 尝试解析为 JSON 元信息
    if (data.startsWith('{') && data.endsWith('}')) {
      try {
        const parsed = JSON.parse(data) as SseMeta
        onMeta?.(parsed)
        return
      } catch {
        // 非合法 JSON，按普通文本块处理
      }
    }
    onChunk(data)
  }

  while (!stopped) {
    const { value, done } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    let idx: number
    while ((idx = buffer.indexOf('\n\n')) !== -1) {
      const rawEvent = buffer.slice(0, idx)
      buffer = buffer.slice(idx + 2)
      handleEvent(rawEvent)
      if (stopped) break
    }
  }
  // 处理流末尾残余数据
  if (!stopped && buffer.trim()) {
    handleEvent(buffer)
  }
}
