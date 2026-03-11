<script setup lang="ts">
import Card from '../components/common/Card.vue';
import  {ref} from 'vue';

const stats = ref([
  {title: 'Presupuesto', data: '5000$', icon:'money_bag', href: '/presupuestos', background: '#20a8d8' },
])

</script>

<template>
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
  
</template>

<style scoped>
  .stats-display {
    display: flex;

  }
</style>
