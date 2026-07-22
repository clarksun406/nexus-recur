import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '../api'
import { useAppStore } from './app'

export const useDashboardStore = defineStore('dashboard', () => {
  const stats = ref(null)

  async function loadStats() {
    const app = useAppStore()
    await app.withLoading(async () => {
      stats.value = await api.dashboardStats()
    })
  }

  return { stats, loadStats }
})
