<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import Card from '../components/common/Card.vue';
import Table from '../components/common/Table.vue';
import { useAuthStore } from '@/stores/auth';
import { usePresupuestoStore } from '@/stores/presupuesto';
import { useOrderStore } from '@/stores/orders';

const authStore = useAuthStore();
const presupuestoStore = usePresupuestoStore();
const ordenStore = useOrderStore();

const rolUsuario = authStore.user?.rol;
const depUsuario = authStore.user?.idDepartamento;

onMounted(async () => {
  if (rolUsuario === 'Administrador') {
    await presupuestoStore.getAllPresupuestos();
    await ordenStore.getAllOrders();
  } else if (depUsuario) {
    await presupuestoStore.getPresupuestosByDept(depUsuario);
    await ordenStore.getOrdersByDept(depUsuario);
  }
});

const dashboardStats = computed(() => {
  const budgets = presupuestoStore.presupuestos;
  if (!budgets || budgets.length === 0) return { general: [], departments: [] };

  const calculateStats = (list: any[]) => {
    const total = list.reduce((acc, b) => acc + (Number(b.cantidad) || 0), 0);
    const spent = list.reduce((acc, b) => acc + (Number(b.gasto) || 0), 0);
    const remaining = total - spent;
    return [
      {
        title: 'Presupuesto Inicial',
        data: total,
        suffix: '€',
        icon: 'payments',
        href: '/presupuestos',
        background: 'linear-gradient(135deg, #20a8d8 0%, #178ab3 100%)'
      },
      {
        title: 'Presupuesto Restante',
        data: remaining,
        suffix: '€',
        icon: 'account_balance_wallet',
        href: '/presupuestos',
        background: 'linear-gradient(135deg, #20d848 0%, #16a334 100%)'
      },
      {
        title: 'Presupuesto Gastado',
        data: spent,
        suffix: '€',
        icon: 'shopping_cart_checkout',
        href: '/presupuestos',
        background: 'linear-gradient(135deg, #20c9b9 0%, #169288 100%)'
      }
    ];
  };

  const general = calculateStats(budgets);

  if (rolUsuario === 'Administrador') {
    const grouped: Record<string, any[]> = {};
    budgets.forEach(b => {
      const key = b.nombredepartamento || 'Sin Departamento';
      if (!grouped[key]) grouped[key] = [];
      grouped[key].push(b);
    });

    const departments = Object.keys(grouped).map(name => ({
      id: name,
      name: name,
      stats: calculateStats(grouped[name])
    }));

    return { general, departments };
  }

  return { general, departments: [] };
});

const showMoreDetails = ref(false);

const visibleDepartments = computed(() => {
  return dashboardStats.value.departments;
});

const viewTitle = computed(() => {
  if (rolUsuario === 'Administrador') return 'Panel de Administración Global';
  return `Presupuesto ${depUsuario || 'Mi Departamento'}`;
});

const orderHeaders = ['ID', 'Proveedor', 'Nº Orden', 'Contenido', 'Presupuesto', 'Importe', 'Estado']
const orderData = computed(() => {
  const orders = ordenStore.orders || [];
  return orders.map(o => [
    o.idOrden || o.idorden || '',
    o.proveedor_nombre || 'S/P',
    o.numero_orden || 'N/A',
    o.Observaciones || 'Sin observaciones',
    o.idPresupuesto || o.idpresupuesto || '',
    (o.Cantidad || 0).toLocaleString() + '€',
    o.Estado || 'Pendiente'
  ]);
});
</script>

<template>
  <div class="dashboard-page">
    <header class="dashboard-header animate-fade-in">
      <div class="header-content">
        <h1>{{ viewTitle }}</h1>
        <p class="subtitle" v-if="rolUsuario === 'Administrador'">Panel de control integral para la gestión financiera y operativa.</p>
        <p class="subtitle" v-else>Estado actual de la asignación presupuestaria y órdenes de compra.</p>
      </div>
      <div class="header-actions" v-if="rolUsuario === 'Administrador'">
        <button class="btn-primary">
          <span class="material-symbols-outlined">analytics</span>
          Informe Global
        </button>
      </div>
    </header>

    <!-- 1. General Summary Section (Always Visible) -->
    <section class="dashboard-section animate-fade-in" style="animation-delay: 0.1s">
      <div class="section-header">
        <span class="material-symbols-outlined">monitoring</span>
        <h2>Resumen Operativo</h2>
      </div>
      <div class="statistics-grid">
        <Card
          v-for="(stat, index) in dashboardStats.general"
          :key="stat.title"
          type="stats"
          v-bind="stat"
          :animate="true"
          :style="{ animationDelay: `${0.2 + index * 0.1}s` }"
        />
      </div>
    </section>

    <!-- 2. The Great "See More" Toggle (Only for Departments now) -->
    <div v-if="rolUsuario === 'Administrador' && dashboardStats.departments.length > 0" class="show-more-container animate-fade-in" style="animation-delay: 0.4s">
      <button @click="showMoreDetails = !showMoreDetails" class="btn-secondary toggle-main">
        <span class="material-symbols-outlined">{{ showMoreDetails ? 'keyboard_double_arrow_up' : 'expand_more' }}</span>
        {{ showMoreDetails ? 'Ocultar desglose por departamentos' : `Ver desglose por departamentos (${dashboardStats.departments.length})` }}
      </button>
    </div>

    <!-- 3. Hidden Detailed Intelligence (Department Accordion) -->
    <transition name="accordion">
      <div v-if="showMoreDetails && rolUsuario === 'Administrador'" class="accordion-content">
        <section 
          v-for="(dept, dIndex) in visibleDepartments" 
          :key="dept.id" 
          class="dashboard-section dept-section animate-fade-in"
        >
          <div class="section-header">
            <div class="dept-badge">Departamento</div>
            <h2>{{ dept.name }}</h2>
          </div>
          <div class="statistics-grid">
            <Card
              v-for="(stat, index) in dept.stats"
              :key="stat.title"
              type="stats"
              v-bind="stat"
              :animate="true"
            />
          </div>
        </section>
      </div>
    </transition>

    <!-- 4. Permanent Intel (Orders Table - Now Outside) -->
    <section class="dashboard-section animate-fade-in" style="animation-delay: 0.6s">
      <div class="section-header">
        <span class="material-symbols-outlined">shopping_bag</span>
        <h2>{{ rolUsuario === 'Administrador' ? 'Registro Global de Órdenes' : 'Órdenes de Compra' }}</h2>
      </div>
      <div class="table-card">
        <div v-if="ordenStore.loading" class="loading-overlay">
          <div class="spinner"></div>
          <p>Sincronizando registros...</p>
        </div>
        <Table
          v-else
          :headers="orderHeaders"
          :data="orderData"
          :searchable="true"
          :statusColumn="6"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
/* Accordion Animation */
.accordion-enter-active,
.accordion-leave-active {
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;
  max-height: 2000px;
}

.accordion-enter-from,
.accordion-leave-to {
  max-height: 0;
  opacity: 0;
  transform: translateY(-20px);
}

.dashboard-page {
  padding: 2rem;
  max-width: 1600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.header-content h1 {
  font-size: 2.5rem;
  font-weight: 850;
  letter-spacing: -0.025em;
  background: linear-gradient(135deg, #1e293b 0%, #64748b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #64748b;
  font-size: 1.1rem;
  font-weight: 500;
}

.btn-primary {
  background: #3b82f6;
  color: white;
  padding: 0.875rem 1.75rem;
  border-radius: 14px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  display: flex;
  align-items: center;
  gap: 0.625rem;
}

.btn-primary:hover {
  background: #2563eb;
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 12px 20px rgba(59, 130, 246, 0.4);
}

.show-more-container {
  display: flex;
  justify-content: center;
  margin-top: -0.5rem;
  margin-bottom: 1rem;
}

.btn-secondary {
  background: rgba(241, 245, 249, 0.8);
  backdrop-filter: blur(8px);
  color: #475569;
  padding: 0.75rem 2rem;
  border-radius: 12px;
  font-weight: 700;
  border: 1px solid rgba(226, 232, 240, 0.8);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
}

.btn-secondary:hover {
  background: #f1f5f9;
  color: #1e293b;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.dashboard-section {
  display: flex;
  flex-direction: column;
  gap: 1.75rem;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.section-header .material-symbols-outlined {
  color: #3b82f6;
  font-size: 1.75rem;
  padding: 0.5rem;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 12px;
}

.section-header h2 {
  font-size: 1.625rem;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.0125em;
}

.dept-badge {
  background: #f1f5f9;
  color: #64748b;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.75rem;
  width: 100%;
}

.table-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 24px;
  padding: 1.75rem;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.05);
  position: relative;
  min-height: 400px;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255,255,255,0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border-radius: 24px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.dept-section {
  padding-top: 1.5rem;
  border-top: 2px solid #f1f5f9;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in {
  animation: fadeIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>
