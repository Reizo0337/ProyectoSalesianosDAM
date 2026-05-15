<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{
  headers: string[]
  data: any[][]
  title?: string
  searchable?: boolean
  statusColumn?: number
  onRowClick?: (row: any[]) => void
}>()

const searchQuery = ref('')
const sortColumn = ref<number | null>(null)
const sortDirection = ref<'asc' | 'desc'>('asc')

const statusStyles: Record<string, { bg: string; color: string; icon: string }> = {
  'aprobado':   { bg: '#dcfce7', color: '#16a34a', icon: 'check_circle' },
  'pendiente':  { bg: '#fef9c3', color: '#ca8a04', icon: 'schedule' },
  'rechazado':  { bg: '#fee2e2', color: '#dc2626', icon: 'cancel' },
  'en proceso': { bg: '#dbeafe', color: '#2563eb', icon: 'autorenew' },
  'cerrada':    { bg: '#fee2e2', color: '#dc2626', icon: 'lock' },
}

function getStatusStyle(value: string) {
  const key = String(value).toLowerCase().trim()
  return statusStyles[key] || { bg: '#f3f4f6', color: '#6b7280', icon: 'info' }
}

function isStatus(colIndex: number) {
  return props.statusColumn !== undefined && props.statusColumn === colIndex
}

function handleSort(colIndex: number) {
  if (sortColumn.value === colIndex) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortColumn.value = colIndex
    sortDirection.value = 'asc'
  }
}

const filteredData = computed(() => {
  let rows = [...props.data]

  // Search filter
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    rows = rows.filter(row =>
      row.some(cell => {
        if (typeof cell === 'object') return false;
        return String(cell).toLowerCase().includes(q);
      })
    )
  }

  // Sort
  if (sortColumn.value !== null) {
    const col = sortColumn.value
    const dir = sortDirection.value === 'asc' ? 1 : -1
    rows.sort((a, b) => {
      const aVal = a[col]
      const bVal = b[col]
      if (typeof aVal === 'number' && typeof bVal === 'number') return (aVal - bVal) * dir
      return String(aVal).localeCompare(String(bVal)) * dir
    })
  }

  return rows
})
</script>

<template>
  <div class="table-wrapper">
    <!-- Search bar -->
    <div class="table-toolbar" v-if="searchable">
      <div class="search-box">
        <span class="material-symbols-outlined search-icon">search</span>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Buscar en la tabla..."
          class="search-input"
        />
      </div>
      <div class="result-count">
        <span>{{ filteredData.length }}</span> resultado{{ filteredData.length !== 1 ? 's' : '' }}
      </div>
    </div>

    <!-- Table -->
    <div class="table-scroll">
      <table>
        <thead>
          <tr>
            <th
              v-for="(header, i) in headers"
              :key="header"
              @click="handleSort(i)"
              class="sortable"
            >
              <div class="th-content">
                <span>{{ header }}</span>
                <span class="sort-icon" v-if="sortColumn === i">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
                <span class="sort-icon sort-icon--idle" v-else>↕</span>
              </div>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(row, rIdx) in filteredData"
            :key="rIdx"
            class="table-row"
            :class="{ clickable: !!onRowClick }"
            @click="onRowClick?.(row)"
            :style="{ animationDelay: `${rIdx * 0.04}s` }"
          >
            <td v-for="(cell, cIdx) in row" :key="cIdx">
              <!-- ID column styling -->
              <span v-if="cIdx === 0" class="cell-id">#{{ cell }}</span>

              <!-- Status badge -->
              <span
                v-else-if="isStatus(cIdx)"
                class="status-badge"
                :style="{
                  backgroundColor: getStatusStyle(cell).bg,
                  color: getStatusStyle(cell).color,
                }"
              >
                <span class="material-symbols-outlined status-icon">
                  {{ getStatusStyle(cell).icon }}
                </span>
                {{ cell }}
              </span>

              <!-- Price column -->
              <span v-else-if="typeof cell === 'string' && cell.includes('€')" class="cell-price">
                {{ cell }}
              </span>

              <!-- Actions column -->
              <div v-else-if="typeof cell === 'object' && cell?.type === 'actions'" class="cell-actions">
                <button 
                  v-for="(action, aIdx) in cell.actions" 
                  :key="aIdx"
                  @click.stop="action.onClick"
                  class="action-btn"
                  :class="action.class"
                  :title="action.label"
                >
                  <span class="material-symbols-outlined">{{ action.icon }}</span>
                </button>
              </div>

              <!-- Default cell -->
              <span v-else>{{ cell }}</span>
            </td>
          </tr>

          <!-- Empty state -->
          <tr v-if="filteredData.length === 0">
            <td :colspan="headers.length" class="empty-state">
              <span class="material-symbols-outlined empty-icon">search_off</span>
              <p>No se encontraron resultados</p>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
/* ─── Wrapper ─────────────────────────────────── */
.table-wrapper {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 4px 12px rgba(0, 0, 0, 0.03);
  animation: fadeInUp 0.5s ease both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ─── Toolbar ─────────────────────────────────── */
.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
  gap: 12px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 0 12px;
  transition: all 0.25s ease;
  flex: 1;
  max-width: 360px;
}

.search-box:focus-within {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
  background: #fff;
}

.search-icon {
  font-size: 20px;
  color: #9ca3af;
  margin-right: 8px;
}

.search-input {
  border: none;
  outline: none;
  background: transparent;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: #1f2937;
  padding: 10px 0;
  width: 100%;
}

.search-input::placeholder {
  color: #9ca3af;
}

.result-count {
  font-size: 13px;
  color: #9ca3af;
  white-space: nowrap;
}

.result-count span {
  font-weight: 600;
  color: #6b7280;
}

/* ─── Table Scroll ────────────────────────────── */
.table-scroll {
  overflow-x: auto;
}

/* ─── Table ───────────────────────────────────── */
table {
  width: 100%;
  border-collapse: collapse;
  border-spacing: 0;
}

/* ─── Header ──────────────────────────────────── */
thead {
  background: linear-gradient(180deg, #f9fafb 0%, #f3f4f6 100%);
}

th {
  padding: 14px 20px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  user-select: none;
  white-space: nowrap;
  border-bottom: 1px solid #e5e7eb;
}

th.sortable {
  cursor: pointer;
  transition: color 0.2s ease;
}

th.sortable:hover {
  color: #4f46e5;
}

.th-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

.sort-icon {
  font-size: 14px;
  color: #4f46e5;
  transition: transform 0.2s ease;
}

.sort-icon--idle {
  color: #d1d5db;
  font-size: 12px;
}

/* ─── Body Rows ───────────────────────────────── */
.table-row {
  animation: rowSlideIn 0.35s ease both;
  transition: background-color 0.18s ease;
}

@keyframes rowSlideIn {
  from {
    opacity: 0;
    transform: translateX(-8px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.table-row:hover {
  background-color: #f8fafc;
}

.table-row.clickable {
  cursor: pointer;
}

.table-row.clickable:hover {
  background-color: #f1f5f9;
}

td {
  padding: 16px 20px;
  font-size: 14px;
  color: #374151;
  white-space: nowrap;
}

/* ─── Cell Variants ───────────────────────────── */
.cell-id {
  font-weight: 700;
  font-size: 13px;
  color: #6366f1;
  background: #eef2ff;
  padding: 4px 10px;
  border-radius: 6px;
  font-variant-numeric: tabular-nums;
}

.cell-price {
  font-weight: 600;
  color: #1f2937;
  font-variant-numeric: tabular-nums;
}

/* ─── Actions Cell ────────────────────────────── */
.cell-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1.5px solid #f3f4f6;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
}

.action-btn:hover {
  background: #f9fafb;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.action-btn .material-symbols-outlined {
  font-size: 18px;
}

/* ─── Status Badge ────────────────────────────── */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.01em;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.status-badge:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.status-icon {
  font-size: 16px;
}

/* ─── Empty State ─────────────────────────────── */
.empty-state {
  text-align: center;
  padding: 48px 20px !important;
  color: #9ca3af;
}

.empty-icon {
  font-size: 40px;
  color: #d1d5db;
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 14px;
  margin-top: 4px;
}
</style>
