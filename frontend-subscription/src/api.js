const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    },
    ...options
  })
  const body = await response.json().catch(() => null)
  if (!response.ok || body?.success === false) {
    throw new Error(body?.message || `HTTP ${response.status}`)
  }
  return body.data
}

export const api = {
  listPlans(status = '') {
    const query = status ? `?status=${encodeURIComponent(status)}` : ''
    return request(`/v1/plans${query}`)
  },
  createPlan(payload) {
    return request('/v1/plans', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  archivePlan(planId) {
    return request(`/v1/plans/${planId}/archive`, { method: 'POST' })
  },
  listSubscriptions(filters = {}) {
    const params = new URLSearchParams()
    if (filters.userId) params.set('userId', filters.userId)
    if (filters.status) params.set('status', filters.status)
    const query = params.toString() ? `?${params.toString()}` : ''
    return request(`/v1/subscriptions${query}`)
  },
  createSubscription(payload) {
    return request('/v1/subscriptions', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  cancelSubscription(subscriptionId, immediate = false) {
    return request(`/v1/subscriptions/${subscriptionId}/cancel`, {
      method: 'POST',
      body: JSON.stringify({ immediate, reason: immediate ? 'manual immediate cancel' : 'manual scheduled cancel' })
    })
  },
  pauseSubscription(subscriptionId) {
    return request(`/v1/subscriptions/${subscriptionId}/pause`, {
      method: 'POST',
      body: JSON.stringify({ reason: 'manual pause', maxPauseDays: 90 })
    })
  },
  resumeSubscription(subscriptionId) {
    return request(`/v1/subscriptions/${subscriptionId}/resume`, { method: 'POST' })
  },
  getSubscription(subscriptionId) {
    return request(`/v1/subscriptions/${subscriptionId}`)
  },
  subscriptionInvoices(subscriptionId, page = 1, limit = 20) {
    return request(`/v1/subscriptions/${subscriptionId}/invoices?page=${page}&limit=${limit}`)
  },
  subscriptionEvents(subscriptionId, page = 1, limit = 20) {
    return request(`/v1/subscriptions/${subscriptionId}/events?page=${page}&limit=${limit}`)
  },
  checkEntitlement(userId) {
    return request(`/v1/entitlements/check?userId=${encodeURIComponent(userId)}`)
  },
  dashboardStats() {
    return request('/v1/dashboard/stats')
  },
  listWallets(merchantId) {
    return request(`/v1/wallets?merchantId=${encodeURIComponent(merchantId)}`)
  },
  walletTransactions(walletId, page = 1, limit = 20) {
    return request(`/v1/wallets/${walletId}/transactions?page=${page}&limit=${limit}`)
  }
}
