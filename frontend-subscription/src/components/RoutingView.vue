<template>
  <section class="workspace">
    <form class="panel form-grid" @submit.prevent="createRule">
      <label>
        提供商
        <input v-model="form.providerName" required placeholder="stripe / creem / adyen" />
      </label>
      <label>
        优先级
        <input v-model.number="form.priority" type="number" min="1" />
      </label>
      <label>
        币种
        <input v-model="form.currency" maxlength="3" placeholder="USD (留空=全部)" />
      </label>
      <label>
        策略
        <select v-model="form.strategy">
          <option value="priority">priority</option>
          <option value="weighted">weighted</option>
          <option value="cost_optimized">cost_optimized</option>
          <option value="success_rate">success_rate</option>
        </select>
      </label>
      <label>
        最小金额 (cents)
        <input v-model.number="form.minAmountCents" type="number" min="0" placeholder="0" />
      </label>
      <label>
        最大金额 (cents)
        <input v-model.number="form.maxAmountCents" type="number" min="0" placeholder="无限" />
      </label>
      <label>
        费率 %
        <input v-model.number="form.costPercentage" type="number" step="0.1" min="0" />
      </label>
      <label>
        成功率
        <input v-model.number="form.successRate" type="number" step="0.01" min="0" max="1" />
      </label>
      <button class="primary" type="submit" :disabled="app.loading">添加规则</button>
    </form>

    <div class="panel">
      <div class="panel-head">
        <h2>路由规则</h2>
        <button @click="loadRules">刷新</button>
      </div>
      <div class="table">
        <div class="row head">
          <span>提供商</span><span>优先级</span><span>币种</span><span>策略</span><span>费率</span><span>成功率</span><span>状态</span><span>操作</span>
        </div>
        <div v-for="rule in rules" :key="rule.id" class="row">
          <span><strong>{{ rule.providerName }}</strong></span>
          <span>{{ rule.priority }}</span>
          <span>{{ rule.currency || '全部' }}</span>
          <span>{{ rule.strategy }}</span>
          <span>{{ rule.costPercentage }}%</span>
          <span>{{ (rule.successRate * 100).toFixed(0) }}%</span>
          <span><mark :class="rule.active ? 'active' : 'archived'">{{ rule.active ? '启用' : '停用' }}</mark></span>
          <span>
            <button @click="toggleRule(rule)">{{ rule.active ? '停用' : '启用' }}</button>
            <button @click="removeRule(rule.id)">删除</button>
          </span>
        </div>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head"><h2>路由测试</h2></div>
      <form class="form-grid" @submit.prevent="testResolve">
        <label>
          币种
          <input v-model="resolveForm.currency" placeholder="USD" />
        </label>
        <label>
          金额 (cents)
          <input v-model.number="resolveForm.amountCents" type="number" />
        </label>
        <label>
          地区
          <input v-model="resolveForm.region" placeholder="US" />
        </label>
        <button class="primary" type="submit">测试路由</button>
      </form>
      <div v-if="resolveResult" class="notice">
        路由结果: <strong>{{ resolveResult.provider }}</strong> — {{ resolveResult.reason }}
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { api } from '../api'
import { useAppStore } from '../stores/app'

const app = useAppStore()
const rules = ref([])
const resolveResult = ref(null)

const form = reactive({
  providerName: '',
  priority: 1,
  currency: '',
  strategy: 'priority',
  minAmountCents: null,
  maxAmountCents: null,
  costPercentage: 2.9,
  successRate: 0.95
})

const resolveForm = reactive({ currency: 'USD', amountCents: 5000, region: '' })

async function loadRules() {
  app.loading = true
  try {
    rules.value = await api.listRoutingRules()
  } catch (e) { app.error = e.message } finally { app.loading = false }
}

async function createRule() {
  app.loading = true
  try {
    const payload = { ...form }
    if (!payload.currency) delete payload.currency
    if (!payload.minAmountCents) delete payload.minAmountCents
    if (!payload.maxAmountCents) delete payload.maxAmountCents
    await api.createRoutingRule(payload)
    app.message = '规则已添加'
    await loadRules()
  } catch (e) { app.error = e.message } finally { app.loading = false }
}

async function toggleRule(rule) {
  try {
    await api.updateRoutingRule(rule.id, { active: !rule.active })
    await loadRules()
  } catch (e) { app.error = e.message }
}

async function removeRule(id) {
  try {
    await api.deleteRoutingRule(id)
    await loadRules()
  } catch (e) { app.error = e.message }
}

async function testResolve() {
  try {
    resolveResult.value = await api.resolveRouting(resolveForm)
  } catch (e) { app.error = e.message }
}

onMounted(loadRules)
</script>
