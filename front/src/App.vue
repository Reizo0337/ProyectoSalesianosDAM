<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from './stores/auth'
import Header from './components/common/Header.vue';
import Aside from './components/common/Aside.vue';

const route = useRoute()
const authStore = useAuthStore()

const showLayout = computed(() => !route.meta.hideLayout)

// Auth check is handled by router guard in router/index.ts

</script>

<template>
  <div class="app-layout">
    <Header v-if="showLayout" />
    <Aside v-if="showLayout" />
    <main class="app-main" :class="{ 'no-layout': !showLayout }">
      <RouterView />
    </main>
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
  background: #f8f9fb;
  color: #1f2937;
  overflow-x: hidden;
}

.app-layout {
  min-height: 100vh;
}

.app-main {
  margin-left: 250px;
  margin-top: 64px;
  padding: 28px;
  min-height: calc(100vh - 64px);
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f8f9fb;
}

.app-main.no-layout {
  margin-left: 0;
  margin-top: 0;
  padding: 0;
  min-height: 100vh;
}
</style>
