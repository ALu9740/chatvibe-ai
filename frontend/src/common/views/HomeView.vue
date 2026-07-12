<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { MessageSquare, FileText, HeartHandshake, ArrowRight } from 'lucide-vue-next'
import { useAuthStore } from '@/module/auth/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const cards = [
  { to: '/chat', title: 'AI 聊天', desc: '多模态对话 · 流式响应', icon: MessageSquare },
  { to: '/pdf', title: 'ChatPDF', desc: 'PDF 知识库问答', icon: FileText },
  { to: '/comfort', title: '学会哄人', desc: '情感沟通教练', icon: HeartHandshake },
] as const

function go(to: string) {
  router.push(to)
}

// 动态问候语与时间
const now = ref(new Date())

const greeting = computed(() => {
  const h = now.value.getHours()
  if (h >= 0 && h < 6) return '凌晨好'
  if (h < 11) return '早上好'
  if (h < 13) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const clock = computed(() => {
  const d = now.value
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${hh}:${mm}:${ss}`
})

let timer: number | null = null

onMounted(async () => {
  timer = window.setInterval(() => {
    now.value = new Date()
  }, 1000)
  if (auth.token && !auth.user) {
    await auth.fetchMe()
  }
})

onUnmounted(() => {
  if (timer !== null) clearInterval(timer)
})
</script>

<template>
  <section class="mx-auto max-w-6xl px-4 py-16 sm:px-6 sm:py-20">
    <!-- Hero stagger 入场 -->
    <div class="mb-14 text-center">
      <p
        class="animate-fade-up text-sm tracking-widest text-amber-400/80"
        style="animation-delay: 0ms"
      >
        CHATVIBE · AMBER LOUNGE
      </p>
      <h1
        class="animate-fade-up mt-3 font-display text-5xl font-bold text-cream sm:text-6xl"
        style="animation-delay: 80ms"
      >
        {{ greeting }}，{{ auth.user?.username || 'Alu' }}
      </h1>
      <p
        class="animate-fade-up mt-2 font-mono text-2xl tracking-wider text-amber-400/90 sm:text-3xl"
        style="animation-delay: 120ms"
      >
        {{ clock }}
      </p>
      <p
        class="animate-fade-up mt-4 text-base text-cream/60 sm:text-lg"
        style="animation-delay: 160ms"
      >
        你的私人 AI 工具
      </p>
    </div>

    <!-- 功能卡片 -->
    <div class="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
      <button
        v-for="(c, i) in cards"
        :key="c.to"
        class="group relative flex animate-fade-up flex-col items-start gap-4 rounded-2xl border border-mocha bg-cocoa/50 p-6 text-left transition hover:-translate-y-1 hover:border-amber-500/60 hover:shadow-[0_0_32px_-8px_rgba(232,163,61,0.45)]"
        :style="{ animationDelay: 240 + i * 100 + 'ms' }"
        @click="go(c.to)"
      >
        <div
          class="rounded-xl bg-amber-500/15 p-3 text-amber-400 transition group-hover:bg-amber-500 group-hover:text-espresso"
        >
          <component :is="c.icon" :size="26" />
        </div>
        <div>
          <h3 class="font-display text-xl font-semibold text-cream">{{ c.title }}</h3>
          <p class="mt-1 text-sm text-cream/55">{{ c.desc }}</p>
        </div>
        <span
          class="mt-2 flex items-center gap-1 text-xs text-amber-400/0 transition group-hover:text-amber-400"
        >
          进入 <ArrowRight :size="14" />
        </span>
      </button>
    </div>
  </section>
</template>
