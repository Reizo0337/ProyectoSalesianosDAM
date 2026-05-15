<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useOrderStore } from '@/stores/orders';

const props = defineProps<{ 
  orderId: string | number,
  canComment: boolean 
}>();

const orderStore = useOrderStore();
const comments = ref<any[]>([]);
const newComment = ref('');
const sendingComment = ref(false);

async function fetchComments() {
  comments.value = await orderStore.fetchComments(props.orderId.toString());
}

async function postComment() {
  if (!newComment.value.trim()) return;
  sendingComment.value = true;
  
  const text = newComment.value;
  newComment.value = ''; // Clean input immediately for instant feedback
  
  // Optimistic UI Update: Push immediately
  const tempComment = {
    idComentario: 'temp-' + Date.now(),
    usuarioNombre: 'Tú', // Local placeholder
    fechaStr: 'Enviando...',
    comentario: text
  };
  comments.value.push(tempComment);
  
  try {
    const res = await orderStore.addComment(props.orderId.toString(), text);
    if (res.status === 'success') {
      await fetchComments(); // Sync with real DB data to get actual ID and Date
    } else {
      // Revert if failed
      comments.value = comments.value.filter(c => c.idComentario !== tempComment.idComentario);
      alert('Error al guardar el comentario: ' + res.message);
    }
  } catch (err) {
    comments.value = comments.value.filter(c => c.idComentario !== tempComment.idComentario);
    alert('Error al añadir comentario de red');
  } finally {
    sendingComment.value = false;
  }
}

onMounted(fetchComments);

defineExpose({ fetchComments });
</script>

<template>
  <div class="section-card comments-section">
    <h2>Comentarios y Observaciones</h2>
    <div class="comments-list">
      <div v-for="com in comments" :key="com.idComentario" class="comment-bubble">
        <div class="comment-header">
          <span class="comment-user">{{ com.usuarioNombre }}</span>
          <span class="comment-date">{{ com.fechaStr }}</span>
        </div>
        <div class="comment-text">{{ com.comentario }}</div>
      </div>
      <div v-if="comments.length === 0" class="empty-comments">
        Aún no hay comentarios.
      </div>
    </div>
    
    <div v-if="canComment" class="add-comment-box">
      <input 
        v-model="newComment" 
        type="text" 
        placeholder="Añadir una observación..." 
        @keyup.enter="postComment"
        :disabled="sendingComment"
      />
      <button @click="postComment" class="send-btn" :disabled="!newComment.trim() || sendingComment">
        <svg v-if="!sendingComment" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18">
          <line x1="22" y1="2" x2="11" y2="13"></line>
          <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
        </svg>
        <span v-else class="loader-mini"></span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.comments-section {
  background: #f8fafc !important;
}
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
  max-height: 400px;
  overflow-y: auto;
  padding-right: 8px;
}
.comment-bubble {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 12px 16px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}
.comment-user {
  font-weight: 700;
  font-size: 13px;
  color: #1e293b;
}
.comment-date {
  font-size: 11px;
  color: #94a3b8;
}
.comment-text {
  font-size: 14px;
  color: #334155;
  line-height: 1.5;
}
.empty-comments {
  text-align: center;
  padding: 20px;
  color: #94a3b8;
  font-style: italic;
  font-size: 14px;
}
.add-comment-box {
  display: flex;
  gap: 12px;
  background: white;
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}
.add-comment-box input {
  flex: 1;
  border: none;
  padding: 8px 12px;
  font-size: 14px;
  outline: none;
  background: transparent;
}
.send-btn {
  background: #2563eb;
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}
.send-btn:hover:not(:disabled) {
  background: #1d4ed8;
  transform: scale(1.05);
}
.send-btn:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}
.loader-mini {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
