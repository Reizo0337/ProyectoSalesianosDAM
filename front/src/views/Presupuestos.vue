<script setup lang="ts">
import { onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { usePresupuestoStore } from '@/stores/presupuesto';
import Table from '../components/common/Table.vue';

const authStore = useAuthStore();
const presupuestoStore = usePresupuestoStore();

const headers = ['ID', 'Código', 'Nombre', 'Tipo', 'Cantidad', 'Gasto', 'Departamento', 'Acciones'];

onMounted(async () => {
  if (authStore.user?.rol === 'Administrador') {
    await presupuestoStore.getAllPresupuestos();
  } else if (authStore.user?.idDepartamento) {
    await presupuestoStore.getPresupuestosByDept(authStore.user.idDepartamento);
  }
});
</script>

<template>
  <div class="view-container">
    <div class="header-section">
      <h1>Gestión de Presupuestos</h1>
      <p>Vista general de la asignación presupuestaria.</p>
    </div>

    <div class="table-card">
      <Table
        v-if="presupuestoStore.presupuestos.length > 0"
        :headers="headers"
        :data="presupuestoStore.presupuestos.map(p => [
          p.idpresupuesto,
          p.codigo,
          p.nombrepresupuesto,
          p.type || 'Presupuesto',
          p.cantidad + '€',
          p.gasto + '€',
          p.nombredepartamento
        ])"
        :searchable="true"
      />
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
  margin-bottom: 32px;
}
.header-section h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}
.header-section p {
  color: #6b7280;
}
.table-card {
  background: white;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}
.empty-state {
  text-align: center;
  padding: 48px;
  color: #9ca3af;
}
</style>
