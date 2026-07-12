import { ref } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'auto'
type Resolved = 'light' | 'dark'

const STORAGE_KEY = 'cv_theme'

/** 自动模式：6:00-18:00 为白天，其余为黑夜 */
function resolveAuto(): Resolved {
  const h = new Date().getHours()
  return h >= 6 && h < 18 ? 'light' : 'dark'
}

function resolve(mode: ThemeMode): Resolved {
  return mode === 'auto' ? resolveAuto() : mode
}

function apply(r: Resolved) {
  document.documentElement.setAttribute('data-theme', r)
}

const stored = (localStorage.getItem(STORAGE_KEY) as ThemeMode) || 'dark'
const mode = ref<ThemeMode>(stored)
const resolved = ref<Resolved>(resolve(stored))

// 模块加载时立即应用主题，避免闪烁
apply(resolved.value)

// 自动模式：每分钟检查一次时间，必要时切换
let autoTimer: number | null = null
function setupAutoCheck() {
  if (autoTimer !== null) {
    clearInterval(autoTimer)
    autoTimer = null
  }
  if (mode.value === 'auto') {
    autoTimer = window.setInterval(() => {
      const r = resolveAuto()
      if (r !== resolved.value) {
        resolved.value = r
        apply(r)
      }
    }, 60000)
  }
}
setupAutoCheck()

export function useTheme() {
  function setMode(m: ThemeMode) {
    mode.value = m
    localStorage.setItem(STORAGE_KEY, m)
    resolved.value = resolve(m)
    apply(resolved.value)
    setupAutoCheck()
  }

  return { mode, resolved, setMode }
}
