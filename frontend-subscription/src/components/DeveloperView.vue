<template>
  <section class="panel">
    <h2>Developer Center</h2>

    <nav class="tabs" style="margin-bottom:1rem">
      <button :class="{ active: tab === 'keys' }" @click="tab = 'keys'">API Keys</button>
      <button :class="{ active: tab === 'webhooks' }" @click="tab = 'webhooks'">Webhooks</button>
    </nav>

    <!-- API Keys Tab -->
    <div v-if="tab === 'keys'">
      <div class="row" style="margin-bottom:1rem;gap:.5rem;align-items:end">
        <div><label>User ID</label><input v-model="keyUserId" placeholder="user_001" /></div>
        <div><label>Merchant ID</label><input v-model="keyMerchantId" placeholder="mch_xxx" /></div>
        <div>
          <label>Scope</label>
          <select v-model="keyScope">
            <option value="full_access">full_access</option>
            <option value="read_only">read_only</option>
            <option value="payments_only">payments_only</option>
            <option value="settlements_only">settlements_only</option>
          </select>
        </div>
        <button class="btn" @click="createKey" :disabled="creatingKey">Generate Key</button>
      </div>

      <div v-if="newKey" class="panel" style="margin-bottom:1rem;background:#f0fff4;border:1px solid #38a169">
        <strong>New API Key (copy now, shown only once):</strong>
        <code style="display:block;margin-top:.5rem;word-break:break-all">{{ newKey }}</code>
      </div>

      <p v-if="keyError" class="negative">{{ keyError }}</p>
      <table v-if="apiKeys.length">
        <thead><tr><th>Prefix</th><th>Scope</th><th>Status</th><th>Created</th><th>Last Used</th><th>Actions</th></tr></thead>
        <tbody>
          <tr v-for="k in apiKeys" :key="k.id">
            <td><code>{{ k.keyPrefix }}...</code></td>
            <td>{{ k.scope }}</td>
            <td><mark :class="k.status">{{ k.status }}</mark></td>
            <td>{{ formatDate(k.createdAt) }}</td>
            <td>{{ k.lastUsedAt ? formatDate(k.lastUsedAt) : 'Never' }}</td>
            <td><button v-if="k.status === 'active'" class="btn-sm" @click="revokeKey(k.id)">Revoke</button></td>
          </tr>
        </tbody>
      </table>
      <p v-if="!apiKeys.length && !keyError" class="row empty">No API keys. Enter a User ID and generate one.</p>
    </div>

    <!-- Webhooks Tab -->
    <div v-if="tab === 'webhooks'">
      <div class="row" style="margin-bottom:1rem;gap:.5rem;align-items:end">
        <div style="flex:1"><label>Endpoint URL</label><input v-model="whUrl" placeholder="https://example.com/webhook" style="width:100%" /></div>
        <div><label>Merchant ID</label><input v-model="whMerchantId" placeholder="mch_xxx" /></div>
        <button class="btn" @click="createEndpoint" :disabled="creatingWh">Add Endpoint</button>
      </div>

      <p v-if="whError" class="negative">{{ whError }}</p>
      <p v-if="!endpoints.length && !whError" class="row empty">No webhook endpoints configured.</p>

      <div v-for="ep in endpoints" :key="ep.id" class="panel" style="margin-bottom:1rem">
        <div class="row" style="justify-content:space-between;align-items:center">
          <div>
            <strong>{{ ep.url }}</strong>
            <mark :class="ep.status" style="margin-left:.5rem">{{ ep.status }}</mark>
          </div>
          <div style="display:flex;gap:.5rem">
            <button class="btn-sm" @click="toggleDeliveries(ep.id)">{{ expandedEp === ep.id ? 'Hide' : 'Show' }} Logs</button>
            <button v-if="ep.status === 'active'" class="btn-sm" @click="disableEndpoint(ep.id)">Disable</button>
            <button v-else class="btn-sm" @click="enableEndpoint(ep.id)">Enable</button>
          </div>
        </div>
        <div style="margin-top:.5rem;font-size:.85rem;color:#666">
          Secret: <code>{{ ep.secret }}</code><br/>
          Events: {{ ep.events.length ? ep.events.join(', ') : 'All events' }}
        </div>

        <div v-if="expandedEp === ep.id" style="margin-top:1rem">
          <h4>Delivery Logs</h4>
          <p v-if="!deliveries.length" class="row empty">No deliveries yet.</p>
          <table v-if="deliveries.length">
            <thead><tr><th>Event</th><th>Status</th><th>Attempts</th><th>Response</th><th>Time</th></tr></thead>
            <tbody>
              <tr v-for="d in deliveries" :key="d.id">
                <td>{{ d.eventType }}</td>
                <td><mark :class="d.status">{{ d.status }}</mark></td>
                <td>{{ d.attempts }}/{{ d.maxAttempts }}</td>
                <td>{{ d.responseCode || '-' }}</td>
                <td>{{ formatDate(d.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { api } from '../api'

const tab = ref('keys')

// API Keys
const keyUserId = ref('')
const keyMerchantId = ref('')
const keyScope = ref('full_access')
const apiKeys = ref([])
const newKey = ref('')
const keyError = ref('')
const creatingKey = ref(false)

async function loadKeys() {
  if (!keyUserId.value) return
  keyError.value = ''
  try {
    const result = await api.listApiKeys(keyUserId.value)
    apiKeys.value = result.items || result || []
  } catch (e) { keyError.value = e.message }
}

async function createKey() {
  keyError.value = ''
  newKey.value = ''
  creatingKey.value = true
  try {
    const result = await api.createApiKey({ userId: keyUserId.value, merchantId: keyMerchantId.value || null, scope: keyScope.value })
    newKey.value = result.key
    await loadKeys()
  } catch (e) { keyError.value = e.message }
  finally { creatingKey.value = false }
}

async function revokeKey(id) {
  try {
    await api.revokeApiKey(id)
    await loadKeys()
  } catch (e) { keyError.value = e.message }
}

// Webhooks
const whUrl = ref('')
const whMerchantId = ref('')
const endpoints = ref([])
const whError = ref('')
const creatingWh = ref(false)
const expandedEp = ref(null)
const deliveries = ref([])

async function loadEndpoints() {
  whError.value = ''
  try {
    endpoints.value = await api.listWebhookEndpoints(whMerchantId.value || undefined)
  } catch (e) { whError.value = e.message }
}

async function createEndpoint() {
  whError.value = ''
  creatingWh.value = true
  try {
    await api.createWebhookEndpoint({ url: whUrl.value, merchantId: whMerchantId.value || null, events: null })
    whUrl.value = ''
    await loadEndpoints()
  } catch (e) { whError.value = e.message }
  finally { creatingWh.value = false }
}

async function disableEndpoint(id) {
  try {
    await api.updateWebhookEndpoint(id, { status: 'disabled' })
    await loadEndpoints()
  } catch (e) { whError.value = e.message }
}

async function enableEndpoint(id) {
  try {
    await api.updateWebhookEndpoint(id, { status: 'active' })
    await loadEndpoints()
  } catch (e) { whError.value = e.message }
}

async function toggleDeliveries(epId) {
  if (expandedEp.value === epId) { expandedEp.value = null; return }
  expandedEp.value = epId
  deliveries.value = []
  try {
    const result = await api.webhookDeliveries(epId)
    deliveries.value = result.items || result || []
  } catch (e) { whError.value = e.message }
}

function formatDate(iso) {
  return iso ? new Date(iso).toLocaleString() : '-'
}
</script>
