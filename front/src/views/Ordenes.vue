<script setup lang="ts">
import { onMounted, computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useOrderStore } from '@/stores/orders';
import { useToast } from 'vue-toastification';
import { useDialogStore } from '@/stores/dialog';
import Table from '../components/common/Table.vue';
import OrderModal from '../components/orders/OrderModal.vue';
 
const toast = useToast();
const router = useRouter();
const authStore = useAuthStore();
const orderStore = useOrderStore();
const dialogStore = useDialogStore();
 
const showModal = ref(false);
const searchQuery = ref('');
 
const canCreate = computed(() => {
  return authStore.user?.rol === 'Administrador' || authStore.user?.rol === 'Jefe de Equipo';
});
 
const filteredOrders = computed(() => {
  if (!searchQuery.value.trim()) return orderStore.orders;
  const q = searchQuery.value.toLowerCase();
  return orderStore.orders.filter(o =>
    (o.numero_orden || '').toLowerCase().includes(q) ||
    (o.numero_plan || '').toLowerCase().includes(q) ||
    (o.estado || '').toLowerCase().includes(q) ||
    String(o.cantidad).includes(q)
  );
});
 
function formatDate(dateStr: string) {
  if (!dateStr) return '---';
  try {
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return dateStr;
    return date.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
  } catch (e) {
    return '---';
  }
}
 
function getEstadoClass(estado: string) {
  const e = (estado || '').toLowerCase();
  if (e.includes('cerrada')) return 'red';
  if (e.includes('abierta')) return 'green';
  if (e.includes('pendiente')) return 'orange';
  return 'gray';
}

function getEstadoIcon(estado: string) {
  const e = (estado || '').toLowerCase();
  if (e.includes('cerrada')) return 'lock';
  if (e.includes('abierta')) return 'lock_open';
  if (e.includes('pendiente')) return 'pending';
  return 'info';
}
 
async function refreshOrders() {
  const role = authStore.user?.rol;
  const dept = (role === 'Administrador' || role === 'Contable') 
    ? 'Admin' 
    : authStore.user?.idDepartamento;
    
  try {
    if (dept) {
      const currentYear = new Date().getFullYear();
      await orderStore.getOrdersByDept(dept, currentYear);
    }
  } catch (err) {
    toast.error('Error al sincronizar el listado de órdenes');
  }
}
 
async function handleDelete(id: number) {
  const confirmed = await dialogStore.confirm('Eliminar Orden', '¿Estás seguro? Esta acción no se puede deshacer.');
  if (confirmed) {
    try {
      await orderStore.deleteOrder(id);
      toast.success('Orden eliminada');
      refreshOrders();
    } catch (error) {
      toast.error('No se pudo eliminar la orden');
    }
  }
}
 
watch(() => authStore.user, (user) => {
  if (user) refreshOrders();
}, { immediate: true });
</script>
 
<template>
  <div class="view-page animate-in">
    <header class="dashboard-header">
      <div class="header-left">
        <h1>Órdenes de Compra</h1>
        <p class="subtitle">Seguimiento y gestión de pedidos realizados. <span class="result-count-inline" v-if="orderStore.orders.length > 0">({{ filteredOrders.length }} resultados)</span></p>
      </div>
      <div class="header-actions">
        <div class="search-box" v-if="orderStore.orders.length > 0">
          <span class="material-symbols-outlined search-icon">search</span>
          <input v-model="searchQuery" type="text" placeholder="Buscar orden..." class="search-input" />
        </div>
        <button v-if="canCreate" class="create-btn" @click="showModal = true">
          <span class="material-symbols-outlined">add_shopping_cart</span>
          Nueva Orden
        </button>
      </div>
    </header>
 
    <Table 
      :loading="orderStore.loading"
      :headers="['ID', 'Número', 'Fecha', 'Cantidad', 'Estado', 'Acciones']"
      :data="filteredOrders.map(o => [
        o.idorden,
        o.numero_orden || o.numero_plan || 'S/N',
        formatDate(o.fechacreacion),
        o.cantidad + '€',
        { 
          component: 'Badge', 
          props: { 
            text: o.estado || 'Pendiente', 
            class: getEstadoClass(o.estado),
            icon: getEstadoIcon(o.estado)
          } 
        },
        {
          type: 'actions',
          actions: [
            { icon: 'visibility', label: 'Ver Detalle', class: 'btn-view', onClick: () => router.push(`/ordenes/${o.idorden}`) },
            { icon: 'delete', label: 'Eliminar', class: 'btn-delete', onClick: () => handleDelete(o.idorden) }
          ]
        }
      ])"
      :onRowClick="(row) => router.push(`/ordenes/${row[0]}`)"
    />
 
    <OrderModal 
      :isOpen="showModal" 
      @close="showModal = false" 
      @success="refreshOrders"
    />
  </div>
</template>
 
<style scoped>
.dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 3.5rem; }
.header-left h1 { font-size: 2.75rem; font-weight: 850; color: #0f172a; letter-spacing: -0.04em; margin-bottom: 4px; }
.subtitle { color: #64748b; font-size: 1.15rem; }
.header-actions { display: flex; align-items: center; gap: 16px; }
 
.create-btn {
  display: flex; align-items: center; gap: 8px; background: #dc2626; color: white;
  border: none; padding: 12px 24px; border-radius: 8px; font-weight: 700; cursor: pointer;
  transition: all 0.2s; box-shadow: 0 4px 6px -1px rgba(220, 38, 38, 0.2);
}
.create-btn:hover { background: #b91c1c; transform: translateY(-2px); box-shadow: 0 10px 15px -3px rgba(220, 38, 38, 0.3); }
 
.result-count-inline { font-weight: 700; color: #1e293b; margin-left: 8px; }
.search-box { display: flex; align-items: center; background: white; border: 1px solid #e2e8f0; border-radius: 8px; padding: 0 12px; width: 300px; transition: all 0.2s; }
.search-box:focus-within { border-color: #ef4444; box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1); }
.search-icon { font-size: 20px; color: #94a3b8; margin-right: 8px; }
.search-input { border: none; outline: none; background: transparent; font-size: 14px; color: #1e293b; padding: 10px 0; width: 100%; }
</style>
