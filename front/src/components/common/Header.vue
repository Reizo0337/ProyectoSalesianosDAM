<script setup lang="ts">
import { ref } from 'vue';

const isLoggedIn = ref(false);
const userName = ref('Admin');
const showUserMenu = ref(false);

function toggleLogin() {
  isLoggedIn.value = !isLoggedIn.value;
}

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value;
}
</script>

<template>
  <header class="app-header">
    <!-- Logo area -->
    <div class="header-logo">
      <img src="/img/logoPrincipal.jpg" alt="Salesianos Logo" class="logo-img" />
    </div>

    <!-- Search bar (center) -->
    <div class="header-search">
      <div class="search-wrapper">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <path d="m21 21-4.35-4.35" />
        </svg>
        <input type="text" placeholder="Buscar productos, facturas..." class="search-input" />
      </div>
    </div>

    <!-- Right actions -->
    <div class="header-actions">
      <!-- Notifications -->
      <button class="action-btn" title="Notificaciones">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        <span class="notification-badge">3</span>
      </button>

      <!-- User / Login -->
      <div class="user-area" v-if="isLoggedIn">
        <button class="user-btn" @click="toggleUserMenu">
          <div class="user-avatar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </div>
          <div class="user-info">
            <span class="user-greeting">Bienvenido,</span>
            <span class="user-name">{{ userName }}</span>
          </div>
          <svg class="chevron" :class="{ open: showUserMenu }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 12 15 18 9" />
          </svg>
        </button>

        <!-- Dropdown menu -->
        <Transition name="dropdown">
          <div class="user-dropdown" v-if="showUserMenu">
            <a href="#" class="dropdown-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              Mi Perfil
            </a>
            <a href="#" class="dropdown-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3" />
                <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
              </svg>
              Configuración
            </a>
            <div class="dropdown-divider"></div>
            <a href="#" class="dropdown-item logout" @click.prevent="toggleLogin">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                <polyline points="16 17 21 12 16 7" />
                <line x1="21" y1="12" x2="9" y2="12" />
              </svg>
              Cerrar Sesión
            </a>
          </div>
        </Transition>
      </div>

      <button v-else class="login-btn" @click="toggleLogin">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
          <polyline points="10 17 15 12 10 7" />
          <line x1="15" y1="12" x2="3" y2="12" />
        </svg>
        <span>Iniciar Sesión</span>
      </button>
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
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  font-family: 'Inter', sans-serif;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

/* ── Logo ── */
.header-logo {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  min-width: 200px;
}

.logo-img {
  height: 36px;
  width: auto;
  object-fit: contain;
  transition: transform 0.2s ease;
}

.logo-img:hover {
  transform: scale(1.03);
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
  border-radius: 12px;
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
  border-radius: 10px;
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
  border-radius: 8px;
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

/* ── Login button ── */
.login-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 20px;
  border: none;
  border-radius: 10px;
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
  border: 1.5px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: 'Inter', sans-serif;
}

.user-btn:hover {
  border-color: #d1d5db;
  background: #f9fafb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 9px;
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
  color: #1f2937;
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
  top: calc(100% + 8px);
  right: 0;
  min-width: 200px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 14px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.12), 0 2px 6px rgba(0, 0, 0, 0.04);
  padding: 6px;
  z-index: 200;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 10px;
  color: #374151;
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.15s ease;
}

.dropdown-item svg {
  width: 16px;
  height: 16px;
  color: #6b7280;
  flex-shrink: 0;
}

.dropdown-item:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.dropdown-item:hover svg {
  color: #dc2626;
}

.dropdown-divider {
  height: 1px;
  background: #f3f4f6;
  margin: 4px 8px;
}

.dropdown-item.logout {
  color: #dc2626;
}

.dropdown-item.logout svg {
  color: #dc2626;
}

.dropdown-item.logout:hover {
  background: #fef2f2;
}

/* ── Dropdown transition ── */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.96);
}
</style>