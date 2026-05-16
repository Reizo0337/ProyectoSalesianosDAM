<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useOrderStore } from '@/stores/orders';
import { useAuthStore } from '@/stores/auth';
import { useDialogStore } from '@/stores/dialog';
import { useToast } from 'vue-toastification';
import Table from '../components/common/Table.vue';

const toast = useToast();
const dialogStore = useDialogStore();
const orderStore = useOrderStore();
const authStore = useAuthStore();
const searchQuery = ref('');
const showModal = ref(false);
const isEditing = ref(false);
const currentId = ref<number | string | null>(null);

const form = ref({
  nombre: '',
  descripcion: '',
  idProveedor: ''
});

const headers = ['ID', 'Nombre', 'Descripción', 'Proveedor', 'Precio Medio', 'Acciones'];

const canManage = computed(() => {
  const rol = authStore.user?.rol;
  return rol === 'Administrador' || rol === 'Jefe de Equipo' || rol === 'Contable';
});

onMounted(async () => {
  await orderStore.fetchProducts();
  await orderStore.fetchSuppliers();
});

const filteredProducts = computed(() => {
  if (!searchQuery.value.trim()) return orderStore.products;
  const q = searchQuery.value.toLowerCase();
  return orderStore.products.filter(p => 
    String(p.idproducto).includes(q) ||
    (p.nombre || '').toLowerCase().includes(q) ||
    (p.descripcion || '').toLowerCase().includes(q) ||
    (p.proveedor || '').toLowerCase().includes(q)
  );
});

const formatPrice = (price: any) => {
  if (price === null || price === undefined || price === '') return '0.00€';
  return parseFloat(price).toFixed(2) + '€';
};

const openCreate = () => {
  isEditing.value = false;
  currentId.value = null;
  form.value = { nombre: '', descripcion: '', idProveedor: '' };
  showModal.value = true;
};

const openEdit = (product: any) => {
  isEditing.value = true;
  currentId.value = product.idproducto;
  form.value = {
    nombre: product.nombre,
    descripcion: product.descripcion,
    idProveedor: product.idproveedor || ''
  };
  showModal.value = true;
};

const handleSubmit = async () => {
  try {
    if (isEditing.value && currentId.value) {
      await orderStore.updateProduct(currentId.value, form.value);
      toast.success('Producto actualizado correctamente');
    } else {
      await orderStore.createProduct(form.value);
      toast.success('Producto añadido al catálogo');
    }
    showModal.value = false;
  } catch (err) {
    toast.error('Error al guardar el producto');
  }
};

const handleDelete = async (id: number | string) => {
  const confirmed = await dialogStore.confirm(
    "Eliminar Producto",
    "¿Estás seguro de eliminar este producto? Se borrará permanentemente del catálogo."
  );
  if (confirmed) {
    await orderStore.deleteProduct(id);
  }
};
</script>

<template>
  <div class="view-container animate-fade-in">
    <div class="header-section">
      <div class="title-box">
        <h1>Catálogo de Productos</h1>
        <p>Listado general de artículos y sus costes medios. <span class="result-count-inline" v-if="orderStore.products.length > 0">({{ filteredProducts.length }} resultados)</span></p>
      </div>
      <div class="header-actions">
        <div class="search-box" v-if="orderStore.products.length > 0">
          <span class="material-symbols-outlined search-icon">search</span>
          <input v-model="searchQuery" type="text" placeholder="Buscar producto..." class="search-input" />
        </div>
        <button v-if="canManage" class="create-btn" @click="openCreate">
          <span class="material-symbols-outlined">add_box</span>
          Nuevo Producto
        </button>
      </div>
    </div>

    <div class="table-card">
      <Table
        v-if="filteredProducts.length > 0"
        :headers="headers"
        :data="filteredProducts.map(p => [
          p.idproducto,
          p.nombre,
          p.descripcion,
          p.proveedor || 'No asignado',
          formatPrice(p.precio_medio),
          {
            type: 'actions',
            actions: [
              { label: 'Editar', icon: 'edit', class: 'btn-edit', onClick: () => openEdit(p) },
              { label: 'Eliminar', icon: 'delete', class: 'btn-delete', onClick: () => handleDelete(p.idproducto) }
            ]
          }
        ])"
        :searchable="false"
      />
      <div v-else-if="orderStore.products.length > 0" class="empty-state">
        <p>No se encontraron resultados para la búsqueda.</p>
      </div>
      <div v-else class="empty-state">
        <span class="material-symbols-outlined">inventory_2</span>
        <p>No hay productos registrados en el sistema.</p>
      </div>
    </div>

    <!-- Modal Producto -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-content animate-slide-up">
        <div class="modal-header">
          <h2>{{ isEditing ? 'Editar Producto' : 'Registrar Nuevo Producto' }}</h2>
          <button @click="showModal = false" class="close-btn">&times;</button>
        </div>
        <form @submit.prevent="handleSubmit" class="modal-body">
          <div class="form-grid">
            <div class="form-group full-width">
              <label>Nombre del Producto</label>
              <input v-model="form.nombre" required placeholder="Ej: Resmas de papel A4" class="form-input" />
            </div>
            <div class="form-group full-width">
              <label>Descripción / Especificaciones</label>
              <textarea v-model="form.descripcion" placeholder="Detalles técnicos, marca, etc." class="form-input" rows="3"></textarea>
            </div>
            <div class="form-group full-width">
              <label>Proveedor Sugerido (Opcional)</label>
              <select v-model="form.idProveedor" class="form-input">
                <option value="">Seleccionar proveedor...</option>
                <option v-for="s in orderStore.suppliers" :key="s.idproveedor" :value="s.idproveedor">
                  {{ s.nombre }}
                </option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="cancel-btn" @click="showModal = false">Cancelar</button>
            <button type="submit" class="submit-btn">{{ isEditing ? 'Actualizar Producto' : 'Añadir al Catálogo' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-container {
  padding: 24px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header-section h1 {
  font-size: 2.25rem;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 0.5rem;
  letter-spacing: -0.02em;
}

.header-section p {
  color: #64748b;
  font-size: 1.1rem;
}

.result-count-inline {
  font-weight: 600;
  color: #475569;
  margin-left: 8px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 0 12px;
  width: 320px;
  transition: all 0.25s;
}

.search-box:focus-within {
  border-color: #0f172a;
  background: #fff;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
}

.search-icon {
  font-size: 20px;
  color: #94a3b8;
  margin-right: 8px;
}

.search-input {
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: #1e293b;
  padding: 10px 0;
  width: 100%;
}

.table-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.create-btn {
  display: flex; align-items: center; gap: 0.5rem;
  background: #0f172a; color: white;
  padding: 0.75rem 1.5rem; border-radius: 4px;
  font-weight: 600; border: none; cursor: pointer;
  transition: all 0.3s;
}

.create-btn:hover { background: #1e293b; transform: translateY(-1px); }

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
  max-width: 600px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.modal-header {
  padding: 1.5rem 2rem;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 { font-size: 1.25rem; font-weight: 800; color: #0f172a; }

.close-btn { background: none; border: none; font-size: 1.5rem; color: #94a3b8; cursor: pointer; }

.modal-body { padding: 2rem; }

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
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
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 0.95rem;
  transition: all 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #0f172a;
  box-shadow: 0 0 0 4px rgba(15, 23, 42, 0.1);
}

.modal-footer {
  margin-top: 2rem;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.cancel-btn {
  padding: 0.75rem 1.25rem;
  border-radius: 4px;
  font-weight: 700;
  background: #f1f5f9;
  color: #64748b;
  border: none;
  cursor: pointer;
}

.submit-btn {
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-weight: 700;
  background: #0f172a;
  color: white;
  border: none;
  cursor: pointer;
}

.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}

.animate-slide-up { animation: slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1); }

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

</style>
