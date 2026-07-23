<template>
  <section class="panel">
    <h2>Reconciliation</h2>
    <div class="row" style="margin-bottom:1rem;gap:.5rem;align-items:end">
      <div><label>Year</label><input v-model.number="year" type="number" style="width:5rem" /></div>
      <div><label>Month</label><input v-model.number="month" type="number" style="width:4rem" min="1" max="12" /></div>
      <div><label>Merchant ID</label><input v-model="merchantId" placeholder="(all)" /></div>
      <button class="btn" @click="load">Generate Report</button>
      <button class="btn" @click="exportCsv">Export CSV</button>
    </div>

    <p v-if="error" class="negative">{{ error }}</p>

    <div v-if="report">
      <div class="row" style="gap:1rem;margin-bottom:1rem;flex-wrap:wrap">
        <div class="panel" style="min-width:8rem;text-align:center">
          <div style="font-size:1.5rem;font-weight:bold">{{ report.totalInvoices }}</div>
          <div style="font-size:.8rem;color:#666">Invoices</div>
        </div>
        <div class="panel" style="min-width:8rem;text-align:center">
          <div style="font-size:1.5rem;font-weight:bold">{{ (report.grossAmountCents / 100).toFixed(2) }}</div>
          <div style="font-size:.8rem;color:#666">Gross</div>
        </div>
        <div class="panel" style="min-width:8rem;text-align:center">
          <div style="font-size:1.5rem;font-weight:bold">{{ (report.netAmountCents / 100).toFixed(2) }}</div>
          <div style="font-size:.8rem;color:#666">Net</div>
        </div>
        <div class="panel" style="min-width:8rem;text-align:center">
          <div style="font-size:1.5rem;font-weight:bold">{{ report.successRate }}%</div>
          <div style="font-size:.8rem;color:#666">Success Rate</div>
        </div>
      </div>

      <table v-if="report.byCurrency && Object.keys(report.byCurrency).length">
        <thead><tr><th>Currency</th><th>Gross</th><th>Net</th><th>Tax</th><th>Discount</th><th>Count</th></tr></thead>
        <tbody>
          <tr v-for="(v, cur) in report.byCurrency" :key="cur">
            <td>{{ cur }}</td>
            <td>{{ (v.grossCents / 100).toFixed(2) }}</td>
            <td>{{ (v.netCents / 100).toFixed(2) }}</td>
            <td>{{ (v.taxCents / 100).toFixed(2) }}</td>
            <td>{{ (v.discountCents / 100).toFixed(2) }}</td>
            <td>{{ v.count }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { api } from '../api'

const now = new Date()
const year = ref(now.getFullYear())
const month = ref(now.getMonth() + 1)
const merchantId = ref('')
const report = ref(null)
const error = ref('')

async function load() {
  error.value = ''
  report.value = null
  try {
    report.value = await api.reconciliationReport(year.value, month.value, merchantId.value || undefined)
  } catch (e) { error.value = e.message }
}

async function exportCsv() {
  error.value = ''
  try {
    await api.reconciliationExport(year.value, month.value, merchantId.value || undefined)
  } catch (e) { error.value = e.message }
}
</script>
