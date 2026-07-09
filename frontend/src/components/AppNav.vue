<script setup lang="ts">
import { useRouter } from 'vue-router'
import { LogOut, MessageSquare, FileText, HeartHandshake, Home as HomeIcon } from 'lucide-vue-next'
import ChatVibeLogo from './ChatVibeLogo.vue'
import ThemeToggle from './ThemeToggle.vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const links = [
  { to: '/', label: '工作台', icon: HomeIcon },
  { to: '/chat', label: '聊天', icon: MessageSquare },
  { to: '/pdf', label: 'ChatPDF', icon: FileText },
  { to: '/comfort', label: '学会哄人', icon: HeartHandshake },
] as const

function onLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <header class="sticky top-0 z-30 border-b border-mocha/60 bg-espresso/70 backdrop-blur-md">
    <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6">
      <router-link to="/" class="shrink-0" aria-label="ChatVibe 首页">
        <ChatVibeLogo :size="32" />
      </router-link>
      <nav class="hidden items-center gap-1 md:flex">
        <router-link
          v-for="l in links"
          :key="l.to"
          :to="l.to"
          class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-cream/70 transition hover:bg-cocoa hover:text-cream"
          exact-active-class="!text-amber-400 bg-cocoa"
        >
          <component :is="l.icon" :size="16" />
          <span>{{ l.label }}</span>
        </router-link>
      </nav>
      <div class="flex items-center gap-2">
        <ThemeToggle />
        <button
          class="flex items-center gap-2 rounded-lg border border-mocha px-3 py-2 text-sm text-cream/80 transition hover:border-amber-500 hover:text-amber-400"
          @click="onLogout"
        >
          <LogOut :size="16" />
          <span class="hidden sm:inline">登出</span>
        </button>
      </div>
    </div>
    <!-- 移动端横向导航 -->
    <nav class="flex items-center gap-1 overflow-x-auto px-4 pb-2 md:hidden">
      <router-link
        v-for="l in links"
        :key="l.to"
        :to="l.to"
        class="flex items-center gap-1.5 whitespace-nowrap rounded-lg px-3 py-1.5 text-xs text-cream/70 transition hover:bg-cocoa hover:text-cream"
        exact-active-class="!text-amber-400 bg-cocoa"
      >
        <component :is="l.icon" :size="14" />
        <span>{{ l.label }}</span>
      </router-link>
    </nav>
  </header>
</template>
