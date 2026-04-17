<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { computed, ref, watch, onMounted } from 'vue'

const props = defineProps<{
  type: 'activity' | 'project' | 'user' | 'task' | 'stats'
  title?: string
  data?: string | number
  suffix?: string
  icon?: string
  href?: string
  color?: string
  localizacion?: string
  background?: string
  animate?: boolean
}>()

const typeClass = computed(() => (props.type ? `  type-${props.type}` : ''))
const displayValue = ref<string | number>(0)

const animateValue = (start: number, end: number, duration: number) => {
  let startTimestamp: number | null = null;
  const step = (timestamp: number) => {
    if (!startTimestamp) startTimestamp = timestamp;
    const progress = Math.min((timestamp - startTimestamp) / duration, 1);
    const currentRawValue = progress * (end - start) + start;
    
    displayValue.value = currentRawValue.toLocaleString(undefined, {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    });

    if (progress < 1) {
      window.requestAnimationFrame(step);
    }
  };
  window.requestAnimationFrame(step);
};

watch(() => props.data, (newVal) => {
  const numericVal = typeof newVal === 'string' ? parseFloat(newVal) : newVal;
  if (props.animate && typeof numericVal === 'number' && !isNaN(numericVal)) {
    const currentVal = typeof displayValue.value === 'string' 
      ? parseFloat(displayValue.value.replace(/[^\d.-]/g, '')) 
      : displayValue.value;
    animateValue(Number(currentVal) || 0, numericVal, 1000);
  } else {
    displayValue.value = numericVal?.toLocaleString() ?? '0';
  }
}, { immediate: true });

onMounted(() => {
  const numericVal = typeof props.data === 'string' ? parseFloat(props.data) : props.data;
  if (props.animate && typeof numericVal === 'number' && !isNaN(numericVal)) {
    // If the watch haven't already started an animation with these values
    if (displayValue.value === 0 || displayValue.value === '0') {
      animateValue(0, numericVal, 1000);
    }
  } else {
    displayValue.value = numericVal?.toLocaleString() ?? '0';
  }
});
</script>

<template>
  <div
    class="statistics-card"
    :class="typeClass"
    :style="background ? { background: background } : {}"
  >
    <div class="card-header">
      <div class="icon-wrapper" v-if="icon" :style="{ backgroundColor: 'rgba(255,255,255,0.2)' }">
        <span class="material-symbols-outlined">{{ icon }} </span>
      </div>
      <h3 v-if="title">{{ title }}</h3>
      <slot name="actions"></slot>
    </div>
    
    <div class="card-body">
      <div class="data-wrapper">
        <span class="value">{{ displayValue }}</span>
        <span class="suffix" v-if="suffix">{{ suffix }}</span>
      </div>
      <slot name="content"></slot>
      
      <div class="localizacion" v-if="localizacion">
        <span class="material-symbols-outlined">location_on</span>
        <p>{{ localizacion }}</p>
      </div>
    </div>

    <div class="card-footer" v-if="href">
      <RouterLink :to="href" class="link">
        <span>Ver detalles</span>
        <span class="material-symbols-outlined">arrow_forward</span>
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.statistics-card {
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  padding: 24px;
  background-color: var(--card-bg, #ffffff);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.statistics-card:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.card-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: inherit;
  margin: 0;
  opacity: 0.9;
}

.card-body {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.data-wrapper {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.value {
  font-size: 2.5rem;
  font-weight: 800;
  letter-spacing: -1px;
}

.suffix {
  font-size: 1.25rem;
  font-weight: 600;
  opacity: 0.8;
}

.localizacion {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 12px;
  font-size: 0.875rem;
  opacity: 0.8;
}

.card-footer {
  margin-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 16px;
}

.link {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 600;
  color: inherit;
  transition: opacity 0.2s;
}

.link:hover {
  opacity: 0.7;
}

/* --- STATS VARIANT --- */
.type-stats {
  color: white;
  border: none;
  min-width: 280px;
}

.type-stats .icon-wrapper {
  background-color: rgba(255, 255, 255, 0.2) !important;
}

.type-stats .link {
  color: white;
}

/* ACTIVITY VARIANT (Small) */
.type-activity {
  background-color: #f3f4f6;
  width: 220px;
  padding: 16px;
}

.type-activity .value {
  font-size: 1.5rem;
}
</style>
