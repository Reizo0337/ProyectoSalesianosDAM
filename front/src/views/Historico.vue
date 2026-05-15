<script setup lang="ts">
import { onMounted, computed, ref, watch } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useOrderStore } from '@/stores/orders';
import OrderTable from '../components/orders/OrderTable.vue';

const authStore = useAuthStore();
const orderStore = useOrderStore();

const currentYear = new Date().getFullYear();
const selectedYear = ref(currentYear);
const years = ref<number[]>([]);
const searchQuery = ref('');

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
  const dept = (role === 'Administrador' || role === 'Contable') ? 'Admin' : authStore.user?.idDepartamento;
  if (dept) {
    await orderStore.getOrdersByDept(dept, selectedYear.value);
  }
}

watch(() => authStore.user, async (user) => {
    if (user) {
      years.value = await orderStore.fetchYears();
      if (years.value.length === 0) years.value = [currentYear, currentYear - 1];
      
      // Select first year available
      if (years.value.length > 0) {
        selectedYear.value = years.value[0];
      }
      
      await refreshOrders();
    }
}, { immediate: true });


watch(selectedYear, () => {
  refreshOrders();
});
</script>

<template>
  <div class="view-container">
    <div class="header-section">
      <div class="title-box">
        <h1>Histórico de Órdenes</h1>
        <p>Consulta de pedidos de años anteriores.</p>
      </div>
      
      <div class="year-selector">
        <label>Año de Ejercicio</label>
        <div class="select-wrapper">
          <select v-model="selectedYear" class="year-select">
            <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
          </select>
          <svg class="select-icon" viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="6 9 12 15 18 9"></polyline>
          </svg>
        </div>
      </div>
    </div>

    <div class="table-card">
      <div v-if="orderStore.orders.length > 0">
        <div class="table-toolbar">
          <div class="search-box">
            <span class="material-symbols-outlined search-icon">search</span>
            <input v-model="searchQuery" type="text" placeholder="Buscar en el histórico..." class="search-input" />
          </div>
          <div class="result-count">
            <span>{{ filteredOrders.length }}</span> órdenes en {{ selectedYear }}
          </div>
        </div>
        <div class="table-scroll">
          <OrderTable :orders="filteredOrders" />
        </div>
      </div>
      <div v-else-if="orderStore.loading" class="loading-state">
        <p>Cargando datos históricos...</p>
      </div>
      <div v-else class="empty-state">
        <p>No se encontraron órdenes para el año {{ selectedYear }}.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-container { padding: 24px; }
.header-section {
  display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 32px;
}
.header-section h1 { font-size: 28px; font-weight: 700; color: #1f2937; margin-bottom: 8px; }
.header-section p { color: #6b7280; }

.year-selector {
  display: flex; align-items: center; gap: 16px;
  background: #f8fafc; padding: 12px 20px; border-radius: 4px;
  border: 1px solid #e2e8f0;
}
.year-selector label { font-weight: 700; color: #64748b; font-size: 13px; text-transform: uppercase; letter-spacing: 0.05em; }

.select-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}
.year-select {
  appearance: none; -webkit-appearance: none;
  background: white; border: 2px solid #cbd5e1; border-radius: 4px;
  padding: 10px 42px 10px 16px; font-size: 16px; font-weight: 700; color: #334155;
  cursor: pointer; transition: all 0.2s ease; min-width: 140px; outline: none;
}
.year-select:hover, .year-select:focus {
  border-color: #3b82f6; box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}
.select-icon {
  position: absolute; right: 14px; pointer-events: none; color: #64748b; transition: transform 0.2s ease, color 0.2s ease;
}
.year-select:focus + .select-icon {
  transform: rotate(180deg); color: #3b82f6;
}

.table-card {
  background: white; border-radius: 4px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); overflow: hidden;
  border: 1px solid #e5e7eb;
}
.table-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-bottom: 1px solid #f3f4f6;
}
.search-box {
  display: flex; align-items: center; background: #f9fafb;
  border: 1px solid #e5e7eb; border-radius: 4px; padding: 0 12px;
  flex: 1; max-width: 360px; transition: all 0.25s;
}
.search-icon { font-size: 20px; color: #9ca3af; margin-right: 8px; }
.search-input { border: none; outline: none; background: transparent; font-size: 14px; color: #1f2937; padding: 10px 0; width: 100%; }
.result-count { font-size: 13px; color: #9ca3af; }
.result-count span { font-weight: 600; color: #6b7280; }
.table-scroll { overflow-x: auto; }

.empty-state, .loading-state { text-align: center; padding: 48px; color: #9ca3af; }
</style>
