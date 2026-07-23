<template>
  <main class="shell">
    <header class="topbar">
      <div>
        <h1>订阅管理</h1>
        <p>仪表盘、计划、订阅、钱包、结汇、权益</p>
      </div>
      <div class="status-pill" :class="{ online: app.health === 'connected' }">{{ healthText }}</div>
    </header>

    <nav class="tabs" aria-label="views">
      <RouterLink :class="{ active: route.name === 'dashboard' }" to="/dashboard">仪表盘</RouterLink>
      <RouterLink :class="{ active: route.name === 'plans' }" to="/plans">计划</RouterLink>
      <RouterLink :class="{ active: route.name === 'subscriptions' || route.name === 'subscription-detail' }" to="/subscriptions">订阅</RouterLink>
      <RouterLink :class="{ active: route.name === 'wallets' }" to="/wallets">钱包</RouterLink>
      <RouterLink :class="{ active: route.name === 'settlements' }" to="/settlements">结汇</RouterLink>
      <RouterLink :class="{ active: route.name === 'entitlements' }" to="/entitlements">权益</RouterLink>
      <RouterLink :class="{ active: route.name === 'developer' }" to="/developer">开发者</RouterLink>
      <RouterLink :class="{ active: route.name === 'payment-orders' }" to="/payment-orders">付款</RouterLink>
      <RouterLink :class="{ active: route.name === 'usage' }" to="/usage">用量</RouterLink>
      <RouterLink :class="{ active: route.name === 'fx' }" to="/fx">换汇</RouterLink>
      <RouterLink :class="{ active: route.name === 'reconciliation' }" to="/reconciliation">对账</RouterLink>
      <RouterLink :class="{ active: route.name === 'routing' }" to="/routing">路由</RouterLink>
      <RouterLink :class="{ active: route.name === 'virtual-cards' }" to="/virtual-cards">虚拟卡</RouterLink>
      <RouterLink :class="{ active: route.name === 'entities' }" to="/entities">主体</RouterLink>
      <RouterLink :class="{ active: route.name === 'licenses' }" to="/licenses">密钥</RouterLink>
    </nav>

    <section v-if="app.error" class="notice error">{{ app.error }}</section>
    <section v-if="app.message" class="notice">{{ app.message }}</section>

    <RouterView />
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from './stores/app'

const app = useAppStore()
const route = useRoute()

const healthText = computed(() => (app.health === 'connected' ? 'API connected' : 'API pending'))
</script>
