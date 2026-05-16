<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue';
import { usePresupuestoStore } from '@/stores/presupuesto';
import { useAuthStore } from '@/stores/auth';
import Table from '@/components/common/Table.vue';
import { useToast } from 'vue-toastification';
 
const presupuestoStore = usePresupuestoStore();
const authStore = useAuthStore();
const toast = useToast();
 
const loading = ref(false);
const currentYear = ref(new Date().getFullYear());
 
// KPIs calculados desde el array de presupuestos del store
const stats = computed(() => {
  const list = presupuestoStore.presupuestos || [];
  
  const total = list.reduce((acc, p) => acc + (p.cantidad || 0), 0);
  const spent = list.reduce((acc, p) => acc + (p.gasto || 0), 0);
  const remaining = total - spent;
  const percent = total > 0 ? Math.round((spent / total) * 100) : 0;
 
  // Separación por tipo (asumiendo que p.type existe o p.nombrepresupuesto indica el tipo)
  const ordinarioTotal = list.filter(p => (p.type || '').toLowerCase().includes('ordinario') || (p.nombrepresupuesto || '').toLowerCase().includes('ordinario'))
                             .reduce((acc, p) => acc + (p.cantidad || 0), 0);
  const inversionTotal = list.filter(p => (p.type || '').toLowerCase().includes('inversion') || (p.nombrepresupuesto || '').toLowerCase().includes('inversión'))
                             .reduce((acc, p) => acc + (p.cantidad || 0), 0);
 
  return { total, spent, remaining, percent, ordinarioTotal, inversionTotal };
});
 
async function fetchData() {
  loading.value = true;
  try {
    const role = authStore.user?.rol;
    if (role === 'Administrador' || role === 'Contable') {
      await presupuestoStore.getAllPresupuestos(currentYear.value);
    } else if (authStore.user?.nombreDepartamento) {
      await presupuestoStore.getPresupuestosByDept(authStore.user.nombreDepartamento, currentYear.value);
    }
  } catch (error) {
    toast.error('Error al cargar presupuestos');
  } finally {
    loading.value = false;
  }
}
 
onMounted(fetchData);
 
// Si el usuario cambia (ej. login tardío), recargar
watch(() => authStore.user, (user) => {
  if (user) fetchData();
});
</script>
 
<template>
  <div class="view-page animate-in">
    <header class="dashboard-header">
      <div class="header-left">
        <h1>Análisis Presupuestario</h1>
        <p class="subtitle">Estado de ejecución y disponibilidad de fondos para el ejercicio {{ currentYear }}.</p>
      </div>
    </header>
 
    <!-- Bento Grid de KPIs -->
    <div class="bento-grid">
      <!-- Card Principal: Ejecución Total -->
      <div class="bento-card main-stat">
        <div class="card-content">
          <span class="card-label">Ejecución Global</span>
          <div class="main-value">
            <span class="number">{{ stats.percent }}</span>
            <span class="unit">%</span>
          </div>
          <div class="progress-bar-container">
            <div class="progress-bar-fill" :style="{ width: stats.percent + '%' }"></div>
          </div>
          <div class="card-footer-stats">
            <span>{{ stats.spent.toLocaleString() }}€ consumidos</span>
            <span>{{ stats.total.toLocaleString() }}€ total</span>
          </div>
        </div>
      </div>
 
      <!-- Card: Disponible -->
      <div class="bento-card secondary-stat">
        <div class="card-icon blue"><span class="material-symbols-outlined">account_balance_wallet</span></div>
        <div class="card-info">
          <span class="card-label">Disponible</span>
          <span class="card-value">{{ stats.remaining.toLocaleString() }}€</span>
        </div>
      </div>
 
      <!-- Card: Ordinario -->
      <div class="bento-card secondary-stat">
        <div class="card-icon green"><span class="material-symbols-outlined">receipt_long</span></div>
        <div class="card-info">
          <span class="card-label">P. Ordinario</span>
          <span class="card-value">{{ stats.ordinarioTotal.toLocaleString() }}€</span>
        </div>
      </div>
 
      <!-- Card: Inversión -->
      <div class="bento-card secondary-stat">
        <div class="card-icon purple"><span class="material-symbols-outlined">precision_manufacturing</span></div>
        <div class="card-info">
          <span class="card-label">P. Inversión</span>
          <span class="card-value">{{ stats.inversionTotal.toLocaleString() }}€</span>
        </div>
      </div>
    </div>
 
    <!-- Tabla de Desglose -->
    <section class="dashboard-section">
      <div class="section-header">
        <span class="material-symbols-outlined">list_alt</span>
        <h2>Desglose por Partidas</h2>
      </div>
 
      <Table 
        :loading="loading"
        :headers="['ID', 'Nombre', 'Asignado', 'Consumido', 'Disponibilidad']"
        :data="presupuestoStore.presupuestos.map(p => [
          p.idpresupuesto,
          p.nombrepresupuesto,
          p.cantidad + '€',
          p.gasto + '€',
          { 
            component: 'ProgressBar', 
            props: { value: p.cantidad > 0 ? (p.gasto/p.cantidad)*100 : 0, color: (p.gasto/p.cantidad) > 0.9 ? '#ef4444' : '#3b82f6' } 
          }
        ])"
      />
    </section>
  </div>
</template>
 
<style scoped>
.dashboard-header { margin-bottom: 3.5rem; }
.dashboard-header h1 { font-size: 2.75rem; font-weight: 850; color: #0f172a; letter-spacing: -0.04em; margin-bottom: 4px; }
.subtitle { color: #64748b; font-size: 1.15rem; }
 
/* Bento Grid Layout */
.bento-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  grid-template-rows: auto auto;
  gap: 1.5rem;
  margin-bottom: 4rem;
}
 
.bento-card {
  background: white;
  border-radius: 20px;
  padding: 1.75rem;
  border: 1px solid #e2e8f0;
  box-shadow: 0 10px 15px -3px rgba(0,0,0,0.02);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
 
.bento-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 25px -5px rgba(0,0,0,0.05);
  border-color: #cbd5e1;
}
 
.main-stat {
  grid-row: span 2;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
 
.main-stat .card-label { color: #94a3b8; }
.main-value { font-size: 5rem; font-weight: 900; line-height: 1; margin: 1rem 0; letter-spacing: -0.05em; }
.main-value .unit { font-size: 2rem; color: #ef4444; }
 
.progress-bar-container { width: 100%; height: 10px; background: rgba(255,255,255,0.1); border-radius: 5px; margin: 1.5rem 0; overflow: hidden; }
.progress-bar-fill { height: 100%; background: #ef4444; border-radius: 5px; transition: width 1s ease-out; }
.card-footer-stats { display: flex; justify-content: space-between; font-size: 0.9rem; font-weight: 600; color: #94a3b8; }
 
.secondary-stat { display: flex; align-items: center; gap: 1.5rem; }
.card-icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; }
.card-icon .material-symbols-outlined { font-size: 28px; }
.card-icon.blue { background: #eff6ff; color: #2563eb; }
.card-icon.green { background: #f0fdf4; color: #16a34a; }
.card-icon.purple { background: #faf5ff; color: #9333ea; }
 
.card-info { display: flex; flex-direction: column; }
.card-label { font-size: 0.85rem; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 4px; }
.card-value { font-size: 1.75rem; font-weight: 800; color: #0f172a; letter-spacing: -0.02em; }
 
.dashboard-section { margin-top: 2rem; }
.section-header { display: flex; align-items: center; gap: 12px; margin-bottom: 2rem; }
.section-header h2 { font-size: 1.75rem; font-weight: 800; color: #0f172a; letter-spacing: -0.02em; }
.section-header .material-symbols-outlined { color: #ef4444; font-size: 28px; }
 
@media (max-width: 1024px) {
  .bento-grid { grid-template-columns: 1fr 1fr; }
  .main-stat { grid-column: span 2; }
}
</style>
