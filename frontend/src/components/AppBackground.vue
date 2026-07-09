<script setup lang="ts">
// 装饰性背景：琥珀光斑 + 渐变网格 + 颗粒噪点（主题自适应）
</script>

<template>
  <div class="pointer-events-none fixed inset-0 overflow-hidden" aria-hidden="true">
    <div class="blob blob-1"></div>
    <div class="blob blob-2"></div>
    <div class="blob blob-3"></div>
    <div class="grid-layer"></div>
    <div class="grain"></div>
  </div>
</template>

<style scoped>
.blob {
  position: absolute;
  border-radius: 9999px;
  filter: blur(120px);
}
.blob-1 {
  top: -8rem;
  left: -6rem;
  height: 30rem;
  width: 30rem;
  background: var(--cv-blob-1);
}
.blob-2 {
  top: 33%;
  right: -8rem;
  height: 32rem;
  width: 32rem;
  background: var(--cv-blob-2);
}
.blob-3 {
  bottom: 0;
  left: 25%;
  height: 22rem;
  width: 22rem;
  background: var(--cv-blob-3);
}
.grid-layer {
  position: absolute;
  inset: 0;
  opacity: 0.06;
  background-image: linear-gradient(var(--cv-grid) 1px, transparent 1px),
    linear-gradient(90deg, var(--cv-grid) 1px, transparent 1px);
  background-size: 56px 56px;
}
.grain {
  position: absolute;
  inset: 0;
  opacity: 0.05;
  mix-blend-mode: overlay;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='2'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
}
</style>
