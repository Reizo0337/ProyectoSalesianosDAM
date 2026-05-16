<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import { useToast } from 'vue-toastification';

const toast = useToast();
const authStore = useAuthStore();
const router = useRouter();

async function handleLogout() {
  await authStore.logout();
  toast.info('Sesión cerrada correctamente');
  router.push('/login');
}
</script>

<template>
  <div class="view-container">
    <div class="header-section">
      <h1>Configuración</h1>
      <p>Gestiona los detalles de tu cuenta y preferencias.</p>
    </div>

    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar">{{ authStore.user?.nombre.charAt(0) }}</div>
        <div class="profile-info">
          <h3>{{ authStore.user?.nombre }}</h3>
          <p>{{ authStore.user?.correo }}</p>
        </div>
      </div>

      <div class="details-grid">
        <div class="detail-item">
          <label>Rol de Usuario</label>
          <span>{{ authStore.user?.rol }}</span>
        </div>
        <div class="detail-item">
          <label>Departamento</label>
          <span>{{ authStore.user?.nombreDepartamento || 'Sin asignar' }}</span>
        </div>
      </div>

      <div class="actions">
        <button @click="handleLogout" class="logout-btn">
          Cerrar Sesión
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-container { padding: 24px; }
.header-section { margin-bottom: 32px; }
.header-section h1 { font-size: 28px; font-weight: 700; color: #1f2937; }

.profile-card {
  background: white;
  max-width: 600px;
  border-radius: 4px;
  padding: 32px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.profile-header { display: flex; align-items: center; gap: 20px; margin-bottom: 32px; }
.avatar {
  width: 64px;
  height: 64px;
  background: #dc2626;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
}

.profile-info h3 { font-size: 20px; font-weight: 700; margin: 0; }
.profile-info p { color: #6b7280; margin: 0; }

.details-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 32px; padding-top: 24px; border-top: 1px solid #f3f4f6; }
.detail-item label { display: block; font-size: 12px; font-weight: 600; color: #9ca3af; text-transform: uppercase; margin-bottom: 4px; }
.detail-item span { font-size: 15px; font-weight: 500; color: #1f2937; }

.logout-btn {
  width: 100%;
  height: 44px;
  border: 1.5px solid #e5e7eb;
  border-radius: 4px;
  background: white;
  color: #ef4444;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.logout-btn:hover { background: #fef2f2; border-color: #ef4444; }
</style>
