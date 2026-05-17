<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useToast } from 'vue-toastification';

const toast = useToast();
const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

// Estado: true para Registro (Back), false para Login (Front)
const isFlipped = ref(false);

const form = ref({
  nombre: '',
  apellidos: '',
  email: '',
  password: '',
  telefono: '',
  confirmPassword: ''
});

const showPassword = ref(false);
const isLoading = ref(false);
const errorMessage = ref('');
const isRegistered = ref(false); // Nuevo estado para éxito de registro

onMounted(() => {
  if (route.path === '/register') isFlipped.value = true;
});

function toggleFlip() {
  isFlipped.value = !isFlipped.value;
  errorMessage.value = '';
  window.history.pushState({}, '', isFlipped.value ? '/register' : '/login');
}

async function handleSubmit() {
  isLoading.value = true;
  errorMessage.value = '';

  if (!isFlipped.value) {
    const success = await authStore.login(form.value.email, form.value.password);
    if (success) {
      toast.success('¡Bienvenido de nuevo!');
      router.push('/');
    } else {
      errorMessage.value = authStore.error || 'Credenciales incorrectas';
      toast.error(errorMessage.value);
    }
  } else {
    if (form.value.password !== form.value.confirmPassword) {
      errorMessage.value = 'Las contraseñas no coinciden';
      toast.warning(errorMessage.value);
      isLoading.value = false;
      return;
    }
    const res = await authStore.register(
      form.value.nombre, form.value.apellidos, form.value.email, form.value.password, form.value.telefono
    );
    
    if (res.success) {
      isRegistered.value = true;
      toast.success('Registro completado. Esperando verificación administrativa.');
      // Opcional: Volver al login tras unos segundos
      setTimeout(() => {
        isRegistered.value = false;
        isFlipped.value = false;
      }, 5000);
    } else {
      errorMessage.value = res.message;
    }
  }
  isLoading.value = false;
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-scene" :class="{ 'is-flipped': isFlipped }">
      <div class="auth-card">
        
        <!-- CARA FRONT: LOGIN -->
        <div class="card-face face-front">
          <div class="auth-logo">
            <img src="/img/logoPrincipal.jpg" alt="Salesianos" />
          </div>
          <div class="auth-header">
            <p>Gestión integral Salesianos Zaragoza</p>
          </div>

          <form @submit.prevent="handleSubmit" class="auth-form">
            <div class="input-group">
              <label>Usuario / Email</label>
              <div class="input-wrapper">
                <span class="material-symbols-outlined input-icon">mail</span>
                <input v-model="form.email" type="text" placeholder="ejemplo@zaragoza.salesianos.edu" required />
              </div>
            </div>

            <div class="input-group">
              <label>Contraseña</label>
              <div class="input-wrapper">
                <span class="material-symbols-outlined input-icon">lock</span>
                <input v-model="form.password" :type="showPassword ? 'text' : 'password'" placeholder="••••••••" required />
                <button type="button" class="toggle-pass" @click="showPassword = !showPassword">
                  <span class="material-symbols-outlined">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
                </button>
              </div>
            </div>

            <div v-if="errorMessage && !isFlipped" class="error-box shake">
              <span class="material-symbols-outlined">error</span>
              <span>{{ errorMessage }}</span>
            </div>

            <button type="submit" class="auth-btn" :disabled="isLoading">
              <div v-if="isLoading" class="spinner"></div>
              <span>Iniciar Sesión</span>
            </button>

            <div class="auth-footer">
              <p>¿Eres nuevo en la plataforma?</p>
              <button type="button" @click="toggleFlip" class="toggle-btn">Solicitar Registro</button>
            </div>
          </form>
          
          <div class="auth-divider"></div>
          <div class="branding-info">
            <div class="feature"><span class="material-symbols-outlined">check_circle</span> Gestión en tiempo real</div>
          </div>
        </div>

        <!-- CARA BACK: REGISTRO -->
        <div class="card-face face-back">
          <template v-if="!isRegistered">
            <div class="auth-logo">
              <img src="/img/logoPrincipal.jpg" alt="Salesianos" />
            </div>
            <div class="auth-header">
              <h2>Crear Cuenta</h2>
              <p>Únete a nuestra plataforma de gestión</p>
            </div>

            <form @submit.prevent="handleSubmit" class="auth-form">
              <div class="form-row">
                <div class="input-group">
                  <label>Nombre</label>
                  <div class="input-wrapper">
                    <span class="material-symbols-outlined input-icon">person</span>
                    <input v-model="form.nombre" type="text" required />
                  </div>
                </div>
                <div class="input-group">
                  <label>Apellidos</label>
                  <div class="input-wrapper">
                    <span class="material-symbols-outlined input-icon">badge</span>
                    <input v-model="form.apellidos" type="text" required />
                  </div>
                </div>
              </div>
              
              <div class="input-group">
                <label>Correo Electrónico</label>
                <div class="input-wrapper">
                  <span class="material-symbols-outlined input-icon">mail</span>
                  <input v-model="form.email" type="email" required />
                </div>
              </div>

              <div class="form-row">
                  <div class="input-group">
                    <label>Contraseña</label>
                    <div class="input-wrapper">
                      <span class="material-symbols-outlined input-icon">lock</span>
                      <input v-model="form.password" type="password" required />
                    </div>
                  </div>
                  <div class="input-group">
                    <label>Confirmar</label>
                    <div class="input-wrapper">
                      <span class="material-symbols-outlined input-icon">lock_reset</span>
                      <input v-model="form.confirmPassword" type="password" required />
                    </div>
                  </div>
              </div>

              <div v-if="errorMessage && isFlipped" class="error-box shake">
                <span class="material-symbols-outlined">error</span>
                <span>{{ errorMessage }}</span>
              </div>

              <button type="submit" class="auth-btn" :disabled="isLoading">
                <div v-if="isLoading" class="spinner"></div>
                <span>Registrarse</span>
              </button>

              <div class="auth-footer">
                <p>¿Ya tienes una cuenta?</p>
                <button type="button" @click="toggleFlip" class="toggle-btn">Acceder al Login</button>
              </div>
            </form>
          </template>

          <!-- VISTA DE ÉXITO (POST-REGISTRO) -->
          <div v-else class="success-screen">
            <div class="success-icon">
              <span class="material-symbols-outlined">verified_user</span>
            </div>
            <h3>¡Solicitud Enviada!</h3>
            <p>Tu cuenta ha sido creada correctamente.</p>
            <div class="info-alert">
              <span class="material-symbols-outlined">info</span>
              <p>Por seguridad, un administrador debe verificar tu identidad antes de que puedas acceder.</p>
            </div>
            <button @click="isRegistered = false; isFlipped = false" class="auth-btn secondary">
              Volver al Login
            </button>
          </div>
        </div>

      </div>
      <p class="copyright">© 2026 Salesianos Zaragoza — Todos los derechos reservados</p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at top right, #1e293b, #0f172a);
  padding: 20px;
  perspective: 1500px; /* Necesario para el efecto 3D */
}

.auth-scene {
  width: 100%;
  max-width: 400px;
  height: 600px; /* Altura optimizada Login */
  transition: all 0.7s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.auth-scene.is-flipped {
  max-width: 560px;
  height: 660px; /* Altura optimizada Registro */
}

.auth-card {
  width: 100%;
  height: 100%;
  position: relative;
  transition: transform 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  transform-style: preserve-3d;
}

.auth-scene.is-flipped .auth-card {
  transform: rotateY(180deg);
}

.card-face {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  background: #1e293b;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
}

.face-back {
  transform: rotateY(180deg);
}

/* Estilos comunes internos */
.auth-logo { display: flex; justify-content: center; margin-bottom: 20px; }
.auth-logo img { height: 50px; filter: grayscale(1) invert(1) brightness(3); mix-blend-mode: lighten; }
.auth-header { text-align: center; margin-bottom: 24px; }
.auth-header h2 { color: white; font-size: 26px; font-weight: 850; margin-bottom: 4px; }
.auth-header p { color: #94a3b8; font-size: 13px; }

.auth-form { display: flex; flex-direction: column; gap: 16px; flex: 1; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.input-group { display: flex; flex-direction: column; gap: 6px; }
.input-group label { font-size: 12px; font-weight: 600; color: #cbd5e1; }

.input-wrapper { position: relative; display: flex; align-items: center; }
.input-icon { position: absolute; left: 12px; font-size: 18px; color: #64748b; }
.input-wrapper input {
  width: 100%; height: 44px; background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 10px;
  padding: 0 12px 0 40px; color: white; font-size: 14px; transition: all 0.2s;
}
.input-wrapper input:focus { border-color: #ef4444; outline: none; box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1); }

.toggle-pass { position: absolute; right: 10px; background: none; border: none; color: #64748b; cursor: pointer; }

.auth-btn {
  height: 48px; background: #ef4444; color: white; border: none; border-radius: 10px;
  font-weight: 700; cursor: pointer; margin-top: 8px; transition: all 0.3s;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  box-shadow: 0 8px 15px -3px rgba(239, 68, 68, 0.3);
}
.auth-btn:hover { transform: translateY(-2px); box-shadow: 0 10px 20px -5px rgba(239, 68, 68, 0.4); }

.auth-footer { text-align: center; margin-top: 14px; }
.auth-footer p { color: #94a3b8; font-size: 13px; margin-bottom: 2px; }
.toggle-btn { background: none; border: none; color: #ef4444; font-weight: 700; cursor: pointer; font-size: 13px; }
.toggle-btn:hover { text-decoration: underline; }

.error-box {
  background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2);
  color: #f87171; padding: 10px; border-radius: 10px; font-size: 12px;
  display: flex; align-items: center; gap: 6px;
}

.shake { animation: shake 0.4s cubic-bezier(.36,.07,.19,.97) both; }
@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-3px, 0, 0); }
}

.auth-divider { height: 1px; background: rgba(255, 255, 255, 0.1); margin: 20px 0; }
.branding-info { display: flex; flex-direction: column; gap: 8px; }
.feature { display: flex; align-items: center; gap: 8px; color: #94a3b8; font-size: 12px; }
.feature .material-symbols-outlined { color: #22c55e; font-size: 16px; }

/* Pantalla de Éxito */
.success-screen {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 20px;
  animation: fadeIn 0.5s ease-out;
}

.success-icon {
  width: 80px;
  height: 80px;
  background: rgba(34, 197, 94, 0.1);
  color: #22c55e;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-icon .material-symbols-outlined {
  font-size: 48px;
}

.success-screen h3 {
  color: white;
  font-size: 24px;
  font-weight: 800;
}

.success-screen p {
  color: #94a3b8;
  font-size: 14px;
}

.info-alert {
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.2);
  padding: 16px;
  border-radius: 12px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
  text-align: left;
  margin-top: 10px;
}

.info-alert .material-symbols-outlined {
  color: #3b82f6;
  font-size: 20px;
}

.auth-btn.secondary {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: none;
  width: 100%;
}

.auth-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: #ef4444;
}

.copyright { text-align: center; margin-top: 20px; color: #475569; font-size: 11px; }

.spinner {
  width: 18px; height: 18px; border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
