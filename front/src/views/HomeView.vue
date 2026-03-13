<script setup lang="ts">
import Card from '../components/common/Card.vue';
import Table from '../components/common/Table.vue';
import  {ref} from 'vue';


var presupuestoInicial = 5000;
var presupuestoGastado = 2000;
var presupuestoRestante = presupuestoInicial - presupuestoGastado;

const stats = ref([
  {title: 'Presupuesto Inicial', data: presupuestoInicial + '€', icon:'money_bag', href: '/presupuestos', background: '#20a8d8' },
  {title: 'Presupuesto Restante', data: presupuestoRestante + '€', icon:'money_bag', href: '/presupuestos', background: '#20d848' },
  {title: 'Presupuesto Gastado', data: presupuestoGastado + '€', icon:'money_bag', href: '/presupuestos', background: '#20c9b9' },
])

</script>

<template>
  <div class="title">
    <h2>Presupuesto</h2>
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
    <Table :headers="['ID','Departamento', 'Nombre', 'Contenido', 'Precio', 'Estado']" :data="[[1, 'Informatica', 'Orden 1', 'Contenido 1', '100€', 'Aprobado'], [2, 'Informatica', 'Orden 2', 'Contenido 2', '200€', 'Pendiente']]"/>
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
    margin-bottom: 2rem;
    display: flex;
    justify-content: start;
  }
</style>
