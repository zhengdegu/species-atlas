<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { speciesTypes, type SpeciesType } from '@/types/species'
import { useSpeciesStore } from '@/stores/species'

const store = useSpeciesStore()
const visible = ref(false)

function selectCategory(type: SpeciesType) {
  store.setType(type)
  // 滚动到网格区域
  const grid = document.getElementById('species-grid')
  if (grid) {
    setTimeout(() => {
      grid.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }, 100)
  }
}

onMounted(() => {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          visible.value = true
          observer.disconnect()
        }
      })
    },
    { threshold: 0.2 }
  )
  
  const section = document.getElementById('category-cards')
  if (section) {
    observer.observe(section)
  }
})
</script>

<template>
  <section id="category-cards" class="category-section">
    <div class="container">
      <div class="section-header" :class="{ visible }">
        <h2 class="section-title">五大物种类别</h2>
        <p class="section-desc">探索水域生态系统中的关键物种群落</p>
      </div>

      <div class="cards-grid" :class="{ visible }">
        <div
          v-for="(type, index) in speciesTypes"
          :key="type.name"
          class="category-card"
          :class="{ active: store.currentType === type.name }"
          :style="{ 
            '--card-color': type.color,
            '--card-gradient': type.gradient,
            '--delay': `${index * 0.1}s`
          }"
          @click="selectCategory(type.name)"
        >
          <div class="card-glow"></div>
          <div class="card-content">
            <div class="card-icon">{{ type.icon }}</div>
            <div class="card-info">
              <h3 class="card-title">{{ type.name }}</h3>
              <p class="card-subtitle">{{ type.nameEn }}</p>
            </div>
            <div class="card-count">
              <span class="count-number">{{ store.stats.byType[type.name] || 0 }}</span>
              <span class="count-label">物种</span>
            </div>
            <p class="card-desc">{{ type.description }}</p>
            <div class="card-arrow">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.category-section {
  padding: 120px 0;
  background: var(--color-bg);
}

.section-header {
  text-align: center;
  margin-bottom: 64px;
  opacity: 0;
  transform: translateY(40px);
  transition: all 0.8s ease-out;
}

.section-header.visible {
  opacity: 1;
  transform: translateY(0);
}

.section-title {
  font-family: var(--font-display);
  font-size: clamp(32px, 5vw, 48px);
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 16px;
  letter-spacing: -0.02em;
}

.section-desc {
  font-size: 18px;
  color: var(--color-text-secondary);
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  opacity: 0;
  transform: translateY(40px);
  transition: all 0.8s ease-out 0.2s;
}

.cards-grid.visible {
  opacity: 1;
  transform: translateY(0);
}

.category-card {
  position: relative;
  padding: 32px 24px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 24px;
  cursor: pointer;
  overflow: hidden;
  transition: all var(--transition-base);
  animation: cardEnter 0.6s ease-out backwards;
  animation-delay: var(--delay);
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.category-card:hover {
  transform: translateY(-8px);
  border-color: var(--card-color);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.category-card.active {
  border-color: var(--card-color);
  background: var(--color-bg-card-hover);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100px;
  background: var(--card-gradient);
  opacity: 0;
  transition: opacity var(--transition-base);
  filter: blur(40px);
}

.category-card:hover .card-glow,
.category-card.active .card-glow {
  opacity: 0.15;
}

.card-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.card-icon {
  font-size: 48px;
  margin-bottom: 20px;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.3));
}

.card-info {
  margin-bottom: 16px;
}

.card-title {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 4px;
}

.card-subtitle {
  font-size: 12px;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.card-count {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 12px;
}

.count-number {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  color: var(--card-color);
  letter-spacing: -0.02em;
}

.count-label {
  font-size: 13px;
  color: var(--color-text-muted);
}

.card-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  flex: 1;
}

.card-arrow {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  color: var(--color-text-muted);
  opacity: 0;
  transform: translateX(-10px);
  transition: all var(--transition-base);
}

.category-card:hover .card-arrow {
  opacity: 1;
  transform: translateX(0);
  color: var(--card-color);
}

/* 响应式 */
@media (max-width: 1200px) {
  .cards-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .cards-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .cards-grid {
    grid-template-columns: 1fr;
  }
}
</style>
