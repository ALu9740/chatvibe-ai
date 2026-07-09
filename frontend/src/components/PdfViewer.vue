<script setup lang="ts">
import { FileText } from 'lucide-vue-next'

defineProps<{ src: string | null }>()
</script>

<template>
  <div
    class="flex h-full w-full items-center justify-center rounded-2xl border border-mocha bg-cocoa/40"
  >
    <iframe
      v-if="src"
      :src="src"
      class="h-full w-full rounded-2xl"
      title="PDF 预览"
    ></iframe>
    <div v-else class="flex flex-col items-center gap-3 py-16 text-cream/40">
      <FileText :size="40" />
      <p class="text-sm">上传 PDF 后在此预览</p>
    </div>
  </div>
</template>
