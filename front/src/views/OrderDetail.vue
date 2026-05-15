<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useOrderStore } from '@/stores/orders';
import { useAuthStore } from '@/stores/auth';
import PdfPreview from '../components/orders/PdfPreview.vue';
import { usePresupuestoStore } from '@/stores/presupuesto';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const orderStore = useOrderStore();
const presupuestoStore = usePresupuestoStore();

import OrderComments from '../components/orders/OrderComments.vue';
import OrderInvoices from '../components/orders/OrderInvoices.vue';

const detail = ref<any>(null);
const loading = ref(true);
const previewUrl = ref<string | null>(null);
const description = ref('');

const isEditing = ref(false);
const editForm = ref({
  cantidad: '',
  tipo: '',
  numero_plan: '',
  idPresupuesto: '',
  descripcion: '',
  inversion: false
});

function toggleEdit() {
  if (!isEditing.value && detail.value) {
    editForm.value = {
      cantidad: detail.value.order.cantidad,
      tipo: detail.value.order.tipo,
      numero_plan: detail.value.order.numero_plan || '',
      idPresupuesto: detail.value.order.idpresupuesto.toString(),
      descripcion: detail.value.order.descripcion || '',
      inversion: detail.value.order.inversion === 'true' || detail.value.order.inversion === true
    };
  }
  isEditing.value = !isEditing.value;
}

async function handleUpdate() {
  try {
    const res = await orderStore.updateOrder(detail.value.order.idorden, editForm.value);
    if (res.status === 'success') {
      isEditing.value = false;
      // Refresh data
      const id = route.params.id as string;
      const updatedData = await orderStore.fetchOrderDetail(id);
      if (updatedData) {
        detail.value = updatedData;
        description.value = updatedData.order.descripcion || '';
      }
    }
  } catch (err) {
    alert('Error al actualizar la orden');
  }
}

const canEdit = computed(() => {
  const user = authStore.user;
  if (!user || !detail.value) return false;
  if ((detail.value.order.estado || '').toLowerCase() === 'cerrada') return false;
  
  if (user.rol === 'Administrador') return true;
  if (user.rol === 'Jefe de Equipo') {
    return user.idDepartamento === detail.value.order.dep_nombre;
  }
  return false;
});

const canComment = computed(() => {
  const rol = authStore.user?.rol;
  return rol === 'Administrador' || rol === 'Contable' || rol === 'Jefe de Equipo';
});

const canManageInvoices = computed(() => {
  const rol = authStore.user?.rol;
  if (detail.value && (detail.value.order.estado || '').toLowerCase() === 'cerrada') return false;
  return rol === 'Administrador' || rol === 'Contable';
});

async function refreshDetail() {
  const id = route.params.id as string;
  const data = await orderStore.fetchOrderDetail(id);
  if (data) {
    detail.value = data;
    description.value = data.order.descripcion || '';
  }
}

// Comment fetching moved to OrderComments.vue

onMounted(async () => {
  const id = route.params.id as string;
  const data = await orderStore.fetchOrderDetail(id);
  if (data) {
    detail.value = data;
    description.value = data.order.descripcion || '';
    await presupuestoStore.getAllPresupuestos();
  }
  loading.value = false;
});

function goBack() {
  router.push('/ordenes');
}

const formatType = (type: string) => {
  const t = (type || '').toLowerCase();
  if (t === 'planinversion' || t.includes('inversion') || t.includes('plan')) {
    return 'Plan Inversión';
  }
  return 'Presupuesto';
};
</script>

<template>
  <div class="detail-container">
    <button class="back-btn" @click="goBack">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18">
        <path d="M19 12H5M12 19l-7-7 7-7" />
      </svg>
      Volver a Órdenes
    </button>

    <div v-if="loading" class="loading-state">
      <p>Cargando detalle...</p>
    </div>

    <div v-else-if="detail && detail.order" class="detail-content">
      <!-- Header -->
      <div class="detail-header">
        <div class="header-left">
          <h1>Orden {{ detail.order.numero_orden }}</h1>
          <div class="description-area">
             <p v-if="!isEditing" class="header-desc">{{ detail.order.descripcion || 'Sin descripción' }}</p>
             <input v-else v-model="editForm.descripcion" class="edit-desc-input" placeholder="Descripción de la orden..." />
          </div>
          <span class="status-badge" :class="'status-' + (detail.order.estado || 'pendiente').toLowerCase()">
            <svg v-if="(detail.order.estado || 'pendiente').toLowerCase() === 'cerrada'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" style="margin-right: 4px;">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
            <svg v-else-if="(detail.order.estado || 'pendiente').toLowerCase() === 'pendiente'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" style="margin-right: 4px;">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
            {{ detail.order.estado || 'Pendiente' }}
          </span>
        </div>
        <div class="header-right">
          <div v-if="canEdit && !isEditing" class="header-actions-inline">
             <button class="edit-info-btn" @click="toggleEdit">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16">
                  <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" />
                  <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
                Editar Información
             </button>
          </div>
          <div v-else-if="canEdit && isEditing" class="header-actions-inline">
             <button class="cancel-btn-inline" @click="isEditing = false">Cancelar</button>
             <button class="save-btn-inline" @click="handleUpdate">Guardar Cambios</button>
          </div>

          <div class="meta-item">
            <span class="meta-label">Departamento</span>
            <span class="meta-value">{{ detail.order.dep_nombre }} ({{ detail.order.dep_codigo }})</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Fecha</span>
            <span class="meta-value">{{ detail.order.fechacreacion }}</span>
          </div>
        </div>
      </div>

      <!-- Info Cards -->
      <div class="info-grid">
        <div class="info-card">
          <span class="info-label">Importe Total</span>
          <span v-if="!isEditing" class="info-value price">{{ detail.order.cantidad }}€</span>
          <div v-else class="edit-input-group">
             <input v-model="editForm.cantidad" type="number" step="0.01" class="edit-input" />
             <span class="currency">€</span>
          </div>
        </div>
        <div class="info-card">
          <span class="info-label">Tipo</span>
          <span v-if="!isEditing" class="info-value">{{ detail.order.tipo }}</span>
          <select v-else v-model="editForm.tipo" class="edit-select">
             <option value="Fungible">Fungible</option>
             <option value="Invariable">Invariable</option>
          </select>
        </div>
        <div class="info-card">
          <span class="info-label">Nº Plan</span>
          <span v-if="!isEditing" class="info-value">{{ detail.order.numero_plan || 'N/A' }}</span>
          <input v-else v-model="editForm.numero_plan" type="text" class="edit-input" maxlength="7" placeholder="Ej: 1234567" />
        </div>
        <div class="info-card">
          <span class="info-label">Presupuesto</span>
          <span v-if="!isEditing" class="info-value">{{ detail.order.presupuesto_codigo }} [{{ formatType(detail.order.presupuesto_tipo) }}]</span>
          <select v-else v-model="editForm.idPresupuesto" class="edit-select">
             <option v-for="p in presupuestoStore.presupuestos.filter(b => b.nombreDepartamento === detail.order.dep_nombre)" :key="p.idPresupuesto" :value="p.idPresupuesto.toString()">
                {{ p.Codigo }} [{{ formatType(p.type) }}]
             </option>
          </select>
        </div>
      </div>

      <!-- Componente de Comentarios Extraído -->
      <OrderComments ref="commentsRef" :orderId="detail.order.idorden" :canComment="canComment" />

      <!-- Productos -->
      <div class="section-card">
        <h2>Productos Asociados</h2>
        <div v-if="detail.productos && detail.productos.length > 0" class="products-table">
          <table>
            <thead>
              <tr>
                <th>Producto</th>
                <th>Descripción</th>
                <th>Proveedor</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="prod in detail.productos" :key="prod.idProducto">
                <td class="prod-name">{{ prod.nombre }}</td>
                <td class="prod-desc">{{ prod.descripcion }}</td>
                <td>
                  <span v-if="prod.proveedor" class="proveedor-badge">{{ prod.proveedor }}</span>
                  <span v-else class="no-prov">Sin proveedor</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="empty-text">No hay productos asociados a esta orden.</p>
      </div>

      <!-- Componente de Facturas Extraído -->
      <OrderInvoices 
        :orderId="detail.order.idorden" 
        :facturas="detail.facturas" 
        :canManageInvoices="canManageInvoices" 
        @uploaded="refreshDetail" 
      />

    </div>

    <div v-else-if="!loading" class="error-state">
      <p>No se pudo cargar el detalle de la orden.</p>
    </div>
  </div>
</template>

<style scoped>
.detail-container {
  padding: 24px;
  max-width: 960px;
  margin: 0 auto;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: 1px solid #e5e7eb;
  padding: 8px 16px;
  border-radius: 4px;
  color: #6b7280;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 24px;
  transition: all 0.2s;
}
.back-btn:hover {
  background: #f9fafb;
  color: #1f2937;
  border-color: #d1d5db;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  flex-wrap: wrap;
  gap: 16px;
}
.header-left h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px;
}
.header-right {
  display: flex;
  gap: 24px;
}
.meta-item { display: flex; flex-direction: column; }
.meta-label { font-size: 11px; text-transform: uppercase; letter-spacing: 0.06em; color: #9ca3af; font-weight: 600; }
.meta-value { font-size: 14px; color: #374151; font-weight: 500; margin-top: 2px; }

.status-badge {
  display: inline-flex; align-items: center; padding: 5px 14px;
  border-radius: 4px; font-size: 13px; font-weight: 600;
}
.status-pendiente { background: #fef9c3; color: #ca8a04; }
.status-aprobado { background: #dcfce7; color: #16a34a; }
.status-rechazado { background: #fee2e2; color: #dc2626; }
.status-cerrada { background: #fee2e2; color: #dc2626; }

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}
.info-card {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 20px;
  display: flex;
  flex-direction: column;
}
.info-label { font-size: 11px; text-transform: uppercase; letter-spacing: 0.06em; color: #9ca3af; font-weight: 600; margin-bottom: 6px; }
.info-value { font-size: 18px; font-weight: 700; color: #1f2937; }
.info-value.price { color: #dc2626; }

.section-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.section-card h2 {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 16px;
}
.observations-text {
  color: #4b5563;
  line-height: 1.6;
  font-size: 14px;
  margin: 0;
  background: #f9fafb;
  padding: 16px;
  border-radius: 4px;
  border: 1px solid #f3f4f6;
}

.products-table table { width: 100%; border-collapse: collapse; }
.products-table thead { background: #f9fafb; }
.products-table th {
  padding: 10px 16px; text-align: left; font-size: 11px; font-weight: 600;
  color: #6b7280; text-transform: uppercase; letter-spacing: 0.06em; border-bottom: 1px solid #e5e7eb;
}
.products-table td { padding: 14px 16px; font-size: 14px; color: #374151; border-bottom: 1px solid #f3f4f6; }
.prod-name { font-weight: 600; }
.prod-desc { color: #6b7280; max-width: 200px; overflow: hidden; text-overflow: ellipsis; }
.prod-price { font-weight: 700; color: #1f2937; }
.proveedor-badge {
  background: #ede9fe; color: #7c3aed; padding: 3px 10px;
  border-radius: 4px; font-size: 12px; font-weight: 600;
}
.no-prov { color: #9ca3af; font-size: 12px; }

.facturas-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.factura-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  transition: all 0.2s;
}

.factura-item:hover {
  border-color: #cbd5e1;
  background: #f1f5f9;
}

.factura-main {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.factura-item svg { color: #dc2626; }

.factura-info { display: flex; flex-direction: column; }
.factura-name { font-size: 14px; font-weight: 700; color: #1f2937; }
.factura-date { font-size: 12px; color: #64748b; }

.list-download-btn {
  background: none;
  border: none;
  color: #64748b;
  padding: 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.list-download-btn:hover {
  background: white;
  color: #0f172a;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.view-hint {
  margin-left: auto;
  font-size: 12px;
  font-weight: 600;
  color: #dc2626;
  opacity: 0;
  transition: opacity 0.2s;
}
.factura-main:hover .view-hint { opacity: 1; }

/* Preview Overlay Styles */
.preview-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.preview-modal {
  background: white;
  width: 100%;
  max-width: 1000px;
  height: 90vh;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.preview-header {
  padding: 16px 24px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-header h3 { font-size: 16px; font-weight: 700; color: #1f2937; margin: 0; }
.close-modal-btn {
  background: none; border: none; font-size: 32px; color: #9ca3af;
  cursor: pointer; line-height: 1; transition: color 0.2s;
}
.close-modal-btn:hover { color: #dc2626; }

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.download-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #0f172a;
  color: white;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.download-btn:hover {
  background: #1e293b;
  transform: translateY(-1px);
}

.preview-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #ffffff;
  display: flex;
  justify-content: center;
}

.empty-text { color: #9ca3af; font-size: 14px; margin: 0; }
.loading-state, .error-state { text-align: center; padding: 64px; color: #9ca3af; }

.edit-obs-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.obs-textarea {
  width: 100%;
  min-height: 100px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  font-family: inherit;
  font-size: 14px;
  color: #1f2937;
  resize: vertical;
  outline: none;
  transition: border-color 0.2s;
}

.obs-textarea:focus {
  border-color: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
}

.save-obs-btn {
  align-self: flex-end;
  background: #dc2626;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.save-obs-btn:hover {
  background: #b91c1c;
  transform: translateY(-1px);
}

/* Comments CSS moved to OrderComments.vue */

.description-area {
  margin-bottom: 12px;
}

.header-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  font-style: italic;
}

.edit-desc-input {
  width: 100%;
  padding: 6px 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 4px;
  font-size: 14px;
  color: #374151;
  outline: none;
}

.edit-desc-input:focus {
  border-color: #dc2626;
}

.header-actions-inline {
  display: flex;
  gap: 12px;
}

.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header-row h2 {
  margin: 0;
}

.upload-label-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #f8fafc;
  color: #1e293b;
  border: 1px solid #e2e8f0;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-label-btn:hover:not(.disabled) {
  background: #f1f5f9;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.upload-label-btn.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.upload-label-btn svg {
  color: #64748b;
}




.edit-info-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: white;
  border: 1.5px solid #e2e8f0;
  padding: 8px 14px;
  border-radius: 4px;
  color: #475569;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-info-btn:hover {
  border-color: #dc2626;
  color: #dc2626;
  background: #fff1f2;
}

.save-btn-inline {
  background: #16a34a;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.save-btn-inline:hover {
  background: #15803d;
  transform: translateY(-1px);
}

.cancel-btn-inline {
  background: #f1f5f9;
  color: #64748b;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-input-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.edit-input {
  width: 100%;
  padding: 8px 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  outline: none;
}

.edit-input:focus {
  border-color: #dc2626;
}

.edit-select {
  width: 100%;
  padding: 8px 10px;
  border: 1.5px solid #e2e8f0;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  outline: none;
  background: white;
}

.edit-select:focus {
  border-color: #dc2626;
}

.currency {
  font-weight: 700;
  color: #64748b;
}
</style>
