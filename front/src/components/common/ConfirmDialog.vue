<script setup lang="ts">
import { useDialogStore } from '@/stores/dialog';

const dialogStore = useDialogStore();
</script>

<template>
  <Transition name="fade">
    <div v-if="dialogStore.isOpen" class="dialog-overlay" @click.self="dialogStore.handleCancel">
      <div class="dialog-card">
        <div class="dialog-header">
          <span class="material-symbols-outlined warning-icon">warning</span>
          <h3>{{ dialogStore.title }}</h3>
        </div>
        <div class="dialog-body">
          <p>{{ dialogStore.message }}</p>
        </div>
        <div class="dialog-actions">
          <button @click="dialogStore.handleCancel" class="dialog-btn secondary">
            Cancelar
          </button>
          <button @click="dialogStore.handleConfirm" class="dialog-btn primary">
            Confirmar
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.dialog-card {
  background: #1e293b;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  width: 90%;
  max-width: 400px;
  padding: 32px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.dialog-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  text-align: center;
}

.warning-icon {
  font-size: 48px;
  color: #fbbf24;
  background: rgba(251, 191, 36, 0.1);
  padding: 12px;
  border-radius: 50%;
}

.dialog-header h3 {
  color: white;
  font-size: 20px;
  font-weight: 800;
}

.dialog-body {
  text-align: center;
  margin-bottom: 32px;
}

.dialog-body p {
  color: #94a3b8;
  font-size: 15px;
  line-height: 1.5;
}

.dialog-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.dialog-btn {
  height: 48px;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.dialog-btn.primary {
  background: #ef4444;
  color: white;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2);
}

.dialog-btn.primary:hover {
  background: #dc2626;
  transform: translateY(-2px);
}

.dialog-btn.secondary {
  background: rgba(255, 255, 255, 0.05);
  color: #cbd5e1;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.dialog-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.1);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px) scale(0.95); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
