<template>
  <section class="workspace">
    <form class="panel form-grid" @submit.prevent="createPlan">
      <label>
        计划名称
        <input v-model="planForm.name" required placeholder="Pro Plan" />
      </label>
      <label>
        周期
        <select v-model="planForm.billingCycle">
          <option value="monthly">Monthly</option>
          <option value="quarterly">Quarterly</option>
          <option value="6month">6-month</option>
          <option value="annual">Annual</option>
        </select>
      </label>
      <label>
        价格
        <input v-model.number="planForm.price" required min="0" step="0.01" type="number" />
      </label>
      <label>
        币种
        <input v-model="planForm.currency" maxlength="3" />
      </label>
      <label>
        试用天数
        <input v-model.number="planForm.trialDays" min="0" type="number" />
      </label>
      <label>
        产品 ID
        <input v-model="planForm.productId" placeholder="Creem/Stripe product id" />
      </label>
      <label class="wide">
        描述
        <textarea v-model="planForm.description" rows="2"></textarea>
      </label>
      <label class="wide">
        权益 JSON
        <textarea v-model="featuresJson" rows="3"></textarea>
      </label>
      <button class="primary" type="submit" :disabled="app.loading">创建计划</button>
    </form>

    <div class="panel">
      <div class="panel-head">
        <h2>计划列表</h2>
        <button @click="planStore.loadPlans()">刷新</button>
      </div>
      <div class="table">
        <div class="row head">
          <span>名称</span><span>周期</span><span>价格</span><span>状态</span><span>操作</span>
        </div>
        <div v-for="plan in planStore.plans" :key="plan.id" class="row">
          <span>
            <strong>{{ plan.name }}</strong>
            <small>{{ plan.id }}</small>
          </span>
          <span>{{ plan.billingCycle }}</span>
          <span>{{ plan.price }} {{ plan.currency }}</span>
          <span><mark :class="plan.status">{{ plan.status }}</mark></span>
          <span>
            <button :disabled="plan.status === 'archived'" @click="planStore.archivePlan(plan.id)">下架</button>
            <button @click="selectPlan(plan)">阶梯</button>
          </span>
        </div>
      </div>
    </div>

    <div v-if="selectedPlan" class="panel">
      <div class="panel-head">
        <h2>阶梯定价 — {{ selectedPlan.name }}</h2>
        <button @click="selectedPlan = null">关闭</button>
      </div>
      <div class="row" style="gap:.5rem;margin-bottom:1rem;align-items:end;flex-wrap:wrap">
        <div><label>起始量</label><input v-model.number="tierForm.tierStart" type="number" min="0" style="width:5rem" /></div>
        <div><label>结束量</label><input v-model.number="tierForm.tierEnd" type="number" min="0" placeholder="∞" style="width:5rem" /></div>
        <div><label>单价(分)</label><input v-model.number="tierForm.unitAmountCents" type="number" min="0" style="width:5rem" /></div>
        <div><label>固定费(分)</label><input v-model.number="tierForm.flatAmountCents" type="number" min="0" style="width:5rem" /></div>
        <button class="btn" @click="addTier">添加阶梯</button>
      </div>
      <p v-if="tierError" class="negative">{{ tierError }}</p>
      <table v-if="tiers.length">
        <thead><tr><th>起始</th><th>结束</th><th>单价(分)</th><th>固定费(分)</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="t in tiers" :key="t.id">
            <td>{{ t.tierStart }}</td>
            <td>{{ t.tierEnd ?? '∞' }}</td>
            <td>{{ t.unitAmountCents }}</td>
            <td>{{ t.flatAmountCents }}</td>
            <td><button class="btn-sm" @click="deleteTier(t.id)">删除</button></td>
          </tr>
        </tbody>
      </table>
      <p v-if="!tiers.length && !tierError" class="row empty">无阶梯配置。</p>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useAppStore } from '../stores/app'
import { usePlanStore } from '../stores/plan'
import { api } from '../api'

const app = useAppStore()
const planStore = usePlanStore()

const planForm = reactive({
  name: '',
  description: '',
  productId: '',
  billingCycle: 'monthly',
  price: 29.9,
  currency: 'USD',
  trialDays: 0
})
const featuresJson = ref('{"max_api_calls":10000,"storage_gb":50,"premium_support":true}')

const selectedPlan = ref(null)
const tiers = ref([])
const tierError = ref('')
const tierForm = reactive({ tierStart: 0, tierEnd: null, unitAmountCents: 0, flatAmountCents: 0 })

async function createPlan() {
  const features = JSON.parse(featuresJson.value || '{}')
  await planStore.createPlan({ ...planForm, features, status: 'active' })
}

async function selectPlan(plan) {
  selectedPlan.value = plan
  tierError.value = ''
  await loadTiers()
}

async function loadTiers() {
  try {
    tiers.value = await api.listPlanTiers(selectedPlan.value.id)
  } catch (e) { tierError.value = e.message }
}

async function addTier() {
  tierError.value = ''
  try {
    await api.createPlanTier(selectedPlan.value.id, {
      planId: selectedPlan.value.id,
      tierStart: tierForm.tierStart,
      tierEnd: tierForm.tierEnd || null,
      unitAmountCents: tierForm.unitAmountCents,
      flatAmountCents: tierForm.flatAmountCents
    })
    await loadTiers()
  } catch (e) { tierError.value = e.message }
}

async function deleteTier(tierId) {
  tierError.value = ''
  try {
    await api.deletePlanTier(selectedPlan.value.id, tierId)
    await loadTiers()
  } catch (e) { tierError.value = e.message }
}

onMounted(() => {
  planStore.loadPlans()
})
</script>
