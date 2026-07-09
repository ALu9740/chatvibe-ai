<script setup lang="ts">
import { computed } from 'vue'
import { Heart } from 'lucide-vue-next'

const props = defineProps<{ score?: number }>()

const value = computed(() => {
  const s = props.score
  if (typeof s !== 'number') return 0
  return Math.max(0, Math.min(100, s))
})

const label = computed(() => {
  if (typeof props.score !== 'number') return '待评分'
  if (value.value >= 80) return '高共情'
  if (value.value >= 60) return '尚可'
  if (value.value >= 40) return '需改进'
  return '低共情'
})
</script>

<template>
  <div>
    <div class="mb-1.5 flex items-center justify-between text-xs">
      <span class="flex items-center gap-1.5 text-cream/70">
        <Heart :size="12" class="text-moss" /> 共情评分
      </span>
      <span class="font-medium text-moss">
        {{ typeof score === 'number' ? value : '--' }} · {{ label }}
      </span>
    </div>
    <div class="h-2 w-full overflow-hidden rounded-full bg-mocha">
      <div
        class="h-full rounded-full bg-moss transition-all duration-700"
        :style="{ width: value + '%' }"
      ></div>
    </div>
  </div>
</template>
