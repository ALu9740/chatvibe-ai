<script setup lang="ts">
import { Plus, Trash2, MessageSquare } from 'lucide-vue-next'
import { useChatStore } from '../stores/chat'

const chat = useChatStore()
</script>

<template>
  <aside class="flex h-full w-full flex-col">
    <button
      class="m-3 flex items-center justify-center gap-2 rounded-xl bg-amber-500 px-3 py-2.5 text-sm font-medium text-espresso transition hover:bg-amber-400"
      @click="chat.newSession()"
    >
      <Plus :size="16" />
      新建会话
    </button>
    <div class="flex-1 overflow-y-auto px-2 pb-3">
      <p v-if="!chat.sessions.length" class="px-3 py-6 text-center text-xs text-cream/40">
        还没有会话
      </p>
      <ul v-else class="space-y-1">
        <li v-for="s in chat.sessions" :key="s.id">
          <div
            class="group flex cursor-pointer items-center gap-2 rounded-lg px-3 py-2 text-sm transition"
            :class="s.id === chat.currentId ? 'bg-mocha text-amber-400' : 'text-cream/70 hover:bg-cocoa'"
            @click="chat.selectSession(s.id)"
          >
            <MessageSquare :size="15" class="shrink-0" />
            <span class="flex-1 truncate">{{ s.title || s.fileName || `会话 ${s.id}` }}</span>
            <button
              class="text-cream/50 opacity-0 transition hover:text-terracotta group-hover:opacity-100"
              title="删除会话"
              @click.stop="chat.deleteSession(s.id)"
            >
              <Trash2 :size="14" />
            </button>
          </div>
        </li>
      </ul>
    </div>
  </aside>
</template>
