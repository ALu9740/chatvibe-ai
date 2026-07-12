import http from '@/common/api/http'
import { encrypt } from '@/common/utils/crypto'

export interface LoginResp {
  token: string
  expiresIn: number
}

export interface MeResp {
  username: string
  role: string
}

export interface RegisterBody {
  username: string
  password: string
}

// 登录：POST /api/auth/login（密码加密传输）
export async function login(username: string, password: string) {
  // 加密密码
  const encryptedPassword = await encrypt(password)
  return http.post<LoginResp>('/auth/login', { username, password: encryptedPassword }).then((r) => r.data)
}

// 当前用户：GET /api/auth/me
export function fetchMe() {
  return http.get<MeResp>('/auth/me').then((r) => r.data)
}

// 注册：POST /api/auth/register（接口已禁用，返回 410）
export function register(body: RegisterBody) {
  return http.post('/auth/register', body).then((r) => r.data)
}
