<script setup lang="ts">
import { ref } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const route = useRoute();
const isCollapsed = ref(false);

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
  isCollapsed.value = !isCollapsed.value;
}
</script>

<template>
  <aside class="app-aside">
    <!-- Main navigation -->
    <nav class="aside-nav">
      <div class="nav-section">
        <span class="nav-label">MENÚ PRINCIPAL</span>
        <ul class="nav-list">
          <li v-for="item in menuItems" :key="item.to">
            <RouterLink
              :to="item.to"
              class="nav-item"
              :class="{ active: route.path === item.to }"
              :title="item.label"
            >
              <span class="nav-text">{{ item.label }}</span>
            </RouterLink>
          </li>
        </ul>
      </div>
    </nav>

    <!-- Bottom section -->
    <div class="aside-bottom">
      <div class="nav-section">
        <span class="nav-label">SOPORTE</span>
        <ul class="nav-list">
          <li v-for="item in bottomItems" :key="item.to" >
            <RouterLink
              :to="item.to"
              class="nav-item"
              :class="{ active: route.path === item.to }"
              :title="isCollapsed ? item.label : ''"
            >
              <span class="nav-text">{{ item.label }}</span>
            </RouterLink>
          </li>
        </ul>
      </div>

      <!-- Version info -->
      <div class="version-info">
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
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid rgba(0, 0, 0, 0.06);
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

/* ── Collapse button ── */
.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1.5px solid #e5e7eb;
  border-radius: 4px;
  background: #fff;
  color: #6b7280;
  cursor: pointer;
  position: absolute;
  top: 16px;
  right: -16px;
  z-index: 10;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.collapse-btn svg {
  width: 16px;
  height: 16px;
  transition: transform 0.3s ease;
}

.collapse-btn svg.rotated {
  transform: rotate(180deg);
}

.collapse-btn:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
  color: #dc2626;
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
  font-weight: 700;
  letter-spacing: 1.2px;
  color: #9ca3af;
  padding: 0 12px;
  margin-bottom: 8px;
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
  padding: 10px 12px;
  border-radius: 4px;
  color: #4b5563;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  white-space: nowrap;
  overflow: hidden;
  position: relative;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: #dc2626;
  border-radius: 0 4px 4px 0;
  transition: height 0.2s ease;
}

.nav-item:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.nav-item:hover .nav-icon {
  color: #dc2626;
}

.nav-item.active {
  background: linear-gradient(135deg, rgba(220, 38, 38, 0.08), rgba(220, 38, 38, 0.04));
  color: #dc2626;
  font-weight: 600;
}

.nav-item.active::before {
  height: 20px;
}

.nav-item.active .nav-icon {
  color: #dc2626;
}

.nav-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  color: #6b7280;
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
  padding: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.version-info {
  text-align: center;
  padding: 12px 0 4px;
  font-size: 11px;
  color: #9ca3af;
  font-weight: 500;
}
</style>
