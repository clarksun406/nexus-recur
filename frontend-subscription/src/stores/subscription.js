import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { api } from '../api'
import { useAppStore } from './app'

export const useSubscriptionStore = defineStore('subscription', () => {
  const subscriptions = ref([])
  const filters = reactive({ userId: '', status: '' })

  async function loadSubscriptions() {
    const app = useAppStore()
    await app.withLoading(async () => {
      const data = await api.listSubscriptions(filters)
      subscriptions.value = data.items
    })
  }

  async function createSubscription(payload) {
    const app = useAppStore()
    await app.withLoading(async () => {
      const data = await api.createSubscription(payload)
      app.notify(`订阅已创建，checkout: ${data.checkoutUrl}`)
      await loadSubscriptions()
    })
  }

  async function pause(id) {
    const app = useAppStore()
    await app.withLoading(async () => {
      await api.pauseSubscription(id)
      await loadSubscriptions()
    })
  }

  async function resume(id) {
    const app = useAppStore()
    await app.withLoading(async () => {
      await api.resumeSubscription(id)
      await loadSubscriptions()
    })
  }

  async function cancel(id, immediate) {
    const app = useAppStore()
    await app.withLoading(async () => {
      await api.cancelSubscription(id, immediate)
      await loadSubscriptions()
    })
  }

  return { subscriptions, filters, loadSubscriptions, createSubscription, pause, resume, cancel }
})
