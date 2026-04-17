<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import api from '@/api/axios'

const router = useRouter()
const nombre = ref('')
const apellidos = ref('')
const email = ref('')
const password = ref('')
const telefono = ref('')
const isLoading = ref(false)
const errorMsg = ref('')

async function handleRegister() {
  if (!nombre.value || !email.value || !password.value) {
    errorMsg.value = 'Por favor, rellena todos los campos obligatorios'
    return
  }

  isLoading.value = true
  errorMsg.value = ''
  
  try {
    const response = await api.post('/register', {
      nombre: nombre.value,
      apellidos: apellidos.value,
      email: email.value,
      password: password.value,
      telefono: telefono.value
    })

    if (response.data.status === 'success') {
      router.push('/login')
    } else {
      errorMsg.value = response.data.message || 'Error en el registro'
    }
  } catch (err: any) {
    errorMsg.value = err.response?.data?.message || 'Error de conexión con el servidor'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-branding">
      <div class="branding-content">
        <h1 class="branding-title">Únete a ZarGestion</h1>
        <p class="branding-subtitle">
          Regístrate para empezar a gestionar los recursos de tu departamento de forma eficiente.
        </p>
        <div class="branding-features">
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="8.5" cy="7" r="4" />
            </svg>
            <span>Perfil personalizado por departamento</span>
          </div>
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
            <span>Acceso seguro y encriptado</span>
          </div>
        </div>
      </div>
      <div class="branding-footer">
        <p>© 2026 Salesianos Zaragoza</p>
      </div>
    </div>

    <div class="login-form-panel">
      <div class="form-container">
        <div class="form-header">
          <h2>Registro de Usuario</h2>
          <p>Crea tu cuenta para acceder al panel de gestión</p>
        </div>

        <form @submit.prevent="handleRegister" class="login-form">
          <div class="input-grid">
            <div class="input-group">
              <label for="nombre">Nombre</label>
              <div class="input-wrapper">
                <input id="nombre" v-model="nombre" type="text" required placeholder="Tu nombre" />
              </div>
            </div>
            <div class="input-group">
              <label for="apellidos">Apellidos</label>
              <div class="input-wrapper">
                <input id="apellidos" v-model="apellidos" type="text" placeholder="Tus apellidos" />
              </div>
            </div>
          </div>

          <div class="input-group">
            <label for="email">Correo electrónico</label>
            <div class="input-wrapper">
              <input id="email" v-model="email" type="email" required placeholder="ejemplo@zaragoza.salesianos.edu" />
            </div>
          </div>

          <div class="input-group">
            <label for="telefono">Teléfono</label>
            <div class="input-wrapper">
              <input id="telefono" v-model="telefono" type="tel" placeholder="600 000 000" />
            </div>
          </div>

          <div class="input-group">
            <label for="password">Contraseña</label>
            <div class="input-wrapper">
              <input id="password" v-model="password" type="password" required placeholder="••••••••" />
            </div>
          </div>

          <div v-if="errorMsg" class="error-banner">
            <span>{{ errorMsg }}</span>
          </div>

          <button type="submit" class="submit-btn" :class="{ loading: isLoading }" :disabled="isLoading">
            <div v-if="isLoading" class="spinner"></div>
            <span>{{ isLoading ? 'Registrando...' : 'Registrarse ahora' }}</span>
          </button>
        </form>

        <div class="register-section">
          <p>¿Ya tienes una cuenta?</p>
          <RouterLink to="/login" class="register-btn">
            <span>Iniciar Sesión</span>
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

.login-page {
  display: flex;
  min-height: 100vh;
  font-family: 'Inter', sans-serif;
}

.login-branding {
  flex: 0 0 420px;
  background: linear-gradient(to top, #dc2626, #991b1b);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px;
}

.branding-title { font-size: 32px; font-weight: 800; margin-bottom: 16px; }
.branding-subtitle { font-size: 15px; opacity: 0.8; margin-bottom: 48px; line-height: 1.6; }
.branding-features { display: flex; flex-direction: column; gap: 20px; }
.feature-item { display: flex; align-items: center; gap: 12px; font-size: 14px; font-weight: 500; }
.feature-item svg { width: 20px; height: 20px; color: #fecaca; }
.branding-footer { font-size: 12px; opacity: 0.5; }

.login-form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  padding: 48px;
}

.form-container { width: 100%; max-width: 500px; }
.form-header { text-align: center; margin-bottom: 32px; }
.form-header h2 { font-size: 28px; font-weight: 800; color: #1f2937; margin-bottom: 8px; }
.form-header p { color: #9ca3af; font-size: 14px; }

.login-form { display: flex; flex-direction: column; gap: 20px; }
.input-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.input-group { display: flex; flex-direction: column; gap: 6px; }
.input-group label { font-size: 13px; font-weight: 600; color: #374151; }
.input-wrapper input {
  width: 100%;
  height: 44px;
  padding: 0 16px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  background: #f9fafb;
  outline: none;
  transition: all 0.2s;
}
.input-wrapper input:focus { border-color: #dc2626; background: #fff; box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1); }

.error-banner { padding: 12px; background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; color: #dc2626; font-size: 13px; text-align: center; }

.submit-btn {
  height: 48px;
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.2);
}
.submit-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(220, 38, 38, 0.3); }

.register-section { margin-top: 32px; text-align: center; }
.register-section p { font-size: 13px; color: #9ca3af; margin-bottom: 12px; }
.register-btn { color: #dc2626; text-decoration: none; font-weight: 600; font-size: 14px; }
.register-btn:hover { text-decoration: underline; }

.spinner { width: 18px; height: 18px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 900px) {
  .login-branding { display: none; }
}
</style>
