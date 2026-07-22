<template>
  <section class="workspace single">
    <form class="panel form-grid" @submit.prevent="checkEntitlement">
      <label>
        用户 ID
        <input v-model="userId" required placeholder="user_xxx" />
      </label>
      <button class="primary" type="submit" :disabled="app.loading">校验权益</button>
    </form>
    <pre v-if="entitlement" class="panel result">{{ JSON.stringify(entitlement, null, 2) }}</pre>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { api } from '../api'
import { useAppStore } from '../stores/app'

const app = useAppStore()
const userId = ref('')
const entitlement = ref(null)

async function checkEntitlement() {
  await app.withLoading(async () => {
    entitlement.value = await api.checkEntitlement(userId.value)
  })
}
</script>
