<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
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

onMounted(async () => {
  const rol = authStore.user?.rol;
  const dep = authStore.user?.idDepartamento;

  if (rol === 'Administrador') {
    await presupuestoStore.getAllPresupuestos();
    await ordenStore.getAllOrders();
  } else if (dep) {
    await presupuestoStore.getPresupuestosByDept(dep);
    await ordenStore.getOrdersByDept(dep);
    
    // Auto-select the user's department if available
    if (presupuestoStore.presupuestos.length > 0) {
      selectedDept.value = presupuestoStore.presupuestos[0].nombredepartamento;
    }
  }
});

const uniqueDepartments = computed(() => {
  const depts = presupuestoStore.presupuestos.map(p => p.nombredepartamento).filter(Boolean);
  return ['Resumen Global', ...new Set(depts)];
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
  console.log('Stats calculation - Data:', filteredBudgets.value);
  
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

  console.log('Resulting Groups - Budgets:', budgets.length, 'Investments:', investments.length);

  return {
    presupuesto: calculateStats(budgets),
    planInversion: calculateStats(investments),
    global: calculateStats(filteredBudgets.value)
  };
});

const viewTitle = computed(() => {
  if (selectedDept.value === 'Resumen Global') return 'Presupuestos: Todos los departamentos';
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
    <header class="dashboard-header animate-fade-in">
      <div class="header-content">
        <h1>{{ viewTitle }}</h1>
        <p class="subtitle">Análisis detallado de recursos y ejecución financiera.</p>
      </div>
    </header>

    <!-- Floating Department Buttons -->
    <div class="dept-selector animate-fade-in" style="animation-delay: 0.1s">
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

    <!-- 1. Presupuestos Summary -->
    <section class="dashboard-section animate-fade-in" style="animation-delay: 0.2s">
      <div class="section-header">
        <span class="material-symbols-outlined">payments</span>
        <h2>Presupuestos Ordinarios</h2>
      </div>
      <div class="statistics-grid">
        <Card
          type="stats"
          title="Asignado"
          :data="statsByType.presupuesto.total"
          suffix="€"
          icon="account_balance"
          background="linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)"
        />
        <Card
          type="stats"
          title="Gastado"
          :data="statsByType.presupuesto.spent"
          suffix="€"
          icon="shopping_cart"
          background="linear-gradient(135deg, #f43f5e 0%, #e11d48 100%)"
        />
        <Card
          type="stats"
          title="Disponible"
          :data="statsByType.presupuesto.remaining"
          suffix="€"
          icon="account_balance_wallet"
          background="linear-gradient(135deg, #10b981 0%, #059669 100%)"
        />
      </div>
    </section>

    <!-- 2. Plan de Inversión Summary -->
    <section class="dashboard-section animate-fade-in" style="animation-delay: 0.3s">
      <div class="section-header">
        <span class="material-symbols-outlined">trending_up</span>
        <h2>Planes de Inversión</h2>
      </div>
      <div class="statistics-grid">
        <Card
          type="stats"
          title="Inversión Prevista"
          :data="statsByType.planInversion.total"
          suffix="€"
          icon="rocket_launch"
          background="linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)"
        />
        <Card
          type="stats"
          title="Inversión Ejecutada"
          :data="statsByType.planInversion.spent"
          suffix="€"
          icon="task_alt"
          background="linear-gradient(135deg, #f59e0b 0%, #d97706 100%)"
        />
        <Card
          type="stats"
          title="Pendiente"
          :data="statsByType.planInversion.remaining"
          suffix="€"
          icon="hourglass_empty"
          background="linear-gradient(135deg, #64748b 0%, #475569 100%)"
        />
      </div>
    </section>

    <!-- 4. Permanent Intel (Orders Table - Now Outside) -->
    <section class="dashboard-section animate-fade-in" style="animation-delay: 0.6s">
      <div class="section-header">
        <span class="material-symbols-outlined">shopping_bag</span>
        <h2>{{ rolUsuario === 'Administrador' ? 'Registro Global de Órdenes' : 'Órdenes de Compra' }}</h2>
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
  border-radius: 100px;
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
  border-radius: 16px;
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
  border-radius: 14px;
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
  border: 1px solid #f1f5f9;
  border-radius: 28px;
  padding: 2rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.035);
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
  border-radius: 28px;
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

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in {
  animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>
