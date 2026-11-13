<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useSpeciesStore } from '@/stores/species'

const store = useSpeciesStore()
const visible = ref(false)
const sectionRef = ref<HTMLElement>()

// 动画数字
const animatedValues = ref({
  total: 0,
  categories: 0,
  completeness: 0
})

function easeOutExpo(t: number): number {
  return t === 1 ? 1 : 1 - Math.pow(2, -10 * t)
}

function animateNumber(
  key: keyof typeof animatedValues.value,
  target: number,
  duration: number = 2000
) {
  const start = 0
  const startTime = performance.now()

  function update(currentTime: number) {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeProgress = easeOutExpo(progress)

    animatedValues.value[key] = Math.floor(start + (target - start) * easeProgress)

    if (progress < 1) {
      requestAnimationFrame(update)
    }
  }

  requestAnimationFrame(update)
}

function startAnimations() {
  animateNumber('total', store.stats.total, 2500)
  setTimeout(() => animateNumber('categories', 5, 1500), 200)
  setTimeout(() => animateNumber('completeness', 100, 2000), 400)
}

onMounted(() => {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting && !visible.value) {
          visible.value = true
          startAnimations()
        }
      })
    },
    { threshold: 0.3 }
  )

  if (sectionRef.value) {
    observer.observe(sectionRef.value)
  }
})

// 当统计数据更新时重新动画
watch(() => store.stats.total, (newVal) => {
  if (visible.value && newVal > 0) {
    animateNumber('total', newVal, 1500)
  }
})
</script>

<template>
  <section ref="sectionRef" class="stats-section">
    <div class="stats-bg">
      <div class="stats-glow stats-glow-1"></div>
      <div class="stats-glow stats-glow-2"></div>
    </div>

    <div class="container">
      <div class="stats-content" :class="{ visible }">
        <!-- 标题 -->
        <div class="stats-header">
          <h2 class="stats-title">
            数据一览
            <span class="title-sub">Data Overview</span>
          </h2>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-grid">
          <div class="stat-card stat-card-main">
            <div class="stat-icon">📊</div>
            <div class="stat-number">
              <span class="number-value">{{ animatedValues.total }}</span>
              <span class="number-suffix">+</span>
            </div>
            <div class="stat-label">物种总数</div>
            <div class="stat-desc">涵盖五大类水生物种的完整数据库</div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">🔬</div>
            <div class="stat-number">
              <span class="number-value">{{ animatedValues.categories }}</span>
            </div>
            <div class="stat-label">物种类别</div>
            <div class="stat-desc">底栖动物、水生植物、浮游植物、鱼类、浮游动物</div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">✅</div>
            <div class="stat-number">
              <span class="number-value">{{ animatedValues.completeness }}</span>
              <span class="number-suffix">%</span>
            </div>
            <div class="stat-label">数据可溯源</div>
            <div class="stat-desc">每条记录均可追溯至原始数据来源</div>
          </div>
        </div>

        <!-- 各类别统计 -->
        <div class="category-stats">
          <div
            v-for="(count, type) in store.stats.byType"
            :key="type"
            class="category-stat"
          >
            <span class="cat-name">{{ type }}</span>
            <span class="cat-bar">
              <span
                class="cat-fill"
                :style="{
                  width: `${(count / store.stats.total) * 100}%`
                }"
              ></span>
            </span>
            <span class="cat-count">{{ count }}</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.stats-section {
  position: relative;
  padding: 120px 0 140px;
  background: var(--color-bg);
  overflow: hidden;
}

.stats-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.stats-glow {
  position: absolute;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.15;
}

.stats-glow-1 {
  top: -200px;
  left: -200px;
  background: var(--color-primary);
}

.stats-glow-2 {
  bottom: -200px;
  right: -200px;
  background: var(--color-accent);
}

.stats-content {
  position: relative;
  z-index: 1;
  opacity: 0;
  transform: translateY(60px);
  transition: all 0.8s ease-out;
}

.stats-content.visible {
  opacity: 1;
  transform: translateY(0);
}

.stats-header {
  text-align: center;
  margin-bottom: 64px;
}

.stats-title {
  font-family: var(--font-display);
  font-size: clamp(36px, 6vw, 56px);
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.03em;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.title-sub {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-muted);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.stats-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 24px;
  margin-bottom: 64px;
}

.stat-card {
  position: relative;
  padding: 40px 32px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 24px;
  text-align: center;
  transition: all var(--transition-base);
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--gradient-primary);
  opacity: 0;
  transition: opacity var(--transition-base);
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: var(--color-primary);
  box-shadow: 0 20px 40px rgba(41, 151, 255, 0.15);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card-main {
  text-align: left;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-card-main .stat-number {
  justify-content: flex-start;
}

.stat-icon {
  font-size: 40px;
  margin-bottom: 20px;
}

.stat-number {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  margin-bottom: 12px;
}

.number-value {
  font-family: var(--font-display);
  font-size: clamp(48px, 8vw, 72px);
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.03em;
  line-height: 1;
}

.number-suffix {
  font-family: var(--font-display);
  font-size: clamp(24px, 4vw, 36px);
  font-weight: 700;
  color: var(--color-primary);
}

.stat-label {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.stat-desc {
  font-size: 14px;
  color: var(--color-text-muted);
  line-height: 1.6;
}

/* 类别统计条 */
.category-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 32px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 20px;
}

.category-stat {
  display: grid;
  grid-template-columns: 100px 1fr 60px;
  align-items: center;
  gap: 16px;
}

.cat-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.cat-bar {
  height: 8px;
  background: var(--color-bg-tertiary);
  border-radius: 4px;
  overflow: hidden;
}

.cat-fill {
  display: block;
  height: 100%;
  background: var(--gradient-primary);
  border-radius: 4px;
  transition: width 1.5s ease-out;
}

.cat-count {
  font-family: var(--font-mono);
  font-size: 14px;
  color: var(--color-text-secondary);
  text-align: right;
}

/* 响应式 */
@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-card-main {
    text-align: center;
  }

  .stat-card-main .stat-number {
    justify-content: center;
  }
}

@media (max-width: 600px) {
  .category-stat {
    grid-template-columns: 80px 1fr 50px;
    gap: 12px;
  }

  .cat-name {
    font-size: 12px;
  }
}
</style>
