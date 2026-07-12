<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Loader2 } from 'lucide-vue-next'
import ChatVibeLogo from '@/common/components/ChatVibeLogo.vue'
import ThemeToggle from '@/common/components/ThemeToggle.vue'
import { useAuthStore } from '@/module/auth/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')
const shake = ref(false)

async function onSubmit() {
  if (loading.value) return
  errorMsg.value = ''
  loading.value = true
  try {
    await auth.login(username.value.trim(), password.value)
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    errorMsg.value = '用户名或密码不正确'
    shake.value = true
    setTimeout(() => (shake.value = false), 500)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (auth.token) router.replace('/')
})
</script>

<template>
  <div class="relative flex min-h-screen items-center justify-center px-4 py-10">
    <!-- 主题切换 -->
    <div class="absolute right-4 top-4 z-10">
      <ThemeToggle />
    </div>
    <div class="w-full max-w-md">
      <div class="relative">
        <!-- 琥珀光晕 -->
        <div class="absolute -inset-6 rounded-[2rem] bg-amber-500/20 blur-3xl"></div>
        <form
          class="glass relative rounded-2xl p-8"
          :class="{ 'animate-shake': shake }"
          @submit.prevent="onSubmit"
        >
          <div class="mb-6 flex flex-col items-center text-center">
            <ChatVibeLogo :size="64" />
            <h1 class="mt-5 font-display text-3xl font-bold text-cream">私人 AI 工具</h1>
            <p class="mt-1 text-sm text-cream/50">欢迎回来</p>
          </div>

          <div class="space-y-3">
            <div>
              <label class="mb-1 block text-xs text-cream/60">用户名</label>
              <input
                v-model="username"
                type="text"
                autocomplete="username"
                placeholder="请输入用户名"
                class="w-full rounded-xl border border-mocha bg-espresso/60 px-3 py-2.5 text-sm text-cream placeholder:text-cream/30 focus:border-amber-500 focus:outline-none"
              />
            </div>
            <div>
              <label class="mb-1 block text-xs text-cream/60">密码</label>
              <input
                v-model="password"
                type="password"
                autocomplete="current-password"
                placeholder="请输入密码"
                class="w-full rounded-xl border border-mocha bg-espresso/60 px-3 py-2.5 text-sm text-cream placeholder:text-cream/30 focus:border-amber-500 focus:outline-none"
              />
            </div>
          </div>

          <p v-if="errorMsg" class="mt-3 text-center text-xs text-terracotta">{{ errorMsg }}</p>

          <button
            type="submit"
            :disabled="loading"
            class="mt-5 flex w-full items-center justify-center gap-2 rounded-xl bg-amber-500 py-2.5 text-sm font-medium text-espresso transition hover:bg-amber-400 disabled:opacity-50"
          >
            <Loader2 v-if="loading" :size="16" class="animate-spin" />
            {{ loading ? '登录中…' : '登录' }}
          </button>

          <div class="mt-6 flex items-center justify-center gap-4 text-xs text-cream/40">
            <router-link to="/agreement" class="hover:text-amber-400">用户协议</router-link>
            <span class="text-mocha">·</span>
            <router-link to="/privacy" class="hover:text-amber-400">隐私政策</router-link>
          </div>
        </form>
      </div>
      <div class="mt-6 flex flex-col items-center gap-1.5 text-center text-xs text-cream/30">
        <br>
         
        <a
          href="https://beian.miit.gov.cn/"
          target="_blank"
          rel="noopener noreferrer"
          class="transition hover:text-amber-400"
        >备案号：湘ICP备2026027106号-1</a>
        <p>© {{ new Date().getFullYear() }} ChatVibe · 版权所有 Alu</p>
      </div>
    </div>
  </div>
</template>
