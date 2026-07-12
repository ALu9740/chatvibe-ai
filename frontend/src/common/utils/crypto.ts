/**
 * AES-256-GCM 加密工具
 * 用于前后端数据加密传输
 */

// AES 密钥：通过 Vite 环境变量 VITE_AES_KEY 注入（见 frontend/.env，已 gitignore）
// 必须与后端 application-local.yml 中的 chatvibe.security.aes.key 保持一致
// 生成: openssl rand -base64 32
const AES_KEY = import.meta.env.VITE_AES_KEY
if (!AES_KEY) {
  throw new Error(
    'AES 密钥未配置：请在 frontend/.env 中设置 VITE_AES_KEY（参考 .env.example）',
  )
}

/**
 * 从 Base64 字符串派生 AES 密钥
 */
async function deriveKey(keyBase64: string): Promise<CryptoKey> {
  const keyData = Uint8Array.from(atob(keyBase64), c => c.charCodeAt(0))
  return await crypto.subtle.importKey(
    'raw',
    keyData,
    { name: 'AES-GCM' },
    false,
    ['encrypt', 'decrypt']
  )
}

/**
 * 加密字符串
 * @param plaintext 明文
 * @returns Base64(IV + 密文 + Tag)
 */
export async function encrypt(plaintext: string): Promise<string> {
  if (!plaintext) return plaintext

  try {
    const key = await deriveKey(AES_KEY)
    const iv = crypto.getRandomValues(new Uint8Array(12)) // 12 字节 IV
    const encoder = new TextEncoder()
    const data = encoder.encode(plaintext)

    const ciphertext = await crypto.subtle.encrypt(
      { name: 'AES-GCM', iv },
      key,
      data
    )

    // 拼接 IV + 密文
    const combined = new Uint8Array(iv.length + ciphertext.byteLength)
    combined.set(iv, 0)
    combined.set(new Uint8Array(ciphertext), iv.length)

    // 返回 Base64
    return btoa(String.fromCharCode(...combined))
  } catch (error) {
    console.error('加密失败:', error)
    throw new Error('加密失败')
  }
}

/**
 * 解密字符串
 * @param ciphertext Base64(IV + 密文 + Tag)
 * @returns 明文
 */
export async function decrypt(ciphertext: string): Promise<string> {
  if (!ciphertext) return ciphertext

  try {
    const key = await deriveKey(AES_KEY)
    const combined = Uint8Array.from(atob(ciphertext), c => c.charCodeAt(0))

    const iv = combined.slice(0, 12)
    const data = combined.slice(12)

    const decrypted = await crypto.subtle.decrypt(
      { name: 'AES-GCM', iv },
      key,
      data
    )

    const decoder = new TextDecoder()
    return decoder.decode(decrypted)
  } catch (error) {
    console.error('解密失败:', error)
    throw new Error('解密失败')
  }
}