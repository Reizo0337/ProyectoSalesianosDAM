<script setup lang="ts">
import { onMounted, computed, ref, watch } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useOrderStore } from '@/stores/orders';
import { usePresupuestoStore } from '@/stores/presupuesto';
import { useToast } from 'vue-toastification';
import OrderTable from '../components/orders/OrderTable.vue';
import Table from '../components/common/Table.vue';
 
const authStore = useAuthStore();
const orderStore = useOrderStore();
const presupuestoStore = usePresupuestoStore();
const toast = useToast();
 
const currentYear = new Date().getFullYear();
const selectedYear = ref(currentYear - 1); 
const years = ref<number[]>([]);
const searchQuery = ref('');
const activeTab = ref('ordenes');
 
const isInvestment = (p: any) => {
  const t = (p.type || p.Type || '').toLowerCase();
  return t === 'planinversion' || t.includes('inversion') || t.includes('plan');
};
 
const formatType = (p: any) => {
  if (isInvestment(p)) return 'Plan Inversión';
  return 'Presupuesto Ordinario';
};
 
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
 
async function refreshData() {
  const role = authStore.user?.rol;
  const dept = (role === 'Administrador' || role === 'Contable') ? 'Admin' : authStore.user?.idDepartamento;
  if (!dept) return;
 
  if (activeTab.value === 'ordenes') {
    await orderStore.getOrdersByDept(dept, selectedYear.value);
  } else {
    if (role === 'Administrador' || role === 'Contable') {
      await presupuestoStore.getAllPresupuestos(selectedYear.value);
    } else {
      await presupuestoStore.getPresupuestosByDept(dept, selectedYear.value);
    }
  }
}
 
const handleCloneToCurrent = async () => {
  const ok = await presupuestoStore.cloneBudgets(selectedYear.value, currentYear);
  if (ok) {
    toast.success(`Presupuestos de ${selectedYear.value} clonados correctamente`);
  } else {
    toast.error('Error al clonar. Posiblemente ya existan datos en el año actual.');
  }
};
 
watch(() => authStore.user, async (user) => {
    if (user) {
      years.value = await orderStore.fetchYears();
      if (years.value.length === 0) years.value = [currentYear, currentYear - 1];
      const histYears = years.value.filter(y => y < currentYear);
      selectedYear.value = histYears.length > 0 ? histYears[0] : currentYear - 1;
      await refreshData();
    }
}, { immediate: true });
 
watch([selectedYear, activeTab], () => {
  refreshData();
});
</script>
 
<template>
  <div class="view-page animate-in">
    <header class="dashboard-header">
      <div class="header-left">
        <h1>Histórico del Centro</h1>
        <p class="subtitle">Consulta de registros financieros de ejercicios anteriores.</p>
      </div>
      <div class="header-actions">
        <div class="tab-switcher">
          <button :class="{ active: activeTab === 'ordenes' }" @click="activeTab = 'ordenes'">
            <span class="material-symbols-outlined">shopping_cart</span>
            Órdenes
          </button>
          <button :class="{ active: activeTab === 'presupuestos' }" @click="activeTab = 'presupuestos'">
            <span class="material-symbols-outlined">payments</span>
            Presupuestos
          </button>
        </div>
        <div class="year-selector">
          <span class="material-symbols-outlined">event_repeat</span>
          <select v-model="selectedYear" class="year-select">
            <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
          </select>
        </div>
      </div>
    </header>
 
    <div v-if="activeTab === 'presupuestos'" class="actions-bar animate-in">
      <button @click="handleCloneToCurrent" class="create-btn">
        <span class="material-symbols-outlined">content_copy</span>
        Clonar presupuestos a {{ currentYear }}
      </button>
    </div>
 
    <div class="table-card animate-in">
      <div v-if="activeTab === 'ordenes'">
        <div v-if="orderStore.orders.length > 0">
          <div class="table-toolbar">
            <div class="search-box">
              <span class="material-symbols-outlined search-icon">search</span>
              <input v-model="searchQuery" type="text" placeholder="Buscar en el histórico..." class="search-input" />
            </div>
            <div class="result-count">
              <span>{{ filteredOrders.length }}</span> resultados
            </div>
          </div>
          <OrderTable :orders="filteredOrders" />
        </div>
        <div v-else-if="orderStore.loading" class="loading-state">
          <div class="spinner"></div>
          <p>Sincronizando registros...</p>
        </div>
        <div v-else class="empty-state">
          <p>Sin órdenes registradas para {{ selectedYear }}.</p>
        </div>
      </div>
 
      <div v-else>
        <Table v-if="presupuestoStore.presupuestos.length > 0"
          :headers="['ID', 'Código', 'Nombre', 'Tipo', 'Cantidad', 'Gasto', 'Departamento']"
          :data="presupuestoStore.presupuestos.map(p => [
            p.idpresupuesto || p.idPresupuesto,
            p.codigo || p.Codigo,
            p.nombrepresupuesto || p.nombrePresupuesto,
            formatType(p),
            Number(p.cantidad || 0).toLocaleString() + '€',
            Number(p.gasto || 0).toLocaleString() + '€',
            p.nombredepartamento || p.nombreDepartamento
          ])"
        />
        <div v-else class="empty-state">
          <p>No hay presupuestos para el año {{ selectedYear }}.</p>
        </div>
      </div>
    </div>
  </div>
</template>
 
<style scoped>
.dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 3.5rem; }
.header-left h1 { font-size: 2.75rem; font-weight: 850; color: #0f172a; letter-spacing: -0.04em; margin-bottom: 4px; }
.subtitle { color: #64748b; font-size: 1.15rem; }
.header-actions { display: flex; align-items: center; gap: 16px; }
 
.tab-switcher { display: flex; background: #f1f5f9; padding: 4px; border-radius: 8px; gap: 4px; }
.tab-switcher button {
  display: flex; align-items: center; gap: 8px; padding: 8px 16px; border-radius: 6px;
  border: none; font-weight: 700; color: #64748b; cursor: pointer; transition: all 0.2s;
}
.tab-switcher button.active { background: white; color: #0f172a; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
 
.year-selector {
  display: flex; align-items: center; gap: 8px; background: white; padding: 10px 16px;
  border-radius: 8px; border: 1px solid #e2e8f0; color: #64748b;
}
.year-select { border: none; outline: none; font-weight: 700; color: #0f172a; cursor: pointer; }
 
.actions-bar { margin-bottom: 2rem; display: flex; justify-content: flex-end; }
.create-btn {
  display: flex; align-items: center; gap: 8px; background: #dc2626; color: white;
  border: none; padding: 12px 24px; border-radius: 8px; font-weight: 700; cursor: pointer;
  transition: all 0.2s; box-shadow: 0 4px 6px -1px rgba(220, 38, 38, 0.2);
}
 
.table-toolbar { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid #f1f5f9; background: white; }
.search-box { display: flex; align-items: center; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 0 12px; width: 300px; }
.search-icon { font-size: 20px; color: #94a3b8; margin-right: 8px; }
.search-input { border: none; outline: none; background: transparent; font-size: 14px; color: #1e293b; padding: 10px 0; width: 100%; }
.result-count { font-size: 13px; color: #94a3b8; font-weight: 700; }
 
.table-card { background: white; border-radius: 12px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
.spinner { width: 30px; height: 30px; border: 3px solid #f1f5f9; border-top-color: #ef4444; border-radius: 50%; animation: spin 0.8s linear infinite; margin: 0 auto 1rem; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty-state, .loading-state { text-align: center; padding: 4rem; color: #94a3b8; }
</style>
