<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from './stores/auth'
import Header from './components/common/Header.vue';
import Aside from './components/common/Aside.vue';
import ConfirmDialog from './components/common/ConfirmDialog.vue';
import { useUIStore } from './stores/ui';

const route = useRoute()
const authStore = useAuthStore()
const uiStore = useUIStore()

const showLayout = computed(() => !route.meta.hideLayout)

// Auth check is handled by router guard in router/index.ts

</script>

<template>
  <div class="app-layout">
    <Header v-if="showLayout" />
    <Aside v-if="showLayout" />
    <main 
      class="app-main" 
      :class="{ 
        'no-layout': !showLayout,
        'collapsed': uiStore.isSidebarCollapsed && showLayout
      }"
    >
      <RouterView v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </RouterView>
    </main>
    <ConfirmDialog />
  </div>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', sans-serif;
  background: #f1f5f9; /* Slate 100 - Más claro para contraste */
  color: #1e293b;
  overflow-x: hidden;
}

.app-layout {
  min-height: 100vh;
}

.app-main {
  margin-left: 250px;
  margin-top: 64px;
  padding: 40px; /* Estándar para todas las vistas */
  min-height: calc(100vh - 64px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f8fafc; /* Fondo unificado */
}

.app-main.collapsed {
  margin-left: 72px;
}

.app-main.no-layout {
  margin-left: 0;
  margin-top: 0;
  padding: 0;
  min-height: 100vh;
}
/* ── Page Transitions ── */
.page-enter-active,
.page-leave-active {
  transition: all 0.3s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
