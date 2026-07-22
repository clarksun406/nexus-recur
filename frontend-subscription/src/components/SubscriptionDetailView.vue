<template>
  <section v-if="subscription" class="workspace single-detail">
    <div class="panel">
      <div class="panel-head">
        <h2>订阅详情</h2>
        <RouterLink to="/subscriptions" class="back-link">返回列表</RouterLink>
      </div>
      <div class="detail-grid">
        <div><label>ID</label><span>{{ subscription.id }}</span></div>
        <div><label>用户</label><span>{{ subscription.userId }}</span></div>
        <div><label>计划</label><span>{{ subscription.planId }}</span></div>
        <div><label>状态</label><span><mark :class="subscription.status">{{ subscription.status }}</mark></span></div>
        <div><label>外部订阅 ID</label><span>{{ subscription.externalSubId || '-' }}</span></div>
        <div><label>支付方式</label><span>{{ subscription.paymentMethodId || '-' }}</span></div>
        <div><label>周期开始</label><span>{{ formatDate(subscription.currentPeriodStart) }}</span></div>
        <div><label>周期结束</label><span>{{ formatDate(subscription.currentPeriodEnd) }}</span></div>
        <div><label>试用到期</label><span>{{ formatDate(subscription.trialEndAt) }}</span></div>
        <div><label>取消原因</label><span>{{ subscription.cancelReason || '-' }}</span></div>
      </div>
      <div class="detail-actions">
        <button @click="pause" :disabled="subscription.status !== 'active'">暂停</button>
        <button @click="resume" :disabled="subscription.status !== 'paused'">恢复</button>
        <button @click="cancel(false)" :disabled="!canCancel">到期取消</button>
        <button @click="cancel(true)" :disabled="!canCancel">立即取消</button>
      </div>
    </div>

    <nav class="tabs detail-tabs" aria-label="detail">
      <button :class="{ active: activeTab === 'invoices' }" @click="activeTab = 'invoices'">发票</button>
      <button :class="{ active: activeTab === 'events' }" @click="activeTab = 'events'">事件</button>
    </nav>

    <div v-if="activeTab === 'invoices'" class="panel">
      <div class="panel-head"><h2>发票记录</h2></div>
      <div class="table">
        <div class="row head">
          <span>发票 ID</span><span>金额</span><span>状态</span><span>周期</span><span>支付时间</span>
        </div>
        <div v-for="inv in invoices" :key="inv.id" class="row">
          <span><strong>{{ inv.id }}</strong></span>
          <span>{{ inv.amount }} {{ inv.currency }}</span>
          <span><mark :class="inv.status">{{ inv.status }}</mark></span>
          <span><small>{{ formatDate(inv.periodStart) }} → {{ formatDate(inv.periodEnd) }}</small></span>
          <span>{{ formatDate(inv.paidAt) }}</span>
        </div>
        <div v-if="invoices.length === 0" class="row empty"><span>暂无发票</span></div>
      </div>
    </div>

    <div v-if="activeTab === 'events'" class="panel">
      <div class="panel-head"><h2>事件日志</h2></div>
      <div class="table">
        <div class="row head">
          <span>事件 ID</span><span>类型</span><span>来源</span><span>时间</span>
        </div>
        <div v-for="evt in events" :key="evt.id" class="row four-col">
          <span><strong>{{ evt.id }}</strong></span>
          <span>{{ evt.eventType }}</span>
          <span>{{ evt.source }}</span>
          <span>{{ formatDate(evt.createdAt) }}</span>
        </div>
        <div v-if="events.length === 0" class="row empty"><span>暂无事件</span></div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../api'
import { useAppStore } from '../stores/app'
import { useSubscriptionStore } from '../stores/subscription'

const route = useRoute()
const app = useAppStore()
const subStore = useSubscriptionStore()

const subscription = ref(null)
const invoices = ref([])
const events = ref([])
const activeTab = ref('invoices')

const canCancel = ref(false)

async function loadDetail() {
  const id = route.params.id
  await app.withLoading(async () => {
    subscription.value = await api.getSubscription(id)
    canCancel.value = ['pending', 'trialing', 'active', 'past_due', 'paused', 'scheduled_cancel'].includes(subscription.value.status)
    const [invData, evtData] = await Promise.all([
      api.subscriptionInvoices(id),
      api.subscriptionEvents(id)
    ])
    invoices.value = invData.items
    events.value = evtData.items
  })
}

async function pause() {
  await subStore.pause(subscription.value.id)
  await loadDetail()
}

async function resume() {
  await subStore.resume(subscription.value.id)
  await loadDetail()
}

async function cancel(immediate) {
  await subStore.cancel(subscription.value.id, immediate)
  await loadDetail()
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString()
}

onMounted(loadDetail)
watch(() => route.params.id, loadDetail)
</script>
