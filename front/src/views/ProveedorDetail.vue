<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useSupplierStore, type Supplier } from '@/stores/suppliers';
import { useOrderStore } from '@/stores/orders';

const route = useRoute();
const router = useRouter();
const supplierStore = useSupplierStore();
const orderStore = useOrderStore();

const id = parseInt(route.params.id as string);
const supplier = computed(() => supplierStore.suppliers.find(s => Number(s.idproveedor) === id));

const isEditing = ref(false);
const showAssignModal = ref(false);
const selectedProductId = ref('');
const editForm = ref({
  nombre: '',
  telefono: '',
  direccion: ''
});

onMounted(async () => {
  if (supplierStore.suppliers.length === 0) {
    await supplierStore.fetchSuppliers();
  }
  await supplierStore.fetchSupplierProducts(id);
  await orderStore.fetchProducts();
});

watch(supplier, (newVal) => {
  if (newVal) {
    editForm.value = {
      nombre: newVal.nombre,
      telefono: newVal.telefono,
      direccion: newVal.direccion
    };
  }
}, { immediate: true });

function toggleEdit() {
  if (!isEditing.value && supplier.value) {
    editForm.value = {
      nombre: supplier.value.nombre,
      telefono: supplier.value.telefono,
      direccion: supplier.value.direccion
    };
  }
  isEditing.value = !isEditing.value;
}

async function handleUpdate() {
  const data = {
    Nombre: editForm.value.nombre,
    Telefono: editForm.value.telefono,
    Direccion: editForm.value.direccion
  };
  try {
    await supplierStore.updateSupplier(id, data);
    isEditing.value = false;
  } catch (err) {
    alert('Error al actualizar el proveedor');
  }
}

const availableProducts = computed(() => {
  const currentIds = supplierStore.currentSupplierProducts.map(p => p.idproducto.toString());
  return orderStore.products.filter(p => !currentIds.includes(p.idproducto.toString()));
});

async function handleAssign() {
  if (!selectedProductId.value) return;
  await supplierStore.assignProduct(id, parseInt(selectedProductId.value));
  selectedProductId.value = '';
  showAssignModal.value = false;
}

async function handleRemove(productId: number) {
  if (confirm('¿Desvincular este producto del proveedor?')) {
    await supplierStore.removeProduct(id, productId);
  }
}

function goBack() {
  router.push('/proveedores');
}
</script>

<template>
  <div class="detail-container animate-fade-in">
    <button class="back-btn" @click="goBack">
      <span class="material-symbols-outlined">arrow_back</span>
      Volver a Proveedores
    </button>

    <div v-if="!supplier" class="loading-state">
      <div class="spinner"></div>
      <p>Cargando información...</p>
    </div>

    <div v-else class="detail-content">
      <!-- Header Section -->
      <div class="detail-header">
        <div class="header-main">
          <div class="title-info">
            <h1 v-if="!isEditing">{{ supplier.nombre }}</h1>
            <input v-else v-model="editForm.nombre" class="edit-input-h1" placeholder="Nombre del proveedor" />
          </div>
        </div>
        <div class="header-actions">
           <button v-if="!isEditing" class="edit-btn" @click="toggleEdit">
             <span class="material-symbols-outlined">edit</span>
             Editar Información
           </button>
           <template v-else>
             <button class="cancel-btn-alt" @click="isEditing = false">Cancelar</button>
             <button class="save-btn" @click="handleUpdate">Guardar Cambios</button>
           </template>
           <button class="assign-btn" @click="showAssignModal = true">
             <span class="material-symbols-outlined">add_box</span>
             Vincular Producto
           </button>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-card">
          <span class="label">Teléfono</span>
          <span v-if="!isEditing" class="value">{{ supplier.telefono || 'No especificado' }}</span>
          <input v-else v-model="editForm.telefono" class="form-input-inline" placeholder="976 000 000" />
        </div>
        <div class="info-card">
          <span class="label">Dirección</span>
          <span v-if="!isEditing" class="value">{{ supplier.direccion || 'No especificado' }}</span>
          <input v-else v-model="editForm.direccion" class="form-input-inline" placeholder="Calle Ejemplo 123" />
        </div>
      </div>

      <!-- Products Section -->
      <div class="products-section">
        <div class="section-header">
          <h2>Productos Suministrados</h2>
          <span class="count-badge">{{ supplierStore.currentSupplierProducts.length }}</span>
        </div>

        <div v-if="supplierStore.currentSupplierProducts.length > 0" class="products-list">
          <div v-for="prod in supplierStore.currentSupplierProducts" :key="prod.idproducto" class="product-item">
            <div class="prod-info">
              <h3>{{ prod.nombre }}</h3>
              <p>{{ prod.descripcion || 'Sin descripción' }}</p>
            </div>
            <button class="remove-btn" @click="handleRemove(prod.idproducto)">
              <span class="material-symbols-outlined">link_off</span>
            </button>
          </div>
        </div>
        <div v-else class="empty-products">
          <span class="material-symbols-outlined">inventory_2</span>
          <p>Este proveedor aún no tiene productos vinculados.</p>
        </div>
      </div>
    </div>

    <!-- Modal de Asignación -->
    <div v-if="showAssignModal" class="modal-overlay" @click.self="showAssignModal = false">
      <div class="modal-content animate-slide-up">
        <div class="modal-header">
          <h2>Vincular Producto</h2>
          <button @click="showAssignModal = false" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Selecciona un producto del inventario</label>
            <select v-model="selectedProductId" class="form-input">
              <option value="">Seleccionar producto...</option>
              <option v-for="p in availableProducts" :key="p.idproducto" :value="p.idproducto">
                {{ p.nombre }}
              </option>
            </select>
            <p v-if="availableProducts.length === 0" class="helper-text">
              No hay más productos disponibles para vincular.
            </p>
          </div>
          <div class="modal-footer">
            <button class="cancel-btn" @click="showAssignModal = false">Cancelar</button>
            <button class="submit-btn" :disabled="!selectedProductId" @click="handleAssign">Vincular</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-container {
  padding: 2.5rem;
  max-width: 1200px;
  margin: 0 auto;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: none;
  color: #64748b;
  font-weight: 700;
  cursor: pointer;
  margin-bottom: 2rem;
  transition: color 0.2s;
}

.back-btn:hover { color: #0f172a; }

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3rem;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.title-info h1 {
  font-size: 2.5rem;
  font-weight: 850;
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.04em;
}

.edit-input-h1 {
  font-size: 2.5rem;
  font-weight: 850;
  color: #0f172a;
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 0.25rem 0.75rem;
  width: 100%;
  letter-spacing: -0.04em;
  font-family: inherit;
}

.edit-input-h1:focus {
  outline: none;
  border-color: #0f172a;
}

.edit-btn {
  background: white;
  color: #475569;
  padding: 0.75rem 1.25rem;
  border-radius: 12px;
  font-weight: 700;
  border: 1.5px solid #e2e8f0;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.2s;
}

.edit-btn:hover { background: #f8fafc; border-color: #cbd5e1; }

.save-btn {
  background: #16a34a;
  color: white;
  padding: 0.75rem 1.25rem;
  border-radius: 12px;
  font-weight: 700;
  border: none;
  cursor: pointer;
}

.cancel-btn-alt {
  background: #f1f5f9;
  color: #64748b;
  padding: 0.75rem 1.25rem;
  border-radius: 12px;
  font-weight: 700;
  border: none;
  cursor: pointer;
}

.form-input-inline {
  width: 100%;
  padding: 0.5rem;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
}

.form-input-inline:focus {
  outline: none;
  border-color: #0f172a;
}

.assign-btn {
  background: #0f172a;
  color: white;
  padding: 0.75rem 1.25rem;
  border-radius: 12px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.2s;
}

.assign-btn:hover { transform: translateY(-2px); box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1); }

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.info-card {
  background: white;
  padding: 1.5rem;
  border-radius: 20px;
  border: 1px solid #f1f5f9;
  display: flex;
  flex-direction: column;
}

.info-card .label { font-size: 0.75rem; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem; }
.info-card .value { font-size: 1.1rem; font-weight: 600; color: #1e293b; }

.products-section {
  background: white;
  border-radius: 24px;
  padding: 2.5rem;
  border: 1px solid #f1f5f9;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.section-header h2 { font-size: 1.5rem; font-weight: 800; color: #0f172a; margin: 0; }
.count-badge { background: #f1f5f9; color: #475569; padding: 0.25rem 0.75rem; border-radius: 20px; font-weight: 700; font-size: 0.9rem; }

.products-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.product-item {
  background: #f8fafc;
  padding: 1.25rem;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  transition: all 0.2s;
}

.product-item:hover { border-color: #cbd5e1; transform: scale(1.02); }

.prod-info h3 { font-size: 1rem; font-weight: 700; color: #1e293b; margin: 0 0 0.25rem 0; }
.prod-info p { font-size: 0.85rem; color: #64748b; margin: 0; }

.remove-btn {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  transition: color 0.2s;
}

.remove-btn:hover { color: #ef4444; }

.empty-products {
  text-align: center;
  padding: 4rem;
  color: #94a3b8;
}

.empty-products .material-symbols-outlined { font-size: 3rem; margin-bottom: 1rem; }

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
  max-width: 500px;
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.modal-header {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.modal-header h2 { font-size: 1.25rem; font-weight: 800; color: #0f172a; margin: 0; }
.close-btn { background: none; border: none; font-size: 1.5rem; color: #94a3b8; cursor: pointer; }

.modal-body { padding: 2rem; }

.form-group label { display: block; font-size: 0.85rem; font-weight: 700; color: #475569; margin-bottom: 0.75rem; }
.form-input { width: 100%; padding: 0.75rem 1rem; border: 2px solid #e2e8f0; border-radius: 10px; font-size: 1rem; }
.form-input:focus { outline: none; border-color: #0f172a; }

.helper-text { font-size: 0.8rem; color: #f59e0b; margin-top: 0.5rem; font-weight: 600; }

.modal-footer { margin-top: 2rem; display: flex; justify-content: flex-end; gap: 0.75rem; }
.cancel-btn { padding: 0.75rem 1.25rem; border-radius: 10px; font-weight: 700; background: #f1f5f9; color: #64748b; border: none; cursor: pointer; }
.submit-btn { padding: 0.75rem 1.25rem; border-radius: 10px; font-weight: 700; background: #0f172a; color: white; border: none; cursor: pointer; }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.loading-state { text-align: center; padding: 5rem; color: #94a3b8; }
.spinner { width: 40px; height: 40px; border: 4px solid #f1f5f9; border-top: 4px solid #0f172a; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem; }
@keyframes spin { to { transform: rotate(360deg); } }
.animate-fade-in { animation: fadeIn 0.5s ease-out; }
.animate-slide-up { animation: slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
</style>
