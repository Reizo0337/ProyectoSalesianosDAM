<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
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

onMounted(async () => {
  await supplierStore.fetchSuppliers();
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
        <p>Directorio de partners y suministradores oficiales.</p>
      </div>
      <button v-if="canManage" class="create-btn" @click="openCreate">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20">
          <path d="M12 5v14M5 12h14" />
        </svg>
        Nuevo Proveedor
      </button>
    </div>

    <div class="table-card">
      <div v-if="supplierStore.loading" class="loading-state">
        <div class="spinner"></div>
        <p>Cargando proveedores...</p>
      </div>
      <Table
        v-else-if="supplierStore.suppliers.length > 0"
        :headers="headers"
        :data="supplierStore.suppliers.map(s => [
          s.idproveedor,
          s.nombre,
          s.telefono,
          s.direccion,
          {
            type: 'actions',
            actions: [
              { label: 'Editar', icon: 'edit', onClick: () => openEdit(s), class: 'btn-edit', hide: !canManage },
              { label: 'Borrar', icon: 'delete', onClick: () => handleDelete(s.idproveedor), class: 'btn-delete', hide: !canManage }
            ]
          }
        ])"
        :searchable="true"
        :onRowClick="(row) => viewProducts(row[0])"
      />
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
  margin-bottom: 2.5rem;
}

.title-box h1 {
  font-size: 2.5rem;
  font-weight: 850;
  letter-spacing: -0.04em;
  background: linear-gradient(135deg, #0f172a 0%, #334155 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.5rem;
}

.title-box p {
  color: #64748b;
  font-size: 1.1rem;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.2);
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(220, 38, 38, 0.3);
}

.table-card {
  background: white;
  padding: 2rem;
  border-radius: 24px;
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
  border-radius: 28px;
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
  border-radius: 12px;
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
  border-radius: 12px;
  font-weight: 700;
  background: #f1f5f9;
  color: #64748b;
  border: none;
  cursor: pointer;
}

.submit-btn {
  padding: 0.875rem 1.5rem;
  border-radius: 12px;
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
