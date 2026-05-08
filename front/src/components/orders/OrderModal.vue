<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useOrderStore } from '@/stores/orders';
import { useAuthStore } from '@/stores/auth';
import { usePresupuestoStore } from '@/stores/presupuesto';

const props = defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits(['close', 'success']);

const orderStore = useOrderStore();
const authStore = useAuthStore();
const presupuestoStore = usePresupuestoStore();

const step = ref(1);
const loading = ref(false);
const showNewProduct = ref(false);

const form = reactive({
  idPresupuesto: '',
  numero_orden: '',
  numero_plan: '',
  usePlan: false,
  Cantidad: '',
  Inversion: false,
  Tipo: 'Fungible',
  descripcion: '',
  departmentCode: authStore.user?.codigoDepartamento || '',
  seqNumber: '',
  year: new Date().getFullYear().toString().slice(-2),
  typeCode: '1', // 1 for Fungible, 0 for Invariable
  products: [] as any[],
  invoices: [] as File[],
});

const newProduct = reactive({
  nombre: '',
  descripcion: '',
  idProveedor: '',
});

// Auto-calculate total from product prices
const totalAmount = computed(() => {
  return form.products.reduce((acc: number, p: any) => acc + (parseFloat(p.precio) || 0), 0);
});

watch(totalAmount, (val) => {
  form.Cantidad = val.toFixed(2);
});

// Computed for order number preview
const orderNumberPreview = computed(() => {
  return `${form.departmentCode}/${form.seqNumber}/${form.year}/${form.typeCode}`;
});

onMounted(async () => {
  await orderStore.fetchProducts();
  await orderStore.fetchSuppliers();
  await presupuestoStore.getAllPresupuestos();
  
  const userDept = authStore.user?.idDepartamento;
  if (userDept) {
     // Auto-fetch next sequence number
     const nextSeq = await orderStore.fetchNextSequence(form.departmentCode, form.year);
     form.seqNumber = nextSeq;
     
     // Automated budget selection
     updateBudgetSelection();
  }
});

function updateBudgetSelection() {
  const userDept = authStore.user?.idDepartamento;
  if (!userDept) return;

  const isPlan = form.typeCode === '0';
  const myBudget = presupuestoStore.presupuestos.find(p => {
    const sameDept = p.nombredepartamento === userDept;
    const isPlanBudget = (p.type || '').toLowerCase().includes('plan') || (p.type || '').toLowerCase().includes('inversion');
    return sameDept && (isPlan ? isPlanBudget : !isPlanBudget);
  });

  if (myBudget) {
    form.idPresupuesto = myBudget.idpresupuesto.toString();
  }
}

watch(() => form.typeCode, () => {
  updateBudgetSelection();
});


function addProduct() {
  form.products.push({ idProducto: '', precio: '' });
}

function removeProduct(index: number) {
  form.products.splice(index, 1);
}

async function handleCreateProduct() {
  if (!newProduct.nombre) return;
  const res = await orderStore.createProduct({
    nombre: newProduct.nombre,
    descripcion: newProduct.descripcion,
    idProveedor: newProduct.idProveedor,
  });
  if (res.status === 'success') {
    await orderStore.fetchProducts();
    if (form.products.length === 0) {
      form.products.push({ idProducto: res.idProducto, precio: '' });
    } else {
      const empty = form.products.find((p: any) => !p.idProducto);
      if (empty) empty.idProducto = res.idProducto;
      else form.products.push({ idProducto: res.idProducto, precio: '' });
    }
    showNewProduct.value = false;
    newProduct.nombre = '';
    newProduct.descripcion = '';
    newProduct.idProveedor = '';
  }
}

function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement;
  if (target.files) {
    form.invoices.push(...Array.from(target.files));
  }
}

async function submitOrder() {
  loading.value = true;
  try {
    const finalOrderNumber = form.usePlan ? '' : orderNumberPreview.value;
    const productIds = form.products.map((p: any) => p.idProducto).filter(Boolean).join(',');
    const productPrices = form.products.map((p: any) => p.precio || '0').join(',');
    const finalOrderData = {
      ...form,
      numero_orden: finalOrderNumber,
      Inversion: form.typeCode === '0' ? 'true' : 'false',
      Tipo: form.typeCode === '0' ? 'Invariable' : 'Fungible',
      products_ids: productIds,
      products_prices: productPrices,
    };
    
    const res = await orderStore.createOrder(finalOrderData);
    if (res.status === 'success') {
      const orderId = res.orderId;
      if (form.invoices.length > 0) {
        for (const file of form.invoices) {
           await orderStore.uploadInvoice(orderId, file);
        }
      }
      emit('success');
      emit('close');
    }
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
}

function nextStep() {
  if (step.value < 3) step.value++;
}

function prevStep() {
  if (step.value > 1) step.value--;
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
  <div v-if="isOpen" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-container">
      <div class="modal-header">
        <div class="header-content">
          <h2>Nueva Orden de Compra</h2>
          <p>Paso {{ step }} de 3: {{ step === 1 ? 'Información Base' : step === 2 ? 'Productos y Costes' : 'Adjuntos y Revisión' }}</p>
        </div>
        <button class="close-btn" @click="emit('close')">&times;</button>
      </div>

      <div class="modal-body">
        <!-- Step 1: Base Info -->
        <div v-if="step === 1" class="form-step">
            <!-- Removed manual budget selection as it's now automated -->

          <div class="type-toggle">
            <button 
              class="toggle-btn" 
              :class="{ active: !form.usePlan }" 
              @click="form.usePlan = false"
            >Nº Orden</button>
            <button 
              class="toggle-btn" 
              :class="{ active: form.usePlan }" 
              @click="form.usePlan = true"
            >Nº Plan</button>
          </div>

          <div v-if="!form.usePlan" class="order-gen-box">
             <div class="gen-inputs">
                <input v-model="form.departmentCode" placeholder="Dpto" class="small-input" readonly />
                <span>/</span>
                <input v-model="form.seqNumber" placeholder="Núm" class="small-input" />
                <span>/</span>
                <input v-model="form.year" placeholder="Año" class="small-input" />
                <span>/</span>
                <select v-model="form.typeCode" class="small-select">
                   <option value="1">Fungible (1)</option>
                   <option value="0">Invariable (0)</option>
                </select>
             </div>
             <p class="preview-text">Vista previa: <strong>{{ orderNumberPreview }}</strong></p>
          </div>

          <div v-else class="form-group">
            <label>Número de Plan (7 dígitos)</label>
            <input v-model="form.numero_plan" type="text" maxlength="7" placeholder="Ej: 1234567" class="form-input" />
          </div>
        </div>

        <!-- Step 2: Products -->
        <div v-if="step === 2" class="form-step">
          <div class="section-header">
             <h3>Productos</h3>
             <button class="add-btn" @click="addProduct">+ Añadir Producto</button>
          </div>

          <div v-for="(prod, idx) in form.products" :key="idx" class="product-row">
             <select v-model="prod.idProducto" class="form-input flex-2">
                <option value="">Seleccionar producto...</option>
                <option v-for="p in orderStore.products" :key="p.idproducto" :value="p.idproducto">
                   {{ p.nombre }}
                </option>
             </select>
             <input v-model="prod.precio" placeholder="Precio €" type="number" class="form-input flex-1" />
             <button class="remove-btn" @click="removeProduct(idx)">&times;</button>
          </div>

          <div class="new-product-link">
             <p v-if="!showNewProduct">¿No encuentras el producto? <a href="#" @click.prevent="showNewProduct = true">Crear uno nuevo</a></p>
             <div v-else class="new-product-form">
                <input v-model="newProduct.nombre" placeholder="Nombre del producto" class="form-input" />
                <textarea v-model="newProduct.descripcion" placeholder="Descripción (opcional)" class="form-input"></textarea>
                <div class="form-group" style="margin-top: 10px; margin-bottom: 10px;">
                  <label style="font-size: 12px; font-weight: 600; color: #475569;">Proveedor del producto</label>
                  <select v-model="newProduct.idProveedor" class="form-input">
                    <option value="">Sin proveedor</option>
                    <option v-for="s in orderStore.suppliers" :key="s.idproveedor" :value="s.idproveedor">
                      {{ s.nombre }}
                    </option>
                  </select>
                </div>
                <div class="actions">
                   <button class="cancel-btn" @click="showNewProduct = false">Cancelar</button>
                   <button class="save-btn" @click="handleCreateProduct">Crear y Asociar</button>
                </div>
             </div>
          </div>

          <div class="form-group total-group">
            <label>Importe Total (€) — calculado automáticamente</label>
            <input :value="totalAmount.toFixed(2)" type="number" step="0.01" class="form-input highlight" readonly />
          </div>
        </div>

        <!-- Step 3: Attachments & Review -->
        <div v-if="step === 3" class="form-step">
          <div class="form-group">
            <label>Descripción</label>
            <textarea v-model="form.descripcion" placeholder="Resumen o detalles de la orden..." class="form-input tall"></textarea>
          </div>

          <div class="form-group">
            <label>Facturas (Archivos)</label>
            <div class="file-drop-zone">
               <input type="file" multiple @change="handleFileChange" id="invoice-upload" hidden />
               <label for="invoice-upload" class="file-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24">
                    <path d="M12 5v14M5 12h14" />
                  </svg>
                  <span>Subir facturas</span>
               </label>
            </div>
            <ul v-if="form.invoices.length" class="file-list">
               <li v-for="(f, i) in form.invoices" :key="i">
                  {{ f.name }}
                  <span class="remove-file" @click="form.invoices.splice(i, 1)">&times;</span>
               </li>
            </ul>
          </div>
          
          <div class="review-box">
             <p><strong>Confirmación:</strong> Estás a punto de crear una orden por <strong>{{ form.Cantidad }}€</strong>.</p>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button v-if="step > 1" class="nav-btn secondary" @click="prevStep">Anterior</button>
        <div class="spacer"></div>
        <button v-if="step < 3" class="nav-btn primary" @click="nextStep">Siguiente</button>
        <button v-else class="nav-btn submit" :disabled="loading" @click="submitOrder">
          {{ loading ? 'Creando...' : 'Finalizar Pedido' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.modal-container {
  background: white;
  width: 90%;
  max-width: 600px;
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

.modal-header {
  padding: 24px 32px;
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.header-content p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #94a3b8;
  cursor: pointer;
  transition: color 0.2s;
}

.close-btn:hover { color: #dc2626; }

.modal-body {
  padding: 32px;
  max-height: 60vh;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  transition: all 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #dc2626;
  box-shadow: 0 0 0 4px rgba(220, 38, 38, 0.1);
}

.type-toggle {
  display: flex;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.toggle-btn {
  flex: 1;
  padding: 8px;
  border: none;
  background: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.toggle-btn.active {
  background: white;
  color: #dc2626;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.order-gen-box {
  background: #fff1f2;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #fecaca;
}

.gen-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.small-input {
  width: 60px;
  padding: 8px;
  border: 1px solid #fda4af;
  border-radius: 8px;
  text-align: center;
  font-weight: 600;
}

.small-select {
  flex: 1;
  padding: 8px;
  border: 1px solid #fda4af;
  border-radius: 8px;
}

.preview-text {
  margin: 0;
  font-size: 12px;
  color: #991b1b;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 { margin: 0; font-size: 16px; color: #1e293b; }

.add-btn {
  background: #f1f5f9;
  border: 1px dashed #cbd5e1;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
}

.product-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  align-items: center;
}

.flex-2 { flex: 2; }
.flex-1 { flex: 1; }

.remove-btn {
  background: #fee2e2;
  color: #ef4444;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
}

.total-group { margin-top: 24px; border-top: 1px solid #e2e8f0; padding-top: 24px; }
.highlight { border-color: #dc2626; font-size: 18px; font-weight: 700; color: #dc2626; }

.tall { height: 100px; resize: none; }

.file-drop-zone {
  border: 2px dashed #e2e8f0;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  transition: all 0.2s;
}

.file-drop-zone:hover { border-color: #dc2626; background: #fff1f2; }

.file-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #64748b;
  cursor: pointer;
}

.file-list {
  list-style: none;
  padding: 0;
  margin: 16px 0 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.file-list li {
  background: #f8fafc;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  color: #475569;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.remove-file { cursor: pointer; color: #ef4444; font-weight: bold; }

.modal-footer {
  padding: 24px 32px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  background: #f8fafc;
}

.nav-btn {
  padding: 10px 24px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.nav-btn.secondary { background: #e2e8f0; color: #475569; }
.nav-btn.primary { background: #1e293b; color: white; }
.nav-btn.submit { background: #dc2626; color: white; }
.nav-btn:hover { opacity: 0.9; transform: translateY(-1px); }
.nav-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.spacer { flex: 1; }

.new-product-form {
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  margin-top: 12px;
}

.new-product-form .actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 12px;
}

.save-btn {
  background: #dc2626;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.cancel-btn {
  background: #e2e8f0;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
</style>
