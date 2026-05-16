<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useOrderStore } from '@/stores/orders';
import Table from '../components/common/Table.vue';

const orderStore = useOrderStore();
const searchQuery = ref('');

const headers = ['ID', 'Nombre', 'Descripción', 'Proveedor', 'Precio Medio'];

onMounted(async () => {
  await orderStore.fetchProducts();
});

const filteredProducts = computed(() => {
  if (!searchQuery.value.trim()) return orderStore.products;
  const q = searchQuery.value.toLowerCase();
  return orderStore.products.filter(p => 
    String(p.idproducto).includes(q) ||
    (p.nombre || '').toLowerCase().includes(q) ||
    (p.descripcion || '').toLowerCase().includes(q) ||
    (p.proveedor || '').toLowerCase().includes(q)
  );
});

const formatPrice = (price: any) => {
  if (price === null || price === undefined || price === '') return '0.00€';
  return parseFloat(price).toFixed(2) + '€';
};
</script>

<template>
  <div class="view-container animate-fade-in">
    <div class="header-section">
      <div class="title-box">
        <h1>Catálogo de Productos</h1>
        <p>Listado general de artículos y sus costes medios. <span class="result-count-inline" v-if="orderStore.products.length > 0">({{ filteredProducts.length }} resultados)</span></p>
      </div>
      <div class="header-actions">
        <div class="search-box" v-if="orderStore.products.length > 0">
          <span class="material-symbols-outlined search-icon">search</span>
          <input v-model="searchQuery" type="text" placeholder="Buscar producto..." class="search-input" />
        </div>
      </div>
    </div>

    <div class="table-card">
      <Table
        v-if="filteredProducts.length > 0"
        :headers="headers"
        :data="filteredProducts.map(p => [
          p.idproducto,
          p.nombre,
          p.descripcion,
          p.proveedor || 'No asignado',
          formatPrice(p.precio_medio)
        ])"
        :searchable="false"
      />
      <div v-else-if="orderStore.products.length > 0" class="empty-state">
        <p>No se encontraron resultados para la búsqueda.</p>
      </div>
      <div v-else class="empty-state">
        <span class="material-symbols-outlined">inventory_2</span>
        <p>No hay productos registrados en el sistema.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-container {
  padding: 24px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header-section h1 {
  font-size: 2.25rem;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 0.5rem;
  letter-spacing: -0.02em;
}

.header-section p {
  color: #64748b;
  font-size: 1.1rem;
}

.result-count-inline {
  font-weight: 600;
  color: #475569;
  margin-left: 8px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 0 12px;
  width: 300px;
  transition: all 0.25s;
}

.search-box:focus-within {
  border-color: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
  background: #fff;
}

.search-icon {
  font-size: 20px;
  color: #9ca3af;
  margin-right: 8px;
}

.search-input {
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: #1f2937;
  padding: 10px 0;
  width: 100%;
}

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

.empty-state .material-symbols-outlined {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
