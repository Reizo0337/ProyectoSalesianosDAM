import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useDialogStore = defineStore('dialog', () => {
  const isOpen = ref(false);
  const title = ref('');
  const message = ref('');
  const resolvePromise = ref<((value: boolean) => void) | null>(null);

  function confirm(newTitle: string, newMessage: string) {
    title.value = newTitle;
    message.value = newMessage;
    isOpen.value = true;
    
    return new Promise<boolean>((resolve) => {
      resolvePromise.value = resolve;
    });
  }

  function handleConfirm() {
    isOpen.value = false;
    if (resolvePromise.value) resolvePromise.value(true);
  }

  function handleCancel() {
    isOpen.value = false;
    if (resolvePromise.value) resolvePromise.value(false);
  }

  return {
    isOpen,
    title,
    message,
    confirm,
    handleConfirm,
    handleCancel
  };
});
