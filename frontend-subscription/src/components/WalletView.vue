<template>
  <section class="workspace single">
    <form class="panel form-grid" @submit.prevent="loadWallets">
      <label>
        商户 ID
        <input v-model="walletStore.merchantId" required placeholder="merchant_default" />
      </label>
      <button class="primary" type="submit" :disabled="app.loading">查询钱包</button>
    </form>

    <div class="panel">
      <div class="panel-head">
        <h2>钱包列表</h2>
      </div>
      <div class="table">
        <div class="row head">
          <span>钱包 ID</span><span>币种</span><span>余额</span><span>冻结</span><span>状态</span>
        </div>
        <div v-for="w in walletStore.wallets" :key="w.id" class="row" :class="{ selected: walletStore.selectedWalletId === w.id }" @click="selectWallet(w.id)">
          <span><strong>{{ w.id }}</strong></span>
          <span>{{ w.currency }}</span>
          <span>{{ w.balance }}</span>
          <span>{{ w.pendingBalance }}</span>
          <span><mark :class="w.status">{{ w.status }}</mark></span>
        </div>
        <div v-if="walletStore.wallets.length === 0" class="row empty"><span>暂无钱包</span></div>
      </div>
    </div>

    <div v-if="walletStore.selectedWalletId" class="panel">
      <div class="panel-head">
        <h2>交易明细</h2>
      </div>
      <div class="table">
        <div class="row head">
          <span>交易 ID</span><span>类型</span><span>金额</span><span>描述</span><span>时间</span>
        </div>
        <div v-for="t in walletStore.transactions" :key="t.id" class="row">
          <span><strong>{{ t.id }}</strong></span>
          <span><mark :class="t.type">{{ t.type }}</mark></span>
          <span :class="{ negative: ['expense', 'fx', 'settlement'].includes(t.type) }">
            {{ ['expense', 'fx', 'settlement'].includes(t.type) ? '-' : '+' }}{{ t.amount }} {{ t.currency }}
          </span>
          <span>{{ t.description || '-' }}</span>
          <span>{{ formatDate(t.createdAt) }}</span>
        </div>
        <div v-if="walletStore.transactions.length === 0" class="row empty"><span>暂无交易</span></div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAppStore } from '../stores/app'
import { useWalletStore } from '../stores/wallet'

const app = useAppStore()
const walletStore = useWalletStore()

async function selectWallet(walletId) {
  walletStore.selectedWalletId = walletId
  await walletStore.loadTransactions()
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString()
}

onMounted(() => {
  walletStore.loadWallets()
})
</script>
