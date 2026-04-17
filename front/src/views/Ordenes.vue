<script setup lang="ts">
import { onMounted, computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useOrderStore } from '@/stores/orders';
import OrderModal from '../components/orders/OrderModal.vue';

const router = useRouter();
const authStore = useAuthStore();
const orderStore = useOrderStore();

const showModal = ref(false);
const searchQuery = ref('');
const headers = ['Nº Orden', 'Cantidad', 'Estado', 'Factura'];

const canCreate = computed(() => {
  return authStore.user?.rol === 'Administrador' || authStore.user?.rol === 'Jefe de Equipo';
});

const filteredOrders = computed(() => {
  if (!searchQuery.value.trim()) return orderStore.orders;
  const q = searchQuery.value.toLowerCase();
  return orderStore.orders.filter(o =>
    (o.numero_orden || '').toLowerCase().includes(q) ||
    (o.Estado || '').toLowerCase().includes(q) ||
    String(o.Cantidad).includes(q)
  );
});

function goToDetail(orderId: number) {
  router.push(`/ordenes/${orderId}`);
}

async function refreshOrders() {
  const dept = authStore.user?.rol === 'Administrador' ? 'Admin' : authStore.user?.idDepartamento;
  if (dept) {
    await orderStore.getOrdersByDept(dept);
  }
}

onMounted(async () => {
  await refreshOrders();
});
</script>

<template>
  <div class="view-container">
    <div class="header-section">
      <div class="title-box">
        <h1>Órdenes de Compra</h1>
        <p>Seguimiento y gestión de pedidos realizados.</p>
      </div>
      <button v-if="canCreate" class="create-btn" @click="showModal = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20">
          <path d="M12 5v14M5 12h14" />
        </svg>
        Nueva Orden
      </button>
    </div>

    <div class="table-card">
      <div v-if="orderStore.orders.length > 0">
        <div class="table-toolbar">
          <div class="search-box">
            <span class="material-symbols-outlined search-icon">search</span>
            <input v-model="searchQuery" type="text" placeholder="Buscar orden..." class="search-input" />
          </div>
          <div class="result-count">
            <span>{{ filteredOrders.length }}</span> resultado{{ filteredOrders.length !== 1 ? 's' : '' }}
          </div>
        </div>
        <div class="table-scroll">
          <table>
            <thead>
              <tr>
                <th v-for="h in headers" :key="h">{{ h }}</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="order in filteredOrders"
                :key="order.idOrden"
                class="table-row clickable"
                @click="goToDetail(order.idOrden)"
              >
                <td><span class="cell-order-num">{{ order.numero_orden || '-' }}</span></td>
                <td><span class="cell-price">{{ order.Cantidad }}€</span></td>
                <td>
                  <span class="status-badge" :class="'status-' + (order.Estado || 'pendiente').toLowerCase()">
                    {{ order.Estado }}
                  </span>
                </td>
                <td>
                  <span class="factura-badge" :class="parseInt(order.numFacturas || '0') > 0 ? 'has-factura' : 'no-factura'">
                    {{ parseInt(order.numFacturas || '0') > 0 ? 'Si' : 'No' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div v-else-if="orderStore.loading" class="loading-state">
        <p>Cargando órdenes...</p>
      </div>
      <div v-else class="empty-state">
        <p>No se encontraron órdenes de compra.</p>
      </div>
    </div>

    <OrderModal 
      :isOpen="showModal" 
      @close="showModal = false" 
      @success="refreshOrders"
    />
  </div>
</template>

<style scoped>
.view-container { padding: 24px; }
.header-section {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px;
}
.header-section h1 { font-size: 28px; font-weight: 700; color: #1f2937; margin-bottom: 8px; }
.header-section p { color: #6b7280; }
.create-btn {
  display: flex; align-items: center; gap: 8px;
  background: linear-gradient(135deg, #dc2626, #b91c1c); color: white;
  border: none; padding: 10px 20px; border-radius: 10px; font-weight: 600;
  cursor: pointer; transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.2);
}
.create-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(220, 38, 38, 0.3); }
.table-card {
  background: white; border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); overflow: hidden;
  border: 1px solid #e5e7eb;
}
.table-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-bottom: 1px solid #f3f4f6;
}
.search-box {
  display: flex; align-items: center; background: #f9fafb;
  border: 1px solid #e5e7eb; border-radius: 10px; padding: 0 12px;
  flex: 1; max-width: 360px; transition: all 0.25s;
}
.search-box:focus-within { border-color: #dc2626; box-shadow: 0 0 0 3px rgba(220,38,38,0.1); background: #fff; }
.search-icon { font-size: 20px; color: #9ca3af; margin-right: 8px; }
.search-input { border: none; outline: none; background: transparent; font-size: 14px; color: #1f2937; padding: 10px 0; width: 100%; }
.result-count { font-size: 13px; color: #9ca3af; }
.result-count span { font-weight: 600; color: #6b7280; }
.table-scroll { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
thead { background: linear-gradient(180deg, #f9fafb, #f3f4f6); }
th { padding: 14px 20px; text-align: left; font-size: 12px; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.06em; border-bottom: 1px solid #e5e7eb; }
td { padding: 16px 20px; font-size: 14px; color: #374151; }
.table-row { transition: background-color 0.18s; }
.table-row:not(:last-child) td { border-bottom: 1px solid #f3f4f6; }
.table-row.clickable { cursor: pointer; }
.table-row.clickable:hover { background-color: #fef2f2; }
.cell-order-num { font-weight: 700; font-size: 13px; color: #dc2626; background: #fef2f2; padding: 4px 10px; border-radius: 6px; }
.cell-price { font-weight: 600; color: #1f2937; font-variant-numeric: tabular-nums; }
.status-badge { display: inline-flex; align-items: center; padding: 5px 12px; border-radius: 20px; font-size: 13px; font-weight: 600; }
.status-pendiente { background: #fef9c3; color: #ca8a04; }
.status-aprobado { background: #dcfce7; color: #16a34a; }
.status-rechazado { background: #fee2e2; color: #dc2626; }
.factura-badge { display: inline-block; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; }
.has-factura { background: #dcfce7; color: #16a34a; }
.no-factura { background: #f3f4f6; color: #9ca3af; }
.empty-state, .loading-state { text-align: center; padding: 48px; color: #9ca3af; }
</style>
