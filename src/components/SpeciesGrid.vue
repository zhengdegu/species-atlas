<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useSpeciesStore } from '@/stores/species'
import { speciesTypes } from '@/types/species'
import SpeciesCard from './SpeciesCard.vue'
import SpeciesDetail from './SpeciesDetail.vue'

const store = useSpeciesStore()
const gridRef = ref<HTMLElement>()
const visible = ref(false)

// 获取当前类型配置
const currentTypeConfig = () => {
  return speciesTypes.find(t => t.name === store.currentType) || speciesTypes[0]
}

// 处理卡片点击
function handleCardClick(species: any) {
  store.fetchSpeciesDetail(species.serialNumber || species.id)
}

// 无限滚动加载
function handleScroll() {
  if (!gridRef.value) return
  
  const { scrollTop, scrollHeight, clientHeight } = document.documentElement
  
  if (scrollTop + clientHeight >= scrollHeight - 500) {
    store.loadMore()
  }
}

onMounted(async () => {
  // 加载数据
  await store.fetchSpeciesList(true)
  
  // 显示动画
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          visible.value = true
          observer.disconnect()
        }
      })
    },
    { threshold: 0.1 }
  )
  
  if (gridRef.value) {
    observer.observe(gridRef.value)
  }
  
  // 监听滚动
  window.addEventListener('scroll', handleScroll)
})

// 监听类型变化
watch(() => store.currentType, () => {
  visible.value = false
  setTimeout(() => {
    visible.value = true
  }, 100)
})
</script>

<template>
  <section id="species-section" class="species-section">
    <div class="container">
      <!-- 区域标题 -->
      <div class="section-header" :class="{ visible }">
        <div class="header-main">
          <span 
            class="type-badge"
            :style="{ background: currentTypeConfig().gradient }"
          >
            {{ currentTypeConfig().icon }} {{ store.currentType }}
          </span>
          <h2 class="section-title">
            物种图谱
            <span class="title-highlight">Gallery</span>
          </h2>
        </div>
        <div class="header-stats">
          <span class="stats-count">{{ store.pagination.total }}</span>
          <span class="stats-label">条记录</span>
        </div>
      </div>

      <!-- 搜索提示 -->
      <div v-if="store.searchKeyword" class="search-info">
        <span>搜索 "{{ store.searchKeyword }}" 的结果</span>
        <button class="clear-search" @click="store.search('')">
          清除搜索
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6 6 18M6 6l12 12"/>
          </svg>
        </button>
      </div>

      <!-- 物种网格 -->
      <div 
        id="species-grid"
        ref="gridRef" 
        class="species-grid"
        :class="{ visible }"
      >
        <SpeciesCard
          v-for="(species, index) in store.speciesList"
          :key="species.serialNumber || species.id || index"
          :species="species"
          :index="index"
          @click="handleCardClick"
        />
      </div>

      <!-- 加载状态 -->
      <div v-if="store.loading" class="loading-wrap">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <!-- 空状态 -->
      <div 
        v-else-if="store.speciesList.length === 0" 
        class="empty-state"
      >
        <div class="empty-icon">🔍</div>
        <h3>暂无数据</h3>
        <p>当前类别下没有物种数据</p>
      </div>

      <!-- 加载更多提示 -->
      <div 
        v-else-if="store.speciesList.length < store.pagination.total"
        class="load-more-hint"
      >
        <span>继续滚动加载更多</span>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <SpeciesDetail />
  </section>
</template>

<style scoped>
.species-section {
  padding: 80px 0 120px;
  background: var(--color-bg-secondary);
  min-height: 100vh;
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 48px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.6s ease-out;
}

.section-header.visible {
  opacity: 1;
  transform: translateY(0);
}

.header-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.type-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  border-radius: 20px;
  width: fit-content;
}

.section-title {
  font-family: var(--font-display);
  font-size: clamp(36px, 6vw, 56px);
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.03em;
  line-height: 1.1;
}

.title-highlight {
  display: block;
  font-weight: 300;
  color: var(--color-text-muted);
  font-size: 0.5em;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-top: 8px;
}

.header-stats {
  text-align: right;
}

.stats-count {
  display: block;
  font-family: var(--font-display);
  font-size: 48px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.02em;
  line-height: 1;
}

.stats-label {
  font-size: 14px;
  color: var(--color-text-muted);
}

.search-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  padding: 16px 24px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.clear-search {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  font-size: 13px;
  color: var(--color-text-muted);
  background: var(--color-bg-tertiary);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.clear-search:hover {
  color: var(--color-text);
  border-color: var(--color-text-muted);
}

.species-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  opacity: 0;
  transition: opacity 0.5s ease-out;
}

.species-grid.visible {
  opacity: 1;
}

.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 64px 0;
  color: var(--color-text-muted);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 24px;
}

.empty-state h3 {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.empty-state p {
  color: var(--color-text-muted);
}

.load-more-hint {
  text-align: center;
  padding: 48px 0;
  color: var(--color-text-muted);
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 24px;
  }
  
  .header-stats {
    text-align: left;
  }
  
  .species-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 16px;
  }
}
</style>
