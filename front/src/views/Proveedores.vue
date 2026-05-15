<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue';
import { useSupplierStore, type Supplier } from '@/stores/suppliers';
import Table from '../components/common/Table.vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

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

const headers = ['ID', 'Nombre', 'Teléfono', 'Dirección'];

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
  const data = {
    Nombre: form.value.nombre,
    Telefono: form.value.telefono,
    Direccion: form.value.direccion
  };

  if (isEditing.value && currentId.value) {
    await supplierStore.updateSupplier(currentId.value, data);
  } else {
    await supplierStore.createSupplier(data);
  }
  showModal.value = false;
}

async function handleDelete(id: number) {
  if (!canManage.value) return;
  if (confirm('¿Estás seguro de eliminar este proveedor? Se borrarán sus asociaciones con productos.')) {
    await supplierStore.deleteSupplier(id);
  }
}

function viewProducts(id: number) {
  router.push(`/proveedores/${id}`);
}
</script>

<template>
  <div class="view-container animate-fade-in">
    <div class="header-section">
      <div class="title-box">
        <h1>Gestión de Proveedores</h1>
        <p>Directorio de partners y suministradores oficiales. <span class="result-count-inline" v-if="supplierStore.suppliers.length > 0">({{ filteredSuppliers.length }} resultados)</span></p>
      </div>
      <div class="header-actions">
        <div class="search-box" v-if="supplierStore.suppliers.length > 0">
          <span class="material-symbols-outlined search-icon">search</span>
          <input v-model="searchQuery" type="text" placeholder="Buscar proveedor..." class="search-input" />
        </div>
        <button v-if="canManage" class="create-btn" @click="openCreate">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20">
            <path d="M12 5v14M5 12h14" />
          </svg>
          Nuevo Proveedor
        </button>
      </div>
    </div>

    <div class="table-card">
      <div v-if="supplierStore.loading" class="loading-state">
        <div class="spinner"></div>
        <p>Cargando proveedores...</p>
      </div>
      <Table
        v-else-if="filteredSuppliers.length > 0"
        :headers="headers"
        :data="filteredSuppliers.map(s => [
          s.idproveedor,
          s.nombre,
          s.telefono,
          s.direccion
        ])"
        :searchable="false"
        :onRowClick="(row) => viewProducts(row[0])"
      />
      <div v-else-if="supplierStore.suppliers.length > 0" class="empty-state">
        <p>No se encontraron resultados para la búsqueda.</p>
      </div>
      <div v-else class="empty-state">
        <span class="material-symbols-outlined">local_shipping</span>
        <p>No hay proveedores registrados.</p>
      </div>
    </div>

    <!-- Modal de Creación/Edición -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-content animate-slide-up">
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
            <button type="button" class="cancel-btn" @click="showModal = false">Cancelar</button>
            <button type="submit" class="submit-btn">{{ isEditing ? 'Guardar Cambios' : 'Registrar Proveedor' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-container {
  padding: 2.5rem;
  max-width: 1600px;
  margin: 0 auto;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}
.header-section h1 { font-size: 2.25rem; font-weight: 800; color: #1e293b; margin-bottom: 0.5rem; letter-spacing: -0.02em; }
.header-section p { color: #64748b; font-size: 1.1rem; }
.result-count-inline { font-weight: 600; color: #475569; margin-left: 8px; }

.header-actions { display: flex; align-items: center; gap: 16px; }

.search-box {
  display: flex; align-items: center; background: #f9fafb;
  border: 1px solid #e5e7eb; border-radius: 4px; padding: 0 12px;
  width: 300px; transition: all 0.25s;
}
.search-box:focus-within { border-color: #dc2626; box-shadow: 0 0 0 3px rgba(220,38,38,0.1); background: #fff; }
.search-icon { font-size: 20px; color: #9ca3af; margin-right: 8px; }
.search-input { border: none; outline: none; background: transparent; font-size: 14px; color: #1f2937; padding: 10px 0; width: 100%; }

.create-btn {
  display: flex; align-items: center; gap: 0.5rem;
  background: #dc2626; color: white;
  padding: 0.75rem 1.5rem; border-radius: 4px;
  font-weight: 600; border: none; cursor: pointer;
  transition: all 0.3s;
}

.create-btn:hover { background: #b91c1c; }

.table-card {
  background: white;
  border-radius: 4px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.05);
  border: 1px solid #f1f5f9;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  width: 90%;
  max-width: 700px;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.modal-header {
  padding: 2rem;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 { font-size: 1.5rem; font-weight: 800; color: #0f172a; }

.close-btn { background: none; border: none; font-size: 2rem; color: #94a3b8; cursor: pointer; }

.modal-body { padding: 2.5rem; }

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.full-width { grid-column: span 2; }

.form-group label {
  display: block;
  font-size: 0.85rem;
  font-weight: 700;
  color: #475569;
  margin-bottom: 0.5rem;
}

.form-input {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 4px;
  font-size: 1rem;
  transition: all 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #0f172a;
  box-shadow: 0 0 0 4px rgba(15, 23, 42, 0.1);
}

.modal-footer {
  margin-top: 2.5rem;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.cancel-btn {
  padding: 0.875rem 1.5rem;
  border-radius: 4px;
  font-weight: 700;
  background: #f1f5f9;
  color: #64748b;
  border: none;
  cursor: pointer;
}

.submit-btn {
  padding: 0.875rem 1.5rem;
  border-radius: 4px;
  font-weight: 700;
  background: #0f172a;
  color: white;
  border: none;
  cursor: pointer;
}

.empty-state {
  text-align: center;
  padding: 4rem;
  color: #94a3b8;
}

.empty-state .material-symbols-outlined { font-size: 4rem; margin-bottom: 1rem; }

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4rem;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f1f5f9;
  border-top: 4px solid #0f172a;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin { to { transform: rotate(360deg); } }

.animate-fade-in { animation: fadeIn 0.5s ease-out; }
.animate-slide-up { animation: slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1); }

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

:deep(.btn-products) { color: #6366f1; }
:deep(.btn-edit) { color: #f59e0b; }
:deep(.btn-delete) { color: #ef4444; }
</style>
