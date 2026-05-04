<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as pdfjsLib from 'pdfjs-dist'

// Configurar el worker de pdf.js usando un CDN para evitar problemas de MIME type en el servidor
pdfjsLib.GlobalWorkerOptions.workerSrc = `https://cdn.jsdelivr.net/npm/pdfjs-dist@4.4.168/build/pdf.worker.min.mjs`

const props = defineProps<{
  pdfUrl: string
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
const containerRef = ref<HTMLDivElement | null>(null)
const numPages = ref(0)
const currentPage = ref(1)
const isLoading = ref(true)
const scale = ref(1.5)
let pdfDoc: any = null

const renderPage = async (num: number) => {
  if (!pdfDoc || !canvasRef.value) return

  const page = await pdfDoc.getPage(num)
  const viewport = page.getViewport({ scale: scale.value })
  const canvas = canvasRef.value
  const context = canvas.getContext('2d')

  canvas.height = viewport.height
  canvas.width = viewport.width

  const renderContext = {
    canvasContext: context,
    viewport: viewport
  }

  await page.render(renderContext).promise
}

const loadPdf = async () => {
  isLoading.value = true
  try {
    const loadingTask = pdfjsLib.getDocument({
      url: props.pdfUrl,
      withCredentials: true
    })
    pdfDoc = await loadingTask.promise
    numPages.value = pdfDoc.numPages
    await renderPage(currentPage.value)
  } catch (error) {
    console.error('Error loading PDF:', error)
  } finally {
    isLoading.value = false
  }
}

watch(() => props.pdfUrl, loadPdf)

onMounted(loadPdf)

const nextPage = () => {
  if (currentPage.value < numPages.value) {
    currentPage.value++
    renderPage(currentPage.value)
  }
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    renderPage(currentPage.value)
  }
}

const zoomIn = () => {
  scale.value += 0.2
  renderPage(currentPage.value)
}

const zoomOut = () => {
  if (scale.value > 0.5) {
    scale.value -= 0.2
    renderPage(currentPage.value)
  }
}
</script>

<template>
  <div class="pdf-viewer-container" ref="containerRef">
    <div class="pdf-toolbar">
      <div class="toolbar-group">
        <button @click="prevPage" :disabled="currentPage <= 1" class="tool-btn">
          <span class="material-symbols-outlined">chevron_left</span>
        </button>
        <span class="page-info">Página {{ currentPage }} de {{ numPages }}</span>
        <button @click="nextPage" :disabled="currentPage >= numPages" class="tool-btn">
          <span class="material-symbols-outlined">chevron_right</span>
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button @click="zoomOut" class="tool-btn">
          <span class="material-symbols-outlined">remove</span>
        </button>
        <span class="zoom-info">{{ Math.round(scale * 100) }}%</span>
        <button @click="zoomIn" class="tool-btn">
          <span class="material-symbols-outlined">add</span>
        </button>
      </div>
    </div>

    <div class="pdf-canvas-wrapper" v-show="!isLoading">
      <canvas ref="canvasRef"></canvas>
    </div>

    <div v-if="isLoading" class="pdf-loading">
      <div class="loader"></div>
      <span>Cargando documento...</span>
    </div>
  </div>
</template>

<style scoped>
.pdf-viewer-container {
  display: flex;
  flex-direction: column;
  background: var(--bg-tertiary);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  width: 100%;
}

.pdf-toolbar {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 20px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  z-index: 10;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: var(--border-color);
}

.tool-btn {
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 4px;
  cursor: pointer;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  transition: all 0.2s;
}

.tool-btn:hover:not(:disabled) {
  background: var(--button-secondary-hover);
  border-color: var(--button-primary);
  color: var(--button-primary);
}

.tool-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info, .zoom-info {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  min-width: 80px;
  text-align: center;
}

.pdf-canvas-wrapper {
  flex: 1;
  overflow: auto;
  padding: 20px;
  display: flex;
  justify-content: center;
  background: #525659; /* Color estándar de visores de PDF */
  min-height: 500px;
}

canvas {
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
  max-width: 100%;
  height: auto !important;
}

.pdf-loading {
  height: 500px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 15px;
  color: var(--text-secondary);
}

.loader {
  width: 40px;
  height: 40px;
  border: 4px solid var(--border-color);
  border-top: 4px solid var(--button-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

:global(.dark) .pdf-viewer-container {
  background: #1a1a1a;
}
</style>
