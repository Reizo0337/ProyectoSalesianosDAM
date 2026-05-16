<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useUIStore } from '@/stores/ui';

const authStore = useAuthStore();
const uiStore = useUIStore();
const route = useRoute();

const isCollapsed = computed(() => uiStore.isSidebarCollapsed);

const menuItems = [
  {
    label: 'Inicio',
    to: '/',
    icon: 'home',
  },
  {
    label: 'Presupuestos',
    to: '/presupuestos',
    icon: 'payments',
  },
  {
    label: 'Ordenes de compra',
    to: '/ordenes',
    icon: 'shopping_cart',
  },
  {
    label: 'Proveedores',
    to: '/proveedores',
    icon: 'local_shipping',
  },
  {
    label: 'Productos',
    to: '/productos',
    icon: 'inventory_2',
  },
  {
    label: 'Historico',
    to: '/historico',
    icon: 'history',
  },
];

const bottomItems = [
  {
    label: 'Configuración',
    to: '/config',
    icon: 'settings',
  },
  {
    label: 'Ayuda',
    to: '/ayuda',
    icon: 'help',
  },
];

function toggleCollapse() {
  uiStore.toggleSidebar();
}
</script>

<template>
  <aside class="app-aside" :class="{ collapsed: isCollapsed }">
    <!-- Main navigation -->
    <nav class="aside-nav">
      <div class="nav-section">
        <div class="section-header-sidebar">
          <span v-if="!isCollapsed" class="nav-label">MENÚ PRINCIPAL</span>
        </div>
        
        <ul class="nav-list">
          <li v-for="item in menuItems" :key="item.to">
            <RouterLink
              :to="item.to"
              class="nav-item"
              :class="{ active: route.path === item.to }"
              :title="isCollapsed ? item.label : ''"
            >
              <!-- Icono solo cuando está colapsado -->
              <span v-if="isCollapsed" class="material-symbols-outlined nav-icon">
                {{ item.icon }}
              </span>
              <!-- Texto solo cuando NO está colapsado -->
              <span v-else class="nav-text">{{ item.label }}</span>
            </RouterLink>
          </li>
          
          <!-- Item especial de Usuarios -->
          <li v-if="authStore.user?.rol === 'Administrador'">
            <RouterLink
              to="/usuarios"
              class="nav-item"
              :class="{ active: route.path === '/usuarios' }"
              :title="isCollapsed ? 'Usuarios' : ''"
            >
              <span v-if="isCollapsed" class="material-symbols-outlined nav-icon">group</span>
              <span v-else class="nav-text">Usuarios</span>
            </RouterLink>
          </li>
        </ul>
      </div>
    </nav>

    <!-- Bottom section -->
    <div class="aside-bottom">
      <div class="nav-section">
        <span v-if="!isCollapsed" class="nav-label">SOPORTE</span>
        <ul class="nav-list">
          <li v-for="item in bottomItems" :key="item.to" >
            <RouterLink
              :to="item.to"
              class="nav-item"
              :class="{ active: route.path === item.to }"
              :title="isCollapsed ? item.label : ''"
            >
              <span v-if="isCollapsed" class="material-symbols-outlined nav-icon">
                {{ item.icon }}
              </span>
              <span v-else class="nav-text">{{ item.label }}</span>
            </RouterLink>
          </li>
        </ul>
      </div>

      <div class="version-info" v-if="!isCollapsed">
        <span>ZarGestion v1.0</span>
      </div>
    </div>
  </aside>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.app-aside {
  position: fixed;
  top: 64px;
  left: 0;
  bottom: 0;
  width: 250px;
  background: #0f172a; /* Slate 900 */
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  font-family: 'Inter', sans-serif;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 90;
  overflow-x: hidden;
}

.app-aside.collapsed {
  width: 72px;
}

/* ── Collapse toggle ── */
.section-header-sidebar {
  display: flex;
  align-items: center;
  padding: 0 16px;
  margin-bottom: 24px;
  min-height: 24px;
}

.collapsed .section-header-sidebar {
  padding: 0;
  justify-content: center;
}


.toggle-sidebar-btn {
  background: none;
  border: none;
  color: #64748b;
  padding: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-sidebar-btn:hover {
  color: #ef4444;
}

.toggle-sidebar-btn span {
  font-size: 20px;
}

/* ── Navigation ── */
.aside-nav {
  flex: 1;
  padding: 24px 12px 12px;
  overflow-y: auto;
  overflow-x: hidden;
}

.aside-nav::-webkit-scrollbar {
  width: 4px;
}

.aside-nav::-webkit-scrollbar-track {
  background: transparent;
}

.aside-nav::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 4px;
}

.nav-section {
  margin-bottom: 8px;
}

.nav-label {
  display: block;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 1.5px;
  color: #64748b; /* Slate 500 */
  padding: 0;
  margin: 0;
  text-transform: uppercase;
  white-space: nowrap;
  overflow: hidden;
}

.nav-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  margin: 0 8px;
  color: #94a3b8; /* Slate 400 */
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  white-space: nowrap;
  overflow: hidden;
  position: relative;
}

.collapsed .nav-item {
  justify-content: center;
  margin: 4px 8px;
  padding: 0;
  height: 44px;
  width: 44px;
  border-radius: 12px;
}

.collapsed .nav-icon {
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.collapsed .aside-nav {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.collapsed .nav-list {
  width: 100%;
  align-items: center;
}

/* Eliminado el ::before indicator */

.nav-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #f8fafc;
}

.nav-item:hover .nav-icon {
  color: #dc2626;
}

.nav-item.active {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: #ffffff;
  font-weight: 700;
  box-shadow: 0 10px 15px -3px rgba(239, 68, 68, 0.3);
}

.nav-item.active .nav-icon {
  color: #ffffff !important;
  transform: scale(1.1);
}


.nav-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  color: #64748b;
  transition: color 0.2s ease;
}

.nav-text {
  opacity: 1;
  transition: opacity 0.2s ease;
}

.collapsed .nav-text {
  opacity: 0;
  width: 0;
}

.collapsed .nav-item {
  justify-content: center;
  padding: 10px;
}

.collapsed .nav-label {
  opacity: 0;
  height: 0;
  margin: 0;
  overflow: hidden;
}

/* ── Bottom section ── */
.aside-bottom {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.version-info {
  text-align: center;
  padding: 12px 0 4px;
  font-size: 11px;
  color: #9ca3af;
  font-weight: 500;
}
</style>
