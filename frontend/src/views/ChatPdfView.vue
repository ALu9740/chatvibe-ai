<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { UploadCloud, FileText, Loader2 } from 'lucide-vue-next'
import PdfViewer from '../components/PdfViewer.vue'
import ChatMessage from '../components/ChatMessage.vue'
import ChatInput from '../components/ChatInput.vue'
import { useChatStore } from '../stores/chat'
import { uploadPdf, fetchPdfBlobUrl } from '../api/pdf'

const chat = useChatStore()
const pdfUrl = ref<string | null>(null)
const uploading = ref(false)
const dragging = ref(false)
const fileName = ref('')
const scrollEl = ref<HTMLElement | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)

async function scrollToBottom() {
  await nextTick()
  if (scrollEl.value) scrollEl.value.scrollTop = scrollEl.value.scrollHeight
}

async function handleFile(file: File) {
  if (chat.currentId == null) chat.currentId = Date.now()
  let chatId = chat.currentId
  uploading.value = true
  fileName.value = file.name
  try {
    const res = await uploadPdf(chatId, file)
    // 使用后端返回的真实 sessionId（后端可能新建了会话）
    if (res.sessionId) {
      chatId = res.sessionId
      chat.currentId = chatId
    }
    if (pdfUrl.value) URL.revokeObjectURL(pdfUrl.value)
    pdfUrl.value = await fetchPdfBlobUrl(chatId)
    chat.messages = []
    await chat.loadPdfSessions()
  } catch {
    pdfUrl.value = null
  } finally {
    uploading.value = false
  }
}

function onPick(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files?.[0]) handleFile(input.files[0])
  input.value = ''
}

function onDrop(e: DragEvent) {
  dragging.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file && file.type === 'application/pdf') handleFile(file)
}

function onSend(text: string, _files: File[]) {
  chat.sendPdfPrompt(text).then(scrollToBottom)
}

watch(() => chat.messages.length, scrollToBottom)
watch(() => chat.messages[chat.messages.length - 1]?.content, scrollToBottom)

onMounted(() => {
  chat.loadPdfSessions()
})

onUnmounted(() => {
  if (pdfUrl.value) URL.revokeObjectURL(pdfUrl.value)
})
</script>

<template>
  <div
    class="mx-auto flex h-[calc(100vh-10rem)] max-w-7xl flex-col p-4 md:h-[calc(100vh-6.5rem)]"
  >
    <!-- 拖拽上传区 -->
    <div
      class="mb-4 rounded-2xl border-2 border-dashed p-4 transition"
      :class="dragging ? 'border-amber-500 bg-amber-500/5' : 'border-mocha'"
      @dragover.prevent="dragging = true"
      @dragleave.prevent="dragging = false"
      @drop.prevent="onDrop"
    >
      <div class="flex items-center gap-3">
        <div class="rounded-lg bg-amber-500/15 p-2.5 text-amber-400">
          <UploadCloud :size="22" />
        </div>
        <div class="flex-1">
          <p class="text-sm text-cream">
            <span v-if="uploading">向量化中…</span>
            <span v-else-if="fileName">{{ fileName }}</span>
            <span v-else>拖拽 PDF 到此处，或点击选择文件</span>
          </p>
          <p class="text-xs text-cream/40">支持 PDF 知识库问答</p>
        </div>
        <input
          ref="fileInput"
          type="file"
          accept="application/pdf"
          class="hidden"
          @change="onPick"
        />
        <button
          class="rounded-lg border border-mocha px-3 py-2 text-xs text-cream/80 transition hover:border-amber-500 hover:text-amber-400"
          @click="fileInput?.click()"
        >
          选择 PDF
        </button>
      </div>
    </div>

    <!-- 两栏：PDF 预览 + 对话 -->
    <div class="grid min-h-0 flex-1 grid-cols-1 gap-4 lg:grid-cols-2">
      <div class="min-h-[240px]">
        <PdfViewer :src="pdfUrl" />
      </div>
      <div class="flex min-h-[320px] flex-col rounded-2xl border border-mocha bg-cocoa/30">
        <div ref="scrollEl" class="flex-1 space-y-4 overflow-y-auto p-4">
          <div
            v-if="!chat.messages.length"
            class="flex h-full flex-col items-center justify-center text-center text-cream/40"
          >
            <FileText :size="32" />
            <p class="mt-2 text-sm">上传 PDF 后开始提问</p>
          </div>
          <ChatMessage v-for="(m, i) in chat.messages" :key="i" :message="m" />
          <div v-if="uploading" class="flex items-center gap-2 text-xs text-cream/50">
            <Loader2 :size="14" class="animate-spin" /> 正在处理文档…
          </div>
        </div>
        <ChatInput
          :disabled="chat.streaming || !pdfUrl"
          placeholder="针对 PDF 提问…"
          @send="onSend"
        />
      </div>
    </div>
  </div>
</template>
