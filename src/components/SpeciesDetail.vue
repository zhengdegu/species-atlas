<script setup lang="ts">
import { computed, watch, ref } from 'vue'
import { useSpeciesStore } from '@/stores/species'

const store = useSpeciesStore()
const dnaVisible = ref(false)

const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

const images = computed(() => {
  if (!store.selectedSpecies?.photoPaths) return []
  return store.selectedSpecies.photoPaths.map(path => {
    if (path.startsWith('http')) return path
    return `${baseUrl}${path}`
  })
})

// DNA 序列格式化（每 60 个字符换行）
const formattedDna = computed(() => {
  const dna = store.selectedSpecies?.dnaCodingProperty
  if (!dna) return ''
  return dna.match(/.{1,60}/g)?.join('\n') || dna
})

// 监听详情打开，触发 DNA 动画
watch(() => store.detailVisible, (visible) => {
  if (visible) {
    dnaVisible.value = false
    setTimeout(() => {
      dnaVisible.value = true
    }, 800)
  }
})

function close() {
  store.closeDetail()
}

function handleBackdropClick(e: MouseEvent) {
  if ((e.target as HTMLElement).classList.contains('detail-overlay')) {
    close()
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div 
        v-if="store.detailVisible && store.selectedSpecies" 
        class="detail-overlay"
        @click="handleBackdropClick"
      >
        <div class="detail-modal">
          <!-- 关闭按钮 -->
          <button class="close-btn" @click="close">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6 6 18M6 6l12 12"/>
            </svg>
          </button>

          <div class="detail-content">
            <!-- 图片区域 -->
            <div class="detail-images" v-if="images.length > 0">
              <div class="main-image">
                <img :src="images[0]" :alt="store.selectedSpecies.chineseName" />
              </div>
              <div v-if="images.length > 1" class="thumb-list">
                <div 
                  v-for="(img, idx) in images.slice(0, 4)" 
                  :key="idx"
                  class="thumb-item"
                >
                  <img :src="img" :alt="`图片 ${idx + 1}`" />
                </div>
              </div>
            </div>

            <!-- 信息区域 -->
            <div class="detail-info">
              <!-- 标题 -->
              <div class="info-header">
                <h2 class="species-name">{{ store.selectedSpecies.chineseName }}</h2>
                <p class="species-latin" v-if="store.selectedSpecies.latinName">{{ store.selectedSpecies.latinName }}</p>
                <p class="species-code">{{ store.selectedSpecies.speciesCode }}</p>
              </div>

              <!-- 基本信息卡片 -->
              <div class="info-cards">
                <div class="info-card" v-if="store.selectedSpecies.taxonomicStatus">
                  <div class="card-label">分类学地位</div>
                  <div class="card-value">{{ store.selectedSpecies.taxonomicStatus }}</div>
                </div>
                <div class="info-card" v-if="store.selectedSpecies.geneName">
                  <div class="card-label">基因名称</div>
                  <div class="card-value">{{ store.selectedSpecies.geneName }}</div>
                </div>
                <div class="info-card" v-if="store.selectedSpecies.dnaSequenceCode">
                  <div class="card-label">DNA序列编号</div>
                  <div class="card-value mono">{{ store.selectedSpecies.dnaSequenceCode }}</div>
                </div>
                <div class="info-card" v-if="store.selectedSpecies.reference">
                  <div class="card-label">数据来源</div>
                  <div class="card-value">{{ store.selectedSpecies.reference }}</div>
                </div>
              </div>

              <!-- 形态学描述 -->
              <div class="info-section" v-if="store.selectedSpecies.morphologicalDescription">
                <h3 class="section-title">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                  形态学描述
                </h3>
                <p class="section-content">{{ store.selectedSpecies.morphologicalDescription }}</p>
              </div>

              <!-- 地理分布 -->
              <div class="info-section" v-if="store.selectedSpecies.geographicalDistribution">
                <h3 class="section-title">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="2" y1="12" x2="22" y2="12"/>
                    <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                  </svg>
                  地理分布
                </h3>
                <p class="section-content">{{ store.selectedSpecies.geographicalDistribution }}</p>
              </div>

              <!-- DNA 序列 -->
              <div class="info-section dna-section" v-if="store.selectedSpecies.dnaCodingProperty">
                <h3 class="section-title">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 2v1m0 18v1M4.93 4.93l.7.7m12.74 12.74.7.7M2 12h1m18 0h1M4.93 19.07l.7-.7m12.74-12.74.7-.7"/>
                    <circle cx="12" cy="12" r="4"/>
                  </svg>
                  DNA 序列
                </h3>
                <div class="dna-container">
                  <pre class="dna-code" :class="{ visible: dnaVisible }">{{ formattedDna }}</pre>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.detail-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(10px);
  overflow-y: auto;
}

.detail-modal {
  position: relative;
  width: 100%;
  max-width: 1000px;
  max-height: 90vh;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 24px;
  overflow: hidden;
  overflow-y: auto;
}

.close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  border: 1px solid var(--color-border);
  border-radius: 50%;
  color: var(--color-text);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: rotate(90deg);
}

.detail-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 500px;
}

/* 图片区域 */
.detail-images {
  background: var(--color-bg-tertiary);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.main-image {
  flex: 1;
  border-radius: 16px;
  overflow: hidden;
  background: var(--color-bg);
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-list {
  display: flex;
  gap: 12px;
}

.thumb-item {
  width: 72px;
  height: 72px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  transition: border-color var(--transition-fast);
}

.thumb-item:hover {
  border-color: var(--color-primary);
}

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 信息区域 */
.detail-info {
  padding: 40px 32px;
  overflow-y: auto;
}

.info-header {
  margin-bottom: 32px;
}

.species-name {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.02em;
  margin-bottom: 8px;
}

.species-latin {
  font-family: var(--font-text);
  font-size: 16px;
  font-style: italic;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.species-code {
  font-family: var(--font-mono);
  font-size: 14px;
  color: var(--color-primary);
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 32px;
}

.info-card {
  padding: 16px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
}

.card-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 6px;
}

.card-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  word-break: break-all;
}

.card-value.mono {
  font-family: var(--font-mono);
  font-size: 12px;
}

.info-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-display);
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);
}

.section-content {
  font-size: 14px;
  line-height: 1.8;
  color: var(--color-text-secondary);
}

/* DNA 序列 */
.dna-container {
  background: #0a0a0a;
  border-radius: 12px;
  padding: 20px;
  overflow-x: auto;
}

.dna-code {
  font-family: var(--font-mono);
  font-size: 12px;
  line-height: 1.6;
  color: #4ade80;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  opacity: 0;
  transition: opacity 0.5s ease;
}

.dna-code.visible {
  opacity: 1;
  animation: typewriter 2s steps(60) forwards;
}

@keyframes typewriter {
  from { max-height: 0; }
  to { max-height: 500px; }
}

/* 动画 */
.modal-enter-active {
  animation: modalIn 0.4s ease-out;
}

.modal-leave-active {
  animation: modalIn 0.3s ease-in reverse;
}

@keyframes modalIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-enter-active .detail-modal {
  animation: modalContentIn 0.4s ease-out;
}

.modal-leave-active .detail-modal {
  animation: modalContentIn 0.3s ease-in reverse;
}

@keyframes modalContentIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .detail-content {
    grid-template-columns: 1fr;
  }
  
  .detail-images {
    max-height: 300px;
  }
  
  .info-cards {
    grid-template-columns: 1fr;
  }
  
  .species-name {
    font-size: 24px;
  }
}
</style>
