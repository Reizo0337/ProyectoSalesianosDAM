<script setup lang="ts">
import Header from './components/common/Header.vue';
import Aside from './components/common/Aside.vue';
</script>

<template>
  <div class="app-layout">
    <Header />
    <Aside />
    <main class="app-main">
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
</style>
