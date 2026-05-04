<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useOrderStore } from '@/stores/orders';
import PdfPreview from '../components/orders/PdfPreview.vue';

const route = useRoute();
const router = useRouter();
const orderStore = useOrderStore();
const detail = ref<any>(null);
const loading = ref(true);
const previewUrl = ref<string | null>(null);

function openPreview(id: string | number) {
  // Use absolute backend path
  previewUrl.value = `http://localhost:8080/backend/api/facturas/view?id=${id}`;
}

function closePreview() {
  previewUrl.value = null;
}

onMounted(async () => {
  const id = route.params.id as string;
  const data = await orderStore.fetchOrderDetail(id);
  if (data) {
    detail.value = data;
  }
  loading.value = false;
});

function goBack() {
  router.push('/ordenes');
}
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
          <span class="status-badge" :class="'status-' + (detail.order.estado || 'pendiente').toLowerCase()">
            {{ detail.order.estado }}
          </span>
        </div>
        <div class="header-right">
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
          <span class="info-value price">{{ detail.order.cantidad }}€</span>
        </div>
        <div class="info-card">
          <span class="info-label">Tipo</span>
          <span class="info-value">{{ detail.order.tipo }}</span>
        </div>
        <div class="info-card">
          <span class="info-label">Nº Plan</span>
          <span class="info-value">{{ detail.order.numero_plan || 'N/A' }}</span>
        </div>
        <div class="info-card">
          <span class="info-label">Presupuesto</span>
          <span class="info-value">{{ detail.order.presupuesto_codigo }} [{{ detail.order.presupuesto_tipo || 'P' }}]</span>
        </div>
      </div>

      <!-- Observaciones -->
      <div v-if="detail.order.observaciones" class="section-card">
        <h2>Observaciones</h2>
        <p class="observations-text">{{ detail.order.observaciones }}</p>
      </div>

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
                <th>Precio</th>
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
                <td class="prod-price">{{ prod.precioUnitario }}€</td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="empty-text">No hay productos asociados a esta orden.</p>
      </div>

      <!-- Facturas -->
      <div class="section-card">
        <h2>Facturas</h2>
        <div v-if="detail.facturas && detail.facturas.length > 0" class="facturas-list">
          <div v-for="fac in detail.facturas" :key="fac.idfactura" 
               class="factura-item clickable" @click="openPreview(fac.idfactura)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20">
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
              <polyline points="14 2 14 8 20 8" />
            </svg>
            <div class="factura-info">
              <span class="factura-name">Factura #{{ fac.idfactura }}</span>
              <span class="factura-date">{{ fac.fechacreacion }}</span>
            </div>
            <span class="view-hint">Ver PDF</span>
          </div>
        </div>
        <p v-else class="empty-text">No hay facturas adjuntas.</p>
      </div>
    </div>

    <!-- PDF Preview Overlay -->
    <div v-if="previewUrl" class="preview-overlay" @click.self="closePreview">
      <div class="preview-modal">
        <div class="preview-header">
          <h3>Vista Previa: Factura</h3>
          <button class="close-modal-btn" @click="closePreview">&times;</button>
        </div>
        <div class="preview-body">
          <PdfPreview :pdfUrl="previewUrl" />
        </div>
      </div>
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
  border-radius: 8px;
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
  border-radius: 20px; font-size: 13px; font-weight: 600;
}
.status-pendiente { background: #fef9c3; color: #ca8a04; }
.status-aprobado { background: #dcfce7; color: #16a34a; }
.status-rechazado { background: #fee2e2; color: #dc2626; }

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}
.info-card {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
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
  border-radius: 16px;
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
  border-radius: 10px;
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
  border-radius: 6px; font-size: 12px; font-weight: 600;
}
.no-prov { color: #9ca3af; font-size: 12px; }

.facturas-list { display: flex; flex-direction: column; gap: 10px; }
.factura-item {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px; background: #f9fafb; border: 1px solid #e5e7eb;
  border-radius: 10px; transition: all 0.2s;
}
.factura-item:hover { background: #f3f4f6; }
.factura-item svg { color: #dc2626; flex-shrink: 0; }
.factura-info { display: flex; flex-direction: column; }
.factura-name { font-size: 14px; font-weight: 600; color: #1f2937; }
.factura-date { font-size: 12px; color: #9ca3af; }

.factura-item.clickable { cursor: pointer; }
.factura-item.clickable:hover { background: #fef2f2; border-color: #fecaca; }
.factura-item.clickable:hover svg { transform: scale(1.1); transition: transform 0.2s; }

.view-hint {
  margin-left: auto;
  font-size: 12px;
  font-weight: 600;
  color: #dc2626;
  opacity: 0;
  transition: opacity 0.2s;
}
.factura-item:hover .view-hint { opacity: 1; }

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
  border-radius: 20px;
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

.preview-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #525659;
}

.empty-text { color: #9ca3af; font-size: 14px; margin: 0; }
.loading-state, .error-state { text-align: center; padding: 64px; color: #9ca3af; }
</style>
