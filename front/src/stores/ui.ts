import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUIStore = defineStore('ui', () => {
  const isSidebarCollapsed = ref(false);

  function toggleSidebar() {
    isSidebarCollapsed.value = !isSidebarCollapsed.value;
  }

  function setSidebar(value: boolean) {
    isSidebarCollapsed.value = value;
  }

  return {
    isSidebarCollapsed,
    toggleSidebar,
    setSidebar
  };
});
