<script setup lang="ts">
import { ref, computed, watch } from 'vue'

const props = defineProps<{
  headers: string[]
  data: any[][]
  title?: string
  searchable?: boolean
  statusColumn?: number
  onRowClick?: (row: any[]) => void
  loading?: boolean
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

const currentPage = ref(1)
const pageSize = 10

const totalPages = computed(() => Math.ceil(filteredData.value.length / pageSize))

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredData.value.slice(start, start + pageSize)
})

function nextPage() {
  if (currentPage.value < totalPages.value) currentPage.value++
}

function prevPage() {
  if (currentPage.value > 1) currentPage.value--
}

watch([searchQuery, () => props.data], () => {
  currentPage.value = 1
}, { deep: true })
</script>

<template>
  <div class="table-component-container">
    <!-- Top Header with Search and Pagination -->
    <div class="table-header-external">
      <div class="header-left-side" v-if="searchable">
        <div class="search-box">
          <span class="material-symbols-outlined search-icon">search</span>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Buscar..."
            class="search-input"
          />
        </div>
      </div>
      <div v-else></div> <!-- Spacer if no search -->

      <div class="header-right-side">
        <!-- Compact Pagination Boxed -->
        <div class="compact-pagination" v-if="totalPages > 1">
          <span class="page-numbers">{{ currentPage }}-{{ totalPages }}</span>
          <div class="pagination-arrows">
            <button @click="prevPage" :disabled="currentPage === 1" class="arrow-btn">
              <span class="material-symbols-outlined">chevron_left</span>
            </button>
            <button @click="nextPage" :disabled="currentPage === totalPages" class="arrow-btn">
              <span class="material-symbols-outlined">chevron_right</span>
            </button>
          </div>
        </div>
        <div class="result-count" v-else-if="filteredData.length > 0">
          <span>{{ filteredData.length }}</span> resultado{{ filteredData.length !== 1 ? 's' : '' }}
        </div>
      </div>
    </div>
 
    <div class="table-wrapper">
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
        <tbody v-if="!loading">
          <tr
            v-for="(row, rowIndex) in paginatedData"
            :key="rowIndex"
            class="table-row"
            :class="{ clickable: !!onRowClick }"
            @click="onRowClick?.(row)"
            :style="{ animationDelay: `${rowIndex * 0.05}s` }"
          >
            <td v-for="(cell, cIdx) in row" :key="cIdx">
              <!-- UserCell component -->
              <div v-if="typeof cell === 'object' && cell?.component === 'UserCell'" class="cell-user">
                <div class="avatar-mini">{{ cell.props.inicial || '?' }}</div>
                <span class="user-name-cell">{{ cell.props.nombre }}</span>
              </div>

              <!-- Badge component -->
              <span v-else-if="typeof cell === 'object' && cell?.component === 'Badge'" 
                class="status-badge" 
                :class="cell.props.class"
              >
                {{ cell.props.text }}
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

              <!-- ProgressBar component -->
              <div v-else-if="typeof cell === 'object' && cell?.component === 'ProgressBar'" class="cell-progress-container">
                <div class="mini-progress-bar">
                  <div class="mini-progress-fill" :style="{ width: Math.min(cell.props.value, 100) + '%', backgroundColor: cell.props.color }"></div>
                </div>
                <span class="progress-percentage">{{ Math.round(cell.props.value) }}%</span>
              </div>

              <!-- ID column styling (ONLY if it's not an object and it's column 0) -->
              <span v-else-if="cIdx === 0" class="cell-id">#{{ cell }}</span>

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

              <!-- UserCell component -->
              <div v-else-if="typeof cell === 'object' && cell?.component === 'UserCell'" class="cell-user">
                <div class="avatar-mini">{{ cell.props.inicial }}</div>
                <span class="user-name-cell">{{ cell.props.nombre }}</span>
              </div>

              <!-- Badge component -->
              <span v-else-if="typeof cell === 'object' && cell?.component === 'Badge'" 
                class="status-badge" 
                :class="cell.props.class"
              >
                {{ cell.props.text }}
              </span>

              <!-- ProgressBar component -->
              <div v-else-if="typeof cell === 'object' && cell?.component === 'ProgressBar'" class="cell-progress-container">
                <div class="mini-progress-bar">
                  <div class="mini-progress-fill" :style="{ width: Math.min(cell.props.value, 100) + '%', backgroundColor: cell.props.color }"></div>
                </div>
                <span class="progress-percentage">{{ Math.round(cell.props.value) }}%</span>
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

        <!-- Loading State -->
        <tbody v-else>
          <tr>
            <td :colspan="headers.length" class="table-loading-cell">
              <div class="loading-spinner-container">
                <div class="spinner-modern"></div>
                <p>Sincronizando datos...</p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    </div>
  </div>
</template>

<style scoped>
.table-wrapper {
  background: white;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
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

.table-component-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.table-header-external {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}


.search-box {
  display: flex;
  align-items: center;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 0 12px;
  transition: all 0.2s ease;
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
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
  background: #f8fafc;
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.result-count span {
  font-weight: 700;
  color: #0f172a;
}

/* ─── Compact Pagination ──────────────────────── */
.compact-pagination {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  border: 1px solid #e2e8f0;
  padding: 4px 4px 4px 12px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.page-numbers {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: 0.05em;
  font-variant-numeric: tabular-nums;
}

.pagination-arrows {
  display: flex;
  gap: 2px;
}

.arrow-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: none;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s;
}

.arrow-btn:hover:not(:disabled) {
  background: #f1f5f9;
  color: #0f172a;
}

.arrow-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.arrow-btn .material-symbols-outlined {
  font-size: 18px;
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
  background: #333333;
  border-bottom: 2px solid #0f172a;
}

th {
  padding: 14px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 700;
  color: #f8fafc;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  user-select: none;
  white-space: nowrap;
  border-bottom: none;
}

th.sortable {
  cursor: pointer;
  transition: color 0.2s ease;
}

th.sortable:hover {
  color: #38bdf8;
}

.th-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

.sort-icon {
  font-size: 14px;
  color: #38bdf8;
  transition: transform 0.2s ease;
}

.sort-icon--idle {
  color: #64748b;
  font-size: 12px;
}

.table-row {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: rowSlideIn 0.5s ease-out both;
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

.table-row:nth-child(even) {
  background-color: #f8fafc;
}

.table-row:hover {
  background-color: #ffffff;
  transform: scale(1.008) translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 5;
}

.table-row.clickable {
  cursor: pointer;
}

.table-row.clickable:hover {
  background-color: #ffffff;
}

td {
  padding: 14px 16px;
  font-size: 13px;
  color: #334155;
  white-space: nowrap;
  border-bottom: 1px solid #e2e8f0;
}

/* ─── Cell Variants ───────────────────────────── */
.cell-id {
  font-weight: 700;
  font-size: 13px;
  color: #6366f1;
  background: #eef2ff;
  padding: 4px 10px;
  border-radius: 4px;
  font-variant-numeric: tabular-nums;
}

.cell-price {
  font-weight: 600;
  color: #1f2937;
  font-variant-numeric: tabular-nums;
}

/* ─── UserCell ─────────────────────────────── */
.cell-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-mini {
  width: 32px;
  height: 32px;
  background: #f1f5f9;
  color: #475569;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 12px;
  border: 1px solid #e2e8f0;
}

.user-name-cell {
  font-weight: 600;
  color: #1e293b;
}

/* ─── Cell Progress ───────────────────────────── */
.cell-progress-container {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 140px;
}

.mini-progress-bar {
  flex: 1;
  height: 6px;
  background: #f1f5f9;
  border-radius: 3px;
  overflow: hidden;
}

.mini-progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 1s ease-out;
}

.progress-percentage {
  font-size: 11px;
  font-weight: 700;
  color: #64748b;
  width: 32px;
}

/* ─── Actions Cell ────────────────────────────── */
.cell-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  border: none;
  background: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #94a3b8;
}

.action-btn:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.btn-edit:hover {
  background: #eff6ff !important;
  color: #2563eb !important;
}

.btn-delete:hover {
  background: #fef2f2 !important;
  color: #ef4444 !important;
}

.btn-view:hover, .btn-products:hover {
  background: #eef2ff !important;
  color: #4f46e5 !important;
}

.action-btn .material-symbols-outlined {
  font-size: 20px;
}

/* ─── Status Badge ────────────────────────────── */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.01em;
  transition: opacity 0.2s ease;
}

.status-badge:hover {
  opacity: 0.85;
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

/* ─── Pagination ──────────────────────────────── */
.pagination-controls {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; border-top: 1px solid #e2e8f0; background: #f8fafc;
}
.page-info { font-size: 13px; font-weight: 600; color: #475569; }
.page-btn { padding: 6px 12px; border-radius: 4px; border: 1px solid #cbd5e1; background: white; font-size: 13px; font-weight: 600; color: #334155; cursor: pointer; transition: all 0.2s; }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.page-btn:not(:disabled):hover { background: #f1f5f9; border-color: #94a3b8; }

/* ── Loading Spinner ── */
.table-loading-cell {
  padding: 80px 0 !important;
}

.loading-spinner-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.spinner-modern {
  width: 40px;
  height: 40px;
  border: 3px solid #f1f5f9;
  border-top: 3px solid #ef4444;
  border-radius: 50%;
  animation: spinTable 0.8s linear infinite;
}

@keyframes spinTable {
  to { transform: rotate(360deg); }
}

.loading-spinner-container p {
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
}
</style>
