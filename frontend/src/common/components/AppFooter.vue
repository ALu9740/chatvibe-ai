<script setup lang="ts">
// 页脚：版权信息 + ICP 备案号
</script>

<template>
  <footer class="border-t border-mocha/60 bg-cocoa/40 px-4 py-3 text-center">
    <div class="mx-auto flex max-w-7xl flex-col items-center gap-1 text-xs text-cream/50 sm:flex-row sm:justify-center sm:gap-4">
      <span>© {{ new Date().getFullYear() }} ChatVibe · 版权所有 Alu</span>
      <span class="hidden sm:inline text-mocha">·</span>
      <a
        href="https://beian.miit.gov.cn/"
        target="_blank"
        rel="noopener noreferrer"
        class="transition hover:text-amber-400"
      >备案号：湘ICP备2026027106号-1</a>
    </div>
  </footer>
</template>
