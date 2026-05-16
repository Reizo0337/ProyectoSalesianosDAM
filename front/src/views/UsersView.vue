<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useUserStore } from '@/stores/users';
import { useAuthStore } from '@/stores/auth';
import { useToast } from 'vue-toastification';
import { useDialogStore } from '@/stores/dialog';
import Table from '@/components/common/Table.vue';
 
const userStore = useUserStore();
const authStore = useAuthStore();
const toast = useToast();
const dialogStore = useDialogStore();
 
const loading = ref(false);
const showVerifyModal = ref(false);
const showEditModal = ref(false);
const selectedUser = ref<any>(null);
const roles = ref<any[]>([]);
const departments = ref<any[]>([]);
 
const form = ref({
  idRol: '',
  idDepartamento: ''
});
 
const editForm = ref({
  nombre: '',
  apellidos: '',
  idRol: '',
  idDepartamento: ''
});
 
const pendingUsers = computed(() => userStore.users.filter(u => u.verificado === 'false' || u.verificado === false));
const verifiedUsers = computed(() => userStore.users.filter(u => u.verificado === 'true' || u.verificado === true));
 
const isJefeSelected = computed(() => {
  const role = roles.value.find(r => r.id === form.value.idRol);
  return role?.nombre === 'Jefe de Equipo';
});
 
const isEditJefeSelected = computed(() => {
  const role = roles.value.find(r => r.id === editForm.value.idRol);
  return role?.nombre === 'Jefe de Equipo';
});
 
async function fetchData() {
  loading.value = true;
  try {
    await userStore.fetchUsers();
    roles.value = await userStore.fetchRoles();
    departments.value = await userStore.fetchDepartments();
  } catch (error) {
    toast.error('Error al cargar datos');
  } finally {
    loading.value = false;
  }
}
 
function openVerify(user: any) {
  selectedUser.value = user;
  form.value = { idRol: '', idDepartamento: '' };
  showVerifyModal.value = true;
}
 
async function handleVerify() {
  if (!form.value.idRol) return;
  try {
    await userStore.verifyUser(selectedUser.value.IdUsuario, form.value.idRol, form.value.idDepartamento);
    toast.success('Usuario verificado correctamente');
    showVerifyModal.value = false;
    fetchData();
  } catch (error) {
    toast.error('Error al verificar');
  }
}
 
function openEdit(user: any) {
  selectedUser.value = user;
  editForm.value = {
    nombre: user.nombre,
    apellidos: user.apellidos,
    idRol: user.idRol || '',
    idDepartamento: user.idDepartamento || ''
  };
  showEditModal.value = true;
}
 
async function handleUpdate() {
  try {
    await userStore.updateUser(selectedUser.value.IdUsuario, editForm.value);
    toast.success('Usuario actualizado');
    showEditModal.value = false;
    fetchData();
  } catch (error) {
    toast.error('Error al actualizar');
  }
}
 
async function deleteUser(id: number) {
  const confirmed = await dialogStore.confirm('Eliminar Usuario', '¿Estás seguro? Esta acción es permanente.');
  if (confirmed) {
    await userStore.deleteUser(id);
    toast.success('Usuario eliminado');
    fetchData();
  }
}
 
function getRolClass(rol: string) {
  const r = (rol || '').toLowerCase();
  if (r.includes('admin')) return 'red';
  if (r.includes('jefe')) return 'blue';
  if (r.includes('contable')) return 'green';
  return 'gray';
}
 
onMounted(fetchData);
</script>
 
<template>
  <div class="view-page animate-in">
    <header class="dashboard-header">
      <div class="header-left">
        <h1>Gestión de Usuarios</h1>
        <p class="subtitle">Administra los accesos, roles y departamentos de la plataforma.</p>
      </div>
    </header>
 
    <!-- Lista de Usuarios Pendientes -->
    <section v-if="pendingUsers.length > 0" class="dashboard-section" style="margin-bottom: 3rem;">
      <div class="section-header">
        <span class="material-symbols-outlined orange-icon">pending_actions</span>
        <h2>Solicitudes Pendientes</h2>
        <span class="badge orange">{{ pendingUsers.length }}</span>
      </div>
 
      <Table 
        :loading="loading"
        :headers="['Usuario', 'Correo', 'Departamento', 'Acciones']"
        :data="pendingUsers.map(user => [
          { 
            component: 'UserCell', 
            props: { nombre: `${user.nombre} ${user.apellidos || ''}`.trim(), inicial: user.nombre.charAt(0) } 
          },
          user.correo,
          user.nombreDepartamento || 'Sin Dept.',
          {
            type: 'actions',
            actions: [
              { icon: 'how_to_reg', label: 'Verificar', class: 'btn-edit', onClick: () => openVerify(user) },
              { icon: 'delete', label: 'Rechazar', class: 'btn-delete', onClick: () => deleteUser(user.IdUsuario) }
            ]
          }
        ])"
      />
    </section>
 
    <!-- Lista Global de Usuarios -->
    <section class="dashboard-section">
      <div class="section-header">
        <span class="material-symbols-outlined blue-icon">group</span>
        <h2>Usuarios Activos</h2>
        <span class="badge blue">{{ verifiedUsers.length }}</span>
      </div>
 
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
    </section>
 
    <!-- Modal de Verificación -->
    <div v-if="showVerifyModal" class="modal-overlay" @click.self="showVerifyModal = false">
      <div class="modal-card animate-up">
        <div class="modal-header">
          <h2>Verificar Usuario</h2>
          <button @click="showVerifyModal = false" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <p class="modal-desc">Asigna rol y departamento a <strong>{{ selectedUser?.nombre }}</strong>.</p>
          <div class="form-grid">
            <div class="form-group">
              <label>Rol</label>
              <select v-model="form.idRol" class="form-select">
                <option value="" disabled>Selecciona un rol</option>
                <option v-for="rol in roles" :key="rol.id" :value="rol.id">{{ rol.nombre }}</option>
              </select>
            </div>
            <div class="form-group" v-if="isJefeSelected">
              <label>Departamento</label>
              <select v-model="form.idDepartamento" class="form-select">
                <option value="" disabled>Selecciona un departamento</option>
                <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.nombre }}</option>
              </select>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showVerifyModal = false" class="btn-cancel">Cancelar</button>
          <button @click="handleVerify" class="btn-submit">Activar Cuenta</button>
        </div>
      </div>
    </div>
  </div>
</template>
 
<style scoped>
.dashboard-header { margin-bottom: 3.5rem; }
.dashboard-header h1 { font-size: 2.75rem; font-weight: 850; color: #0f172a; letter-spacing: -0.04em; margin-bottom: 4px; }
.subtitle { color: #64748b; font-size: 1.15rem; }
 
.dashboard-section { margin-bottom: 2rem; }
.section-header { display: flex; align-items: center; gap: 12px; margin-bottom: 1.5rem; }
.section-header h2 { font-size: 1.5rem; font-weight: 700; color: #1e293b; }
 
.orange-icon { color: #f59e0b; }
.blue-icon { color: #3b82f6; }
 
.badge { padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 700; }
.badge.orange { background: #fff7ed; color: #ea580c; border: 1px solid #ffedd5; }
.badge.blue { background: #eff6ff; color: #2563eb; border: 1px solid #dbeafe; }
 
/* Modal Styles */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(15, 23, 42, 0.7); backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.modal-card { background: white; width: 90%; max-width: 500px; border-radius: 16px; overflow: hidden; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5); }
.modal-header { padding: 1.5rem; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
.modal-body { padding: 2rem; }
.modal-footer { padding: 1.5rem; background: #f8fafc; display: flex; justify-content: flex-end; gap: 1rem; }
.form-select { width: 100%; padding: 12px; border-radius: 8px; border: 1px solid #e2e8f0; outline: none; }
.btn-submit { background: #0f172a; color: white; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; }
.btn-cancel { background: transparent; border: none; color: #64748b; font-weight: 600; cursor: pointer; }
</style>
