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
  const dep = authStore.user?.nombreDepartamento;

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

// ── Lógica de Saludo Dinámico ──
const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 12) return 'Buenos días';
  if (hour < 20) return 'Buenas tardes';
  return 'Buenas noches';
});

const quickActions = computed(() => {
  const actions = [
    { label: 'Nueva Orden', icon: 'add_shopping_cart', to: '/ordenes', color: '#ef4444' },
  ];
  
  // "Mi Presupuesto" solo tiene sentido para usuarios que no son Admins/Contables 
  // (aunque en esta app el jefe de equipo es el que más lo usa)
  if (!isAdminOrContable.value) {
    actions.push({ label: 'Mi Presupuesto', icon: 'account_balance_wallet', to: '#stats', color: '#3b82f6' });
  }

  if (isAdminOrContable.value) {
    actions.push({ label: 'Usuarios', icon: 'group', to: '/usuarios', color: '#8b5cf6' });
  }
  return actions;
});

// ── Lógica de KPIs ──
const kpis = computed(() => {
  const orders = ordenStore.orders || [];
  const pendingCount = orders.filter(o => (o.estado || '').toLowerCase() !== 'cerrada').length;
  const noInvoicesCount = orders.filter(o => parseInt(o.numfacturas || '0') === 0).length;
  
  const totalBudget = statsByType.value.presupuesto.total + statsByType.value.planInversion.total;
  const totalSpent = statsByType.value.presupuesto.spent + statsByType.value.planInversion.spent;
  const executionRate = totalBudget > 0 ? Math.round((totalSpent / totalBudget) * 100) : 0;

  return [
    { label: 'Órdenes Activas', value: pendingCount, icon: 'inventory_2', color: '#ef4444' },
    { label: 'Faltan Facturas', value: noInvoicesCount, icon: 'description', color: '#f59e0b' },
    { label: 'Ejecución Global', value: executionRate + '%', icon: 'analytics', color: '#10b981' }
  ];
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

// ── Lógica de Animación de Números (Count Up) ──
const animatedStats = ref({
  presupuesto: { remaining: 0, total: 0, spent: 0 },
  planInversion: { remaining: 0, total: 0, spent: 0 }
});

function animateValue(parent: any, key: string, target: number) {
  const start = parent[key];
  const duration = 1200; // Duración en ms
  const startTime = performance.now();

  function update(currentTime: number) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    
    // Función de easing (easeOutQuart) para que frene suavemente al final
    const ease = 1 - Math.pow(1 - progress, 4);
    
    parent[key] = start + (target - start) * ease;

    if (progress < 1) {
      requestAnimationFrame(update);
    }
  }
  requestAnimationFrame(update);
}

// Observamos statsByType para disparar la animación cuando cambien los datos
watch(statsByType, (newVal) => {
  animateValue(animatedStats.value.presupuesto, 'remaining', newVal.presupuesto.remaining);
  animateValue(animatedStats.value.presupuesto, 'spent', newVal.presupuesto.spent);
  
  animateValue(animatedStats.value.planInversion, 'remaining', newVal.planInversion.remaining);
  animateValue(animatedStats.value.planInversion, 'spent', newVal.planInversion.spent);
}, { deep: true, immediate: true });

</script>

<template>
  <div class="dashboard-page">
    <header class="dashboard-header animate-in">
      <!-- Row 1: Greeting -->
      <div class="header-top-row">
        <h2 class="dynamic-greeting">{{ greeting }}, <span>{{ authStore.user?.nombre }}</span></h2>
      </div>

      <!-- Row 2: Title + Actions/KPIs -->
      <div class="header-main-row">
        <div class="header-title-area">
          <h1>{{ viewTitle }}</h1>
          <p class="subtitle">Análisis detallado de recursos y ejecución financiera.</p>
        </div>
        
        <div class="header-actions-area">
          <div class="quick-actions-grid">
            <RouterLink 
              v-for="action in quickActions" 
              :key="action.label" 
              :to="action.to" 
              class="action-card"
              :style="{ '--accent': action.color }"
            >
              <span class="material-symbols-outlined">{{ action.icon }}</span>
              <span class="action-label">{{ action.label }}</span>
            </RouterLink>
          </div>
          <div class="kpi-grid-header">
            <div v-for="kpi in kpis" :key="kpi.label" class="kpi-item-mini">
              <span class="kpi-dot" :style="{ backgroundColor: kpi.color }"></span>
              <span class="kpi-val-mini">{{ kpi.value }}</span>
              <span class="kpi-lab-mini">{{ kpi.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <div v-if="isAdminOrContable" class="dept-selector animate-in delay-1">
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
    <section class="dashboard-section animate-in delay-2">
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
                 <span class="amount">{{ Math.round(animatedStats.presupuesto.remaining).toLocaleString() }}</span>
                 <span class="currency">€</span>
              </div>
              <div class="budget-total">
                 <span class="sep">/</span>
                 <span class="total-val">{{ Math.round(statsByType.presupuesto.total).toLocaleString() }}€</span>
              </div>
           </div>
           <div class="budget-footer">
              <div class="spent-tag">
                 <span class="label">Gastado</span>
                 <span class="val">{{ Math.round(animatedStats.presupuesto.spent).toLocaleString() }}€</span>
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
                 <span class="amount">{{ Math.round(animatedStats.planInversion.remaining).toLocaleString() }}</span>
                 <span class="currency">€</span>
              </div>
              <div class="budget-total">
                 <span class="sep">/</span>
                 <span class="total-val">{{ Math.round(statsByType.planInversion.total).toLocaleString() }}€</span>
              </div>
           </div>
           <div class="budget-footer">
              <div class="spent-tag">
                 <span class="label">Ejecutado</span>
                 <span class="val">{{ Math.round(animatedStats.planInversion.spent).toLocaleString() }}€</span>
              </div>
           </div>
           <div class="progress-bar">
              <div class="progress-fill" :style="{ width: (statsByType.planInversion.spent / statsByType.planInversion.total * 100) + '%' }"></div>
           </div>
        </div>
      </div>
    </section>

    <!-- 4. Permanent Intel (Orders Table - Now Outside) -->
    <section class="dashboard-section animate-in delay-3">
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
  max-width: 1600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

.dashboard-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 2rem;
}
 
.header-main-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
 
.header-title-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
 
.header-actions-area {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}
 
.header-title-area h1 {
  font-size: 2.75rem;
  font-weight: 850;
  color: #0f172a;
  letter-spacing: -0.04em;
  margin: 0;
}
 
.subtitle {
  color: #64748b;
  font-size: 1.15rem;
  font-weight: 500;
  margin: 0;
}
 
.dynamic-greeting {
  font-size: 1rem;
  font-weight: 700;
  color: #ef4444;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin: 0;
}

.dynamic-greeting span {
  color: #1e293b;
}

/* ── Quick Actions Grid ── */
.quick-actions-grid {
  display: flex;
  gap: 12px;
}
 
.action-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  padding: 10px 20px;
  border-radius: 8px;
  text-decoration: none;
  color: #1e293b;
  font-weight: 700;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
 
.action-card .material-symbols-outlined {
  color: var(--accent);
  font-size: 22px;
}
 
.action-card:hover {
  transform: translateY(-2px);
  border-color: var(--accent);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  background: #f8fafc;
}

.dept-selector {
  display: flex;
  gap: 1.25rem;
  overflow-x: auto;
  padding: 1.5rem 0.5rem; /* Aumentado padding vertical para evitar corte de sombras */
  margin: -1.5rem 0;
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

/* ── KPI Grid ── */
/* ── Header KPIs ── */
.kpi-grid-header {
  display: flex;
  gap: 1.25rem;
  padding-right: 4px;
}

.kpi-item-mini {
  display: flex;
  align-items: center;
  gap: 6px;
  opacity: 0.9;
}

.kpi-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.kpi-val-mini {
  font-size: 1.1rem;
  font-weight: 850;
  color: #1e293b;
}

.kpi-lab-mini {
  font-size: 0.7rem;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
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

/* ── Animaciones de Entrada ── */
.animate-in {
  animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }
.delay-3 { animation-delay: 0.3s; }

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
