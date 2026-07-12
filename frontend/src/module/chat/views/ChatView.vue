<script setup lang="ts">
import { ref, nextTick, watch, onMounted } from 'vue'
import { Menu } from 'lucide-vue-next'
import ChatSidebar from '@/module/chat/components/ChatSidebar.vue'
import ChatMessage from '@/common/components/ChatMessage.vue'
import ChatInput from '@/common/components/ChatInput.vue'
import { useChatStore } from '@/common/stores/chat'

const chat = useChatStore()
const drawerOpen = ref(false)
const scrollEl = ref<HTMLElement | null>(null)

async function scrollToBottom() {
  await nextTick()
  if (scrollEl.value) scrollEl.value.scrollTop = scrollEl.value.scrollHeight
}

function onSend(text: string, files: File[]) {
  chat.sendPrompt(text, files).then(scrollToBottom)
}

watch(() => chat.messages.length, scrollToBottom)
watch(() => chat.messages[chat.messages.length - 1]?.content, scrollToBottom)

onMounted(() => {
  chat.loadSessions()
})
</script>

<template>
  <div class="mx-auto flex h-[calc(100vh-10rem)] max-w-7xl md:h-[calc(100vh-6.5rem)]">
    <!-- 侧栏：桌面常驻 -->
    <div class="hidden w-72 shrink-0 border-r border-mocha bg-cocoa/30 md:block">
      <ChatSidebar />
    </div>

    <!-- 侧栏：平板/移动端抽屉 -->
    <transition name="slide">
      <div v-if="drawerOpen" class="fixed inset-0 z-40 flex md:hidden">
        <div class="w-72 border-r border-mocha bg-espresso">
          <ChatSidebar />
        </div>
        <div class="flex-1 bg-black/50" @click="drawerOpen = false"></div>
      </div>
    </transition>

    <!-- 主体 -->
    <div class="flex min-w-0 flex-1 flex-col">
      <div class="flex items-center justify-between border-b border-mocha px-3 py-2 md:hidden">
        <button class="rounded-lg p-2 text-cream/70 hover:bg-cocoa" @click="drawerOpen = true">
          <Menu :size="18" />
        </button>
        <span class="text-sm text-cream/60">对话</span>
        <span class="w-8"></span>
      </div>

      <!-- 消息流 -->
      <div ref="scrollEl" class="flex-1 space-y-4 overflow-y-auto px-4 py-5">
        <div
          v-if="!chat.messages.length"
          class="flex h-full flex-col items-center justify-center text-center text-cream/40"
        >
          <p class="font-display text-2xl text-cream/60">开始一段新对话</p>
          <p class="mt-2 text-sm">在下方输入消息，或从侧栏选择历史会话</p>
        </div>
        <ChatMessage v-for="(m, i) in chat.messages" :key="i" :message="m" />
      </div>

      <ChatInput
        :disabled="chat.streaming"
        placeholder="输入消息，Enter 发送"
        @send="onSend"
      />
    </div>
  </div>
</template>

<style scoped>
.slide-enter-active,
.slide-leave-active {
  transition: transform 0.25s ease;
}
.slide-enter-from,
.slide-leave-to {
  transform: translateX(-100%);
}
</style>
