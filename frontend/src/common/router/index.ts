import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/module/auth/stores/auth'

// 路由元信息类型扩展
declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    public?: boolean
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: () => import('@/common/layouts/AuthLayout.vue'),
    meta: { public: true },
    children: [
      { path: '', name: 'login', component: () => import('@/module/auth/views/LoginView.vue') },
    ],
  },
  {
    path: '/',
    component: () => import('@/common/layouts/DefaultLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('@/common/views/HomeView.vue'), meta: { requiresAuth: true } },
      { path: 'chat', name: 'chat', component: () => import('@/module/chat/views/ChatView.vue'), meta: { requiresAuth: true } },
      { path: 'pdf', name: 'pdf', component: () => import('@/module/pdf/views/ChatPdfView.vue'), meta: { requiresAuth: true } },
      { path: 'comfort', name: 'comfort', component: () => import('@/module/comfort/views/ComfortView.vue'), meta: { requiresAuth: true } },
      { path: 'agreement', name: 'agreement', component: () => import('@/common/views/AgreementView.vue'), meta: { public: true } },
      { path: 'privacy', name: 'privacy', component: () => import('@/common/views/PrivacyView.vue'), meta: { public: true } },
      { path: ':pathMatch(.*)*', name: 'not-found', component: () => import('@/common/views/NotFoundView.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

// 鉴权守卫：未登录访问受保护页 → 跳登录并带 redirect
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && auth.token) {
    return { path: '/' }
  }
})

export default router
