import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const loading = ref(false)
  const error = ref('')
  const message = ref('')
  const health = ref('idle')

  async function withLoading(task) {
    loading.value = true
    error.value = ''
    message.value = ''
    try {
      await task()
      health.value = 'connected'
    } catch (err) {
      error.value = err.message
      health.value = 'error'
    } finally {
      loading.value = false
    }
  }

  function notify(msg) {
    message.value = msg
  }

  return { loading, error, message, health, withLoading, notify }
})
