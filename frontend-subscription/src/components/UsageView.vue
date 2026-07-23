<template>
  <section class="panel">
    <h2>Usage Records</h2>
    <div class="row" style="margin-bottom:1rem;gap:.5rem;align-items:end;flex-wrap:wrap">
      <div><label>Subscription ID</label><input v-model="form.subscriptionId" placeholder="sub_xxx" /></div>
      <div><label>Plan ID</label><input v-model="form.planId" placeholder="plan_xxx" /></div>
      <div><label>Quantity</label><input v-model.number="form.quantity" type="number" /></div>
      <div><label>Unit</label><input v-model="form.unitName" placeholder="api_calls" /></div>
      <button class="btn" @click="report" :disabled="submitting">Report Usage</button>
    </div>
    <div class="row" style="margin-bottom:1rem;gap:.5rem;align-items:end">
      <div><label>Query Subscription ID</label><input v-model="querySubId" placeholder="sub_xxx" /></div>
      <button class="btn" @click="load">Query</button>
    </div>

    <p v-if="error" class="negative">{{ error }}</p>
    <p v-if="message" class="row" style="color:#38a169">{{ message }}</p>
    <p v-if="!records.length && !error" class="row empty">No usage records. Query by subscription ID.</p>

    <table v-if="records.length">
      <thead><tr><th>ID</th><th>Subscription</th><th>Quantity</th><th>Unit</th><th>Idempotency Key</th><th>Recorded At</th></tr></thead>
      <tbody>
        <tr v-for="r in records" :key="r.id">
          <td>{{ r.id }}</td>
          <td class="sub-link">{{ r.subscriptionId }}</td>
          <td>{{ r.quantity }}</td>
          <td>{{ r.unitName }}</td>
          <td><code>{{ r.idempotencyKey }}</code></td>
          <td>{{ formatDate(r.recordedAt) }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { api } from '../api'

const records = ref([])
const error = ref('')
const message = ref('')
const submitting = ref(false)
const querySubId = ref('')
const form = ref({ subscriptionId: '', planId: '', quantity: 0, unitName: 'api_calls' })

async function load() {
  if (!querySubId.value) return
  error.value = ''
  try {
    const result = await api.listUsage(querySubId.value)
    records.value = result.content || result || []
  } catch (e) { error.value = e.message }
}

async function report() {
  error.value = ''
  message.value = ''
  submitting.value = true
  try {
    await api.reportUsage(form.value)
    message.value = 'Usage reported successfully'
    querySubId.value = form.value.subscriptionId
    await load()
  } catch (e) { error.value = e.message }
  finally { submitting.value = false }
}

function formatDate(iso) {
  return iso ? new Date(iso).toLocaleString() : '-'
}
</script>
