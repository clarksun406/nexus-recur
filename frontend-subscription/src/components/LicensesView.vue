<template>
  <section class="workspace">
    <form class="panel form-grid" @submit.prevent="generate">
      <label>
        商户 ID
        <input v-model="form.merchantId" required placeholder="mch_xxx" />
      </label>
      <label>
        计划 ID
        <input v-model="form.planId" required placeholder="plan_xxx" />
      </label>
      <label>
        订阅 ID
        <input v-model="form.subscriptionId" placeholder="sub_xxx (可选)" />
      </label>
      <label>
        最大激活数
        <input v-model.number="form.maxActivations" type="number" min="1" />
      </label>
      <label>
        有效天数
        <input v-model.number="form.expiryDays" type="number" min="1" placeholder="365" />
      </label>
      <button class="primary" type="submit" :disabled="app.loading">生成密钥</button>
    </form>

    <div class="panel">
      <div class="panel-head">
        <h2>License Keys</h2>
        <button @click="loadLicenses">刷新</button>
      </div>
      <div class="table">
        <div class="row head">
          <span>密钥</span><span>计划</span><span>激活</span><span>状态</span><span>到期</span><span>操作</span>
        </div>
        <div v-for="lic in licenses" :key="lic.id" class="row">
          <span><strong>{{ lic.licenseKey }}</strong></span>
          <span>{{ lic.planId }}</span>
          <span>{{ lic.currentActivations }} / {{ lic.maxActivations }}</span>
          <span><mark :class="lic.status">{{ statusText(lic.status) }}</mark></span>
          <span>{{ lic.expiresAt ? new Date(lic.expiresAt).toLocaleDateString() : '永久' }}</span>
          <span>
            <button v-if="lic.status === 'active'" @click="suspend(lic.id)">暂停</button>
            <button v-if="lic.status === 'suspended'" @click="reactivate(lic.id)">恢复</button>
            <button v-if="lic.status !== 'revoked'" @click="revoke(lic.id)">吊销</button>
          </span>
        </div>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head"><h2>校验密钥</h2></div>
      <form class="form-grid" @submit.prevent="validate">
        <label>
          密钥
          <input v-model="validateForm.licenseKey" required placeholder="XXXX-XXXX-XXXX-XXXX" />
        </label>
        <label>
          设备指纹
          <input v-model="validateForm.deviceFingerprint" required placeholder="device-001" />
        </label>
        <button class="primary" type="submit">校验</button>
      </form>
      <div v-if="validateResult" class="notice">
        校验通过 — 激活次数: {{ validateResult.currentActivations }}/{{ validateResult.maxActivations }}
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { api } from '../api'
import { useAppStore } from '../stores/app'

const app = useAppStore()
const licenses = ref([])
const validateResult = ref(null)

const form = reactive({
  merchantId: '',
  planId: '',
  subscriptionId: '',
  maxActivations: 5,
  expiryDays: 365
})

const validateForm = reactive({ licenseKey: '', deviceFingerprint: '' })

function statusText(s) {
  return { active: '活跃', suspended: '暂停', revoked: '已吊销', expired: '过期' }[s] || s
}

async function loadLicenses() {
  app.loading = true
  try {
    const res = await api.listLicenses()
    licenses.value = res.items || res.content || res
  } catch (e) { app.error = e.message } finally { app.loading = false }
}

async function generate() {
  app.loading = true
  try {
    const payload = { ...form }
    if (!payload.subscriptionId) delete payload.subscriptionId
    if (!payload.expiryDays) delete payload.expiryDays
    await api.generateLicense(payload)
    app.message = '密钥已生成'
    await loadLicenses()
  } catch (e) { app.error = e.message } finally { app.loading = false }
}

async function validate() {
  try {
    validateResult.value = await api.validateLicense(validateForm)
  } catch (e) { app.error = e.message }
}

async function suspend(id) {
  try { await api.suspendLicense(id); await loadLicenses() } catch (e) { app.error = e.message }
}
async function reactivate(id) {
  try { await api.reactivateLicense(id); await loadLicenses() } catch (e) { app.error = e.message }
}
async function revoke(id) {
  try { await api.revokeLicense(id); await loadLicenses() } catch (e) { app.error = e.message }
}

onMounted(loadLicenses)
</script>
