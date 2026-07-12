<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { Lightbulb, BookOpen } from 'lucide-vue-next'
import ChatMessage from '@/common/components/ChatMessage.vue'
import ChatInput from '@/common/components/ChatInput.vue'
import ComfortScoreBar from '@/module/comfort/components/ComfortScoreBar.vue'
import { useChatStore } from '@/common/stores/chat'
import { getScenarios, type ComfortScenario } from '@/module/comfort/api/comfort'

const chat = useChatStore()
const scenarios = ref<ComfortScenario[]>([])
const activeScenario = ref<string>('')
const context = ref('')
const drawerOpen = ref(true)
const scrollEl = ref<HTMLElement | null>(null)

async function scrollToBottom() {
  await nextTick()
  if (scrollEl.value) scrollEl.value.scrollTop = scrollEl.value.scrollHeight
}

function onSend(text: string, _files: File[]) {
  if (!activeScenario.value && scenarios.value.length) {
    activeScenario.value = scenarios.value[0].key
  }
  chat
    .sendComfort(activeScenario.value, text, context.value.trim() || undefined)
    .then(scrollToBottom)
}

watch(() => chat.messages.length, scrollToBottom)
watch(() => chat.messages[chat.messages.length - 1]?.content, scrollToBottom)

onMounted(async () => {
  try {
    scenarios.value = await getScenarios()
    if (scenarios.value.length) activeScenario.value = scenarios.value[0].key
  } catch {
    scenarios.value = []
  }
  chat.newSession()
})

const tips = [
  { t: '先共情，再讲理', d: '先回应对方的情绪，再讨论事情本身。' },
  { t: '复述与命名', d: '用对方能接受的话复述感受，为情绪命名。' },
  { t: '避免评判', d: '少用"你应该"，多用"我理解""我感受到"。' },
  { t: '留白', d: '给彼此沉默与缓冲的空间，不必急于填补。' },
]

const examples = [
  '听起来你现在很累，我先不打扰你。',
  '你是希望我多陪你说说话，对吗？',
  '我感受到这件事对你很重要，我愿意听你说完。',
]
</script>

<template>
  <div class="mx-auto flex h-[calc(100vh-10rem)] max-w-7xl gap-4 p-4 md:h-[calc(100vh-6.5rem)]">
    <!-- 主体 -->
    <div class="flex min-w-0 flex-1 flex-col">
      <!-- 场景 chips -->
      <div class="mb-3 flex flex-wrap gap-2">
        <button
          v-for="s in scenarios"
          :key="s.key"
          class="rounded-full border px-3 py-1.5 text-xs transition"
          :class="
            activeScenario === s.key
              ? 'border-amber-500 bg-amber-500/15 text-amber-400'
              : 'border-mocha text-cream/60 hover:border-amber-500/60'
          "
          :title="s.desc"
          @click="activeScenario = s.key"
        >
          {{ s.label }}
        </button>
      </div>

      <!-- 情境描述 -->
      <textarea
        v-model="context"
        rows="2"
        placeholder="可选：描述当下的情境（例如：对方刚下班很累，我想要……）"
        class="mb-3 resize-none rounded-xl border border-mocha bg-espresso/60 px-3 py-2 text-sm text-cream placeholder:text-cream/30 focus:border-amber-500 focus:outline-none"
      ></textarea>

      <!-- 对话区 -->
      <div
        ref="scrollEl"
        class="flex-1 space-y-4 overflow-y-auto rounded-2xl border border-mocha bg-cocoa/20 p-4"
      >
        <div
          v-if="!chat.messages.length"
          class="flex h-full flex-col items-center justify-center text-center text-cream/40"
        >
          <Lightbulb :size="32" class="text-amber-400" />
          <p class="mt-2 text-sm">选择对象、描述情境，开始练习你的共情表达</p>
        </div>
        <template v-for="(m, i) in chat.messages" :key="i">
          <ChatMessage :message="m" />
          <!-- AI 回复的评分与点评 -->
          <div
            v-if="m.role === 'assistant' && !m.streaming && m.meta"
            class="ml-2 space-y-2 border-l-2 border-mocha pl-3"
          >
            <ComfortScoreBar :score="m.meta.score" />
            <div
              v-if="m.meta.tip"
              class="flex items-start gap-2 rounded-lg bg-cocoa/60 p-2.5 text-xs text-cream/80"
            >
              <Lightbulb :size="14" class="mt-0.5 shrink-0 text-amber-400" />
              <span>{{ m.meta.tip }}</span>
            </div>
          </div>
        </template>
      </div>

      <ChatInput :disabled="chat.streaming" placeholder="试着说出你的回应…" @send="onSend" />
    </div>

    <!-- 抽屉切换 -->
    <button
      class="hidden items-center gap-1 self-start rounded-lg border border-mocha px-2.5 py-1.5 text-xs text-cream/60 transition hover:border-amber-500 hover:text-amber-400 lg:flex"
      @click="drawerOpen = !drawerOpen"
    >
      <BookOpen :size="14" />
      {{ drawerOpen ? '收起技巧' : '展开技巧' }}
    </button>

    <!-- 右侧抽屉：技巧速查 -->
    <aside
      v-if="drawerOpen"
      class="hidden w-80 shrink-0 space-y-5 overflow-y-auto rounded-2xl border border-mocha bg-cocoa/30 p-5 lg:block"
    >
      <div>
        <h3 class="mb-3 flex items-center gap-2 font-display text-lg font-semibold text-cream">
          <Lightbulb :size="18" class="text-amber-400" /> 沟通技巧速查
        </h3>
        <ul class="space-y-3">
          <li v-for="(tp, i) in tips" :key="i" class="text-sm">
            <p class="font-medium text-amber-400">{{ tp.t }}</p>
            <p class="mt-0.5 text-xs text-cream/60">{{ tp.d }}</p>
          </li>
        </ul>
      </div>
      <div class="border-t border-mocha pt-4">
        <h3 class="mb-3 font-display text-lg font-semibold text-cream">使用示例</h3>
        <ul class="space-y-2">
          <li
            v-for="(ex, i) in examples"
            :key="i"
            class="rounded-lg bg-espresso/50 p-2.5 text-xs italic text-cream/70"
          >
            "{{ ex }}"
          </li>
        </ul>
      </div>
    </aside>
  </div>
</template>
