<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  size?: 16 | 32 | 64 | 128
  showText?: boolean
}>()

const px = computed(() => props.size ?? 32)
const showText = computed(() => props.showText ?? props.size !== 16)
const textSize = computed(() => Math.round(px.value * 0.62))
</script>

<template>
  <div class="flex items-center gap-2 select-none">
    <svg
      :width="px"
      :height="px"
      viewBox="0 0 64 64"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <defs>
        <linearGradient id="cv-logo-grad" x1="0" y1="0" x2="64" y2="64" gradientUnits="userSpaceOnUse">
          <stop stop-color="#F0B860" />
          <stop offset="1" stop-color="#C7862A" />
        </linearGradient>
      </defs>
      <path
        d="M12 8h40a8 8 0 0 1 8 8v26a8 8 0 0 1-8 8H30l-12 10v-10h-6a8 8 0 0 1-8-8V16a8 8 0 0 1 8-8Z"
        fill="url(#cv-logo-grad)"
      />
      <g fill="#1A1410">
        <rect x="22" y="24" width="4" height="14" rx="2" />
        <rect x="30" y="20" width="4" height="22" rx="2" />
        <rect x="38" y="26" width="4" height="10" rx="2" />
      </g>
    </svg>
    <span
      v-if="showText"
      class="font-display font-bold tracking-tight text-cream"
      :style="{ fontSize: textSize + 'px' }"
      >ChatVibe</span
    >
  </div>
</template>
