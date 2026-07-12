<script setup lang="ts">
import type { Message } from '@/common/stores/chat'
import MarkdownRenderer from '@/common/components/MarkdownRenderer.vue'

defineProps<{ message: Message }>()
</script>

<template>
  <div class="flex w-full" :class="message.role === 'user' ? 'justify-end' : 'justify-start'">
    <div
      class="max-w-[85%] rounded-2xl px-4 py-3 text-sm shadow-sm sm:max-w-[75%]"
      :class="
        message.role === 'user'
          ? 'rounded-br-sm bg-cream text-espresso'
          : 'rounded-bl-sm border border-mocha bg-cocoa text-cream'
      "
    >
      <!-- 用户消息：纯文本 -->
      <p v-if="message.role === 'user'" class="whitespace-pre-wrap break-words">{{ message.content }}</p>
      <!-- AI 消息：Markdown -->
      <MarkdownRenderer v-else :content="message.content" />
      <!-- 流式光标 -->
      <span
        v-if="message.streaming"
        class="ml-0.5 inline-block h-4 w-1.5 translate-y-0.5 animate-blink bg-amber-400"
      ></span>
    </div>
  </div>
</template>
