<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import Card from '../components/common/Card.vue';
import OrderTable from '../components/orders/OrderTable.vue';
import { useAuthStore } from '@/stores/auth';
import { usePresupuestoStore } from '@/stores/presupuesto';
import { useOrderStore } from '@/stores/orders';

const authStore = useAuthStore();
const presupuestoStore = usePresupuestoStore();
const ordenStore = useOrderStore();

const rolUsuario = computed(() => authStore.user?.rol);
const selectedDept = ref<string>('Resumen Global');
const currentYear = new Date().getFullYear();
const selectedYear = ref<number>(currentYear);

const isAdminOrContable = computed(() => {
  const rol = authStore.user?.rol;
  return rol === 'Administrador' || rol === 'Contable';
});

async function refreshData() {
  const rol = authStore.user?.rol;
  const dep = authStore.user?.idDepartamento;

  if (rol === 'Administrador' || rol === 'Contable') {
    await presupuestoStore.getAllPresupuestos(selectedYear.value);
    await ordenStore.getAllOrders(selectedYear.value);
  } else if (dep) {
    await presupuestoStore.getPresupuestosByDept(dep, selectedYear.value);
    await ordenStore.getOrdersByDept(dep, selectedYear.value);
    
    if (presupuestoStore.presupuestos.length > 0) {
      selectedDept.value = presupuestoStore.presupuestos[0].nombredepartamento;
    }
  } 
}

watch(() => authStore.user, () => {
  refreshData();
}, { immediate: true });


const uniqueDepartments = computed(() => {
  const depts = presupuestoStore.presupuestos.map(p => p.nombredepartamento).filter(Boolean);
  const setDepts = [...new Set(depts)];
  if (isAdminOrContable.value) {
    return ['Resumen Global', ...setDepts];
  }
  return setDepts;
});


const filteredBudgets = computed(() => {
  if (selectedDept.value === 'Resumen Global') return presupuestoStore.presupuestos;
  return presupuestoStore.presupuestos.filter(p => p.nombredepartamento === selectedDept.value);
});

const calculateStats = (list: any[]) => {
  const total = list.reduce((acc, b) => {
    const val = b.cantidad || b.Cantidad || 0;
    return acc + Number(val);
  }, 0);
  const spent = list.reduce((acc, b) => {
    const val = b.gasto || b.Gasto || 0;
    return acc + Number(val);
  }, 0);
  const remaining = total - spent;
  return { total, spent, remaining };
};

const statsByType = computed(() => {
  const isInvestment = (p: any) => {
    const t = (p.type || p.Type || '').toLowerCase();
    return t === 'planinversion' || t.includes('inversion') || t.includes('plan');
  };

  const isPresupuesto = (p: any) => {
    const t = (p.type || p.Type || '').toLowerCase();
    // It's a budget if it's explicitly 'presupuesto' OR if it has NO type and isn't an investment
    return t === 'presupuesto' || t === 'ordinario' || (!t && !isInvestment(p));
  };

  const budgets = filteredBudgets.value.filter(isPresupuesto);
  const investments = filteredBudgets.value.filter(isInvestment);

  return {
    presupuesto: calculateStats(budgets),
    planInversion: calculateStats(investments),
    global: calculateStats(filteredBudgets.value)
  };
});

const viewTitle = computed(() => {
  if (selectedDept.value === 'Resumen Global') {
    return isAdminOrContable.value 
      ? 'Presupuestos: Todos los departamentos' 
      : 'Cargando presupuestos...';
  }
  return `Presupuestos: ${selectedDept.value}`;
});


const filteredOrdersTable = computed(() => {
  let orders = ordenStore.orders || [];
  
  if (selectedDept.value !== 'Resumen Global') {
    orders = orders.filter(o => {
      const deptName = o.nombredepartamento || o.nombreDepartamento || '';
      return deptName.toLowerCase() === selectedDept.value.toLowerCase();
    });
  }
  return orders;
});
</script>

<template>
  <div class="dashboard-page">
    <header class="dashboard-header">
      <div class="header-content">
        <h1>{{ viewTitle }}</h1>
        <p class="subtitle">Análisis detallado de recursos y ejecución financiera.</p>
      </div>
    </header>

    <!-- Floating Department Buttons - Only for Admins and Contables -->
    <div v-if="isAdminOrContable" class="dept-selector">

      <button 
        v-for="dept in uniqueDepartments" 
        :key="dept"
        class="dept-btn"
        :class="{ active: selectedDept === dept }"
        @click="selectedDept = dept"
      >
        {{ dept }}
      </button>
    </div>

    <!-- Presupuestos Summary Grid -->
    <section class="dashboard-section">
      <div class="section-header">
        <span class="material-symbols-outlined">analytics</span>
        <h2>Estado Financiero Actual</h2>
      </div>
      
      <div class="stats-summary-grid">
        <!-- 1. Presupuestos Summary -->
        <div class="premium-budget-card">
           <div class="budget-type-label">Ordinario</div>
           <div class="budget-main">
              <div class="budget-available">
                 <span class="amount">{{ statsByType.presupuesto.remaining.toLocaleString() }}</span>
                 <span class="currency">€</span>
              </div>
              <div class="budget-total">
                 <span class="sep">/</span>
                 <span class="total-val">{{ statsByType.presupuesto.total.toLocaleString() }}€</span>
              </div>
           </div>
           <div class="budget-footer">
              <div class="spent-tag">
                 <span class="label">Gastado</span>
                 <span class="val">{{ statsByType.presupuesto.spent.toLocaleString() }}€</span>
              </div>
           </div>
           <div class="progress-bar">
              <div class="progress-fill" :style="{ width: (statsByType.presupuesto.spent / statsByType.presupuesto.total * 100) + '%' }"></div>
           </div>
        </div>

        <!-- 2. Plan de Inversión Summary -->
        <div class="premium-budget-card investment">
           <div class="budget-type-label">Plan Inversión</div>
           <div class="budget-main">
              <div class="budget-available">
                 <span class="amount">{{ statsByType.planInversion.remaining.toLocaleString() }}</span>
                 <span class="currency">€</span>
              </div>
              <div class="budget-total">
                 <span class="sep">/</span>
                 <span class="total-val">{{ statsByType.planInversion.total.toLocaleString() }}€</span>
              </div>
           </div>
           <div class="budget-footer">
              <div class="spent-tag">
                 <span class="label">Ejecutado</span>
                 <span class="val">{{ statsByType.planInversion.spent.toLocaleString() }}€</span>
              </div>
           </div>
           <div class="progress-bar">
              <div class="progress-fill" :style="{ width: (statsByType.planInversion.spent / statsByType.planInversion.total * 100) + '%' }"></div>
           </div>
        </div>
      </div>
    </section>

    <!-- 4. Permanent Intel (Orders Table - Now Outside) -->
    <section class="dashboard-section">
      <div class="section-header">
        <span class="material-symbols-outlined">shopping_bag</span>
        <h2>{{ isAdminOrContable ? 'Registro Global de Órdenes' : 'Órdenes de Compra' }}</h2>

      </div>
      <div class="table-card">
        <div v-if="ordenStore.loading" class="loading-overlay">
          <div class="spinner"></div>
          <p>Sincronizando registros...</p>
        </div>
        <OrderTable
          v-else
          :orders="filteredOrdersTable"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.year-selector {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: white;
  border: 1px solid #e2e8f0;
  padding: 0.625rem 1.25rem;
  border-radius: 4px;
  color: #64748b;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.year-select {
  border: none;
  background: transparent;
  font-weight: 700;
  color: #0f172a;
  cursor: pointer;
  outline: none;
  font-size: 1rem;
}

.no-year-selected {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8rem 2rem;
  text-align: center;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  gap: 1.5rem;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
}

.large-icon {
  font-size: 6rem !important;
  color: #f1f5f9;
  background: #f8fafc;
  padding: 2rem;
  border-radius: 50%;
}

.no-year-selected h2 {
  font-size: 2rem;
  color: #0f172a;
  font-weight: 850;
  letter-spacing: -0.02em;
}

.no-year-selected p {
  color: #64748b;
  max-width: 600px;
  font-size: 1.2rem;
  line-height: 1.6;
}

.dashboard-page {
  padding: 2.5rem;
  max-width: 1600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h1 {
  font-size: 3rem;
  font-weight: 850;
  letter-spacing: -0.04em;
  background: linear-gradient(135deg, #0f172a 0%, #334155 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #64748b;
  font-size: 1.15rem;
  font-weight: 500;
}

.dept-selector {
  display: flex;
  gap: 1rem;
  overflow-x: auto;
  padding: 0.5rem;
  margin: -1rem 0;
  scrollbar-width: none;
}

.dept-selector::-webkit-scrollbar {
  display: none;
}

.dept-btn {
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
  background: white;
  color: #64748b;
  font-weight: 700;
  font-size: 0.95rem;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.dept-btn:hover {
  border-color: #cbd5e1;
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  color: #1e293b;
}

.dept-btn.active {
  background: #0f172a;
  color: white;
  border-color: #0f172a;
  box-shadow: 0 20px 25px -5px rgba(15, 23, 42, 0.2);
}

.btn-primary {
  background: #0f172a;
  color: white;
  padding: 0.875rem 1.75rem;
  border-radius: 4px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.15);
  background: #1e293b;
}

.dashboard-section {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.section-header .material-symbols-outlined {
  color: #0f172a;
  font-size: 2rem;
  padding: 0.625rem;
  background: #f1f5f9;
  border-radius: 4px;
}

.section-header h2 {
  font-size: 1.75rem;
  font-weight: 850;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 2rem;
}

.table-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  position: relative;
  min-height: 400px;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255,255,255,0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border-radius: 4px;
  backdrop-filter: blur(4px);
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #f1f5f9;
  border-top: 4px solid #0f172a;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}


.stats-summary-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

@media (max-width: 1024px) {
  .stats-summary-grid {
    grid-template-columns: 1fr;
  }
}

.budget-type-label {
  font-size: 0.875rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  opacity: 0.5;
  margin-bottom: -8px;
}

.premium-budget-card {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  border-radius: 4px;
  padding: 32px;
  color: white;
  position: relative;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(15, 23, 42, 0.2);
  display: flex;
  flex-direction: column;
  gap: 24px;
  border: 1px solid rgba(255,255,255,0.1);
  transition: transform 0.3s;
}

.premium-budget-card:hover {
  transform: translateY(-4px);
}

.premium-budget-card.investment {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
}

.budget-main {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.budget-available {
  display: flex;
  align-items: baseline;
}

.budget-available .amount {
  font-size: 4rem;
  font-weight: 850;
  letter-spacing: -2px;
  line-height: 1;
}

.budget-available .currency {
  font-size: 1.5rem;
  font-weight: 600;
  margin-left: 4px;
  opacity: 0.8;
}

.budget-total {
  display: flex;
  align-items: baseline;
  color: rgba(255,255,255,0.5);
  font-weight: 600;
}

.budget-total .sep {
  font-size: 2rem;
  margin-right: 8px;
}

.budget-total .total-val {
  font-size: 1.25rem;
}

.budget-footer {
  display: flex;
  justify-content: flex-end;
}

.spent-tag {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  background: rgba(255,255,255,0.05);
  padding: 10px 16px;
  border-radius: 4px;
  border: 1px solid rgba(255,255,255,0.1);
}

.spent-tag .label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  opacity: 0.6;
  font-weight: 700;
}

.spent-tag .val {
  font-size: 1.25rem;
  font-weight: 700;
  color: #fb7185; /* Soft red for spent */
}

.investment .spent-tag .val {
  color: #38bdf8; /* Soft blue for investment execution */
}

.progress-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 6px;
  background: rgba(255,255,255,0.1);
}

.progress-fill {
  height: 100%;
  background: #dc2626;
  transition: width 1s ease-out;
}

.investment .progress-fill {
  background: #3b82f6;
}

</style>
