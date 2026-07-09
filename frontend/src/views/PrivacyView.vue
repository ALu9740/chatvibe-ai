<script setup lang="ts">
import { Lock } from 'lucide-vue-next'

const sections = [
  { id: 'collect', title: '一、数据收集范围' },
  { id: 'storage', title: '二、存储方式' },
  { id: 'encrypt', title: '三、加密措施' },
  { id: 'retain', title: '四、数据保留与删除' },
  { id: 'rights', title: '五、用户权利' },
  { id: 'contact', title: '六、联系方式' },
] as const
</script>

<template>
  <article class="mx-auto max-w-[860px] px-6 py-12">
    <header class="mb-10 text-center">
      <Lock :size="36" class="mx-auto text-amber-400" />
      <h1 class="mt-3 font-display text-4xl font-bold text-cream">隐私政策</h1>
      <p class="mt-2 text-sm text-cream/50">最近更新：2026 年 7 月 7 日</p>
    </header>

    <nav class="mb-8 flex flex-wrap justify-center gap-x-4 gap-y-1 text-xs text-cream/60">
      <a v-for="s in sections" :key="s.id" :href="'#' + s.id" class="transition hover:text-amber-400">
        {{ s.title }}
      </a>
    </nav>

    <section id="collect" class="border-t border-amber-500/30 py-6">
      <h2 class="mb-3 font-display text-2xl font-semibold text-cream">一、数据收集范围</h2>
      <div class="space-y-3 text-sm leading-relaxed text-cream/70">
        <p>本服务为个人私有工具，不面向任何第三方收集个人信息。出于服务运行需要，本服务会收集和处理主理人自身在使用过程中产生的以下数据：</p>
        <ul class="list-disc space-y-1 pl-5">
          <li>账户信息：主理人用户名与登录凭证（JWT）。</li>
          <li>对话记录：主理人与 AI 的聊天内容、学会哄人练习记录。</li>
          <li>文档资料：主理人上传的 PDF 文件及其向量化后的索引数据。</li>
          <li>必要日志： 服务运行的服务器访问日志与 API 调用日志。</li>
        </ul>
      </div>
    </section>

    <section id="storage" class="border-t border-amber-500/30 py-6">
      <h2 class="mb-3 font-display text-2xl font-semibold text-cream">二、存储方式</h2>
      <div class="space-y-3 text-sm leading-relaxed text-cream/70">
        <p>账户信息与对话记录存储于 MySQL 数据库；高频会话上下文缓存于 Redis；上传的 PDF 文件存储于服务端本地文件系统；向量化索引存储于向量数据库。</p>
        <p>所有数据均部署于受控的服务器环境中，仅授权主理人本人可访问。</p>
      </div>
    </section>

    <section id="encrypt" class="border-t border-amber-500/30 py-6">
      <h2 class="mb-3 font-display text-2xl font-semibold text-cream">三、加密措施</h2>
      <div class="space-y-3 text-sm leading-relaxed text-cream/70">
        <p>敏感数据在落库前采用 AES-256-GCM 算法加密存储；全部网络传输强制使用 HTTPS（TLS 1.3）加密通道。</p>
        <p>登录凭证以 JWT 形式签发，并设置合理过期时间，降低泄露风险。</p>
      </div>
    </section>

    <section id="retain" class="border-t border-amber-500/30 py-6">
      <h2 class="mb-3 font-display text-2xl font-semibold text-cream">四、数据保留与删除</h2>
      <div class="space-y-3 text-sm leading-relaxed text-cream/70">
        <p>对话记录与文档资料将在你使用期间持续保留；你可在应用内随时删除指定会话或文档，删除后将立即从活跃存储移除，并在合理期限内从备份中清除。</p>
        <p>日志数据默认保留不超过 90 天。</p>
      </div>
    </section>

    <section id="rights" class="border-t border-amber-500/30 py-6">
      <h2 class="mb-3 font-display text-2xl font-semibold text-cream">五、用户权利</h2>
      <div class="space-y-3 text-sm leading-relaxed text-cream/70">
        <p>因本服务为私有性质，主理人对本服务内的全部数据享有完整的所有权与控制权。</p>
      </div>
    </section>

    <section id="contact" class="border-t border-amber-500/30 py-6">
      <h2 class="mb-3 font-display text-2xl font-semibold text-cream">六、联系方式</h2>
      <div class="space-y-3 text-sm leading-relaxed text-cream/70">
        <p>如对本隐私政策有任何疑问或需行使数据权利，请通过应用内提供的联系方式与主理人（运营方）联系，我们将在合理期限内回复。</p>
        <p>联系邮箱：alu9740@163.com</p>
        <p>收到邮件后，主理人将在合理期限内进行回复和处理。</p>
      </div>
    </section>
  </article>
</template>
