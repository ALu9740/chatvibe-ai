<script setup lang="ts">
import AppBackground from '@/common/components/AppBackground.vue'
</script>

<template>
  <div class="relative min-h-screen overflow-x-hidden">
    <AppBackground />
    <main class="relative z-10">
      <router-view />
    </main>
  </div>
</template>
