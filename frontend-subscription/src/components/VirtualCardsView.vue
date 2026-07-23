<template>
  <section class="workspace">
    <form class="panel form-grid" @submit.prevent="issueCard">
      <label>
        客户 ID
        <input v-model="form.customerId" placeholder="cus_xxx" />
      </label>
      <label>
        币种
        <input v-model="form.currency" maxlength="3" placeholder="USD" />
      </label>
      <label>
        消费限额 (cents)
        <input v-model.number="form.spendingLimitCents" type="number" min="0" />
      </label>
      <label>
        有效期 (月)
        <input v-model.number="form.validityMonths" type="number" min="1" placeholder="36" />
      </label>
      <label>
        标签
        <input v-model="form.label" placeholder="Marketing Card" />
      </label>
      <button class="primary" type="submit" :disabled="app.loading">发行虚拟卡</button>
    </form>

    <div class="panel">
      <div class="panel-head">
        <h2>虚拟卡列表</h2>
        <button @click="loadCards">刷新</button>
      </div>
      <div class="table">
        <div class="row head">
          <span>卡号</span><span>客户</span><span>限额/已用</span><span>币种</span><span>状态</span><span>操作</span>
        </div>
        <div v-for="card in cards" :key="card.id" class="row">
          <span>
            <strong>**** {{ card.last4 }}</strong>
            <small>{{ card.label || card.id }}</small>
          </span>
          <span>{{ card.customerId || '-' }}</span>
          <span>{{ (card.spentCents / 100).toFixed(2) }} / {{ (card.spendingLimitCents / 100).toFixed(2) }}</span>
          <span>{{ card.currency }}</span>
          <span><mark :class="card.status">{{ statusText(card.status) }}</mark></span>
          <span>
            <button v-if="card.status === 'active'" @click="freeze(card.id)">冻结</button>
            <button v-if="card.status === 'frozen'" @click="unfreeze(card.id)">解冻</button>
            <button v-if="card.status !== 'closed'" @click="close(card.id)">关闭</button>
          </span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { api } from '../api'
import { useAppStore } from '../stores/app'

const app = useAppStore()
const cards = ref([])

const form = reactive({
  customerId: '',
  currency: 'USD',
  spendingLimitCents: 100000,
  validityMonths: 36,
  label: ''
})

function statusText(s) {
  return { active: '活跃', frozen: '冻结', closed: '已关闭', expired: '过期' }[s] || s
}

async function loadCards() {
  app.loading = true
  try {
    const res = await api.listVirtualCards()
    cards.value = res.items || res.content || res
  } catch (e) { app.error = e.message } finally { app.loading = false }
}

async function issueCard() {
  app.loading = true
  try {
    await api.issueVirtualCard(form)
    app.message = '虚拟卡已发行'
    await loadCards()
  } catch (e) { app.error = e.message } finally { app.loading = false }
}

async function freeze(id) {
  try { await api.freezeVirtualCard(id); await loadCards() } catch (e) { app.error = e.message }
}
async function unfreeze(id) {
  try { await api.unfreezeVirtualCard(id); await loadCards() } catch (e) { app.error = e.message }
}
async function close(id) {
  try { await api.closeVirtualCard(id); await loadCards() } catch (e) { app.error = e.message }
}

onMounted(loadCards)
</script>
