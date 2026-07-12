<script setup lang="ts">
import { ShieldCheck } from 'lucide-vue-next'

const sections = [
  { id: 'terms', title: '一、服务条款' },
  { id: 'usage', title: '二、使用规范' },
  { id: 'disclaimer', title: '三、免责声明' },
  { id: 'dispute', title: '四、争议解决' },
] as const
</script>

<template>
  <article class="mx-auto max-w-[860px] px-6 py-12">
    <header class="mb-10 text-center">
      <ShieldCheck :size="36" class="mx-auto text-amber-400" />
      <h1 class="mt-3 font-display text-4xl font-bold text-cream">用户协议</h1>
      <p class="mt-2 text-sm text-cream/50">最近更新：2026 年 7 月 7 日</p>
    </header>

    <nav class="mb-8 flex flex-wrap justify-center gap-x-4 gap-y-1 text-xs text-cream/60">
      <a v-for="s in sections" :key="s.id" :href="'#' + s.id" class="transition hover:text-amber-400">
        {{ s.title }}
      </a>
    </nav>

    <section id="terms" class="border-t border-amber-500/30 py-6">
      <h2 class="mb-3 font-display text-2xl font-semibold text-cream">一、服务条款</h2>
      <div class="space-y-3 text-sm leading-relaxed text-cream/70">
        <p>ChatVibe（下称"本服务"）是一项面向主理人的私人 AI 应用。开始使用本服务即视为你已阅读并同意本协议全部条款。</p>
        <p>本服务以非公开账户形式提供，账户凭证由主理人自行保管，因凭证泄露造成的损失由主理人自行承担。</p>
      </div>
    </section>

    <section id="usage" class="border-t border-amber-500/30 py-6">
      <h2 class="mb-3 font-display text-2xl font-semibold text-cream">二、使用规范</h2>
      <div class="space-y-3 text-sm leading-relaxed text-cream/70">
        <p>主理人应仅将本服务用于合法的个人学习、情感练习与知识问答目的，不得利用本服务从事任何违反法律法规或公序良俗的行为。</p>
        <p>不得上传含有恶意代码、侵犯他人知识产权或隐私的文件，不得对服务进行逆向工程、压力测试或恶意攻击。</p>
      </div>
    </section>

    <section id="disclaimer" class="border-t border-amber-500/30 py-6">
      <h2 class="mb-3 font-display text-2xl font-semibold text-cream">三、免责声明</h2>
      <div class="space-y-3 text-sm leading-relaxed text-cream/70">
        <p>本服务生成的所有内容均由人工智能模型产出，可能存在不准确、不完整或不适用的情况，不构成任何专业建议（包括但不限于医疗、法律、财务、心理咨询建议）。</p>
        <p>主理人应对自身基于 AI 内容做出的任何决定独立判断并自行承担风险，本服务不对使用后果承担责任。</p>
      </div>
    </section>

    <section id="dispute" class="border-t border-amber-500/30 py-6">
      <h2 class="mb-3 font-display text-2xl font-semibold text-cream">四、争议解决</h2>
      <div class="space-y-3 text-sm leading-relaxed text-cream/70">
        <p>本协议的订立、效力、解释与争议解决均适用中华人民共和国法律。</p>
        <p>因本服务产生的争议，双方应首先友好协商解决；协商不成的，可向本服务运营方所在地有管辖权的人民法院提起诉讼。</p>
      </div>
    </section>
  </article>
</template>
