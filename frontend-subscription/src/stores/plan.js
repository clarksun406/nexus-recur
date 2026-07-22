import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '../api'
import { useAppStore } from './app'

export const usePlanStore = defineStore('plan', () => {
  const plans = ref([])
  const activePlans = computed(() => plans.value.filter((p) => p.status === 'active'))

  async function loadPlans() {
    const app = useAppStore()
    await app.withLoading(async () => {
      const data = await api.listPlans()
      plans.value = data.items
    })
  }

  async function createPlan(payload) {
    const app = useAppStore()
    await app.withLoading(async () => {
      await api.createPlan(payload)
      app.notify('计划已创建')
      await loadPlans()
    })
  }

  async function archivePlan(planId) {
    const app = useAppStore()
    await app.withLoading(async () => {
      await api.archivePlan(planId)
      app.notify('计划已下架')
      await loadPlans()
    })
  }

  return { plans, activePlans, loadPlans, createPlan, archivePlan }
})
