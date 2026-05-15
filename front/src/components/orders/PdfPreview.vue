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
  background: #f8fafc;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  width: 100%;
}

.pdf-toolbar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  padding: 10px 20px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
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
  background: #e2e8f0;
}

.tool-btn {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 6px;
  cursor: pointer;
  color: #1e293b;
  display: flex;
  align-items: center;
  transition: all 0.2s;
}

.tool-btn:hover:not(:disabled) {
  background: #f8fafc;
  border-color: #0f172a;
  color: #0f172a;
}

.tool-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-info, .zoom-info {
  font-size: 13px;
  font-weight: 700;
  color: #475569;
  min-width: 100px;
  text-align: center;
}

.pdf-canvas-wrapper {
  flex: 1;
  overflow: auto;
  padding: 30px;
  display: flex;
  justify-content: center;
  background: #f1f5f9;
  min-height: 600px;
}

canvas {
  box-shadow: 0 10px 30px rgba(0,0,0,0.15);
  max-width: 100%;
  height: auto !important;
  background: white;
}

.pdf-loading {
  height: 500px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 15px;
  color: #64748b;
}

.loader {
  width: 40px;
  height: 40px;
  border: 4px solid #f1f5f9;
  border-top: 4px solid #0f172a;
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
