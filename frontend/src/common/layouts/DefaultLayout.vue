<script setup lang="ts">
import AppNav from '@/common/components/AppNav.vue'
import AppBackground from '@/common/components/AppBackground.vue'
import AppFooter from '@/common/components/AppFooter.vue'
</script>

<template>
  <div class="relative flex min-h-screen flex-col overflow-x-hidden">
    <AppBackground />
    <AppNav />
    <main class="relative z-10 flex-1">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <AppFooter />
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
