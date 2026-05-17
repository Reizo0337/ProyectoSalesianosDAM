<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue';
import { useOrderStore } from '@/stores/orders';
import api from '@/api/axios';
import { useToast } from 'vue-toastification';

const toast = useToast();
const props = defineProps<{ 
  orderId: string | number,
  canComment: boolean 
}>();

const orderStore = useOrderStore();
const comments = ref<any[]>([]);
const newComment = ref('');
const sendingComment = ref(false);

// @mention system
const users = ref<any[]>([]);
const showMentionDropdown = ref(false);
const mentionFilter = ref('');
const mentionStartPos = ref(0);
const selectedMentionIndex = ref(0);
const inputRef = ref<HTMLInputElement | null>(null);

const filteredMentionUsers = computed(() => {
  const q = mentionFilter.value.toLowerCase();
  if (!q) return users.value.slice(0, 8);
  return users.value.filter(u => u.nombre.toLowerCase().includes(q)).slice(0, 8);
});

async function fetchUsers() {
  try {
    const res = await api.get(`/ordenes/usuarios?idOrden=${props.orderId}`);
    if (res.data.status === 'success') {
      users.value = res.data.usuarios || [];
    }
  } catch (err) {
    console.error('Error fetching users for mentions:', err);
  }
}

function handleInput() {
  const input = inputRef.value;
  if (!input) return;
  const text = input.value;
  const cursor = input.selectionStart || 0;
  const beforeCursor = text.substring(0, cursor);
  
  // Buscar el último @ que no esté dentro de corchetes ya cerrados
  const lastAtIndex = beforeCursor.lastIndexOf('@');

  if (lastAtIndex >= 0) {
    // Verificar que no es parte de un @[...] ya completado
    const afterAt = text.substring(lastAtIndex);
    const isCompleted = afterAt.match(/^@\[.+?\]/);
    if (isCompleted) {
      showMentionDropdown.value = false;
      return;
    }

    // Solo activar si el @ está al inicio o precedido por espacio
    const charBefore = lastAtIndex > 0 ? text[lastAtIndex - 1] : ' ';
    if (charBefore === ' ' || lastAtIndex === 0) {
      const partial = beforeCursor.substring(lastAtIndex + 1);
      // No activar si hay un corchete de cierre (ya se completó)
      if (!partial.includes(']')) {
        mentionFilter.value = partial.replace('[', '');
        mentionStartPos.value = lastAtIndex;
        showMentionDropdown.value = true;
        selectedMentionIndex.value = 0;
        return;
      }
    }
  }
  showMentionDropdown.value = false;
}

function handleKeydown(e: KeyboardEvent) {
  if (showMentionDropdown.value && filteredMentionUsers.value.length > 0) {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      selectedMentionIndex.value = Math.min(selectedMentionIndex.value + 1, filteredMentionUsers.value.length - 1);
      return;
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      selectedMentionIndex.value = Math.max(selectedMentionIndex.value - 1, 0);
      return;
    } else if (e.key === 'Enter' || e.key === 'Tab') {
      e.preventDefault();
      insertMention(filteredMentionUsers.value[selectedMentionIndex.value]);
      return;
    } else if (e.key === 'Escape') {
      showMentionDropdown.value = false;
      return;
    }
  }

  if (e.key === 'Enter' && !showMentionDropdown.value) {
    postComment();
  }
}

function insertMention(user: any) {
  const text = newComment.value;
  const cursor = inputRef.value?.selectionStart || text.length;
  const before = text.substring(0, mentionStartPos.value);
  const after = text.substring(cursor);
  
  // Formato: @[NombreCompleto]
  const mention = '@[' + user.nombre + '] ';
  newComment.value = before + mention + after;
  showMentionDropdown.value = false;
  
  nextTick(() => {
    if (inputRef.value) {
      const pos = mentionStartPos.value + mention.length;
      inputRef.value.focus();
      inputRef.value.setSelectionRange(pos, pos);
    }
  });
}

// Resalta @[Nombre] en los comentarios con un badge azul
function highlightComment(text: string): string {
  if (!text) return '';
  const escaped = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
  return escaped.replace(/@\[([^\]]+)\]/g, '<span class="mention-tag">@$1</span>');
}

function hideMentionDropdown() {
  window.setTimeout(() => { showMentionDropdown.value = false; }, 200);
}

async function fetchComments() {
  comments.value = await orderStore.fetchComments(props.orderId.toString());
}

async function postComment() {
  if (!newComment.value.trim()) return;
  sendingComment.value = true;
  
  const text = newComment.value;
  newComment.value = '';

  const tempComment = {
    idComentario: 'temp-' + Date.now(),
    usuario: 'Tú',
    fecha: 'Enviando...',
    comentario: text
  };
  comments.value.push(tempComment);
  
  try {
    const res = await orderStore.addComment(props.orderId.toString(), text);
    if (res.status === 'success') {
      await fetchComments();
    } else {
      comments.value = comments.value.filter(c => c.idComentario !== tempComment.idComentario);
      toast.error('Error al guardar el comentario: ' + res.message);
    }
  } catch (err) {
    comments.value = comments.value.filter(c => c.idComentario !== tempComment.idComentario);
    toast.error('Error al añadir comentario');
  } finally {
    sendingComment.value = false;
  }
}

onMounted(() => {
  fetchComments();
  fetchUsers();
});

defineExpose({ fetchComments });
</script>

<template>
  <div class="section-card comments-section">
    <h2>Comentarios y Observaciones</h2>
    <div class="comments-list">
      <div v-for="com in comments" :key="com.idComentario" class="comment-bubble">
        <div class="comment-header">
          <span class="comment-user">{{ com.usuario }}</span>
          <span class="comment-date">{{ com.fecha }}</span>
        </div>
        <div class="comment-text" v-html="highlightComment(com.comentario)"></div>
      </div>
      <div v-if="comments.length === 0" class="empty-comments">
        Aún no hay comentarios.
      </div>
    </div>
    
    <div v-if="canComment" class="add-comment-box">
      <div class="input-wrapper">
        <input 
          ref="inputRef"
          v-model="newComment" 
          type="text" 
          placeholder="Escribe un comentario... Usa @ para mencionar" 
          @input="handleInput"
          @keydown="handleKeydown"
          @blur="hideMentionDropdown"
          :disabled="sendingComment"
        />
        <transition name="mention-fade">
          <div v-if="showMentionDropdown && filteredMentionUsers.length > 0" class="mention-dropdown">
            <div 
              v-for="(user, idx) in filteredMentionUsers" 
              :key="user.id" 
              class="mention-option"
              :class="{ selected: idx === selectedMentionIndex }"
              @mousedown.prevent="insertMention(user)"
            >
              <div class="mention-avatar">{{ (user.nombre || '?')[0].toUpperCase() }}</div>
              <div class="mention-info">
                <span class="mention-name">{{ user.nombre }}</span>
                <span v-if="user.rol" class="mention-role">{{ user.rol }}</span>
              </div>
            </div>
          </div>
        </transition>
      </div>
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
.comments-section { background: #f8fafc !important; }
.comments-list {
  display: flex; flex-direction: column; gap: 16px;
  margin-bottom: 20px; max-height: 400px; overflow-y: auto; padding-right: 8px;
}
.comment-bubble {
  background: white; border: 1px solid #e2e8f0; border-radius: 4px;
  padding: 12px 16px; box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.comment-header { display: flex; justify-content: space-between; margin-bottom: 6px; }
.comment-user { font-weight: 700; font-size: 13px; color: #1e293b; }
.comment-date { font-size: 11px; color: #94a3b8; }
.comment-text { font-size: 14px; color: #334155; line-height: 1.5; }
.comment-text :deep(.mention-tag) {
  background: #dbeafe; color: #1d4ed8; padding: 1px 6px;
  border-radius: 3px; font-weight: 600; font-size: 13px;
}
.empty-comments { text-align: center; padding: 20px; color: #94a3b8; font-style: italic; font-size: 14px; }

.add-comment-box {
  display: flex; gap: 12px; background: white; padding: 8px;
  border-radius: 4px; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
}
.input-wrapper { flex: 1; position: relative; }
.input-wrapper input {
  width: 100%; border: none; padding: 8px 12px;
  font-size: 14px; outline: none; background: transparent;
}

.mention-dropdown {
  position: absolute; bottom: calc(100% + 6px); left: 0; right: 0;
  background: white; border: 1px solid #e2e8f0; border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.15); max-height: 260px;
  overflow-y: auto; z-index: 50;
}
.mention-option {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 14px; cursor: pointer; transition: background 0.15s;
}
.mention-option:hover, .mention-option.selected { background: #f0f7ff; }
.mention-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  color: white; font-weight: 700; font-size: 14px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.mention-info { display: flex; flex-direction: column; }
.mention-name { font-size: 13px; font-weight: 600; color: #1e293b; }
.mention-role { font-size: 11px; color: #94a3b8; }

.mention-fade-enter-active, .mention-fade-leave-active { transition: all 0.15s ease; }
.mention-fade-enter-from, .mention-fade-leave-to { opacity: 0; transform: translateY(4px); }

.send-btn {
  background: #2563eb; color: white; border: none; width: 40px; height: 40px;
  border-radius: 4px; display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.2s; flex-shrink: 0;
}
.send-btn:hover:not(:disabled) { background: #1d4ed8; transform: scale(1.05); }
.send-btn:disabled { background: #94a3b8; cursor: not-allowed; }
.loader-mini {
  width: 18px; height: 18px; border: 2px solid rgba(255,255,255,0.3);
  border-radius: 50%; border-top-color: white; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
