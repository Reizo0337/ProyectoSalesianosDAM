<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import api from '@/api/axios';
import { useToast } from 'vue-toastification';
import { useDialogStore } from '@/stores/dialog';
import Table from '@/components/common/Table.vue';

const toast = useToast();
const dialogStore = useDialogStore();
const authStore = useAuthStore();
const users = ref<any[]>([]);
const roles = ref<any[]>([]);
const departments = ref<any[]>([]);
const loading = ref(true);

// Paginación
const currentPage = ref(1);
const pageSize = 10;
const totalPages = computed(() => Math.ceil(verifiedUsers.value.length / pageSize));
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return verifiedUsers.value.slice(start, start + pageSize);
});

function nextPage() { if (currentPage.value < totalPages.value) currentPage.value++; }
function prevPage() { if (currentPage.value > 1) currentPage.value--; }

function getRolClass(rol: string) {
  if (!rol) return 'badge-default';
  const r = rol.toLowerCase();
  if (r.includes('admin')) return 'badge-admin';
  if (r.includes('jefe')) return 'badge-jefe';
  if (r.includes('contable') || r.includes('vendedor')) return 'badge-user';
  return 'badge-default';
}

// Modales
const showVerifyModal = ref(false);
const showEditModal = ref(false);
const selectedUser = ref<any>(null);

// Formulario de verificación/edición
const form = ref({
  idRol: '',
  idDepartamento: '',
  nombre: '',
  apellidos: '',
  correo: '',
  password: ''
});

const isFormValid = computed(() => {
  if (!form.value.idRol) return false;
  const selectedRole = roles.value.find(r => r.id == form.value.idRol);
  const isJefe = selectedRole && selectedRole.nombre === 'Jefe de Equipo';
  if (isJefe && !form.value.idDepartamento) return false;
  return true;
});

const isJefeSelected = computed(() => {
  const selectedRole = roles.value.find(r => r.id == form.value.idRol);
  return selectedRole && selectedRole.nombre === 'Jefe de Equipo';
});

async function fetchData() {
  loading.value = true;
  try {
    const [uRes, rRes, dRes] = await Promise.all([
      api.get('/usuarios'),
      api.get('/usuarios/roles'),
      api.get('/usuarios/departamentos')
    ]);
    users.value = uRes.data;
    roles.value = rRes.data;
    departments.value = dRes.data;
  } catch (e) {
    console.error("Error fetching users data", e);
  } finally {
    loading.value = false;
  }
}

const pendingUsers = computed(() => users.value.filter(u => String(u.isVerified) === 'false'));
const verifiedUsers = computed(() => users.value.filter(u => String(u.isVerified) === 'true'));

function openVerify(user: any) {
  selectedUser.value = user;
  form.value = {
    idRol: '',
    idDepartamento: '',
    nombre: user.nombre,
    apellidos: user.apellidos || '',
    correo: user.correo,
    password: ''
  };
  showVerifyModal.value = true;
}

function openEdit(user: any) {
  selectedUser.value = user;
  form.value = {
    idRol: roles.value.find(r => r.nombre === user.rol)?.id || '',
    idDepartamento: user.idDepartamento || '',
    nombre: user.nombre,
    apellidos: user.apellidos || '',
    correo: user.correo,
    password: ''
  };
  showEditModal.value = true;
}

async function handleVerify() {
  const selectedRole = roles.value.find(r => r.id == form.value.idRol);
  const isSpecialRole = selectedRole && (selectedRole.nombre === 'Administrador' || selectedRole.nombre === 'Contable');
  
  if (!form.value.idRol) {
    toast.warning("Debes asignar un rol");
    return;
  }
  
  if (!isSpecialRole && !form.value.idDepartamento) {
    toast.warning("Debes asignar un departamento para este rol");
    return;
  }
  
  const res = await api.post(`/usuarios/verify/${selectedUser.value.IdUsuario}`, {
    idRol: form.value.idRol,
    idDepartamento: form.value.idDepartamento || null
  });
  
  if (res.data.status === 'success') {
    toast.success("Usuario verificado con éxito");
    showVerifyModal.value = false;
    fetchData();
  } else {
    toast.error("Error al verificar usuario");
  }
}

async function handleUpdate() {
  const res = await api.post(`/usuarios/update/${selectedUser.value.IdUsuario}`, {
    nombre: form.value.nombre,
    apellidos: form.value.apellidos,
    correo: form.value.correo,
    idRol: form.value.idRol,
    idDepartamento: form.value.idDepartamento
  });
  
  if (res.data.status === 'success') {
    if (form.value.password) {
        await api.post(`/usuarios/password/${selectedUser.value.IdUsuario}`, { 
          newPassword: form.value.password 
        });
    }
    showEditModal.value = false;
    fetchData();
  } else {
    alert("Error al actualizar usuario");
  }
}

async function deleteUser(id: number) {
  const confirmed = await dialogStore.confirm(
    "Eliminar Usuario",
    "¿Estás seguro de que deseas eliminar permanentemente a este usuario? Esta acción no se puede deshacer."
  );
  
  if (!confirmed) return;
  
  const res = await api.delete(`/usuarios/delete/${id}`);
  
  if (res.data.status === 'success') {
    toast.success("Usuario eliminado");
    fetchData();
  } else {
    toast.error("Error al eliminar usuario");
  }
}

onMounted(fetchData);
</script>

<template>
  <div class="dashboard-page">
    <header class="dashboard-header">
      <div class="header-content">
        <h1>Gestión de Usuarios</h1>
        <p class="subtitle">Administración de accesos, roles y departamentos.</p>
      </div>
    </header>

    <!-- Sección de Pendientes -->
    <section v-if="pendingUsers.length > 0" class="dashboard-section">
      <div class="section-header">
        <h2>Pendientes de Verificación</h2>
        <span class="badge red">{{ pendingUsers.length }}</span>
      </div>
      
      <div class="pending-grid">
        <div v-for="(user, index) in pendingUsers" 
             :key="user.IdUsuario" 
             class="pending-card animate-in"
             :style="{ animationDelay: `${index * 0.1}s` }">
          <div class="user-info">
            <div class="avatar">{{ user.nombre.charAt(0) }}</div>
            <div class="details">
              <h3>{{ user.nombre }} {{ user.apellidos }}</h3>
              <p>{{ user.correo }}</p>
            </div>
          </div>
          <div class="actions">
            <button @click="openVerify(user)" class="btn-verify">
              <span class="material-symbols-outlined">check_circle</span>
              Verificar
            </button>
            <button @click="deleteUser(user.IdUsuario)" class="btn-reject">
              <span class="material-symbols-outlined">delete</span>
            </button>
          </div>
        </div>
      </div>
    </section>
 
    <!-- Lista Global de Usuarios -->
    <section class="dashboard-section">
      <div class="section-header">
        <h2>Usuarios Activos</h2>
        <span class="badge blue">{{ verifiedUsers.length }}</span>
      </div>
 
      <div class="table-card">
        <Table 
          :loading="loading"
          :headers="['Usuario', 'Correo', 'Rol', 'Departamento', 'Acciones']"
          :data="verifiedUsers.map(user => [
            { 
              component: 'UserCell', 
              props: { nombre: `${user.nombre} ${user.apellidos || ''}`.trim(), inicial: user.nombre.charAt(0) } 
            },
            user.correo,
            { 
              component: 'Badge', 
              props: { text: user.rol || 'Sin Rol', class: getRolClass(user.rol) } 
            },
            user.nombreDepartamento || 'Sin Dept.',
            {
              type: 'actions',
              actions: [
                { icon: 'edit', label: 'Editar', class: 'btn-edit', onClick: () => openEdit(user) },
                { icon: 'delete', label: 'Eliminar', class: 'btn-delete', onClick: () => deleteUser(user.IdUsuario) }
              ]
            }
          ])"
        />
      </div>
    </section>

    <!-- Modal de Verificación -->
    <div v-if="showVerifyModal" class="modal-overlay">
      <div class="modal-card">
        <div class="modal-header">
          <h2>Verificar Usuario</h2>
          <button @click="showVerifyModal = false" class="close-btn">&times;</button>
        </div>
        <p class="modal-desc">Asigna un rol y departamento para activar la cuenta de <strong>{{ selectedUser?.nombre }}</strong>.</p>
        
        <div class="form-grid">
          <div class="form-group">
            <label>Rol</label>
            <select v-model="form.idRol">
              <option value="" disabled>Selecciona un rol</option>
              <option v-for="rol in roles" :key="rol.id" :value="rol.id">{{ rol.nombre }}</option>
            </select>
          </div>
          <div class="form-group" v-if="isJefeSelected">
            <label>Departamento</label>
            <select v-model="form.idDepartamento">
              <option value="" disabled>Selecciona un departamento</option>
              <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.nombre }}</option>
            </select>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="showVerifyModal = false" class="btn-ghost">Cancelar</button>
          <button @click="handleVerify" :disabled="!isFormValid" class="btn-primary">
            Confirmar Registro
          </button>
        </div>
      </div>
    </div>

    <!-- Modal de Edición -->
    <div v-if="showEditModal" class="modal-overlay">
      <div class="modal-card">
        <div class="modal-header">
          <h2>Editar Usuario</h2>
          <button @click="showEditModal = false" class="close-btn">&times;</button>
        </div>
        
        <div class="form-grid">
          <div class="form-group">
            <label>Nombre</label>
            <input v-model="form.nombre" type="text" />
          </div>
          <div class="form-group">
            <label>Apellidos</label>
            <input v-model="form.apellidos" type="text" />
          </div>
          <div class="form-group full">
            <label>Correo Electrónico</label>
            <input v-model="form.correo" type="email" />
          </div>
          <div class="form-group">
            <label>Rol</label>
            <select v-model="form.idRol">
              <option v-for="rol in roles" :key="rol.id" :value="rol.id">{{ rol.nombre }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Departamento</label>
            <select v-model="form.idDepartamento">
              <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.nombre }}</option>
            </select>
          </div>
          <div class="form-group full">
            <label>Nueva Contraseña (vacío para no cambiar)</label>
            <input v-model="form.password" type="password" placeholder="••••••••" />
          </div>
        </div>

        <div class="modal-actions">
          <button @click="showEditModal = false" class="btn-ghost">Cancelar</button>
          <button @click="handleUpdate" class="btn-primary">Guardar Cambios</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-page {
  padding: 2.5rem;
  max-width: 1600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

.header-content h1 {
  font-size: 3rem;
  font-weight: 850;
  letter-spacing: -0.04em;
  background: linear-gradient(135deg, #0f172a 0%, #334155 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #64748b;
  font-size: 1.15rem;
  font-weight: 500;
}

.dashboard-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.section-header h2 {
  font-size: 1.75rem;
  font-weight: 850;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 800;
  color: white;
}
.badge.red { background: #ef4444; }
.badge.blue { background: #3b82f6; }

/* Pending Grid */
.pending-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 1.5rem;
}

.pending-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.avatar {
  width: 48px;
  height: 48px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: #0f172a;
  font-size: 1.25rem;
}

.details h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 2px;
}

.details p {
  font-size: 0.9rem;
  color: #64748b;
}

.pending-card .actions {
  display: flex;
  gap: 10px;
}

.btn-verify {
  background: #ef4444;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 700;
  font-size: 0.85rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-reject {
  background: #f8fafc;
  color: #94a3b8;
  border: 1px solid #e2e8f0;
  width: 36px;
  height: 36px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}


/* Modales */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-card {
  background: white;
  width: 100%;
  max-width: 600px;
  border-radius: 8px;
  padding: 2.5rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.modal-header h2 {
  font-size: 2rem;
  font-weight: 850;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.modal-desc {
  color: #64748b;
  margin-bottom: 2rem;
  font-size: 1.1rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.form-group.full {
  grid-column: 1 / -1;
}

.form-group label {
  display: block;
  font-size: 0.85rem;
  font-weight: 700;
  color: #64748b;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.form-group input, .form-group select {
  width: 100%;
  height: 48px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 0 16px;
  color: #0f172a;
}

.modal-actions {
  margin-top: 2.5rem;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.btn-primary {
  background: #ef4444;
  color: white;
  padding: 0.875rem 1.75rem;
  border-radius: 4px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  box-shadow: 0 10px 15px -3px rgba(239, 68, 68, 0.2);
}

.btn-ghost {
  background: transparent;
  color: #64748b;
  padding: 0.875rem 1.75rem;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
  font-weight: 700;
  cursor: pointer;
}

.loading-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  backdrop-filter: blur(4px);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f1f5f9;
  border-top-color: #ef4444;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.close-btn {
  background: none;
  border: none;
  color: #64748b;
  font-size: 2rem;
  cursor: pointer;
}
.btn-primary:disabled {
  background: #94a3b8;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* ── Animaciones de Entrada ── */
.animate-in {
  animation: slideUpFade 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
}

@keyframes slideUpFade {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
