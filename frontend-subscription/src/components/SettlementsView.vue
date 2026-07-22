<template>
  <section class="panel">
    <h2>Settlements</h2>
    <div class="row two-col" style="margin-bottom:1rem">
      <div>
        <label>Filter by status</label>
        <select v-model="statusFilter" @change="load">
          <option value="">All</option>
          <option value="pending">Pending</option>
          <option value="approved">Approved</option>
          <option value="processing">Processing</option>
          <option value="completed">Completed</option>
          <option value="rejected">Rejected</option>
        </select>
      </div>
      <div style="align-self:end">
        <button class="btn" @click="showForm = !showForm">New Settlement</button>
      </div>
    </div>

    <div v-if="showForm" class="panel" style="margin-bottom:1rem;background:#f8fffe">
      <h3>Initiate Settlement</h3>
      <div class="row two-col">
        <div><label>Wallet ID</label><input v-model="form.walletId" placeholder="wal_xxx" /></div>
        <div><label>Amount (cents)</label><input v-model.number="form.amountCents" type="number" /></div>
      </div>
      <div class="row two-col">
        <div><label>Target Currency</label><input v-model="form.targetCurrency" placeholder="CNY" /></div>
        <div><label>Bank Account</label><input v-model="form.bankAccount" placeholder="CNXX XXXX XXXX" /></div>
      </div>
      <div class="row">
        <div><label>Background Refs</label><input v-model="form.backgroundRefs" placeholder="invoice IDs or trade refs" /></div>
      </div>
      <button class="btn" @click="submitSettlement" :disabled="submitting">Submit</button>
      <span v-if="formError" class="negative" style="margin-left:1rem">{{ formError }}</span>
    </div>

    <p v-if="error" class="negative">{{ error }}</p>
    <p v-if="!error && settlements.length === 0" class="row empty">No settlements found.</p>

    <table v-if="settlements.length">
      <thead>
        <tr>
          <th>ID</th><th>Amount</th><th>Currency</th><th>Target</th><th>Status</th><th>Approved By</th><th>Created</th><th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in settlements" :key="s.id">
          <td class="sub-link">{{ s.id }}</td>
          <td>{{ (s.amountCents / 100).toFixed(2) }}</td>
          <td>{{ s.currency }}</td>
          <td>{{ s.targetCurrency }}</td>
          <td><mark :class="s.status">{{ s.status }}</mark></td>
          <td>{{ s.approvedBy || '-' }}</td>
          <td>{{ formatDate(s.createdAt) }}</td>
          <td>
            <button v-if="s.status === 'pending'" class="btn-sm" @click="approve(s.id)">Approve</button>
            <button v-if="s.status === 'pending'" class="btn-sm" @click="reject(s.id)">Reject</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const settlements = ref([])
const statusFilter = ref('')
const error = ref('')
const showForm = ref(false)
const submitting = ref(false)
const formError = ref('')
const form = ref({ walletId: '', amountCents: 0, targetCurrency: 'CNY', bankAccount: '', backgroundRefs: '' })

async function load() {
  error.value = ''
  try {
    const result = await api.listSettlements(statusFilter.value)
    settlements.value = result.items || result || []
  } catch (e) {
    error.value = e.message
  }
}

async function submitSettlement() {
  formError.value = ''
  submitting.value = true
  try {
    await api.initiateSettlement(form.value)
    showForm.value = false
    form.value = { walletId: '', amountCents: 0, targetCurrency: 'CNY', bankAccount: '', backgroundRefs: '' }
    await load()
  } catch (e) {
    formError.value = e.message
  } finally {
    submitting.value = false
  }
}

async function approve(id) {
  try {
    await api.approveSettlement(id, 'owner_admin')
    await load()
  } catch (e) { error.value = e.message }
}

async function reject(id) {
  const reason = prompt('Rejection reason:')
  if (!reason) return
  try {
    await api.rejectSettlement(id, 'owner_admin', reason)
    await load()
  } catch (e) { error.value = e.message }
}

function formatDate(iso) {
  return iso ? new Date(iso).toLocaleDateString() : '-'
}

onMounted(load)
</script>
