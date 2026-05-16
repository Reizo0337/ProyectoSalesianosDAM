<script setup lang="ts">
import { onMounted, watch, ref, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { usePresupuestoStore } from '@/stores/presupuesto';
import Table from '../components/common/Table.vue';
import Card from '../components/common/Card.vue';

const authStore = useAuthStore();
const presupuestoStore = usePresupuestoStore();

const searchQuery = ref('');
const selectedYear = ref<number>(new Date().getFullYear());

const headers = ['ID', 'Código', 'Nombre', 'Tipo', 'Cantidad', 'Gasto', 'Departamento', ];

const isInvestment = (p: any) => {
  const t = (p.type || p.Type || '').toLowerCase();
  return t === 'planinversion' || t.includes('inversion') || t.includes('plan');
};

const formatType = (p: any) => {
  if (isInvestment(p)) return 'Plan Inversión';
  return 'Presupuesto Ordinario';
};

const fetchData = async () => {
  const user = authStore.user;
  if (user) {
    if (user.rol === 'Administrador' || user.rol === 'Contable') {
      await presupuestoStore.getAllPresupuestos(selectedYear.value);
    } else if (user.idDepartamento) {
      await presupuestoStore.getPresupuestosByDept(user.idDepartamento, selectedYear.value);
    }
  }
};

watch(() => authStore.user, () => {
  fetchData();
}, { immediate: true });

const filteredPresupuestos = computed(() => {
  const all = presupuestoStore.presupuestos;
  if (!searchQuery.value.trim()) return all;
  
  const q = searchQuery.value.toLowerCase();
  return all.filter(p => {
    const id = String(p.idpresupuesto || p.idPresupuesto || '');
    const cod = String(p.codigo || p.Codigo || '').toLowerCase();
    const nom = String(p.nombrepresupuesto || p.nombrePresupuesto || '').toLowerCase();
    const type = String(p.type || p.Type || '').toLowerCase();
    const dep = String(p.nombredepartamento || p.nombreDepartamento || '').toLowerCase();
    
    return id.includes(q) || cod.includes(q) || nom.includes(q) || type.includes(q) || dep.includes(q);
  });
});

const stats = computed(() => {
  const all = filteredPresupuestos.value;
  
  const calculate = (list: any[]) => {
    const total = list.reduce((acc, p) => acc + Number(p.cantidad || p.Cantidad || 0), 0);
    const spent = list.reduce((acc, p) => acc + Number(p.gasto || p.Gasto || 0), 0);
    return { total, spent, remaining: total - spent };
  };

  const ordinarios = all.filter(p => !isInvestment(p));
  const inversiones = all.filter(p => isInvestment(p));

  return {
    global: calculate(all),
    ordinarios: calculate(ordinarios),
    inversiones: calculate(inversiones),
    countInv: inversiones.length,
    countOrd: ordinarios.length
  };
});

</script>

<template>
  <div class="view-container">
    <div class="header-section">
      <div class="title-box">
        <h1>Gestión de Presupuestos</h1>
        <p>Control y seguimiento de asignaciones presupuestarias y planes de inversión.</p>
      </div>
      <div class="header-actions">
        <div class="search-box">
          <span class="material-symbols-outlined search-icon">search</span>
          <input v-model="searchQuery" type="text" placeholder="Buscar presupuesto..." class="search-input" />
        </div>
      </div>
    </div>

    <!-- Quick Stats Summary -->
    <div class="stats-summary-grid">
      <div class="premium-budget-card">
         <div class="budget-main">
            <div class="budget-available">
               <span class="amount">{{ stats.ordinarios.remaining.toLocaleString() }}</span>
               <span class="currency">€</span>
            </div>
            <div class="budget-total">
               <span class="sep">/</span>
               <span class="total-val">{{ stats.ordinarios.total.toLocaleString() }}€</span>
            </div>
         </div>
         <div class="budget-footer">
            <div class="spent-tag">
               <span class="label">Gastado</span>
               <span class="val">{{ stats.ordinarios.spent.toLocaleString() }}€</span>
            </div>
         </div>
         <div class="progress-bar">
            <div class="progress-fill" :style="{ width: (stats.ordinarios.spent / stats.ordinarios.total * 100) + '%' }"></div>
         </div>
      </div>

      <div class="premium-budget-card investment">
         <div class="budget-main">
            <div class="budget-available">
               <span class="amount">{{ stats.inversiones.remaining.toLocaleString() }}</span>
               <span class="currency">€</span>
            </div>
            <div class="budget-total">
               <span class="sep">/</span>
               <span class="total-val">{{ stats.inversiones.total.toLocaleString() }}€</span>
            </div>
         </div>
         <div class="budget-footer">
            <div class="spent-tag">
               <span class="label">Ejecutado</span>
               <span class="val">{{ stats.inversiones.spent.toLocaleString() }}€</span>
            </div>
         </div>
         <div class="progress-bar">
            <div class="progress-fill" :style="{ width: (stats.inversiones.spent / stats.inversiones.total * 100) + '%' }"></div>
         </div>
      </div>
    </div>

    <div class="table-card">
      <div class="table-header-custom">
        <span class="material-symbols-outlined">list_alt</span>
        <h2>Listado Detallado</h2>
      </div>
      <Table
        v-if="filteredPresupuestos.length > 0"
        :headers="headers"
        :data="filteredPresupuestos.map(p => [
          p.idpresupuesto || p.idPresupuesto,
          p.codigo || p.Codigo,
          p.nombrepresupuesto || p.nombrePresupuesto,
          formatType(p),
          (p.cantidad || p.Cantidad) + '€',
          (p.gasto || p.Gasto) + '€',
          p.nombredepartamento || p.nombreDepartamento
        ])"
        :searchable="false"
      />
      <div v-else class="empty-state">
        <span class="material-symbols-outlined">search_off</span>
        <p>No se encontraron presupuestos que coincidan con los criterios.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.year-selector {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  color: #64748b;
}

.year-select {
  border: none;
  background: transparent;
  font-weight: 700;
  color: #0f172a;
  cursor: pointer;
  outline: none;
}

.btn-clone {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  color: #0f172a;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-clone:hover {
  background: #e2e8f0;
}

.no-year-selected {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10rem 2rem;
  text-align: center;
  background: #f8fafc;
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  gap: 1.5rem;
}

.large-icon {
  font-size: 5rem !important;
  color: #cbd5e1;
}

.no-year-selected h2 {
  font-size: 1.75rem;
  color: #1e293b;
  font-weight: 800;
}

.no-year-selected p {
  color: #64748b;
  max-width: 500px;
  font-size: 1.1rem;
}

.btn-primary {
  background: #0f172a;
  color: white;
  padding: 0.875rem 2rem;
  border-radius: 4px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.view-container {
  padding: 2.5rem;
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
  max-width: 1600px;
  margin: 0 auto;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.title-box h1 {
  font-size: 2.5rem;
  font-weight: 850;
  letter-spacing: -0.03em;
  color: #0f172a;
  margin-bottom: 0.5rem;
}

.title-box p {
  color: #64748b;
  font-size: 1.1rem;
}

.search-box {
  display: flex;
  align-items: center;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 0 1rem;
  width: 400px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  transition: all 0.2s;
}

.search-box:focus-within {
  border-color: #0f172a;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
}

.search-icon {
  color: #94a3b8;
  margin-right: 0.75rem;
}

.search-input {
  border: none;
  outline: none;
  padding: 0.75rem 0;
  width: 100%;
  font-size: 0.95rem;
}

.stats-summary-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.stat-group {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.group-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 700;
  color: #475569;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.group-label .material-symbols-outlined {
  font-size: 1.25rem;
}

.cards-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
}

.table-card {
  background: white;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  overflow: hidden;
}

.table-header-custom {
  padding: 1.5rem;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.table-header-custom h2 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
}

.table-header-custom .material-symbols-outlined {
  color: #64748b;
}

.empty-state {
  padding: 5rem;
  text-align: center;
  color: #94a3b8;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.empty-state .material-symbols-outlined {
  font-size: 3rem;
  opacity: 0.5;
}

@media (max-width: 1200px) {
  .stats-summary-grid {
    grid-template-columns: 1fr;
  }
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
  color: #fb7185;
}

.investment .spent-tag .val {
  color: #38bdf8;
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
