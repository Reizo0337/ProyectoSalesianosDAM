<script setup lang="ts">
import Card from '../components/common/Card.vue';
import Table from '../components/common/Table.vue';
import { ref } from 'vue';

const presupuestoInicial = 5000;
const presupuestoGastado = 2000;
const presupuestoRestante = presupuestoInicial - presupuestoGastado;

const stats = ref([
  { title: 'Presupuesto Inicial', data: presupuestoInicial + '€', icon: 'money_bag', href: '/presupuestos', background: '#20a8d8' },
  { title: 'Presupuesto Restante', data: presupuestoRestante + '€', icon: 'money_bag', href: '/presupuestos', background: '#20d848' },
  { title: 'Presupuesto Gastado', data: presupuestoGastado + '€', icon: 'money_bag', href: '/presupuestos', background: '#20c9b9' },
])

const orderHeaders = ['ID', 'Departamento', 'Nombre', 'Contenido', 'Precio', 'Estado']

const orderData = ref([
  [1, 'Informática', 'Portátiles Dell Latitude', '10x Dell Latitude 5540', '8.500€', 'Aprobado'],
  [2, 'Administración', 'Material de oficina', 'Papel, tóner, carpetas', '320€', 'Pendiente'],
  [3, 'Informática', 'Licencias Microsoft 365', '50x licencias anuales', '4.200€', 'Aprobado'],
  [4, 'Marketing', 'Cartelería evento', 'Banners y roll-ups', '750€', 'En proceso'],
  [5, 'RRHH', 'Uniformes personal', '25x uniformes corporativos', '1.125€', 'Pendiente'],
  [6, 'Mantenimiento', 'Repuestos climatización', 'Filtros y compresor', '2.340€', 'Rechazado'],
  [7, 'Informática', 'Switches de red', '3x Cisco Catalyst 1000', '1.890€', 'Aprobado'],
  [8, 'Administración', 'Mobiliario sala reuniones', 'Mesa y 12 sillas ergonómicas', '3.600€', 'En proceso'],
])
</script>

<template>
  <div class="welcome">
    <h3>Bienvenido, Admin</h3>
  </div>
  <div class="title">
    <h2>Presupuesto Informática</h2>
  </div>
  <div class="statistics-container">
    <Card
      v-for="(stat, index) in stats"
      :key="stat.title"
      type="stats"
      v-bind="stat"
      :animate="true"
      :style="{ animationDelay: `${index * 0.1}s` }"
    />
  </div>

  <div class="title">
    <h2>Ordenes de compra</h2>
  </div>
  <div class="table-container">
    <Table
      :headers="orderHeaders"
      :data="orderData"
      :searchable="true"
      :statusColumn="5"
    />
  </div>
</template>

<style scoped>
  .statistics-container {
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
    width: 100%;
    margin-bottom: 1rem;
  }

  .title {
    margin-bottom: 1rem;
    display: flex;
    justify-content: start;
  }

  .title h2 {
    font-size: 1.25rem;
    font-weight: 700;
    color: #1f2937;
  }

  .welcome {
    margin-bottom: 1rem;
    display: flex;
    justify-content: start;
  }

  .welcome h3 {
    font-size: 1.5rem;
    font-weight: 700;
    color: #1f2937;
  }

  .table-container {
    margin-bottom: 2rem;
  }
</style>
