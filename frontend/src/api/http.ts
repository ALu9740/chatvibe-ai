import axios from 'axios'

// 后端基础地址：取自环境变量，默认 /api（走 Vite 代理，适用于普通 HTTP 请求）
export const API_BASE = import.meta.env.VITE_API_BASE || '/api'

// SSE 直连地址：绕过 Vite 代理，避免代理缓冲导致流式输出失效
// 动态使用当前页面 hostname，确保无论通过 localhost 还是 chatvibe.icu 访问都能连通
export const SSE_BASE = import.meta.env.VITE_SSE_BASE || `https://${location.hostname}:8080/api`

const http = axios.create({
  baseURL: API_BASE,
  timeout: 60000,
})

// 请求拦截器：注入 JWT
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('cv_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：自动解包 Result<T>（{code, message, data}）并处理 401
http.interceptors.response.use(
  (res) => {
    // 后端统一返回 { code, message, data }；code === 0 表示成功
    const body = res.data
    if (body && typeof body === 'object' && 'code' in body && 'data' in body) {
      if (body.code === 0) {
        res.data = body.data
        return res
      }
      return Promise.reject(new Error(body.message || `请求失败 (code=${body.code})`))
    }
    return res
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      localStorage.removeItem('cv_token')
      if (!location.pathname.startsWith('/login')) {
        const redirect = encodeURIComponent(location.pathname + location.search)
        location.href = `/login?redirect=${redirect}`
      }
    }
    return Promise.reject(error)
  },
)

export default http
