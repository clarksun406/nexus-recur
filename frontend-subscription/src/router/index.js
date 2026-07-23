import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'dashboard', component: () => import('../components/DashboardView.vue') },
  { path: '/plans', name: 'plans', component: () => import('../components/PlansView.vue') },
  { path: '/subscriptions', name: 'subscriptions', component: () => import('../components/SubscriptionsView.vue') },
  { path: '/subscriptions/:id', name: 'subscription-detail', component: () => import('../components/SubscriptionDetailView.vue') },
  { path: '/wallets', name: 'wallets', component: () => import('../components/WalletView.vue') },
  { path: '/settlements', name: 'settlements', component: () => import('../components/SettlementsView.vue') },
  { path: '/entitlements', name: 'entitlements', component: () => import('../components/EntitlementsView.vue') },
  { path: '/developer', name: 'developer', component: () => import('../components/DeveloperView.vue') },
  { path: '/payment-orders', name: 'payment-orders', component: () => import('../components/PaymentOrdersView.vue') },
  { path: '/usage', name: 'usage', component: () => import('../components/UsageView.vue') },
  { path: '/fx', name: 'fx', component: () => import('../components/FxView.vue') },
  { path: '/reconciliation', name: 'reconciliation', component: () => import('../components/ReconciliationView.vue') }
]

export const router = createRouter({
  history: createWebHistory(),
  routes
})
