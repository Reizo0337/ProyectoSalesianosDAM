<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useOrderStore } from '@/stores/orders';
import { useRouter } from 'vue-router';
import { useToast } from 'vue-toastification';
import { useUIStore } from '@/stores/ui';

const toast = useToast();
const authStore = useAuthStore();
const orderStore = useOrderStore();
const router = useRouter();
const uiStore = useUIStore();
const showUserMenu = ref(false);
const showNotifMenu = ref(false);
const notifications = ref<any[]>([]);
let notifInterval: any = null;

const unreadCount = computed(() => notifications.value.filter(n => n.leida === 'false' || n.leida === false).length);

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value;
  showNotifMenu.value = false;
}

function toggleNotifMenu() {
  showNotifMenu.value = !showNotifMenu.value;
  showUserMenu.value = false;
}

async function handleLogout() {
  await authStore.logout();
  showUserMenu.value = false;
  router.push('/login');
}

async function fetchNotifications() {
  if (authStore.isAuthenticated) {
    const newNotifs = await orderStore.fetchNotifications();
    
    // Si hay notificaciones nuevas que no estaban antes, avisar con Toast
    if (newNotifs.length > notifications.value.length) {
      const latest = newNotifs[0]; // Asumiendo que las nuevas vienen primero
      if (latest && (latest.leida === 'false' || latest.leida === false)) {
        toast.info(latest.mensaje, {
          onClick: () => markAsRead(latest)
        });
      }
    }
    
    notifications.value = newNotifs;
  }
}

async function markAsRead(notif: any) {
  await orderStore.markNotificationAsRead(notif.idNotificacion);
  notif.leida = true;
  if (notif.idOrden) {
    router.push(`/ordenes/${notif.idOrden}`);
    showNotifMenu.value = false;
  }
}

onMounted(() => {
  fetchNotifications();
  notifInterval = setInterval(fetchNotifications, 3000); // Polling cada 3s para notificaciones casi instantáneas
});

onUnmounted(() => {
  if (notifInterval) clearInterval(notifInterval);
});
</script>

<template>
  <header class="app-header">
    <!-- Logo & Toggle area -->
    <div class="header-left-side">
      <button @click="uiStore.toggleSidebar()" class="sidebar-toggle-btn" :title="uiStore.isSidebarCollapsed ? 'Expandir menú' : 'Contraer menú'">
        <span class="material-symbols-outlined">
          {{ uiStore.isSidebarCollapsed ? 'menu_open' : 'menu' }}
        </span>
      </button>
      <div class="header-logo" @click="router.push('/')">
        <img src="/img/logoPrincipal.jpg" alt="Salesianos Logo" class="logo-img" />
      </div>
    </div>

    <!-- Right actions -->
    <div class="header-actions">
      <!-- Notifications -->
      <div class="notif-wrapper" v-if="authStore.isAuthenticated">
        <button class="action-btn" @click="toggleNotifMenu" title="Notificaciones">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
          <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
        </button>

        <transition name="dropdown">
          <div v-if="showNotifMenu" class="notif-dropdown">
            <div class="notif-header">Notificaciones</div>
            <div class="notif-list">
              <div v-if="notifications.length === 0" class="notif-empty">No hay notificaciones</div>
              <div 
                v-for="n in notifications" 
                :key="n.idNotificacion" 
                class="notif-item" 
                :class="{ unread: n.leida === 'false' || n.leida === false }"
                @click="markAsRead(n)"
              >
                <p class="notif-msg">{{ n.mensaje }}</p>
                <span class="notif-time">{{ n.fecha }}</span>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- User Area (Only if Logged In) -->
      <div class="user-area" v-if="authStore.isAuthenticated">
        <button class="user-btn" @click="toggleUserMenu">
          <div class="user-avatar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </div>
          <div class="user-info">
            <span class="user-greeting">{{ authStore.user?.nombre || "Usuario" }}</span>
          </div>
          <svg class="chevron" :class="{ open: showUserMenu }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 12 15 18 9" />
          </svg>
        </button>

        <!-- User Dropdown Menu -->
        <transition name="dropdown">
          <div v-if="showUserMenu" class="user-dropdown">
            <RouterLink to="/profile" class="dropdown-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" />
              </svg>
              <span>Mi Perfil</span>
            </RouterLink>
            <div class="dropdown-divider"></div>
            <button @click="handleLogout" class="dropdown-item logout">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><polyline points="16 17 21 12 16 7" /><line x1="21" y1="12" x2="9" y2="12" />
              </svg>
              <span>Cerrar Sesión</span>
            </button>
          </div>
        </transition>
      </div>

      <!-- Login Button (Only if NOT Logged In) -->
      <RouterLink to="/login" v-else>
        <button class="login-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
            <polyline points="10 17 15 12 10 7" />
            <line x1="15" y1="12" x2="3" y2="12" />
          </svg>
          <span>Iniciar Sesión</span>
        </button>
      </RouterLink>
    </div>
  </header>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: 64px;
  background: #0f172a; /* Azul oscuro profundo (Slate 900) */
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  font-family: 'Inter', sans-serif;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  color: #f8fafc;
}

/* ── Header Left ── */
.header-left-side {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sidebar-toggle-btn {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #94a3b8;
  width: 38px;
  height: 38px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sidebar-toggle-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.4);
}

.sidebar-toggle-btn .material-symbols-outlined {
  font-size: 22px;
}

.header-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.app-brand-name {
  font-size: 1.25rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: white;
}

.app-brand-name span {
  color: #ef4444;
}

.logo-img {
  height: 32px;
  width: auto;
  filter: grayscale(1) invert(1) brightness(2);
  mix-blend-mode: screen;
}

/* ── Search ── */
.header-search {
  flex: 1;
  max-width: 480px;
  margin: 0 32px;
}

.search-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 14px;
  width: 18px;
  height: 18px;
  color: #9ca3af;
  pointer-events: none;
  transition: color 0.2s;
}

.search-input {
  width: 100%;
  height: 40px;
  padding: 0 16px 0 42px;
  border: 1.5px solid #e5e7eb;
  border-radius: 4px;
  background: #f9fafb;
  font-size: 14px;
  font-family: 'Inter', sans-serif;
  color: #1f2937;
  outline: none;
  transition: all 0.25s ease;
}

.search-input::placeholder {
  color: #9ca3af;
}

.search-input:focus {
  border-color: #dc2626;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
}

.search-input:focus + .search-icon,
.search-wrapper:focus-within .search-icon {
  color: #dc2626;
}

/* ── Actions ── */
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.action-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn svg {
  width: 20px;
  height: 20px;
}

.action-btn:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.notification-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 4px;
  background: #dc2626;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  animation: badgePulse 2s infinite;
}

@keyframes badgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

/* ── Notifications ── */
.notif-wrapper {
  position: relative;
}

.notif-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 320px;
  background: #0f172a;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.4);
  z-index: 200;
  overflow: hidden;
}

.notif-header {
  padding: 14px 18px;
  font-weight: 700;
  font-size: 14px;
  color: #ffffff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(255, 255, 255, 0.02);
}

.notif-list {
  max-height: 380px;
  overflow-y: auto;
}

.notif-item {
  padding: 14px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.notif-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.notif-item.unread {
  background: rgba(59, 130, 246, 0.05);
}

.notif-item.unread::after {
  content: '';
  position: absolute;
  top: 18px;
  right: 14px;
  width: 8px;
  height: 8px;
  background: #3b82f6;
  border-radius: 50%;
}

.notif-msg {
  font-size: 13px;
  color: #cbd5e1;
  margin: 0 0 4px;
  line-height: 1.4;
}

.notif-time {
  font-size: 11px;
  color: #64748b;
}

.notif-empty {
  padding: 32px;
  text-align: center;
  color: #64748b;
  font-size: 14px;
}

/* ── Login button ── */
.login-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 20px;
  border: none;
  border-radius: 4px;
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: 'Inter', sans-serif;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.3);
}

.login-btn svg {
  width: 18px;
  height: 18px;
}

.login-btn:hover {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  box-shadow: 0 4px 16px rgba(220, 38, 38, 0.4);
  transform: translateY(-1px);
}

.login-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.3);
}

/* ── User area ── */
.user-area {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 44px;
  padding: 4px 12px 4px 4px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
  color: #f8fafc;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: 'Inter', sans-serif;
}

.user-btn:hover {
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 4px;
  background: linear-gradient(135deg, #dc2626, #f87171);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar svg {
  width: 18px;
  height: 18px;
  color: #fff;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.2;
}

.user-greeting {
  font-size: 11px;
  color: #9ca3af;
  font-weight: 500;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: #f8fafc;
}

.chevron {
  width: 16px;
  height: 16px;
  color: #9ca3af;
  transition: transform 0.25s ease;
  flex-shrink: 0;
}

.chevron.open {
  transform: rotate(180deg);
}

/* ── Dropdown ── */
.user-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: -24px;
  width: 260px;
  background: #0f172a;
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 0 0 0 12px;
  box-shadow: -10px 10px 40px rgba(0, 0, 0, 0.5);
  padding: 8px;
  z-index: 200;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  width: 100%;
  border: none;
  background: transparent;
  color: #cbd5e1;
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
  outline: none;
}

.dropdown-item svg {
  width: 18px;
  height: 18px;
  color: #64748b;
  flex-shrink: 0;
}

.dropdown-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #ffffff;
}

.dropdown-item:hover svg {
  color: #ef4444;
}

.dropdown-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.05);
  margin: 6px 8px;
}

.dropdown-item.logout {
  color: #f87171;
}

.dropdown-item.logout svg {
  color: #f87171;
}

.dropdown-item.logout:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

/* ── Dropdown transition (Accordion style) ── */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform-origin: top right; /* Despliegue desde la esquina superior derecha */
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: scaleY(0) scaleX(0.95) translateY(-10px);
}
</style>
