<template>
  <main class="shell">
    <header class="topbar">
      <div>
        <h1>订阅管理</h1>
        <p>仪表盘、计划、订阅、钱包、权益</p>
      </div>
      <div class="status-pill" :class="{ online: app.health === 'connected' }">{{ healthText }}</div>
    </header>

    <nav class="tabs" aria-label="views">
      <RouterLink :class="{ active: route.name === 'dashboard' }" to="/dashboard">仪表盘</RouterLink>
      <RouterLink :class="{ active: route.name === 'plans' }" to="/plans">计划</RouterLink>
      <RouterLink :class="{ active: route.name === 'subscriptions' || route.name === 'subscription-detail' }" to="/subscriptions">订阅</RouterLink>
      <RouterLink :class="{ active: route.name === 'wallets' }" to="/wallets">钱包</RouterLink>
      <RouterLink :class="{ active: route.name === 'entitlements' }" to="/entitlements">权益</RouterLink>
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
