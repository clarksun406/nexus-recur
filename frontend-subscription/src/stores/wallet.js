import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { api } from '../api'
import { useAppStore } from './app'

export const useWalletStore = defineStore('wallet', () => {
  const wallets = ref([])
  const transactions = ref([])
  const merchantId = ref('merchant_default')
  const selectedWalletId = ref('')

  async function loadWallets() {
    const app = useAppStore()
    await app.withLoading(async () => {
      const data = await api.listWallets(merchantId.value)
      wallets.value = data || []
    })
  }

  async function loadTransactions() {
    if (!selectedWalletId.value) return
    const app = useAppStore()
    await app.withLoading(async () => {
      const data = await api.walletTransactions(selectedWalletId.value)
      transactions.value = data.items
    })
  }

  return { wallets, transactions, merchantId, selectedWalletId, loadWallets, loadTransactions }
})
