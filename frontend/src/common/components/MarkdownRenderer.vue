<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js'

const props = defineProps<{ content: string }>()

const container = ref<HTMLElement | null>(null)

// marked 解析 → DOMPurify 消毒
const html = computed(() => {
  const raw = marked.parse(props.content ?? '') as string
  return DOMPurify.sanitize(raw, { USE_PROFILES: { html: true } })
})

// 渲染后高亮代码块
watch(
  html,
  async () => {
    await nextTick()
    container.value?.querySelectorAll('pre code').forEach((el) => {
      const node = el as HTMLElement
      if (node.dataset.highlighted) return
      try {
        hljs.highlightElement(node)
        node.dataset.highlighted = 'true'
      } catch {
        // 忽略高亮失败
      }
    })
  },
  { immediate: true },
)
</script>

<template>
  <div ref="container" class="md-body" v-html="html"></div>
</template>
