<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue';
import { usePresupuestoStore } from '@/stores/presupuesto';
import { useAuthStore } from '@/stores/auth';
import Table from '@/components/common/Table.vue';
 
const presupuestoStore = usePresupuestoStore();
const authStore = useAuthStore();
const loading = ref(true);
const selectedYear = ref(new Date().getFullYear());
 
async function loadData() {
  loading.value = true;
  try {
    const role = authStore.user?.rol;
    const dept = authStore.user?.nombreDepartamento;
    
    if (role === 'Administrador' || role === 'Contable') {
      await presupuestoStore.getAllPresupuestos(selectedYear.value);
    } else if (dept) {
      await presupuestoStore.getPresupuestosByDept(dept, selectedYear.value);
    }
  } finally {
    loading.value = false;
  }
}
 
const stats = computed(() => {
  const list = presupuestoStore.presupuestos || [];
  
  const total = list.reduce((acc, p) => acc + Number(p.cantidad || 0), 0);
  const spent = list.reduce((acc, p) => acc + Number(p.gasto || 0), 0);
  const available = total - spent;
  const percent = total > 0 ? Math.round((spent / total) * 100) : 0;
 
  const ordinario = list
    .filter(p => !p.type?.toLowerCase().includes('inversion'))
    .reduce((acc, p) => acc + Number(p.cantidad || 0), 0);
    
  const inversion = list
    .filter(p => p.type?.toLowerCase().includes('inversion'))
    .reduce((acc, p) => acc + Number(p.cantidad || 0), 0);
 
  return { total, spent, available, percent, ordinario, inversion };
});
 
onMounted(loadData);
watch(selectedYear, loadData);
</script>
 
<template>
  <div class="view-page animate-in">
    <header class="dashboard-header">
      <div class="header-left">
        <h1>Análisis Presupuestario</h1>
        <p class="subtitle">Estado de ejecución y disponibilidad de fondos para el ejercicio {{ selectedYear }}.</p>
      </div>
      <div class="header-actions">
        <div class="year-selector">
          <span class="material-symbols-outlined">calendar_today</span>
          <select v-model="selectedYear" class="year-select">
            <option v-for="y in [2024, 2025, 2026]" :key="y" :value="y">{{ y }}</option>
          </select>
        </div>
      </div>
    </header>
 
    <!-- KPIs Premium Section -->
    <div class="stats-container">
      <div class="main-stat-card">
        <div class="card-content">
          <div class="stat-label">Ejecución Global</div>
          <div class="stat-value">{{ stats.percent }}%</div>
          <div class="progress-container">
            <div class="progress-bar" :style="{ width: stats.percent + '%' }"></div>
          </div>
          <div class="stat-footer">
            <span><strong>{{ stats.spent.toLocaleString() }}€</strong> consumidos</span>
            <span><strong>{{ stats.total.toLocaleString() }}€</strong> total</span>
          </div>
        </div>
        <div class="card-bg-icon">
          <span class="material-symbols-outlined">analytics</span>
        </div>
      </div>
 
      <div class="side-stats">
        <div class="mini-card available">
          <div class="mini-icon"><span class="material-symbols-outlined">account_balance_wallet</span></div>
          <div class="mini-info">
            <span class="mini-label">Disponible</span>
            <span class="mini-value">{{ stats.available.toLocaleString() }}€</span>
          </div>
        </div>
        <div class="mini-card ordinario">
          <div class="mini-icon"><span class="material-symbols-outlined">receipt_long</span></div>
          <div class="mini-info">
            <span class="mini-label">P. Ordinario</span>
            <span class="mini-value">{{ stats.ordinario.toLocaleString() }}€</span>
          </div>
        </div>
        <div class="mini-card inversion">
          <div class="mini-icon"><span class="material-symbols-outlined">precision_manufacturing</span></div>
          <div class="mini-info">
            <span class="mini-label">P. Inversión</span>
            <span class="mini-value">{{ stats.inversion.toLocaleString() }}€</span>
          </div>
        </div>
      </div>
    </div>
 
    <!-- Listado Detallado -->
    <section class="dashboard-section">
      <div class="section-header">
        <span class="material-symbols-outlined">list_alt</span>
        <h2>Desglose por Partidas</h2>
      </div>
      <Table 
        :loading="loading"
        :headers="['Departamento', 'Tipo', 'Asignado', 'Gastado', 'Ejecución']"
        :data="presupuestoStore.presupuestos.map(p => [
          p.nombredepartamento,
          p.type?.toUpperCase() || 'ORDINARIO',
          Number(p.cantidad).toLocaleString() + '€',
          Number(p.gasto).toLocaleString() + '€',
          { 
            component: 'ProgressBar', 
            props: { 
              value: (Number(p.gasto) / Number(p.cantidad) * 100) || 0,
              color: p.type?.toLowerCase().includes('inversion') ? '#8b5cf6' : '#ef4444'
            } 
          }
        ])"
      />
    </section>
  </div>
</template>
 
<style scoped>
.dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
.header-left h1 { font-size: 2.75rem; font-weight: 850; color: #0f172a; letter-spacing: -0.04em; margin-bottom: 4px; }
.subtitle { color: #64748b; font-size: 1.15rem; }
 
.year-selector {
  display: flex; align-items: center; gap: 8px; background: white; padding: 10px 16px;
  border-radius: 8px; border: 1px solid #e2e8f0; color: #64748b;
}
.year-select { border: none; outline: none; font-weight: 700; color: #0f172a; cursor: pointer; }
 
/* KPIs Design */
.stats-container { display: grid; grid-template-columns: 1.5fr 1fr; gap: 2rem; margin-bottom: 4rem; }
 
.main-stat-card {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  border-radius: 20px; padding: 3rem; color: white; position: relative; overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(15, 23, 42, 0.2);
}
 
.stat-label { font-size: 0.9rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.1em; opacity: 0.6; margin-bottom: 1rem; }
.stat-value { font-size: 5rem; font-weight: 900; letter-spacing: -4px; line-height: 1; margin-bottom: 2rem; }
.progress-container { height: 12px; background: rgba(255,255,255,0.1); border-radius: 6px; overflow: hidden; margin-bottom: 1.5rem; }
.progress-bar { height: 100%; background: #ef4444; border-radius: 6px; transition: width 1s cubic-bezier(0.4, 0, 0.2, 1); }
.stat-footer { display: flex; justify-content: space-between; font-size: 1rem; color: rgba(255,255,255,0.6); }
.stat-footer strong { color: white; }
 
.card-bg-icon { position: absolute; right: -20px; top: -20px; opacity: 0.05; }
.card-bg-icon .material-symbols-outlined { font-size: 15rem; }
 
.side-stats { display: flex; flex-direction: column; gap: 1rem; }
.mini-card {
  background: white; border-radius: 16px; padding: 1.5rem; display: flex; align-items: center; gap: 1.5rem;
  border: 1px solid #e2e8f0; transition: all 0.2s;
}
.mini-card:hover { transform: translateX(8px); border-color: #cbd5e1; }
.mini-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.mini-icon .material-symbols-outlined { font-size: 24px; }
 
.available .mini-icon { background: #eff6ff; color: #3b82f6; }
.ordinario .mini-icon { background: #f0fdf4; color: #16a34a; }
.inversion .mini-icon { background: #f5f3ff; color: #8b5cf6; }
 
.mini-label { display: block; font-size: 0.75rem; font-weight: 700; color: #64748b; text-transform: uppercase; margin-bottom: 2px; }
.mini-value { font-size: 1.5rem; font-weight: 800; color: #0f172a; }
 
.dashboard-section { margin-top: 2rem; }
.section-header { display: flex; align-items: center; gap: 12px; margin-bottom: 2rem; }
.section-header h2 { font-size: 1.75rem; font-weight: 800; color: #0f172a; }
</style>
