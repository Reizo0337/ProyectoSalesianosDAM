<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { computed, ref, watch, onMounted } from 'vue'

const props = defineProps<{
  type: 'activity' | 'project' | 'user' | 'task' | 'stats'
  title?: string
  data?: string | number
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
    displayValue.value = Math.floor(progress * (end - start) + start);
    if (progress < 1) {
      window.requestAnimationFrame(step);
    }
  };
  window.requestAnimationFrame(step);
};

watch(() => props.data, (newVal) => {
  if (props.animate && typeof newVal === 'number') {
    animateValue(Number(displayValue.value) || 0, newVal, 1000);
  } else {
    displayValue.value = newVal ?? 0;
  }
}, { immediate: false });

onMounted(() => {
  if (props.animate && typeof props.data === 'number') {
    animateValue(0, props.data, 1000);
  } else {
    displayValue.value = props.data ?? 0;
  }
});
</script>

<template>
  <div
    class="statistics-card"
    :class="typeClass"
    :style="background ? { backgroundColor: background } : {}"
  >
    <div class="title">
      <span
        class="material-symbols-outlined"
        :style="{ color: color }"
        v-if="icon"
        >{{ icon }}</span
      >
      <h3>{{ title }}</h3>
      <slot name="actions"></slot>
    </div>
    <div class="unify">
      <div class="data">
        <p v-if="props.data !== undefined && props.data !== null">{{ displayValue }}</p>
        <slot name="content"></slot>
      </div>
      <div class="localizacion" v-if="localizacion">
        <p>{{ localizacion }}</p>
      </div>
      <RouterLink v-if="href" class="link" :to="href">
        Ver detalles
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
/* --- 1. BASE COMÚN --- */
.statistics-card {
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid var(--border-color);
  box-sizing: border-box;
  background-color: var(--card-bg);
  transition: all 0.3s ease;
}

.title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title h3 {
  color: var(--stats-primary);
  transition: color 0.3s ease;
}

.unify {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.data {
  color: var(--button-primary);
  transition: color 0.3s ease;
}
.link {
  font-size: 14px;
  color: var(--button-primary);
  text-decoration: none;
  transition: color 0.3s ease;
}

/* --- 3. VARIANTES --- */
.type-activity {
  background-color: #eaebed;
  width: 200px;
}

/* --- 4. STATS (Específicos) --- */
.type-stats {
  width: 300px;
  min-height: 100px;
  height: 150px;
  position: relative;
  color: #fff;
  border: none;
}
.type-stats .title {
  justify-content: center;
  color: #fff;
}
.type-stats .data {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 38px;
  font-weight: bold;
  color: #fff;
}
.type-stats .link {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  color: #fff;
}
</style>
