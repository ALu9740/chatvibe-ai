<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ArrowLeft } from 'lucide-vue-next'

const router = useRouter()
</script>

<template>
  <div class="flex min-h-[70vh] flex-col items-center justify-center px-4 text-center">
    <h1 class="font-display text-7xl font-bold text-amber-400 sm:text-8xl">404</h1>
    <p class="mt-4 font-display text-2xl text-cream">这片区域没有 vibe</p>
    <p class="mt-2 text-sm text-cream/50">你访问的页面不存在，或已被移除。</p>
    <button
      class="mt-8 flex items-center gap-2 rounded-xl bg-amber-500 px-5 py-2.5 text-sm font-medium text-espresso transition hover:bg-amber-400"
      @click="router.push('/')"
    >
      <ArrowLeft :size="16" />
      回到休息室
    </button>
  </div>
</template>
