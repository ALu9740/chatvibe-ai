<script setup lang="ts">
import { ref, computed } from 'vue'
import { Send, Paperclip, X } from 'lucide-vue-next'

const props = defineProps<{
  disabled?: boolean
  placeholder?: string
}>()

const emit = defineEmits<{
  (e: 'send', text: string, files: File[]): void
}>()

const text = ref('')
const files = ref<File[]>([])
const accepted = 'image/*,audio/*,video/*'

const fileNames = computed(() => files.value.map((f) => f.name))

function onPick(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files) {
    files.value.push(...Array.from(input.files))
  }
  input.value = ''
}

function removeFile(i: number) {
  files.value.splice(i, 1)
}

function submit() {
  const t = text.value.trim()
  if ((!t && files.value.length === 0) || props.disabled) return
  emit('send', t, files.value.slice())
  text.value = ''
  files.value = []
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    submit()
  }
}
</script>

<template>
  <div class="border-t border-mocha bg-cocoa/40 p-3 backdrop-blur-md">
    <div v-if="files.length" class="mb-2 flex flex-wrap gap-2">
      <span
        v-for="(name, i) in fileNames"
        :key="i"
        class="flex items-center gap-1 rounded-full bg-mocha px-2.5 py-1 text-xs text-cream"
      >
        <Paperclip :size="12" />
        {{ name }}
        <button class="ml-1 text-cream/60 hover:text-terracotta" @click="removeFile(i)">
          <X :size="12" />
        </button>
      </span>
    </div>
    <div class="flex items-end gap-2">
      <label
        class="cursor-pointer rounded-lg border border-mocha p-2.5 text-cream/70 transition hover:border-amber-500 hover:text-amber-400"
        title="上传图片/音频/视频"
      >
        <Paperclip :size="18" />
        <input type="file" class="hidden" multiple :accept="accepted" @change="onPick" />
      </label>
      <textarea
        v-model="text"
        rows="1"
        :placeholder="placeholder ?? '输入消息…'"
        :disabled="disabled"
        class="max-h-40 flex-1 resize-none rounded-xl border border-mocha bg-espresso/60 px-3 py-2.5 text-sm text-cream placeholder:text-cream/40 focus:border-amber-500 focus:outline-none disabled:opacity-50"
        @keydown="onKeydown"
      ></textarea>
      <button
        class="flex items-center gap-1.5 rounded-xl bg-amber-500 px-4 py-2.5 text-sm font-medium text-espresso transition hover:bg-amber-400 disabled:cursor-not-allowed disabled:opacity-50"
        :disabled="disabled || (!text.trim() && !files.length)"
        @click="submit"
      >
        <Send :size="16" />
        <span class="hidden sm:inline">发送</span>
      </button>
    </div>
  </div>
</template>
