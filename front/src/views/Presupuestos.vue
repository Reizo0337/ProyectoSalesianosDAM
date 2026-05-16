<script setup lang="ts">
import { onMounted, watch, ref, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { usePresupuestoStore } from '@/stores/presupuesto';
import { useToast } from 'vue-toastification';
import Table from '../components/common/Table.vue';
import Card from '../components/common/Card.vue';

const toast = useToast();
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
  try {
    if (user) {
      if (user.rol === 'Administrador' || user.rol === 'Contable') {
        await presupuestoStore.getAllPresupuestos(selectedYear.value);
      } else if (user.idDepartamento) {
        await presupuestoStore.getPresupuestosByDept(user.idDepartamento, selectedYear.value);
      }
    }
  } catch (err) {
    toast.error('Error al sincronizar presupuestos');
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
    </div>    <!-- Modern Bento KPI Grid -->
    <div class="bento-grid animate-in delay-1">
      <div class="bento-card bento-card--primary">
        <div class="bento-icon"><span class="material-symbols-outlined">payments</span></div>
        <div class="bento-info">
          <span class="bento-label">Presupuesto Total</span>
          <span class="bento-value">{{ stats.global.total.toLocaleString() }}€</span>
        </div>
      </div>

      <div class="bento-card bento-card--success">
        <div class="bento-icon"><span class="material-symbols-outlined">savings</span></div>
        <div class="bento-info">
          <span class="bento-label">Disponible Total</span>
          <span class="bento-value">{{ stats.global.remaining.toLocaleString() }}€</span>
        </div>
      </div>

      <div class="bento-card">
        <div class="bento-icon"><span class="material-symbols-outlined">receipt_long</span></div>
        <div class="bento-info">
          <span class="bento-label">Gasto Ordinario</span>
          <span class="bento-value">{{ stats.ordinarios.spent.toLocaleString() }}€</span>
        </div>
        <div class="bento-progress">
          <div class="bento-progress-fill" :style="{ width: (stats.ordinarios.spent / stats.ordinarios.total * 100) + '%' }"></div>
        </div>
      </div>

      <div class="bento-card">
        <div class="bento-icon"><span class="material-symbols-outlined">rocket_launch</span></div>
        <div class="bento-info">
          <span class="bento-label">Plan Inversión</span>
          <span class="bento-value">{{ stats.inversiones.spent.toLocaleString() }}€</span>
        </div>
        <div class="bento-progress">
          <div class="bento-progress-fill bento-progress-fill--alt" :style="{ width: (stats.inversiones.spent / stats.inversiones.total * 100) + '%' }"></div>
        </div>
      </div>
    </div>

    <div class="table-card animate-in delay-2">
      <div class="table-header-custom">
        <span class="material-symbols-outlined">list_alt</span>
        <h2>Listado Detallado</h2>
      </div>
      <Table
        v-if="filteredPresupuestos.length > 0"
        :headers="['Código', 'Nombre', 'Departamento', 'Cantidad', 'Disponible', 'Ejecución']"
        :data="filteredPresupuestos.map(p => ({
          'Código': p.codigo || p.Codigo,
          'Nombre': p.nombrepresupuesto || p.nombrePresupuesto,
          'Departamento': p.nombredepartamento || p.nombreDepartamento,
          'Cantidad': (p.cantidad || p.Cantidad).toLocaleString() + '€',
          'Disponible': (p.cantidad - p.gasto).toLocaleString() + '€',
          'Ejecución': {
            component: 'ProgressBar',
            props: { value: (p.gasto / p.cantidad * 100), color: isInvestment(p) ? '#3b82f6' : '#ef4444' }
          }
        }))"
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
.bento-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.5rem;
}

.bento-card {
  background: white;
  padding: 1.75rem;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 160px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.bento-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  border-color: #cbd5e1;
}

.bento-card--primary {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  color: white;
  border: none;
}

.bento-card--success {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  border-color: #bbf7d0;
}

.bento-icon {
  width: 48px;
  height: 48px;
  background: #f8fafc;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  margin-bottom: 1rem;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.05);
}

.bento-card--primary .bento-icon {
  background: rgba(255, 255, 255, 0.1);
  color: #38bdf8;
  backdrop-filter: blur(4px);
}

.bento-card--success .bento-icon {
  background: white;
  color: #16a34a;
  box-shadow: 0 4px 10px rgba(22, 163, 74, 0.15);
}

.bento-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.bento-label {
  font-size: 0.75rem;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.bento-card--primary .bento-label {
  color: rgba(255, 255, 255, 0.5);
}

.bento-value {
  font-size: 1.85rem;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.bento-card--primary .bento-value {
  color: white;
}

.bento-card--success .bento-value {
  color: #14532d;
}

.bento-progress {
  height: 6px;
  background: #f1f5f9;
  border-radius: 10px;
  margin-top: 1.25rem;
  overflow: hidden;
}

.bento-card--primary .bento-progress {
  background: rgba(255,255,255,0.1);
}

.bento-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ef4444, #f87171);
  border-radius: 10px;
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.bento-progress-fill--alt {
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
}

/* ── Animaciones ── */
.animate-in {
  animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }

@keyframes slideUpFade {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
