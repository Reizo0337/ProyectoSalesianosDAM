<script setup lang="ts">
import { useRouter } from 'vue-router';

interface Order {
  idorden: number;
  numero_orden: string;
  numero_plan: string;
  cantidad: number;
  fechacreacion: string;
  estado: string;
  numfacturas?: string;
}

const props = defineProps<{
  orders: Order[];
}>();

const router = useRouter();

const emit = defineEmits(['edit']);

function goToDetail(orderId: number) {
  router.push(`/ordenes/${orderId}`);
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
          <th>Nº Orden</th>
          <th>Fecha</th>
          <th>Cantidad</th>
          <th>Estado</th>
          <th>Factura</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="order in orders"
          :key="order.idorden"
          class="table-row clickable"
          @click="goToDetail(order.idorden)"
        >
          <td><span class="cell-order-num">{{ order.numero_orden || order.numero_plan || '-' }}</span></td>
          <td><span class="cell-date">{{ formatDate(order.fechacreacion) }}</span></td>
          <td><span class="cell-price">{{ order.cantidad }}€</span></td>
          <td>
            <span class="status-badge" :class="'status-' + (order.estado || 'pendiente').toLowerCase()">
              {{ order.estado }}
            </span>
          </td>
          <td>
            <span class="factura-badge" :class="parseInt(order.numfacturas || '0') > 0 ? 'has-factura' : 'no-factura'">
              {{ parseInt(order.numfacturas || '0') > 0 ? 'Si' : 'No' }}
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.table-scroll { overflow-x: auto; width: 100%; }
table { width: 100%; border-collapse: collapse; }
thead { background: linear-gradient(180deg, #f9fafb, #f3f4f6); }
th { padding: 14px 20px; text-align: left; font-size: 12px; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.06em; border-bottom: 1px solid #e5e7eb; }
td { padding: 16px 20px; font-size: 14px; color: #374151; }
.table-row { transition: background-color 0.18s; }
.table-row:not(:last-child) td { border-bottom: 1px solid #f3f4f6; }
.table-row.clickable { cursor: pointer; }
.table-row.clickable:hover { background-color: #fef2f2; }
.cell-order-num { font-weight: 700; font-size: 13px; color: #dc2626; background: #fef2f2; padding: 4px 10px; border-radius: 6px; }
.cell-date { font-size: 13px; color: #6b7280; font-variant-numeric: tabular-nums; }
.cell-price { font-weight: 600; color: #1f2937; font-variant-numeric: tabular-nums; }
.status-badge { display: inline-flex; align-items: center; padding: 5px 12px; border-radius: 20px; font-size: 13px; font-weight: 600; }
.status-pendiente { background: #fef9c3; color: #ca8a04; }
.status-aprobado { background: #dcfce7; color: #16a34a; }
.status-rechazado { background: #fee2e2; color: #dc2626; }
.factura-badge { display: inline-block; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; }
.has-factura { background: #dcfce7; color: #16a34a; }
.no-factura { background: #f3f4f6; color: #9ca3af; }

.edit-icon-btn {
  background: none;
  border: none;
  color: #dc2626;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-icon-btn:hover {
  background: #fef2f2;
  color: #b91c1c;
  transform: scale(1.1);
}
</style>
