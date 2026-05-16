<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { computed } from 'vue';

const authStore = useAuthStore();
const role = computed(() => authStore.user?.rol || 'Invitado');

const sections = computed(() => {
  const all = [
    {
      title: 'Panel de Control',
      icon: 'dashboard',
      content: 'El centro neurálgico de la aplicación. Aquí puedes ver un resumen visual de los gastos actuales, el estado de los presupuestos ordinarios y los planes de inversión del año en curso.',
      roles: ['Administrador', 'Contable', 'Jefe de Equipo']
    },
    {
      title: 'Gestión de Órdenes',
      icon: 'shopping_cart',
      content: 'Permite realizar el seguimiento de los pedidos. Los Jefes de Equipo pueden crear nuevas órdenes, mientras que los Contables y Administradores pueden supervisar el flujo de facturación. No olvides que puedes adjuntar facturas en formato PDF y mencionar a compañeros en los comentarios usando "@".',
      roles: ['Administrador', 'Contable', 'Jefe de Equipo']
    },
    {
      title: 'Control Presupuestario',
      icon: 'account_balance_wallet',
      content: 'Visualiza el dinero disponible en tiempo real. Los presupuestos se dividen por departamentos. Si eres Jefe de Equipo, verás solo lo asignado a tu unidad; si eres Administrador o Contable, tienes una visión global de toda la empresa.',
      roles: ['Administrador', 'Contable', 'Jefe de Equipo']
    },
    {
      title: 'Directorio de Proveedores',
      icon: 'store',
      content: 'Accede a la base de datos de suministradores oficiales. Puedes ver qué productos ofrece cada proveedor y sus datos de contacto.',
      roles: ['Administrador', 'Contable', 'Jefe de Equipo']
    },
    {
      title: 'Catálogo de Productos',
      icon: 'inventory_2',
      content: 'Listado general de artículos con su precio medio. Es la base para generar órdenes de compra precisas.',
      roles: ['Administrador', 'Contable', 'Jefe de Equipo']
    },
    {
      title: 'Histórico y Archivo',
      icon: 'history',
      content: 'Consulta los datos de años anteriores para comparar el rendimiento. Los administradores pueden clonar presupuestos de un año a otro para agilizar la planificación anual.',
      roles: ['Administrador', 'Contable', 'Jefe de Equipo']
    },
    {
      title: 'Gestión de Usuarios',
      icon: 'group',
      content: 'Módulo exclusivo de administración. Aquí se verifican las nuevas cuentas, se asignan roles (Admin, Contable, Jefe de Equipo) y se vinculan a sus respectivos departamentos.',
      roles: ['Administrador']
    }
  ];

  return all.filter(s => s.roles.includes(role.value));
});
</script>

<template>
  <div class="view-container animate-fade-in">
    <div class="help-header">
      <span class="material-symbols-outlined help-icon">help_center</span>
      <div class="header-text">
        <h1>Centro de Ayuda</h1>
        <p>Bienvenido, <strong>{{ authStore.user?.nombre }}</strong>. Esta guía está personalizada para tu rol como <strong>{{ role }}</strong>.</p>
      </div>
    </div>

    <div class="manual-grid">
      <div v-for="(section, index) in sections" :key="index" class="manual-card">
        <div class="card-glow"></div>
        <div class="card-header-manual">
          <span class="material-symbols-outlined card-icon">{{ section.icon }}</span>
          <h3>{{ section.title }}</h3>
        </div>
        <div class="card-divider"></div>
        <p>{{ section.content }}</p>
      </div>
    </div>

    <div class="support-footer">
      <p>¿Necesitas más ayuda? Contacta con el departamento de TI o consulta el manual técnico extendido.</p>
      <div class="version-tag">ZarGestion v1.0.4 - Sistema de Gestión Operativa</div>
    </div>
  </div>
</template>

<style scoped>
.view-container {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.help-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 48px;
  padding-bottom: 32px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.help-icon {
  font-size: 64px;
  color: #ef4444;
  background: #fef2f2;
  padding: 16px;
  border-radius: 20px;
}

.header-text h1 {
  font-size: 32px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 8px;
}

.header-text p {
  color: #64748b;
  font-size: 16px;
}

.manual-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}

.manual-card {
  background: white;
  padding: 32px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.manual-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  border-color: #ef4444;
}

.manual-card h3 {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  position: relative;
  z-index: 2;
}

.card-header-manual {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.card-icon {
  color: #ef4444;
  font-size: 24px;
}

.card-divider {
  height: 2px;
  width: 40px;
  background: #ef4444;
  margin-bottom: 16px;
  border-radius: 2px;
}

.manual-card p {
  color: #475569;
  font-size: 14px;
  line-height: 1.6;
  position: relative;
  z-index: 2;
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at center, rgba(239, 68, 68, 0.03) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.manual-card:hover .card-glow {
  opacity: 1;
}

.support-footer {
  margin-top: 64px;
  text-align: center;
  padding: 32px;
  background: #f8fafc;
  border-radius: 16px;
  color: #64748b;
}

.version-tag {
  margin-top: 16px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #94a3b8;
}

.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
