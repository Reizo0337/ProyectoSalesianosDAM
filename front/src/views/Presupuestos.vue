<script setup lang="ts">
import { onMounted, watch, ref, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { usePresupuestoStore } from '@/stores/presupuesto';
import Table from '../components/common/Table.vue';

const authStore = useAuthStore();
const presupuestoStore = usePresupuestoStore();

const searchQuery = ref('');

const headers = ['ID', 'Código', 'Nombre', 'Tipo', 'Cantidad', 'Gasto', 'Departamento', ];

const formatType = (type: string) => {
  const t = (type || '').toLowerCase();
  if (t === 'planinversion' || t.includes('inversion') || t.includes('plan')) {
    return 'Plan Inversión';
  }
  return 'Presupuesto';
};

watch(() => authStore.user, (user) => {
  if (user) {
    if (user.rol === 'Administrador' || user.rol === 'Contable') {
      presupuestoStore.getAllPresupuestos();
    } else if (user.idDepartamento) {
      presupuestoStore.getPresupuestosByDept(user.idDepartamento);
    }
  }
}, { immediate: true });

const filteredPresupuestos = computed(() => {
  if (!searchQuery.value.trim()) return presupuestoStore.presupuestos;
  const q = searchQuery.value.toLowerCase();
  return presupuestoStore.presupuestos.filter(p => 
    String(p.idpresupuesto).includes(q) ||
    (p.codigo || '').toLowerCase().includes(q) ||
    (p.nombrepresupuesto || '').toLowerCase().includes(q) ||
    (p.type || '').toLowerCase().includes(q) ||
    (p.nombredepartamento || '').toLowerCase().includes(q)
  );
});

</script>

<template>
  <div class="view-container">
    <div class="header-section">
      <div class="title-box">
        <h1>Gestión de Presupuestos</h1>
        <p>Vista general de la asignación presupuestaria. <span class="result-count-inline" v-if="presupuestoStore.presupuestos.length > 0">({{ filteredPresupuestos.length }} resultados)</span></p>
      </div>
      <div class="header-actions" v-if="presupuestoStore.presupuestos.length > 0">
        <div class="search-box">
          <span class="material-symbols-outlined search-icon">search</span>
          <input v-model="searchQuery" type="text" placeholder="Buscar presupuesto..." class="search-input" />
        </div>
      </div>
    </div>

    <div class="table-card">
      <Table
        v-if="filteredPresupuestos.length > 0"
        :headers="headers"
        :data="filteredPresupuestos.map(p => [
          p.idpresupuesto,
          p.codigo,
          p.nombrepresupuesto,
          formatType(p.type),
          p.cantidad + '€',
          p.gasto + '€',
          p.nombredepartamento
        ])"
        :searchable="false"
      />
      <div v-else-if="presupuestoStore.presupuestos.length > 0" class="empty-state">
        <p>No se encontraron resultados para la búsqueda.</p>
      </div>
      <div v-else class="empty-state">
        <p>No hay presupuestos disponibles para mostrar.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-container {
  padding: 24px;
}
.header-section {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px;
}
.header-section h1 { font-size: 28px; font-weight: 700; color: #1f2937; margin-bottom: 8px; }
.header-section p { color: #6b7280; }
.result-count-inline { font-weight: 600; color: #475569; margin-left: 8px; }
.header-actions { display: flex; align-items: center; gap: 16px; }

.search-box {
  display: flex; align-items: center; background: #f9fafb;
  border: 1px solid #e5e7eb; border-radius: 4px; padding: 0 12px;
  width: 300px; transition: all 0.25s;
}
.search-box:focus-within { border-color: #dc2626; box-shadow: 0 0 0 3px rgba(220,38,38,0.1); background: #fff; }
.search-icon { font-size: 20px; color: #9ca3af; margin-right: 8px; }
.search-input { border: none; outline: none; background: transparent; font-size: 14px; color: #1f2937; padding: 10px 0; width: 100%; }

.table-card {
  background: white;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
}
.empty-state {
  text-align: center;
  padding: 48px;
  color: #9ca3af;
}
</style>
