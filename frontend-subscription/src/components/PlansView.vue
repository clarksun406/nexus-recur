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
          </span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useAppStore } from '../stores/app'
import { usePlanStore } from '../stores/plan'

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

async function createPlan() {
  const features = JSON.parse(featuresJson.value || '{}')
  await planStore.createPlan({ ...planForm, features, status: 'active' })
}

onMounted(() => {
  planStore.loadPlans()
})
</script>
