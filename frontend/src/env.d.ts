/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE?: string
  readonly VITE_SSE_BASE?: string
  readonly VITE_AES_KEY: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
