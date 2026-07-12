<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Sun, Moon, Clock, Check } from 'lucide-vue-next'
import { useTheme, type ThemeMode } from '@/common/composables/useTheme'

const { mode, resolved, setMode } = useTheme()
const open = ref(false)

const options: { value: ThemeMode; label: string; icon: typeof Sun }[] = [
  { value: 'light', label: '白天', icon: Sun },
  { value: 'dark', label: '黑夜', icon: Moon },
  { value: 'auto', label: '自动', icon: Clock },
]

function toggle() {
  open.value = !open.value
}

function choose(m: ThemeMode) {
  setMode(m)
  open.value = false
}

const CurrentIcon = () => {
  if (mode.value === 'auto') return Clock
  return resolved.value === 'light' ? Sun : Moon
}

function onDocClick(e: MouseEvent) {
  const el = e.target as HTMLElement
  if (!el.closest('.theme-toggle')) open.value = false
}

onMounted(() => document.addEventListener('click', onDocClick))
onUnmounted(() => document.removeEventListener('click', onDocClick))
</script>

<template>
  <div class="theme-toggle relative">
    <button
      class="flex items-center gap-2 rounded-lg border border-mocha px-2.5 py-2 text-sm text-cream/80 transition hover:border-amber-500 hover:text-amber-400"
      :title="`主题：${mode === 'auto' ? '自动' : resolved === 'light' ? '白天' : '黑夜'}`"
      @click="toggle"
    >
      <component :is="CurrentIcon()" :size="16" />
      <span class="hidden sm:inline">{{ mode === 'auto' ? '自动' : resolved === 'light' ? '白天' : '黑夜' }}</span>
    </button>
    <transition name="drop">
      <div
        v-if="open"
        class="absolute right-0 z-50 mt-2 w-36 overflow-hidden rounded-xl border border-mocha bg-cocoa shadow-lg"
      >
        <button
          v-for="o in options"
          :key="o.value"
          class="flex w-full items-center justify-between px-3 py-2 text-sm transition hover:bg-mocha"
          :class="mode === o.value ? 'text-amber-400' : 'text-cream/70'"
          @click="choose(o.value)"
        >
          <span class="flex items-center gap-2">
            <component :is="o.icon" :size="15" />
            {{ o.label }}
          </span>
          <Check v-if="mode === o.value" :size="14" />
        </button>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.drop-enter-active,
.drop-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.drop-enter-from,
.drop-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
