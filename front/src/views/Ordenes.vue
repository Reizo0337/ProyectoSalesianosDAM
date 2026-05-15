<script setup lang="ts">
import { onMounted, computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useOrderStore } from '@/stores/orders';
import OrderModal from '../components/orders/OrderModal.vue';
import OrderTable from '../components/orders/OrderTable.vue';

const router = useRouter();
const authStore = useAuthStore();
const orderStore = useOrderStore();

const showModal = ref(false);
const searchQuery = ref('');
const headers = ['Nº Orden', 'Fecha', 'Cantidad', 'Estado', 'Factura'];

const canCreate = computed(() => {
  return authStore.user?.rol === 'Administrador' || authStore.user?.rol === 'Jefe de Equipo';
});

const filteredOrders = computed(() => {
  if (!searchQuery.value.trim()) return orderStore.orders;
  const q = searchQuery.value.toLowerCase();
  return orderStore.orders.filter(o =>
    (o.numero_orden || '').toLowerCase().includes(q) ||
    (o.numero_plan || '').toLowerCase().includes(q) ||
    (o.estado || '').toLowerCase().includes(q) ||
    String(o.cantidad).includes(q)
  );
});

async function refreshOrders() {
  const role = authStore.user?.rol;
  // Admin and Contable see everything (passing 'Admin' as dept triggers this in backend)
  const dept = (role === 'Administrador' || role === 'Contable') 
    ? 'Admin' 
    : authStore.user?.idDepartamento;
    
  if (dept) {
    const currentYear = new Date().getFullYear();
    await orderStore.getOrdersByDept(dept, currentYear);
  }
}

// Watch for user to be available (handles F5/refresh issues)
watch(() => authStore.user, (user) => {
  if (user) {
    refreshOrders();
  }
}, { immediate: true });

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
          <OrderTable :orders="filteredOrders" />
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

.empty-state, .loading-state { text-align: center; padding: 48px; color: #9ca3af; }
</style>
