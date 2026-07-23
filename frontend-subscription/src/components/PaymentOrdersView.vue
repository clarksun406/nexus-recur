<template>
  <section class="panel">
    <h2>Payment Orders</h2>
    <div class="row" style="margin-bottom:1rem;gap:.5rem;align-items:end;flex-wrap:wrap">
      <div><label>Merchant ID</label><input v-model="form.merchantId" placeholder="merchant_default" /></div>
      <div><label>Wallet ID</label><input v-model="form.walletId" placeholder="wal_xxx" /></div>
      <div><label>Currency</label><input v-model="form.currency" placeholder="USD" /></div>
      <div><label>Amount (cents)</label><input v-model.number="form.amountCents" type="number" /></div>
      <div>
        <label>Method</label>
        <select v-model="form.method">
          <option value="ach">ACH</option>
          <option value="sepa">SEPA</option>
          <option value="wire">Wire</option>
        </select>
      </div>
    </div>
    <div class="row" style="margin-bottom:1rem;gap:.5rem;align-items:end;flex-wrap:wrap">
      <div><label>Beneficiary Name</label><input v-model="form.beneficiaryName" /></div>
      <div><label>Account</label><input v-model="form.beneficiaryAccount" /></div>
      <div><label>Bank</label><input v-model="form.beneficiaryBank" /></div>
      <div><label>Country</label><input v-model="form.beneficiaryCountry" placeholder="US" style="width:4rem" /></div>
      <button class="btn" @click="create" :disabled="submitting">Submit Payment</button>
    </div>

    <p v-if="error" class="negative">{{ error }}</p>
    <p v-if="!orders.length && !error" class="row empty">No payment orders.</p>

    <table v-if="orders.length">
      <thead><tr><th>ID</th><th>Amount</th><th>Currency</th><th>Method</th><th>Beneficiary</th><th>Sanctions</th><th>Status</th><th>Actions</th></tr></thead>
      <tbody>
        <tr v-for="o in orders" :key="o.id">
          <td class="sub-link">{{ o.id }}</td>
          <td>{{ (o.amountCents / 100).toFixed(2) }}</td>
          <td>{{ o.currency }}</td>
          <td>{{ o.method }}</td>
          <td>{{ o.beneficiaryName }}</td>
          <td><mark :class="o.sanctionsResult">{{ o.sanctionsResult }}</mark></td>
          <td><mark :class="o.status">{{ o.status }}</mark></td>
          <td>
            <button v-if="o.status === 'pending_approval'" class="btn-sm" @click="approve(o.id)">Approve</button>
            <button v-if="o.status === 'pending_approval'" class="btn-sm" @click="reject(o.id)">Reject</button>
            <button v-if="o.status === 'processing'" class="btn-sm" @click="complete(o.id)">Complete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const orders = ref([])
const error = ref('')
const submitting = ref(false)
const form = ref({ merchantId: 'merchant_default', walletId: '', currency: 'USD', amountCents: 0, method: 'ach', beneficiaryName: '', beneficiaryAccount: '', beneficiaryBank: '', beneficiaryCountry: '' })

async function load() {
  error.value = ''
  try {
    const result = await api.listPaymentOrders(form.value.merchantId)
    orders.value = result.items || result.content || result || []
  } catch (e) { error.value = e.message }
}

async function create() {
  error.value = ''
  submitting.value = true
  try {
    await api.createPaymentOrder(form.value)
    await load()
  } catch (e) { error.value = e.message }
  finally { submitting.value = false }
}

async function approve(id) {
  try { await api.approvePaymentOrder(id, 'owner_admin'); await load() } catch (e) { error.value = e.message }
}
async function reject(id) {
  try { await api.rejectPaymentOrder(id, 'owner_admin'); await load() } catch (e) { error.value = e.message }
}
async function complete(id) {
  try { await api.completePaymentOrder(id); await load() } catch (e) { error.value = e.message }
}

onMounted(load)
</script>
