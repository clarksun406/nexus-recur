<template>
  <section class="panel">
    <h2>FX Exchange</h2>
    <div class="row" style="margin-bottom:1rem;gap:.5rem;align-items:end;flex-wrap:wrap">
      <div><label>Merchant ID</label><input v-model="merchantId" placeholder="merchant_default" /></div>
      <div><label>Source Wallet ID</label><input v-model="form.sourceWalletId" placeholder="wal_xxx" /></div>
      <div><label>Target Wallet ID</label><input v-model="form.targetWalletId" placeholder="wal_xxx" /></div>
      <div><label>Amount (cents)</label><input v-model.number="form.sourceAmountCents" type="number" /></div>
      <button class="btn" @click="exchange" :disabled="submitting">Exchange</button>
    </div>

    <div v-if="lastQuote" class="panel" style="margin-bottom:1rem;background:#f0fff4;border:1px solid #38a169">
      <strong>Exchange completed:</strong> rate {{ lastQuote.exchangeRate }} (spread {{ lastQuote.spreadBps }} bps)
    </div>

    <p v-if="error" class="negative">{{ error }}</p>
    <p v-if="!transactions.length && !error" class="row empty">No FX transactions.</p>

    <table v-if="transactions.length">
      <thead><tr><th>ID</th><th>Source</th><th>Target</th><th>Rate</th><th>Spread</th><th>Status</th><th>Created</th></tr></thead>
      <tbody>
        <tr v-for="t in transactions" :key="t.id">
          <td>{{ t.id }}</td>
          <td>{{ t.sourceCurrency }} {{ (t.sourceAmountCents / 100).toFixed(2) }}</td>
          <td>{{ t.targetCurrency }} {{ (t.targetAmountCents / 100).toFixed(2) }}</td>
          <td>{{ t.exchangeRate }}</td>
          <td>{{ t.spreadBps }} bps</td>
          <td><mark :class="t.status">{{ t.status }}</mark></td>
          <td>{{ formatDate(t.createdAt) }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const merchantId = ref('merchant_default')
const transactions = ref([])
const error = ref('')
const submitting = ref(false)
const lastQuote = ref(null)
const form = ref({ sourceWalletId: '', targetWalletId: '', sourceAmountCents: 0 })

async function load() {
  error.value = ''
  try {
    const result = await api.listFx(merchantId.value)
    transactions.value = result.content || result || []
  } catch (e) { error.value = e.message }
}

async function exchange() {
  error.value = ''
  lastQuote.value = null
  submitting.value = true
  try {
    lastQuote.value = await api.fxExchange(merchantId.value, form.value)
    await load()
  } catch (e) { error.value = e.message }
  finally { submitting.value = false }
}

function formatDate(iso) {
  return iso ? new Date(iso).toLocaleString() : '-'
}

onMounted(load)
</script>
