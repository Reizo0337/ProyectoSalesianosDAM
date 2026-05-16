<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue';
import { useSupplierStore, type Supplier } from '@/stores/suppliers';
import Table from '../components/common/Table.vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useDialogStore } from '@/stores/dialog';
import { useToast } from 'vue-toastification';

const toast = useToast();
const dialogStore = useDialogStore();
const supplierStore = useSupplierStore();
const authStore = useAuthStore();
const router = useRouter();
 
const showModal = ref(false);
const isEditing = ref(false);
const currentId = ref<number | null>(null);
const searchQuery = ref('');
 
const form = ref({
  nombre: '',
  telefono: '',
  direccion: ''
});
 
const headers = ['ID', 'Nombre', 'Teléfono', 'Dirección', 'Acciones'];
 
const canManage = computed(() => {
  const rol = authStore.user?.rol;
  return rol === 'Administrador' || rol === 'Jefe de Equipo';
});
 
watch(() => authStore.user, (user) => {
  if (user) {
    supplierStore.fetchSuppliers();
  }
}, { immediate: true });
 
const filteredSuppliers = computed(() => {
  if (!searchQuery.value.trim()) return supplierStore.suppliers;
  const q = searchQuery.value.toLowerCase();
  return supplierStore.suppliers.filter(s => 
    String(s.idproveedor).includes(q) ||
    (s.nombre || '').toLowerCase().includes(q) ||
    (s.telefono || '').toLowerCase().includes(q) ||
    (s.direccion || '').toLowerCase().includes(q)
  );
});
 
function openCreate() {
  if (!canManage.value) return;
  isEditing.value = false;
  currentId.value = null;
  form.value = { nombre: '', telefono: '', direccion: '' };
  showModal.value = true;
}
 
function openEdit(supplier: any) {
  if (!canManage.value) return;
  isEditing.value = true;
  currentId.value = supplier.idproveedor;
  form.value = {
    nombre: supplier.nombre,
    telefono: supplier.telefono,
    direccion: supplier.direccion
  };
  showModal.value = true;
}
 
async function handleSubmit() {
  try {
    const data = {
      Nombre: form.value.nombre,
      Telefono: form.value.telefono,
      Direccion: form.value.direccion
    };
 
    if (isEditing.value && currentId.value) {
      await supplierStore.updateSupplier(currentId.value, data);
      toast.success('Proveedor actualizado con éxito');
    } else {
      await supplierStore.createSupplier(data);
      toast.success('Proveedor creado correctamente');
    }
    showModal.value = false;
  } catch (err) {
    toast.error('Error al procesar la solicitud');
  }
}
 
async function handleDelete(id: number) {
  if (!canManage.value) return;
  const confirmed = await dialogStore.confirm(
    "Eliminar Proveedor",
    "¿Estás seguro de eliminar este proveedor? Esta acción borrará permanentemente sus asociaciones con productos y catálogos."
  );
  if (confirmed) {
    await supplierStore.deleteSupplier(id);
  }
}
 
function viewProducts(id: number) {
  router.push(`/proveedores/${id}`);
}
</script>
 
<template>
  <div class="view-page animate-in">
    <header class="dashboard-header">
      <div class="header-left">
        <h1>Gestión de Proveedores</h1>
        <p class="subtitle">Directorio de partners y suministradores oficiales. <span class="result-count-inline" v-if="supplierStore.suppliers.length > 0">({{ filteredSuppliers.length }} resultados)</span></p>
      </div>
      <div class="header-actions">
        <div class="search-box" v-if="supplierStore.suppliers.length > 0">
          <span class="material-symbols-outlined search-icon">search</span>
          <input v-model="searchQuery" type="text" placeholder="Buscar proveedor..." class="search-input" />
        </div>
        <button v-if="canManage" class="create-btn" @click="openCreate">
          <span class="material-symbols-outlined">add_circle</span>
          Nuevo Proveedor
        </button>
      </div>
    </header>
 
    <Table
      :loading="supplierStore.loading"
      :headers="headers"
      :data="filteredSuppliers.map(s => [
        s.idproveedor,
        s.nombre,
        s.telefono,
        s.direccion,
        {
          type: 'actions',
          actions: [
            { label: 'Ver Productos', icon: 'inventory_2', class: 'btn-products', onClick: () => viewProducts(s.idproveedor) },
            { label: 'Editar', icon: 'edit', class: 'btn-edit', onClick: () => openEdit(s) },
            { label: 'Eliminar', icon: 'delete', class: 'btn-delete', onClick: () => handleDelete(s.idproveedor) }
          ]
        }
      ])"
      :onRowClick="(row) => viewProducts(row[0])"
    />
 
    <!-- Modal de Creación/Edición -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card animate-up">
        <div class="modal-header">
          <h2>{{ isEditing ? 'Editar Proveedor' : 'Nuevo Proveedor' }}</h2>
          <button @click="showModal = false" class="close-btn">&times;</button>
        </div>
        <form @submit.prevent="handleSubmit" class="modal-body">
          <div class="form-grid">
            <div class="form-group">
              <label>Nombre Comercial</label>
              <input v-model="form.nombre" required placeholder="Ej: Suministros Pro" class="form-input" />
            </div>
            <div class="form-group">
              <label>Teléfono</label>
              <input v-model="form.telefono" placeholder="976 000 000" class="form-input" />
            </div>
            <div class="form-group full-width">
              <label>Dirección Física</label>
              <input v-model="form.direccion" placeholder="Calle Ejemplo 123, Zaragoza" class="form-input" />
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn-cancel" @click="showModal = false">Cancelar</button>
            <button type="submit" class="btn-submit">{{ isEditing ? 'Guardar Cambios' : 'Registrar Proveedor' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
 
<style scoped>
.dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; gap: 2rem; }
.header-left h1 { font-size: 2.75rem; font-weight: 850; color: #0f172a; letter-spacing: -0.04em; line-height: 1.1; margin-bottom: 4px; }
.subtitle { color: #64748b; font-size: 1.15rem; font-weight: 500; }
.header-actions { display: flex; align-items: center; gap: 1rem; }
 
.search-box { display: flex; align-items: center; background: white; border: 1px solid #e2e8f0; border-radius: 8px; padding: 0 12px; width: 300px; transition: all 0.2s; }
.search-box:focus-within { border-color: #ef4444; box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1); }
.search-icon { font-size: 20px; color: #94a3b8; margin-right: 8px; }
.search-input { border: none; outline: none; background: transparent; font-size: 14px; color: #1e293b; padding: 10px 0; width: 100%; }
 
.create-btn {
  display: flex; align-items: center; gap: 8px; background: #dc2626; color: white;
  padding: 12px 24px; border-radius: 8px; font-weight: 700; border: none; cursor: pointer;
  transition: all 0.2s; box-shadow: 0 4px 6px -1px rgba(220, 38, 38, 0.2);
}
.create-btn:hover { background: #b91c1c; transform: translateY(-2px); box-shadow: 0 10px 15px -3px rgba(220, 38, 38, 0.3); }
 
.result-count-inline { font-weight: 700; color: #1e293b; margin-left: 8px; }
 
/* Modal Styles */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(15, 23, 42, 0.7); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-card { background: white; width: 90%; max-width: 600px; border-radius: 16px; overflow: hidden; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5); }
.modal-header { padding: 1.5rem; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
.modal-body { padding: 2rem; }
.modal-footer { padding: 1.5rem; background: #f8fafc; display: flex; justify-content: flex-end; gap: 1rem; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
.full-width { grid-column: span 2; }
.form-group label { display: block; font-size: 0.85rem; font-weight: 700; color: #475569; margin-bottom: 0.5rem; }
.form-input { width: 100%; padding: 0.875rem 1rem; border: 2px solid #e2e8f0; border-radius: 8px; font-size: 1rem; transition: all 0.2s; }
.form-input:focus { outline: none; border-color: #0f172a; box-shadow: 0 0 0 4px rgba(15, 23, 42, 0.1); }
.btn-submit { background: #0f172a; color: white; border: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; cursor: pointer; }
.btn-cancel { background: transparent; border: none; color: #64748b; font-weight: 600; cursor: pointer; }
.close-btn { background: none; border: none; font-size: 2rem; color: #94a3b8; cursor: pointer; }
</style>
