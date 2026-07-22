<template>
  <section v-if="dashStore.stats" class="dashboard">
    <div class="metrics">
      <div class="panel metric">
        <span class="value">{{ dashStore.stats.activeSubscriptions }}</span>
        <span class="label">活跃订阅</span>
      </div>
      <div class="panel metric">
        <span class="value">{{ dashStore.stats.trialingSubscriptions }}</span>
        <span class="label">试用中</span>
      </div>
      <div class="panel metric">
        <span class="value">{{ dashStore.stats.mrr }} {{ dashStore.stats.mrrCurrency }}</span>
        <span class="label">MRR (月经常性收入)</span>
      </div>
      <div class="panel metric">
        <span class="value">{{ dashStore.stats.chargeSuccessRate }}%</span>
        <span class="label">扣款成功率 ({{ dashStore.stats.totalCharges }} 笔)</span>
      </div>
      <div class="panel metric">
        <span class="value">{{ dashStore.stats.pendingActions?.length || 0 }}</span>
        <span class="label">待办操作</span>
      </div>
    </div>

    <div class="dashboard-grid">
      <div class="panel">
        <div class="panel-head">
          <h2>各币种收入</h2>
        </div>
        <div class="table">
          <div class="row head">
            <span>币种</span><span>收入</span>
          </div>
          <div v-for="(amount, currency) in dashStore.stats.revenueByCurrency" :key="currency" class="row two-col">
            <span><strong>{{ currency }}</strong></span>
            <span>{{ amount }}</span>
          </div>
          <div v-if="!dashStore.stats.revenueByCurrency || Object.keys(dashStore.stats.revenueByCurrency).length === 0" class="row empty">
            <span>暂无收入数据</span>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">
          <h2>钱包余额</h2>
        </div>
        <div class="table">
          <div class="row head">
            <span>币种</span><span>余额</span><span>冻结</span>
          </div>
          <div v-for="w in dashStore.stats.walletBalances" :key="w.currency" class="row three-col">
            <span><strong>{{ w.currency }}</strong></span>
            <span>{{ w.balance }}</span>
            <span>{{ w.pendingBalance }}</span>
          </div>
          <div v-if="!dashStore.stats.walletBalances || dashStore.stats.walletBalances.length === 0" class="row empty">
            <span>暂无钱包</span>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">
          <h2>待办操作</h2>
        </div>
        <div class="table">
          <div class="row head">
            <span>订阅</span><span>用户</span><span>类型</span>
          </div>
          <div v-for="a in dashStore.stats.pendingActions" :key="a.subscriptionId" class="row pending">
            <span>
              <RouterLink :to="`/subscriptions/${a.subscriptionId}`">{{ a.subscriptionId }}</RouterLink>
            </span>
            <span>{{ a.userId }}</span>
            <span><mark :class="a.status">{{ a.actionType }}</mark></span>
          </div>
          <div v-if="!dashStore.stats.pendingActions || dashStore.stats.pendingActions.length === 0" class="row empty">
            <span>暂无待办</span>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">
          <h2>最近事件</h2>
          <button @click="dashStore.loadStats()">刷新</button>
        </div>
        <div class="table">
          <div class="row head">
            <span>订阅</span><span>事件</span><span>时间</span>
          </div>
          <div v-for="e in dashStore.stats.recentEvents" :key="e.id" class="row three-col">
            <span>
              <RouterLink :to="`/subscriptions/${e.subscriptionId}`">{{ e.subscriptionId.substring(0, 12) }}…</RouterLink>
            </span>
            <span>{{ e.eventType }}</span>
            <span><small>{{ formatDate(e.createdAt) }}</small></span>
          </div>
          <div v-if="!dashStore.stats.recentEvents || dashStore.stats.recentEvents.length === 0" class="row empty">
            <span>暂无事件</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAppStore } from '../stores/app'
import { useDashboardStore } from '../stores/dashboard'

const app = useAppStore()
const dashStore = useDashboardStore()

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString()
}

onMounted(() => {
  dashStore.loadStats()
})
</script>
