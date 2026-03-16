<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const isLoading = ref(false)
const rememberMe = ref(false)

async function handleLogin() {
  if (!email.value || !password.value) return
  isLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 1500))
  isLoading.value = false
  router.push('/')
}
</script>

<template>
  <div class="login-page">
    <!-- Left panel — red gradient -->
    <div class="login-branding">
      <div class="branding-content">
        <h1 class="branding-title">Salesianos Zaragoza</h1>
        <p class="branding-subtitle">
          Gestión integral de presupuestos, órdenes de compra e inventario del centro.
        </p>
        <div class="branding-features">
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 12l2 2 4-4" /><circle cx="12" cy="12" r="10" />
            </svg>
            <span>Control de presupuestos en tiempo real</span>
          </div>
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 12l2 2 4-4" /><circle cx="12" cy="12" r="10" />
            </svg>
            <span>Aprobación de órdenes de compra</span>
          </div>
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 12l2 2 4-4" /><circle cx="12" cy="12" r="10" />
            </svg>
            <span>Reportes y analíticas avanzadas</span>
          </div>
        </div>
      </div>
      <div class="branding-footer">
        <p>© 2026 Salesianos Zaragoza — Todos los derechos reservados</p>
      </div>
    </div>

    <!-- Right panel — white, form -->
    <div class="login-form-panel">
      <div class="form-container">
        <!-- Logo centered -->
        <div class="form-logo">
          <img src="/img/logoPrincipal.jpg" alt="Salesianos" class="form-logo-img" />
        </div>

        <div class="form-header">
          <h2>Iniciar Sesión</h2>
          <p>Introduce tus credenciales para acceder al panel</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <!-- Email -->
          <div class="input-group">
            <label for="email">Correo electrónico</label>
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="4" width="20" height="16" rx="2" />
                <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
              </svg>
              <input
                id="email"
                v-model="email"
                type="email"
                placeholder="ejemplo@zaragoza.salesianos.edu"
                autocomplete="email"
                required
              />
            </div>
          </div>

          <!-- Password -->
          <div class="input-group">
            <label for="password">Contraseña</label>
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
              <input
                id="password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="••••••••"
                autocomplete="current-password"
                required
              />
              <button
                type="button"
                class="toggle-password"
                @click="showPassword = !showPassword"
                tabindex="-1"
              >
                <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
                  <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" />
                  <path d="m1 1 22 22" />
                  <path d="M14.12 14.12a3 3 0 1 1-4.24-4.24" />
                </svg>
              </button>
            </div>
          </div>

          <!-- Options row -->
          <div class="form-options">
            <label class="checkbox-label">
              <input type="checkbox" v-model="rememberMe" />
              Recordarme
            </label>
            <a href="#" class="forgot-link">¿Olvidaste tu contraseña?</a>
          </div>

          <!-- Submit -->
          <button
            type="submit"
            class="submit-btn"
            :class="{ loading: isLoading }"
            :disabled="isLoading"
          >
            <svg v-if="!isLoading" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
              <polyline points="10 17 15 12 10 7" />
              <line x1="15" y1="12" x2="3" y2="12" />
            </svg>
            <div v-else class="spinner"></div>
            <span>{{ isLoading ? 'Accediendo...' : 'Acceder' }}</span>
          </button>
        </form>

        <!-- Register link -->
        <div class="register-section">
          <p>¿No tienes una cuenta?</p>
          <RouterLink to="/register" class="register-btn">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
              <circle cx="8.5" cy="7" r="4" />
              <line x1="20" y1="8" x2="20" y2="14" />
              <line x1="23" y1="11" x2="17" y2="11" />
            </svg>
            <span>Crear cuenta</span>
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

/* ─── Page Layout ─────────────────────────────── */
.login-page {
  display: flex;
  min-height: 100vh;
  font-family: 'Inter', sans-serif;
}

/* ─── Left Branding Panel — Soft Red Gradient upward ─ */
.login-branding {
  flex: 0 0 480px;
  background: linear-gradient(to top, #ffffff 0%, #fef2f2 15%, #fecaca 35%, #f87171 60%, #dc2626 85%, #b91c1c 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px;
  position: relative;
  overflow: hidden;
}

.login-branding::before {
  content: '';
  position: absolute;
  top: -100px;
  right: -100px;
  width: 360px;
  height: 360px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.12) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

.login-branding::after {
  content: '';
  position: absolute;
  bottom: -60px;
  left: -40px;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.08) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

.branding-content {
  position: relative;
  z-index: 1;
}



.branding-title {
  font-size: 34px;
  font-weight: 800;
  line-height: 1.15;
  margin-bottom: 16px;
  letter-spacing: -0.02em;
}

.branding-subtitle {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.75);
  line-height: 1.6;
  max-width: 340px;
  margin-bottom: 48px;
}

.branding-features {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
}

.feature-item svg {
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  color: #fef2f2;
}

.branding-footer {
  position: relative;
  z-index: 1;
}

.branding-footer p {
  font-size: 12px;
  color: rgba(120, 40, 40, 0.5);
}

/* ─── Right Form Panel ────────────────────────── */
.login-form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 48px 48px 80px;
  background: #ffffff;
}

.form-container {
  width: 100%;
  max-width: 600px;
  animation: formFadeIn 0.6s ease both;
}

@keyframes formFadeIn {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ─── Logo centered ───────────────────────────── */
.form-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
}

.form-logo-img {
  height: 64px;
  width: auto;
  object-fit: contain;
  border-radius: 10px;
}

/* ─── Header ──────────────────────────────────── */
.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 26px;
  font-weight: 800;
  color: #1f2937;
  margin-bottom: 8px;
  letter-spacing: -0.02em;
}

.form-header p {
  font-size: 14px;
  color: #9ca3af;
  line-height: 1.5;
}

/* ─── Form ────────────────────────────────────── */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.input-group label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  width: 18px;
  height: 18px;
  color: #9ca3af;
  pointer-events: none;
  transition: color 0.2s;
}

.input-wrapper input {
  width: 100%;
  height: 48px;
  padding: 0 48px 0 44px;
  border: 1.5px solid #e5e7eb;
  border-radius: 12px;
  background: #f9fafb;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: #1f2937;
  outline: none;
  transition: all 0.25s ease;
}

.input-wrapper input::placeholder {
  color: #c4c9d4;
}

.input-wrapper input:focus {
  border-color: #dc2626;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
}

.input-wrapper:focus-within .input-icon {
  color: #dc2626;
}

.toggle-password {
  position: absolute;
  right: 12px;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  transition: all 0.2s;
}

.toggle-password:hover {
  background: #f3f4f6;
  color: #6b7280;
}

.toggle-password svg {
  width: 18px;
  height: 18px;
}

/* ─── Form Options ────────────────────────────── */
.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
  user-select: none;
}

.checkbox-label input[type="checkbox"] {
  appearance: none;
  width: 18px;
  height: 18px;
  border: 1.5px solid #d1d5db;
  border-radius: 5px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  flex-shrink: 0;
}

.checkbox-label input[type="checkbox"]:checked {
  background: #dc2626;
  border-color: #dc2626;
}

.checkbox-label input[type="checkbox"]:checked::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 2px;
  width: 5px;
  height: 9px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.forgot-link {
  font-size: 13px;
  color: #dc2626;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #b91c1c;
  text-decoration: underline;
}

/* ─── Submit Button ───────────────────────────── */
.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 50px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  font-family: 'Inter', sans-serif;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 14px rgba(220, 38, 38, 0.35);
  margin-top: 4px;
}

.submit-btn svg {
  width: 20px;
  height: 20px;
}

.submit-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.45);
  transform: translateY(-2px);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.3);
}

.submit-btn:disabled {
  opacity: 0.85;
  cursor: not-allowed;
}

.submit-btn.loading {
  pointer-events: none;
}

/* ─── Spinner ─────────────────────────────────── */
.spinner {
  width: 20px;
  height: 20px;
  border: 2.5px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ─── Register Section ────────────────────────── */
.register-section {
  margin-top: 28px;
  text-align: center;
}

.register-section p {
  font-size: 13px;
  color: #9ca3af;
  margin-bottom: 12px;
}

.register-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 48px;
  border: 1.5px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.25s ease;
}

.register-btn svg {
  width: 20px;
  height: 20px;
  color: #dc2626;
}

.register-btn:hover {
  border-color: #dc2626;
  color: #dc2626;
  background: #fef2f2;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.1);
  transform: translateY(-1px);
}

/* ─── Responsive ──────────────────────────────── */
@media (max-width: 1024px) {
  .login-branding {
    display: none;
  }

  .login-form-panel {
    padding: 32px 24px;
  }
}
</style>