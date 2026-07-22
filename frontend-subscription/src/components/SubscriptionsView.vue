<template>
  <section class="workspace">
    <form class="panel form-grid" @submit.prevent="createSubscription">
      <label>
        用户 ID
        <input v-model="subscriptionForm.userId" required placeholder="user_xxx" />
      </label>
      <label>
        计划
        <select v-model="subscriptionForm.planId" required>
          <option value="" disabled>选择计划</option>
          <option v-for="plan in planStore.activePlans" :key="plan.id" :value="plan.id">{{ plan.name }}</option>
        </select>
      </label>
      <label class="wide">
        成功跳转
        <input v-model="subscriptionForm.successUrl" required />
      </label>
      <label class="wide">
        取消跳转
        <input v-model="subscriptionForm.cancelUrl" required />
      </label>
      <button class="primary" type="submit" :disabled="app.loading">创建订阅</button>
    </form>

    <div class="panel">
      <div class="panel-head">
        <h2>订阅列表</h2>
        <div class="filters">
          <input v-model="subStore.filters.userId" placeholder="userId" />
          <select v-model="subStore.filters.status">
            <option value="">全部状态</option>
            <option value="pending">pending</option>
            <option value="trialing">trialing</option>
            <option value="active">active</option>
            <option value="past_due">past_due</option>
            <option value="paused">paused</option>
            <option value="scheduled_cancel">scheduled_cancel</option>
            <option value="canceled">canceled</option>
            <option value="expired">expired</option>
          </select>
          <button @click="subStore.loadSubscriptions()">查询</button>
        </div>
      </div>
      <div class="table subscriptions">
        <div class="row head">
          <span>订阅</span><span>用户</span><span>计划</span><span>状态</span><span>到期</span><span>操作</span>
        </div>
        <div v-for="item in subStore.subscriptions" :key="item.id" class="row">
          <span><RouterLink :to="`/subscriptions/${item.id}`" class="sub-link"><strong>{{ item.id }}</strong></RouterLink><small>{{ item.externalSubId }}</small></span>
          <span>{{ item.userId }}</span>
          <span>{{ item.planId }}</span>
          <span><mark :class="item.status">{{ item.status }}</mark></span>
          <span>{{ formatDate(item.currentPeriodEnd) }}</span>
          <span class="actions">
            <button @click="subStore.pause(item.id)" :disabled="item.status !== 'active'">暂停</button>
            <button @click="subStore.resume(item.id)" :disabled="item.status !== 'paused'">恢复</button>
            <button @click="subStore.cancel(item.id, false)" :disabled="!canCancel(item.status)">到期取消</button>
            <button @click="subStore.cancel(item.id, true)" :disabled="!canCancel(item.status)">立即取消</button>
          </span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useAppStore } from '../stores/app'
import { usePlanStore } from '../stores/plan'
import { useSubscriptionStore } from '../stores/subscription'

const app = useAppStore()
const planStore = usePlanStore()
const subStore = useSubscriptionStore()

const subscriptionForm = reactive({
  userId: '',
  planId: '',
  successUrl: 'https://example.com/subscription/success',
  cancelUrl: 'https://example.com/subscription/cancel'
})

async function createSubscription() {
  await subStore.createSubscription(subscriptionForm)
}

function canCancel(status) {
  return ['pending', 'trialing', 'active', 'past_due', 'paused', 'scheduled_cancel'].includes(status)
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString()
}

onMounted(() => {
  planStore.loadPlans()
  subStore.loadSubscriptions()
})
</script>
