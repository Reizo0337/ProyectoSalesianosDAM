<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue';
import { usePresupuestoStore } from '@/stores/presupuesto';
import { useAuthStore } from '@/stores/auth';
import { useToast } from 'vue-toastification';
import api from '@/api/axios';
import Table from '@/components/common/Table.vue';
 
const presupuestoStore = usePresupuestoStore();
const authStore = useAuthStore();
const toast = useToast();
const loading = ref(true);
const selectedYear = ref(new Date().getFullYear());

// Estados del modal de edición/creación
const showModal = ref(false);
const isEditing = ref(false);
const departamentos = ref<{ id: string, nombre: string }[]>([]);
const form = ref({
  idpresupuesto: 0,
  codigo: '',
  nombrepresupuesto: '',
  cantidad: 0,
  gasto: 0,
  iddepartamento: '',
  type: 'presupuesto'
});
 
async function loadData() {
  loading.value = true;
  try {
    const role = authStore.user?.rol;
    const dept = authStore.user?.nombreDepartamento;
    
    if (role === 'Administrador' || role === 'Contable') {
      await presupuestoStore.getAllPresupuestos(selectedYear.value);
    } else if (dept) {
      await presupuestoStore.getPresupuestosByDept(dept, selectedYear.value);
    }
  } finally {
    loading.value = false;
  }
}

async function loadDepartamentos() {
  if (authStore.user?.rol === 'Administrador') {
    try {
      const response = await api.get('/usuarios/departamentos');
      departamentos.value = response.data;
    } catch (err) {
      console.error('Error al cargar departamentos:', err);
    }
  }
}

// Propiedades computadas para la vista previa en tiempo real de los campos autogenerados
const selectedDeptInfo = computed(() => {
  return departamentos.value.find(d => String(d.id) === String(form.value.iddepartamento));
});

const generatedCodePreview = computed(() => {
  if (!selectedDeptInfo.value) return '-';
  const prefix = form.value.type === 'planInversion' ? 'PLAN' : 'PRES';
  // Extraemos una abreviatura simplificada para el preview visual
  const deptPart = selectedDeptInfo.value.nombre.substring(0, 3).toUpperCase();
  return `${prefix}-${deptPart}-${selectedYear.value}`;
});

const generatedNamePreview = computed(() => {
  if (!selectedDeptInfo.value) return '-';
  const prefix = form.value.type === 'planInversion' ? 'Plan de Inversión' : 'Presupuesto';
  return `${prefix} ${selectedDeptInfo.value.nombre} ${selectedYear.value}`;
});

function openCreateModal() {
  isEditing.value = false;
  form.value = {
    idpresupuesto: 0,
    codigo: '',
    nombrepresupuesto: '',
    cantidad: 0,
    gasto: 0,
    iddepartamento: departamentos.value[0]?.id || '',
    type: 'presupuesto'
  };
  showModal.value = true;
}

function openEditModal(presupuesto: any) {
  isEditing.value = true;
  const dept = departamentos.value.find(d => d.nombre === presupuesto.nombredepartamento);
  
  form.value = {
    idpresupuesto: presupuesto.idpresupuesto,
    codigo: presupuesto.codigo || '',
    nombrepresupuesto: presupuesto.nombrepresupuesto || presupuesto.Nombre || '',
    cantidad: Number(presupuesto.cantidad || 0),
    gasto: Number(presupuesto.gasto || 0),
    iddepartamento: dept ? dept.id : (presupuesto.iddepartamento || ''),
    type: presupuesto.type || 'presupuesto'
  };
  showModal.value = true;
}

async function handleSave() {
  if (form.value.cantidad <= 0 || !form.value.iddepartamento) {
    toast.warning('Por favor, asigne una cantidad válida mayor que 0 y seleccione un departamento');
    return;
  }
  
  loading.value = true;
  try {
    let result;
    const payload = {
      ...form.value,
      anio: selectedYear.value
    };
    
    if (isEditing.value) {
      result = await presupuestoStore.updatePresupuesto(payload);
    } else {
      result = await presupuestoStore.createPresupuesto(payload);
    }
    
    if (result.success) {
      toast.success(isEditing.value ? 'Presupuesto actualizado correctamente' : 'Presupuesto creado correctamente');
      showModal.value = false;
      await loadData();
    } else {
      toast.error(result.error || 'Error al guardar el presupuesto');
    }
  } catch (error) {
    toast.error('Ocurrió un error inesperado');
  } finally {
    loading.value = false;
  }
}
 
const stats = computed(() => {
  const list = presupuestoStore.presupuestos || [];
  
  const total = list.reduce((acc, p) => acc + Number(p.cantidad || 0), 0);
  const spent = list.reduce((acc, p) => acc + Number(p.gasto || 0), 0);
  const available = total - spent;
  const percent = total > 0 ? Math.round((spent / total) * 100) : 0;
 
  const ordinario = list
    .filter(p => !p.type?.toLowerCase().includes('inversion'))
    .reduce((acc, p) => acc + Number(p.cantidad || 0), 0);
    
  const inversion = list
    .filter(p => p.type?.toLowerCase().includes('inversion'))
    .reduce((acc, p) => acc + Number(p.cantidad || 0), 0);
 
  return { total, spent, available, percent, ordinario, inversion };
});

const tableHeaders = computed(() => {
  const base = ['Departamento', 'Tipo', 'Código', 'Partida', 'Asignado', 'Gastado', 'Ejecución'];
  if (authStore.user?.rol === 'Administrador') {
    base.push('Acciones');
  }
  return base;
});

const tableData = computed(() => {
  const list = presupuestoStore.presupuestos || [];
  return list.map(p => {
    const row: any[] = [
      p.nombredepartamento,
      p.type?.toLowerCase() === 'planinversion' ? 'INVERSIÓN' : 'ORDINARIO',
      p.codigo || '-',
      p.nombrepresupuesto || '-',
      Number(p.cantidad).toLocaleString() + '€',
      Number(p.gasto).toLocaleString() + '€',
      { 
        component: 'ProgressBar', 
        props: { 
          value: (Number(p.gasto) / Number(p.cantidad) * 100) || 0,
          color: p.type?.toLowerCase().includes('inversion') ? '#8b5cf6' : '#ef4444'
        } 
      }
    ];
    if (authStore.user?.rol === 'Administrador') {
      row.push({
        type: 'actions',
        actions: [
          {
            icon: 'edit',
            label: 'Editar Presupuesto',
            class: 'btn-edit',
            onClick: () => openEditModal(p)
          }
        ]
      });
    }
    return row;
  });
});
 
onMounted(() => {
  loadData();
  loadDepartamentos();
});

watch(selectedYear, loadData);
</script>
 
<template>
  <div class="view-page animate-in">
    <header class="dashboard-header">
      <div class="header-left">
        <h1>Análisis Presupuestario</h1>
        <p class="subtitle">Estado de ejecución y disponibilidad de fondos para el ejercicio {{ selectedYear }}.</p>
      </div>
      <div class="header-actions">
        <button 
          v-if="authStore.user?.rol === 'Administrador'" 
          class="btn-primary" 
          @click="openCreateModal"
        >
          <span class="material-symbols-outlined">add</span>
          Nuevo Presupuesto
        </button>
        <div class="year-selector">
          <span class="material-symbols-outlined">calendar_today</span>
          <select v-model="selectedYear" class="year-select">
            <option v-for="y in [2024, 2025, 2026]" :key="y" :value="y">{{ y }}</option>
          </select>
        </div>
      </div>
    </header>
 
    <!-- KPIs Premium Section -->
    <div class="stats-container">
      <div class="main-stat-card">
        <div class="card-content">
          <div class="stat-label">Ejecución Global</div>
          <div class="stat-value">{{ stats.percent }}%</div>
          <div class="progress-container">
            <div class="progress-bar" :style="{ width: stats.percent + '%' }"></div>
          </div>
          <div class="stat-footer">
            <span><strong>{{ stats.spent.toLocaleString() }}€</strong> consumidos</span>
            <span><strong>{{ stats.total.toLocaleString() }}€</strong> total</span>
          </div>
        </div>
        <div class="card-bg-icon">
          <span class="material-symbols-outlined">analytics</span>
        </div>
      </div>
 
      <div class="side-stats">
        <div class="mini-card available">
          <div class="mini-icon"><span class="material-symbols-outlined">account_balance_wallet</span></div>
          <div class="mini-info">
            <span class="mini-label">Disponible</span>
            <span class="mini-value">{{ stats.available.toLocaleString() }}€</span>
          </div>
        </div>
        <div class="mini-card ordinario">
          <div class="mini-icon"><span class="material-symbols-outlined">receipt_long</span></div>
          <div class="mini-info">
            <span class="mini-label">P. Ordinario</span>
            <span class="mini-value">{{ stats.ordinario.toLocaleString() }}€</span>
          </div>
        </div>
        <div class="mini-card inversion">
          <div class="mini-icon"><span class="material-symbols-outlined">precision_manufacturing</span></div>
          <div class="mini-info">
            <span class="mini-label">P. Inversión</span>
            <span class="mini-value">{{ stats.inversion.toLocaleString() }}€</span>
          </div>
        </div>
      </div>
    </div>
 
    <!-- Listado Detallado -->
    <section class="dashboard-section">
      <div class="section-header">
        <span class="material-symbols-outlined">list_alt</span>
        <h2>Desglose por Partidas</h2>
      </div>
      <Table 
        :loading="loading"
        :headers="tableHeaders"
        :data="tableData"
      />
    </section>

    <!-- Modal de Crear / Editar Presupuesto -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card animate-up">
        <div class="modal-header">
          <h2>{{ isEditing ? 'Editar Presupuesto' : 'Nuevo Presupuesto' }}</h2>
          <button @click="showModal = false" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-grid">
            
            <!-- Tarjeta de Vista Previa Dinámica y Premium -->
            <div class="preview-card-container">
              <span class="preview-badge">Autogenerado</span>
              <div class="preview-item">
                <span class="preview-label">Código del Presupuesto</span>
                <span class="preview-value code">{{ isEditing ? form.codigo : generatedCodePreview }}</span>
              </div>
              <div class="preview-item">
                <span class="preview-label">Nombre de la Partida</span>
                <span class="preview-value name">{{ isEditing ? form.nombrepresupuesto : generatedNamePreview }}</span>
              </div>
            </div>

            <div class="form-group">
              <label>Cantidad Asignada (€)</label>
              <input 
                v-model.number="form.cantidad" 
                type="number" 
                step="0.01" 
                class="form-input" 
                placeholder="0.00" 
                required 
              />
            </div>
            <div class="form-group" v-if="isEditing">
              <label>Gasto Acumulado (€)</label>
              <input 
                v-model.number="form.gasto" 
                type="number" 
                step="0.01" 
                class="form-input" 
                placeholder="0.00" 
              />
            </div>
            <div class="form-group">
              <label>Departamento Responsable</label>
              <select v-model="form.iddepartamento" class="form-select" required :disabled="isEditing">
                <option value="" disabled>Selecciona un departamento</option>
                <option v-for="dept in departamentos" :key="dept.id" :value="dept.id">
                  {{ dept.nombre }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>Tipo de Presupuesto</label>
              <select v-model="form.type" class="form-select" required :disabled="isEditing">
                <option value="presupuesto">Presupuesto Ordinario (PRES-)</option>
                <option value="planInversion">Plan de Inversión (PLAN-)</option>
              </select>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showModal = false" class="btn-cancel">Cancelar</button>
          <button @click="handleSave" class="btn-submit">
            {{ isEditing ? 'Guardar Cambios' : 'Crear Presupuesto' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
 
<style scoped>
.dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
.header-left h1 { font-size: 2.75rem; font-weight: 850; color: #0f172a; letter-spacing: -0.04em; margin-bottom: 4px; }
.subtitle { color: #64748b; font-size: 1.15rem; }
 
.year-selector {
  display: flex; align-items: center; gap: 8px; background: white; padding: 10px 16px;
  border-radius: 8px; border: 1px solid #e2e8f0; color: #64748b;
}
.year-select { border: none; outline: none; font-weight: 700; color: #0f172a; cursor: pointer; }
 
/* KPIs Design */
.stats-container { display: grid; grid-template-columns: 1.5fr 1fr; gap: 2rem; margin-bottom: 4rem; }
 
.main-stat-card {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  border-radius: 20px; padding: 3rem; color: white; position: relative; overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(15, 23, 42, 0.2);
}
 
.stat-label { font-size: 0.9rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.1em; opacity: 0.6; margin-bottom: 1rem; }
.stat-value { font-size: 5rem; font-weight: 900; letter-spacing: -4px; line-height: 1; margin-bottom: 2rem; }
.progress-container { height: 12px; background: rgba(255,255,255,0.1); border-radius: 6px; overflow: hidden; margin-bottom: 1.5rem; }
.progress-bar { height: 100%; background: #ef4444; border-radius: 6px; transition: width 1s cubic-bezier(0.4, 0, 0.2, 1); }
.stat-footer { display: flex; justify-content: space-between; font-size: 1rem; color: rgba(255,255,255,0.6); }
.stat-footer strong { color: white; }
 
.card-bg-icon { position: absolute; right: -20px; top: -20px; opacity: 0.05; }
.card-bg-icon .material-symbols-outlined { font-size: 15rem; }
 
.side-stats { display: flex; flex-direction: column; gap: 1rem; }
.mini-card {
  background: white; border-radius: 16px; padding: 1.5rem; display: flex; align-items: center; gap: 1.5rem;
  border: 1px solid #e2e8f0; transition: all 0.2s;
}
.mini-card:hover { transform: translateX(8px); border-color: #cbd5e1; }
.mini-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.mini-icon .material-symbols-outlined { font-size: 24px; }
 
.available .mini-icon { background: #eff6ff; color: #3b82f6; }
.ordinario .mini-icon { background: #f0fdf4; color: #16a34a; }
.inversion .mini-icon { background: #f5f3ff; color: #8b5cf6; }
 
.mini-label { display: block; font-size: 0.75rem; font-weight: 700; color: #64748b; text-transform: uppercase; margin-bottom: 2px; }
.mini-value { font-size: 1.5rem; font-weight: 800; color: #0f172a; }
 
.dashboard-section { margin-top: 2rem; }
.section-header { display: flex; align-items: center; gap: 12px; margin-bottom: 2rem; }
.section-header h2 { font-size: 1.75rem; font-weight: 800; color: #0f172a; }

/* Botón primario */
.header-actions { display: flex; align-items: center; gap: 12px; }
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 6px -1px rgba(15, 23, 42, 0.1), 0 2px 4px -2px rgba(15, 23, 42, 0.1);
}
.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(15, 23, 42, 0.2), 0 4px 6px -4px rgba(15, 23, 42, 0.2);
  filter: brightness(1.15);
}
.btn-primary:active {
  transform: translateY(0);
}
.btn-primary span {
  font-size: 20px;
}

/* Modal Styles */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(15, 23, 42, 0.7); backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.modal-card { 
  background: white; width: 95%; max-width: 500px; border-radius: 16px; 
  overflow: hidden; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5); 
}
.modal-header { 
  padding: 1.5rem; border-bottom: 1px solid #e2e8f0; 
  display: flex; justify-content: space-between; align-items: center; 
}
.modal-header h2 { font-size: 1.5rem; font-weight: 800; color: #0f172a; margin: 0; }
.modal-body { padding: 1.5rem 2rem; }
.modal-footer { 
  padding: 1.5rem; background: #f8fafc; 
  display: flex; justify-content: flex-end; gap: 1rem; 
}
.form-grid { display: grid; grid-template-columns: 1fr; gap: 1rem; }
.form-group label { 
  display: block; font-size: 11px; font-weight: 700; color: #64748b; 
  text-transform: uppercase; margin-bottom: 6px; 
}
.form-select, .form-input { 
  width: 100%; padding: 12px; border-radius: 8px; border: 1px solid #e2e8f0; 
  outline: none; font-size: 14px; box-sizing: border-box;
}
.form-input:focus, .form-select:focus { 
  border-color: #0f172a; box-shadow: 0 0 0 3px rgba(15,23,42,0.1); 
}
.btn-submit { 
  background: #0f172a; color: white; border: none; padding: 10px 20px; 
  border-radius: 8px; font-weight: 600; cursor: pointer; transition: all 0.2s; 
}
.btn-submit:hover { background: #1e293b; }
.btn-cancel { 
  background: transparent; border: none; color: #64748b; 
  font-weight: 600; cursor: pointer; 
}
.close-btn { background: none; border: none; font-size: 24px; color: #94a3b8; cursor: pointer; }

/* Tarjeta de Vista Previa Premium */
.preview-card-container {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.25rem;
  margin-bottom: 0.5rem;
  position: relative;
  overflow: hidden;
  box-shadow: inset 0 2px 4px 0 rgba(0, 0, 0, 0.02);
}
.preview-badge {
  position: absolute;
  top: 10px;
  right: 12px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 10px;
  font-weight: 800;
  padding: 2px 8px;
  border-radius: 999px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border: 1px solid #dbeafe;
}
.preview-item {
  margin-bottom: 0.75rem;
}
.preview-item:last-child {
  margin-bottom: 0;
}
.preview-label {
  display: block;
  font-size: 10px;
  font-weight: 800;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  margin-bottom: 2px;
}
.preview-value {
  display: block;
  font-size: 0.95rem;
  font-weight: 700;
  color: #334155;
}
.preview-value.code {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  color: #2563eb;
  font-size: 0.85rem;
  letter-spacing: -0.02em;
}
.preview-value.name {
  color: #0f172a;
}

/* Animación Modal */
.animate-up {
  animation: slideUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
@keyframes slideUp {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>
