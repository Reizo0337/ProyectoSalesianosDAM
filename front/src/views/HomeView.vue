<script setup lang="ts">
</script>

<template>
  <div class="home-view">
    <div class="welcome-card">
      <div class="welcome-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
          <polyline points="9 22 9 12 15 12 15 22" />
        </svg>
      </div>
      <h1>Bienvenido a ZarGestion</h1>
      <p>Sistema de gestión integral para Salesianos María Auxiliadora</p>
    </div>
  </div>
</template>

<style scoped>
.home-view {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 120px);
}

.welcome-card {
  text-align: center;
  padding: 48px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  max-width: 480px;
}

.welcome-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, rgba(220, 38, 38, 0.1), rgba(220, 38, 38, 0.05));
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-icon svg {
  width: 32px;
  height: 32px;
  color: #dc2626;
}

.welcome-card h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.welcome-card p {
  font-size: 15px;
  color: #6b7280;
  line-height: 1.5;
}
</style>
