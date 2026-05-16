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
const selectedYear = ref(currentYear - 1); // Por defecto un año atrás para el histórico
const years = ref<number[]>([]);
const searchQuery = ref('');
const activeTab = ref('ordenes'); // 'ordenes' o 'presupuestos'

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
    toast.success(`Presupuestos de ${selectedYear.value} clonados correctamente al año ${currentYear}`);
  } else {
    toast.error('Error al clonar los presupuestos. Es posible que ya existan presupuestos para el año actual.');
  }
};

watch(() => authStore.user, async (user) => {
    if (user) {
      years.value = await orderStore.fetchYears();
      if (years.value.length === 0) years.value = [currentYear, currentYear - 1];
      
      // Select first historical year if available
      const histYears = years.value.filter(y => y < currentYear);
      if (histYears.length > 0) {
        selectedYear.value = histYears[0];
      } else {
        selectedYear.value = currentYear - 1;
      }
      
      await refreshData();
    }
}, { immediate: true });

watch([selectedYear, activeTab], () => {
  refreshData();
});
</script>

<template>
  <div class="view-container">
    <div class="header-section animate-in">
      <div class="title-box">
        <h1 class="gradient-text">Histórico del Centro</h1>
        <p class="subtitle">Consulta de registros financieros y presupuestarios de ejercicios anteriores.</p>
      </div>
      
      <div class="header-controls">
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
          <label>Ejercicio</label>
          <div class="select-wrapper">
            <select v-model="selectedYear" class="year-select">
              <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <div v-if="activeTab === 'presupuestos'" class="actions-bar animate-in delay-1">
      <button @click="handleCloneToCurrent" class="btn-clone-history">
        <span class="material-symbols-outlined">content_copy</span>
        Establecer como presupuesto para el año actual ({{ currentYear }})
      </button>
    </div>

    <div class="table-card animate-in delay-2">
      <div v-if="activeTab === 'ordenes'">
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

      <div v-else>
        <!-- Presupuestos Table -->
        <div v-if="presupuestoStore.presupuestos.length > 0">
           <Table
            :headers="['ID', 'Código', 'Nombre', 'Tipo', 'Cantidad', 'Gasto', 'Departamento']"
            :data="presupuestoStore.presupuestos.map(p => [
              p.idpresupuesto || p.idPresupuesto,
              p.codigo || p.Codigo,
              p.nombrepresupuesto || p.nombrePresupuesto,
              formatType(p),
              (p.cantidad || p.Cantidad) + '€',
              (p.gasto || p.Gasto) + '€',
              p.nombredepartamento || p.nombreDepartamento
            ])"
            :searchable="true"
          />
        </div>
        <div v-else class="empty-state">
          <p>No hay presupuestos registrados en el año {{ selectedYear }}.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-container { padding: 24px; }
.header-section {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px;
}

.gradient-text {
  font-size: 3rem;
  font-weight: 850;
  letter-spacing: -0.04em;
  background: linear-gradient(135deg, #0f172a 0%, #334155 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.5rem;
}

.subtitle { color: #64748b; font-size: 1.15rem; font-weight: 500; }

.header-controls {
  display: flex;
  align-items: center;
  gap: 24px;
}

.tab-switcher {
  display: flex;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 8px;
  gap: 4px;
}

.tab-switcher button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-switcher button.active {
  background: white;
  color: #0f172a;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.actions-bar {
  margin-bottom: 24px;
  display: flex;
  justify-content: flex-end;
}

.btn-clone-history {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  background: #0f172a;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 10px 15px -3px rgba(15, 23, 42, 0.2);
}

.btn-clone-history:hover {
  transform: translateY(-2px);
  background: #1e293b;
}

.year-selector {
  display: flex; align-items: center; gap: 16px;
  background: white; padding: 8px 16px; border-radius: 6px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
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

/* ── Animaciones de Entrada ── */
.animate-in {
  animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }

@keyframes slideUpFade {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
