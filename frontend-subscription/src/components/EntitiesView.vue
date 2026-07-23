<template>
  <section class="workspace">
    <form class="panel form-grid" @submit.prevent="createEntity">
      <label>
        主体名称
        <input v-model="form.name" required placeholder="Nexus US Inc" />
      </label>
      <label>
        国家
        <input v-model="form.country" maxlength="2" placeholder="US" />
      </label>
      <label>
        税号
        <input v-model="form.taxId" placeholder="12-3456789" />
      </label>
      <label>
        注册号
        <input v-model="form.registrationNumber" placeholder="REG-001" />
      </label>
      <label>
        基础币种
        <input v-model="form.baseCurrency" maxlength="3" placeholder="USD" />
      </label>
      <label>
        联系人
        <input v-model="form.primaryContact" placeholder="John" />
      </label>
      <label>
        联系邮箱
        <input v-model="form.primaryEmail" type="email" placeholder="john@nexus.io" />
      </label>
      <button class="primary" type="submit" :disabled="app.loading">创建主体</button>
    </form>

    <div class="panel">
      <div class="panel-head">
        <h2>法律主体</h2>
        <div>
          <input v-model="countryFilter" placeholder="按国家筛选" style="margin-right:8px" />
          <button @click="loadEntities">刷新</button>
        </div>
      </div>
      <div class="table">
        <div class="row head">
          <span>名称</span><span>国家</span><span>税号</span><span>币种</span><span>状态</span><span>操作</span>
        </div>
        <div v-for="entity in entities" :key="entity.id" class="row">
          <span><strong>{{ entity.name }}</strong><small>{{ entity.id }}</small></span>
          <span>{{ entity.country }}</span>
          <span>{{ entity.taxId || '-' }}</span>
          <span>{{ entity.baseCurrency }}</span>
          <span><mark :class="entity.status">{{ statusText(entity.status) }}</mark></span>
          <span>
            <button v-if="entity.status === 'active'" @click="suspend(entity.id)">暂停</button>
            <button v-if="entity.status === 'suspended'" @click="activate(entity.id)">激活</button>
          </span>
        </div>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head"><h2>分配商户</h2></div>
      <form class="form-grid" @submit.prevent="assignMerchant">
        <label>
          主体 ID
          <input v-model="assignForm.entityId" required placeholder="ent_xxx" />
        </label>
        <label>
          商户 ID
          <input v-model="assignForm.merchantId" required placeholder="mch_xxx" />
        </label>
        <button class="primary" type="submit">分配</button>
      </form>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { api } from '../api'
import { useAppStore } from '../stores/app'

const app = useAppStore()
const entities = ref([])
const countryFilter = ref('')

const form = reactive({
  name: '',
  country: 'US',
  taxId: '',
  registrationNumber: '',
  baseCurrency: 'USD',
  primaryContact: '',
  primaryEmail: ''
})

const assignForm = reactive({ entityId: '', merchantId: '' })

function statusText(s) {
  return { active: '活跃', suspended: '暂停', dissolved: '已注销' }[s] || s
}

async function loadEntities() {
  app.loading = true
  try {
    entities.value = await api.listLegalEntities(countryFilter.value)
  } catch (e) { app.error = e.message } finally { app.loading = false }
}

async function createEntity() {
  app.loading = true
  try {
    await api.createLegalEntity(form)
    app.message = '主体已创建'
    await loadEntities()
  } catch (e) { app.error = e.message } finally { app.loading = false }
}

async function suspend(id) {
  try { await api.updateLegalEntity(id, { status: 'suspended' }); await loadEntities() } catch (e) { app.error = e.message }
}
async function activate(id) {
  try { await api.updateLegalEntity(id, { status: 'active' }); await loadEntities() } catch (e) { app.error = e.message }
}

async function assignMerchant() {
  try {
    await api.assignMerchantToEntity(assignForm.entityId, assignForm.merchantId)
    app.message = '商户已分配'
  } catch (e) { app.error = e.message }
}

onMounted(loadEntities)
</script>
