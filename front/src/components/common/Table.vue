<script setup lang="ts">
import { computed, ref } from 'vue';

const props = defineProps<{
  headers: string[];
  data: any[][];
  loading?: boolean;
  onRowClick?: (row: any[]) => void;
  itemsPerPage?: number;
}>();

const currentPage = ref(1);
const perPage = ref(props.itemsPerPage || 10);
const sortColumn = ref<number | null>(null);
const sortDirection = ref<'asc' | 'desc'>('asc');

// Lógica de Ordenación
const sortedData = computed(() => {
  if (sortColumn.value === null) return props.data;

  return [...props.data].sort((a, b) => {
    let valA = a[sortColumn.value!];
    let valB = b[sortColumn.value!];

    // Extraer texto si es un objeto (Badge, UserCell, etc)
    if (typeof valA === 'object' && valA?.props?.text) valA = valA.props.text;
    if (typeof valA === 'object' && valA?.props?.nombre) valA = valA.props.nombre;
    if (typeof valB === 'object' && valB?.props?.text) valB = valB.props.text;
    if (typeof valB === 'object' && valB?.props?.nombre) valB = valB.props.nombre;

    // Limpiar símbolos de moneda para ordenar números correctamente
    if (typeof valA === 'string' && valA.includes('€')) valA = parseFloat(valA.replace(/[.€]/g, '').replace(',', '.'));
    if (typeof valB === 'string' && valB.includes('€')) valB = parseFloat(valB.replace(/[.€]/g, '').replace(',', '.'));

    if (valA < valB) return sortDirection.value === 'asc' ? -1 : 1;
    if (valA > valB) return sortDirection.value === 'asc' ? 1 : -1;
    return 0;
  });
});

const totalPages = computed(() => Math.ceil(sortedData.value.length / perPage.value));

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return sortedData.value.slice(start, start + perPage.value);
});

function handleSort(index: number) {
  if (sortColumn.value === index) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortColumn.value = index;
    sortDirection.value = 'asc';
  }
}

function prevPage() {
  if (currentPage.value > 1) currentPage.value--;
}

function nextPage() {
  if (currentPage.value < totalPages.value) currentPage.value++;
}
</script>

<template>
  <div class="table-component-container">
    <div class="table-header-external">
      <div class="header-info">
        <slot name="header-left"></slot>
      </div>
      
      <div v-if="totalPages > 1" class="pagination-compact">
        <span class="pag-info">{{ currentPage }}-{{ totalPages }}</span>
        <div class="pag-controls">
          <button @click="prevPage" :disabled="currentPage === 1" class="pag-btn">
            <span class="material-symbols-outlined">chevron_left</span>
          </button>
          <button @click="nextPage" :disabled="currentPage === totalPages" class="pag-btn">
            <span class="material-symbols-outlined">chevron_right</span>
          </button>
        </div>
      </div>
    </div>

    <div class="table-wrapper">
      <div v-if="loading" class="loading-overlay">
        <div class="spinner"></div>
      </div>

      <table class="custom-table">
        <thead>
          <tr>
            <th 
              v-for="(header, index) in headers" 
              :key="header"
              @click="handleSort(index)"
              :class="{ 'sortable-header': true, 'active-sort': sortColumn === index }"
            >
              <div class="header-content">
                {{ header }}
                <span class="material-symbols-outlined sort-icon">
                  {{ sortColumn === index ? (sortDirection === 'asc' ? 'arrow_upward' : 'arrow_downward') : 'unfold_more' }}
                </span>
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
          >
            <td v-for="(cell, cIdx) in row" :key="cIdx">
              <!-- Render UserCell -->
              <div v-if="typeof cell === 'object' && cell?.component === 'UserCell'" class="cell-user">
                <div class="avatar-mini">{{ cell.props.inicial || '?' }}</div>
                <span class="user-name-cell">{{ cell.props.nombre }}</span>
              </div>

              <!-- Render Badge -->
              <span v-else-if="typeof cell === 'object' && cell?.component === 'Badge'" 
                class="status-badge" 
                :class="cell.props.class"
              >
                <span v-if="cell.props.icon" class="material-symbols-outlined status-icon">
                  {{ cell.props.icon }}
                </span>
                {{ cell.props.text }}
              </span>

              <!-- Actions column -->
              <div v-else-if="typeof cell === 'object' && cell?.type === 'actions'" class="cell-actions">
                <button 
                  v-for="(action, aIdx) in cell.actions" 
                  :key="aIdx"
                  class="action-btn"
                  :class="action.class"
                  @click.stop="action.onClick"
                  :title="action.label"
                >
                  <span class="material-symbols-outlined">{{ action.icon }}</span>
                </button>
              </div>

              <!-- Progress Bar -->
              <div v-else-if="typeof cell === 'object' && cell?.component === 'ProgressBar'" class="cell-progress">
                <div class="progress-bg">
                  <div class="progress-fill" :style="{ width: cell.props.value + '%', backgroundColor: cell.props.color }"></div>
                </div>
                <span class="progress-text">{{ Math.round(cell.props.value) }}%</span>
              </div>

              <span v-else>{{ cell }}</span>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div v-if="!loading && data.length === 0" class="empty-state">
        <span class="material-symbols-outlined">database_off</span>
        <p>No se encontraron registros</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.table-component-container { display: flex; flex-direction: column; gap: 12px; }
.table-header-external { display: flex; justify-content: space-between; align-items: center; padding: 0 4px; }
.pagination-compact { display: flex; align-items: center; gap: 12px; background: white; padding: 6px 12px; border-radius: 8px; border: 1px solid #e2e8f0; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
.pag-info { font-size: 0.85rem; font-weight: 700; color: #64748b; font-variant-numeric: tabular-nums; }
.pag-controls { display: flex; gap: 4px; }
.pag-btn { background: transparent; border: none; color: #94a3b8; cursor: pointer; display: flex; align-items: center; padding: 2px; border-radius: 4px; transition: all 0.2s; }
.pag-btn:hover:not(:disabled) { background: #f1f5f9; color: #0f172a; }
.pag-btn:disabled { opacity: 0.3; cursor: not-allowed; }

.table-wrapper { background: white; border-radius: 12px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); position: relative; }
.custom-table { width: 100%; border-collapse: collapse; text-align: left; }
.custom-table thead { background: #1e293b; color: white; }

.sortable-header { cursor: pointer; transition: background 0.2s; }
.sortable-header:hover { background: #334155; }
.active-sort { background: #334155; }
.header-content { display: flex; align-items: center; gap: 8px; }

.custom-table th { padding: 14px 20px; font-size: 0.75rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid #334155; }
.sort-icon { font-size: 16px; opacity: 0.5; }
.active-sort .sort-icon { opacity: 1; color: #ef4444; }

.custom-table td { padding: 16px 20px; font-size: 0.9rem; color: #334155; border-bottom: 1px solid #f1f5f9; }
.table-row { transition: all 0.2s; }
.table-row:hover { background: #f8fafc; }
.table-row.clickable { cursor: pointer; }

/* Components inside cells */
.cell-user { display: flex; align-items: center; gap: 10px; }
.avatar-mini { width: 28px; height: 28px; border-radius: 8px; background: #f1f5f9; color: #64748b; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; }
.user-name-cell { font-weight: 600; color: #0f172a; }

.status-badge { display: inline-flex; align-items: center; gap: 6px; padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.03em; }
.status-badge .status-icon { font-size: 14px; }
.status-badge.orange { background: #fff7ed; color: #ea580c; border: 1px solid #ffedd5; }
.status-badge.green { background: #f0fdf4; color: #16a34a; border: 1px solid #dcfce7; }
.status-badge.red { background: #fef2f2; color: #dc2626; border: 1px solid #fee2e2; }
.status-badge.blue { background: #eff6ff; color: #2563eb; border: 1px solid #dbeafe; }
.status-badge.gray { background: #f8fafc; color: #64748b; border: 1px solid #e2e8f0; }

.cell-actions { display: flex; gap: 6px; }
.action-btn { background: white; border: 1px solid #e2e8f0; border-radius: 6px; padding: 4px; color: #64748b; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; justify-content: center; }
.action-btn .material-symbols-outlined { font-size: 18px; }
.action-btn:hover { color: #0f172a; border-color: #cbd5e1; background: #f8fafc; }
.action-btn.btn-delete:hover { color: #ef4444; border-color: #fee2e2; background: #fef2f2; }

.cell-progress { display: flex; align-items: center; gap: 10px; min-width: 120px; }
.progress-bg { flex: 1; height: 6px; background: #f1f5f9; border-radius: 3px; overflow: hidden; }
.progress-fill { height: 100%; border-radius: 3px; transition: width 0.6s ease; }
.progress-text { font-size: 11px; font-weight: 700; color: #64748b; width: 30px; }

.loading-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(255,255,255,0.7); display: flex; align-items: center; justify-content: center; z-index: 10; }
.spinner { width: 30px; height: 30px; border: 3px solid #f1f5f9; border-top-color: #ef4444; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty-state { padding: 4rem; text-align: center; color: #94a3b8; }
.empty-state .material-symbols-outlined { font-size: 3rem; margin-bottom: 1rem; opacity: 0.5; }
</style>
