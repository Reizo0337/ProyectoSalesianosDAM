<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useOrderStore } from '@/stores/orders';
import { useToast } from 'vue-toastification';
import { useDialogStore } from '@/stores/dialog';

const toast = useToast();
const dialogStore = useDialogStore();
const router = useRouter();

interface Order {
  idorden: number;
  numero_orden: string;
  numero_plan: string;
  cantidad: number;
  fechacreacion: string;
  estado: string;
  numfacturas?: string;
  numcomentarios?: string;
}

const props = defineProps<{
  orders: Order[];
}>();

const authStore = useAuthStore();
const orderStore = useOrderStore();

const currentPage = ref(1);
const pageSize = 10;

const totalPages = computed(() => Math.ceil(props.orders.length / pageSize));

const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return props.orders.slice(start, start + pageSize);
});

function nextPage() {
  if (currentPage.value < totalPages.value) currentPage.value++;
}

function prevPage() {
  if (currentPage.value > 1) currentPage.value--;
}

watch(() => props.orders, () => {
  currentPage.value = 1;
});

const emit = defineEmits(['edit', 'refresh']);

function goToDetail(orderId: number) {
  router.push(`/ordenes/${orderId}`);
}

function canDelete(order: Order) {
  const user = authStore.user;
  if (!user) return false;
  const isAdmin = user.rol === 'Administrador';
  const isJefe = user.rol === 'Jefe de Equipo';
  return (isAdmin || isJefe) && (order.estado || '').toLowerCase() !== 'cerrada';
}

async function handleDelete(e: Event, orderId: number) {
  e.stopPropagation();

  const confirmed = await dialogStore.confirm(
    "Eliminar Orden",
    "¿Estás seguro de eliminar esta orden? Esta acción eliminará también sus facturas y comentarios asociados."
  );

  if (confirmed) {
    try {
      const res = await orderStore.deleteOrder(orderId);
      if (res.status === 'success') {
        emit('refresh');
      } else {
        toast.error(res.message || 'Error al eliminar');
      }
    } catch (err) {
      toast.error('Error de conexión al eliminar la orden');
    }
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-';
  try {
    const d = new Date(dateStr);
    return d.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
  } catch (e) {
    return dateStr;
  }
}
</script>

<template>
  <div class="table-scroll">
    <table>
      <thead>
        <tr>
          <th style="width: 1%; white-space: nowrap;">Nº Orden</th>
          <th style="width: 1%; white-space: nowrap;">Fecha</th>
          <th style="width: 1%; white-space: nowrap;">Cantidad</th>
          <th style="width: 1%; white-space: nowrap;">Estado</th>
          <th style="width: 1%; white-space: nowrap;">Factura</th>
          <th style="width: 1%; white-space: nowrap;">Mensajes</th>
          <th style="width: 1%; white-space: nowrap;">Acciones</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(order, index) in paginatedOrders"
          :key="order.idorden"
          class="table-row clickable"
          :style="{ '--index': index }"
          @click="goToDetail(order.idorden)"
        >
          <td><span class="cell-order-num">{{ order.numero_orden || order.numero_plan || '-' }}</span></td>
          <td><span class="cell-date">{{ formatDate(order.fechacreacion) }}</span></td>
          <td><span class="cell-price">{{ order.cantidad }}€</span></td>
          <td>
            <span class="status-badge" :class="'status-' + (order.estado || 'pendiente').toLowerCase()">
              <svg v-if="(order.estado || 'pendiente').toLowerCase() === 'cerrada'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" style="margin-right: 4px;">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <svg v-else-if="(order.estado || 'pendiente').toLowerCase() === 'pendiente'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" style="margin-right: 4px;">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              {{ order.estado || 'Pendiente' }}
            </span>
          </td>
          <td>
            <span class="factura-badge" :class="parseInt(order.numfacturas || '0') > 0 ? 'has-factura' : 'no-factura'">
              {{ parseInt(order.numfacturas || '0') > 0 ? 'Si' : 'No' }}
            </span>
          </td>
          <td>
            <span v-if="parseInt(order.numcomentarios || '0') > 0" class="comment-indicator">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
              <span class="comment-count">{{ order.numcomentarios }}</span>
            </span>
            <span v-else class="no-comments">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16" style="opacity: 0.3;">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
            </span>
          </td>
          <td>
            <div class="actions-cell">
              <button
                v-if="canDelete(order)"
                class="delete-icon-btn"
                @click="handleDelete($event, order.idorden)"
                title="Eliminar orden"
              >
                <span class="material-symbols-outlined">delete</span>
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="pagination-controls" v-if="totalPages > 1">
      <button @click="prevPage" :disabled="currentPage === 1" class="page-btn">Anterior</button>
      <span class="page-info">Página {{ currentPage }} de {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage === totalPages" class="page-btn">Siguiente</button>
    </div>
  </div>
</template>

<style scoped>
.table-scroll { overflow-x: auto; width: 100%; }
table { width: 100%; border-collapse: collapse; }
thead { background: #1e293b; border-bottom: 2px solid #0f172a; }
th { padding: 14px 16px; text-align: left; font-size: 12px; font-weight: 700; color: #f8fafc; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: none; }
td { padding: 14px 16px; font-size: 13px; color: #334155; border-bottom: 1px solid #e2e8f0; }
.table-row { 
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: rowIn 0.4s ease-out both;
  animation-delay: calc(var(--index) * 0.05s);
}
.table-row:nth-child(even) { background-color: #f8fafc; }
.table-row.clickable { cursor: pointer; }
.table-row.clickable:hover { 
  background-color: #ffffff; 
  transform: scale(1.005) translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  z-index: 10;
  position: relative;
}

@keyframes rowIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.cell-order-num { font-weight: 700; font-size: 13px; color: #dc2626; background: #fef2f2; padding: 4px 10px; border-radius: 4px; }
.cell-date { font-size: 13px; color: #6b7280; font-variant-numeric: tabular-nums; }
.cell-price { font-weight: 600; color: #1f2937; font-variant-numeric: tabular-nums; }
.status-badge { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 4px; font-size: 13px; font-weight: 600; }
.status-pendiente { background: #fef9c3; color: #ca8a04; }
.status-aprobado { background: #dcfce7; color: #16a34a; }
.status-rechazado { background: #fee2e2; color: #dc2626; }
.status-cerrada { background: #fee2e2; color: #dc2626; }
.factura-badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 600; }
.has-factura { background: #dcfce7; color: #16a34a; }
.no-factura { background: #f3f4f6; color: #9ca3af; }

.comment-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #2563eb;
  background: #eff6ff;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  animation: commentPulse 2s ease-in-out infinite;
}

.comment-count {
  font-size: 12px;
  font-weight: 700;
}

.no-comments {
  display: inline-flex;
  align-items: center;
  padding: 4px;
  color: #cbd5e1;
}

@keyframes commentPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.delete-icon-btn {
  background: none;
  border: none;
  color: #94a3b8;
  padding: 6px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-icon-btn:hover {
  background: #fee2e2;
  color: #ef4444;
}

.delete-icon-btn .material-symbols-outlined {
  font-size: 20px;
}

.pagination-controls {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; border-top: 1px solid #e2e8f0; background: #f8fafc;
}
.page-info { font-size: 13px; font-weight: 600; color: #475569; }
.page-btn { padding: 6px 12px; border-radius: 4px; border: 1px solid #cbd5e1; background: white; font-size: 13px; font-weight: 600; color: #334155; cursor: pointer; transition: all 0.2s; }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.page-btn:not(:disabled):hover { background: #f1f5f9; border-color: #94a3b8; }
</style>
