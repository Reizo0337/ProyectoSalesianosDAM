<script setup lang="ts">
import { ref } from 'vue';
import { useOrderStore } from '@/stores/orders';
import PdfPreview from './PdfPreview.vue';

const props = defineProps<{ 
  orderId: string | number,
  facturas: any[],
  canManageInvoices: boolean 
}>();

const emit = defineEmits(['uploaded']);

const orderStore = useOrderStore();
const isUploading = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);
const previewUrl = ref<string | null>(null);

async function handleFileUpload(event: Event) {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  isUploading.value = true;
  try {
    const res = await orderStore.uploadInvoice(props.orderId, file);
    if (res.status === 'success') {
      emit('uploaded');
    } else {
      alert(res.message || 'Error al subir la factura');
    }
  } catch (err) {
    alert('Error al subir el archivo');
  } finally {
    isUploading.value = false;
    if (fileInput.value) fileInput.value.value = '';
  }
}

function openPreview(id: string | number) {
  previewUrl.value = `http://localhost:8080/backend/api/facturas/view?id=${id}`;
}

function closePreview() {
  previewUrl.value = null;
}

async function downloadFactura(id: string | number) {
  try {
    const url = `http://localhost:8080/backend/api/facturas/view?id=${id}&action=download`;
    const response = await fetch(url);
    const blob = await response.blob();
    const blobUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = blobUrl;
    link.setAttribute('download', `factura_${id}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(blobUrl);
  } catch (err) {
    console.error('Error al descargar factura:', err);
    alert('No se pudo descargar la factura');
  }
}
</script>

<template>
  <div class="section-card invoices-section">
    <div class="section-header-row">
      <h2>Facturas</h2>
      <div v-if="canManageInvoices" class="upload-action">
        <label class="upload-label-btn" :class="{ disabled: isUploading }">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
            <polyline points="17 8 12 3 7 8" />
            <line x1="12" y1="3" x2="12" y2="15" />
          </svg>
          {{ isUploading ? 'Subiendo...' : 'Subir Factura' }}
          <input 
            type="file" 
            ref="fileInput" 
            @change="handleFileUpload" 
            accept=".pdf" 
            :disabled="isUploading"
            hidden 
          />
        </label>
      </div>
    </div>

    <div v-if="facturas && facturas.length > 0" class="facturas-list">
      <div v-for="fac in facturas" :key="fac.idfactura" class="factura-item">
        <div class="factura-main" @click="openPreview(fac.idfactura)">
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
        <button @click="downloadFactura(fac.idfactura)" class="list-download-btn" title="Descargar PDF">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v4" />
            <polyline points="7 10 12 15 17 10" />
            <line x1="12" y1="15" x2="12" y2="3" />
          </svg>
        </button>
      </div>
    </div>
    <p v-else class="empty-text">No hay facturas adjuntas.</p>

    <!-- PDF Preview Overlay -->
    <div v-if="previewUrl" class="preview-overlay" @click.self="closePreview">
      <div class="preview-modal">
        <div class="preview-header">
          <h3>Vista Previa: Factura</h3>
          <div class="header-actions">
            <button @click="downloadFactura(previewUrl.split('id=')[1].split('&')[0])" class="download-btn">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v4a2 2 0 0 1 2-2h14" />
                <polyline points="7 10 12 15 17 10" />
                <line x1="12" y1="15" x2="12" y2="3" />
              </svg>
              Descargar PDF
            </button>
            <button class="close-modal-btn" @click="closePreview">&times;</button>
          </div>
        </div>
        <div class="preview-body">
          <PdfPreview :pdfUrl="previewUrl" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.section-card {
  background: white;
  border-radius: 4px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
}
.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.section-header-row h2 {
  margin: 0;
  color: #1e293b;
  font-size: 18px;
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

.facturas-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.factura-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 12px 16px;
  transition: all 0.2s;
}
.factura-item:hover {
  border-color: #cbd5e1;
  background: #f1f5f9;
}
.factura-main {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  cursor: pointer;
}
.factura-main svg {
  color: #ef4444;
}
.factura-info {
  display: flex;
  flex-direction: column;
}
.factura-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}
.factura-date {
  font-size: 12px;
  color: #64748b;
}
.view-hint {
  margin-left: auto;
  font-size: 12px;
  color: #2563eb;
  font-weight: 600;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.2s;
}
.factura-item:hover .view-hint {
  opacity: 1;
  transform: translateX(0);
}
.list-download-btn {
  background: white;
  border: 1px solid #e2e8f0;
  padding: 8px;
  border-radius: 4px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  margin-left: 16px;
}
.list-download-btn:hover {
  color: #1e293b;
  border-color: #cbd5e1;
  background: #f8fafc;
}

.empty-text {
  color: #64748b;
  font-size: 14px;
  font-style: italic;
  margin: 0;
}

/* Modal CSS */
.preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
}
.preview-modal {
  background: white;
  border-radius: 4px;
  width: 100%;
  max-width: 1000px;
  height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  animation: modalIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
@keyframes modalIn {
  from { opacity: 0; transform: translateY(20px) scale(0.95); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}
.preview-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1e293b;
  font-weight: 600;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.download-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: white;
  border: 1px solid #e2e8f0;
  padding: 6px 12px;
  border-radius: 4px;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.download-btn:hover {
  background: #f1f5f9;
  color: #1e293b;
}
.close-modal-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #64748b;
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
  transition: color 0.2s;
}
.close-modal-btn:hover {
  color: #ef4444;
}
.preview-body {
  flex: 1;
  background: #e2e8f0;
  position: relative;
  overflow: hidden;
}
</style>
