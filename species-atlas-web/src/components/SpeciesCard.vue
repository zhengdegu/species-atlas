<script setup lang="ts">
import { computed, ref } from 'vue'
import type { Species } from '@/types/species'

const props = defineProps<{
  species: Species
  index: number
}>()

const emit = defineEmits<{
  click: [species: Species]
}>()

const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

// 图片加载状态
const imageLoading = ref(true)
const imageError = ref(false)

const imageUrl = computed(() => {
  if (props.species.photoPaths && props.species.photoPaths.length > 0) {
    const path = props.species.photoPaths[0]
    if (path.startsWith('http')) return path
    // 图片通过后端 API 提供，使用 /api/uploads/ 前缀
    const cleanPath = path.startsWith('/') ? path.slice(1) : path
    return `${baseUrl}/uploads/${cleanPath}`
  }
  return null
})

const delay = computed(() => `${(props.index % 10) * 0.05}s`)

// 图片加载完成
function onImageLoad() {
  imageLoading.value = false
}

// 图片加载失败
function onImageError() {
  imageLoading.value = false
  imageError.value = true
}
</script>

<template>
  <div 
    class="species-card"
    :style="{ '--delay': delay }"
    @click="emit('click', species)"
  >
    <div class="card-image">
      <!-- 骨架屏/加载状态 -->
      <div v-if="imageUrl && imageLoading" class="image-skeleton">
        <div class="skeleton-shimmer"></div>
      </div>
      <!-- 实际图片 -->
      <img 
        v-if="imageUrl && !imageError" 
        :src="imageUrl" 
        :alt="species.chineseName"
        loading="lazy"
        decoding="async"
        :class="{ 'image-loaded': !imageLoading }"
        @load="onImageLoad"
        @error="onImageError"
      />
      <!-- 占位符（无图片或加载失败） -->
      <div v-if="!imageUrl || imageError" class="image-placeholder">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
          <circle cx="8.5" cy="8.5" r="1.5"/>
          <polyline points="21 15 16 10 5 21"/>
        </svg>
      </div>
      <div class="card-overlay">
        <span class="view-detail">
          查看详情
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M5 12h14M12 5l7 7-7 7"/>
          </svg>
        </span>
      </div>
    </div>
    
    <div class="card-content">
      <h3 class="card-title">{{ species.chineseName || '未命名物种' }}</h3>
      <p class="card-code">{{ species.speciesCode }}</p>
      <p class="card-taxonomy" v-if="species.taxonomicStatus">
        {{ species.taxonomicStatus }}
      </p>
    </div>
  </div>
</template>

<style scoped>
.species-card {
  position: relative;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
  animation: cardFadeIn 0.5s ease-out backwards;
  animation-delay: var(--delay);
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.species-card:hover {
  transform: translateY(-6px) scale(1.02);
  border-color: var(--color-primary);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.3),
    0 0 0 1px var(--color-primary);
}

.card-image {
  position: relative;
  width: 100%;
  aspect-ratio: 4/3;
  overflow: hidden;
  background: var(--color-bg-tertiary);
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow), opacity 0.3s ease;
  opacity: 0;
}

.card-image img.image-loaded {
  opacity: 1;
}

.species-card:hover .card-image img {
  transform: scale(1.1);
}

/* 骨架屏加载动画 */
.image-skeleton {
  position: absolute;
  inset: 0;
  background: var(--color-bg-tertiary);
  overflow: hidden;
}

.skeleton-shimmer {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.05) 50%,
    transparent 100%
  );
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.8) 0%,
    rgba(0, 0, 0, 0) 50%
  );
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 16px;
  opacity: 0;
  transition: opacity var(--transition-base);
}

.species-card:hover .card-overlay {
  opacity: 1;
}

.view-detail {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  transform: translateY(10px);
  transition: transform var(--transition-base);
}

.species-card:hover .view-detail {
  transform: translateY(0);
}

.card-content {
  padding: 16px 20px 20px;
}

.card-title {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 6px;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-code {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-primary);
  margin-bottom: 8px;
}

.card-taxonomy {
  font-size: 12px;
  color: var(--color-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
