<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ParticleBackground from './ParticleBackground.vue'
import { useSpeciesStore } from '@/stores/species'

const store = useSpeciesStore()
const visible = ref(false)

// 数字动画
const animatedTotal = ref(0)

function animateNumber(target: number, duration: number = 2000) {
  const start = 0
  const startTime = performance.now()
  
  function update(currentTime: number) {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    
    // 使用 easeOutExpo 缓动函数
    const easeProgress = 1 - Math.pow(2, -10 * progress)
    animatedTotal.value = Math.floor(start + (target - start) * easeProgress)
    
    if (progress < 1) {
      requestAnimationFrame(update)
    }
  }
  
  requestAnimationFrame(update)
}

function scrollToSection() {
  const section = document.getElementById('species-section')
  if (section) {
    section.scrollIntoView({ behavior: 'smooth' })
  }
}

onMounted(async () => {
  // 加载统计数据
  await store.fetchStats()
  
  // 延迟显示动画
  setTimeout(() => {
    visible.value = true
    // 数字动画
    setTimeout(() => {
      animateNumber(store.stats.total)
    }, 500)
  }, 100)
})
</script>

<template>
  <section class="hero">
    <ParticleBackground />
    
    <!-- 装饰元素 -->
    <div class="hero__decorations">
      <div class="decoration decoration--1"></div>
      <div class="decoration decoration--2"></div>
      <div class="decoration decoration--3"></div>
    </div>
    
    <div class="hero__content" :class="{ 'hero__content--visible': visible }">
      <!-- 顶部标签 -->
      <div class="hero__badge animate-fade-in-up stagger-1">
        <span class="badge-dot"></span>
        <span>实时数据更新</span>
      </div>
      
      <!-- 主标题 -->
      <div class="hero__title-group">
        <h1 class="hero__subtitle animate-fade-in-up stagger-1">
          重点水域指示物种
        </h1>
        <h2 class="hero__title animate-fade-in-up stagger-2">
          <span class="title-char" v-for="(char, i) in '溯源图谱'" :key="i" :style="{ '--char-delay': `${i * 0.1}s` }">
            <span class="text-gradient">{{ char }}</span>
          </span>
        </h2>
        <p class="hero__desc animate-fade-in-up stagger-3">
          探索水生态系统中五大类关键物种的科学数据库
        </p>
      </div>

      <!-- 统计数据 -->
      <div class="hero__stats animate-fade-in-up stagger-3">
        <div class="stat-item">
          <span class="stat-number">{{ animatedTotal }}+</span>
          <span class="stat-label">物种数据</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-number">5</span>
          <span class="stat-label">大类别</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-number">100%</span>
          <span class="stat-label">数据可溯源</span>
        </div>
      </div>

      <!-- CTA 按钮组 -->
      <div class="hero__cta-group animate-fade-in-up stagger-4">
        <button class="hero__cta hero__cta--primary" @click="scrollToSection">
          <span>探索图谱</span>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 5v14M19 12l-7 7-7-7"/>
          </svg>
        </button>
        <a href="#category-cards" class="hero__cta hero__cta--secondary">
          <span>了解更多</span>
        </a>
      </div>
    </div>

    <!-- 滚动提示 -->
    <div class="hero__scroll-hint animate-fade-in stagger-5">
      <div class="scroll-mouse">
        <div class="scroll-wheel"></div>
      </div>
      <span class="scroll-text">向下滚动</span>
    </div>

    <!-- 渐变遮罩 -->
    <div class="hero__gradient"></div>
  </section>
</template>

<style scoped>
.hero {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 装饰元素 */
.hero__decorations {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.decoration {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
}

.decoration--1 {
  top: -20%;
  right: -10%;
  width: 600px;
  height: 600px;
  background: var(--color-primary);
  animation: float 20s ease-in-out infinite;
}

.decoration--2 {
  bottom: -10%;
  left: -10%;
  width: 500px;
  height: 500px;
  background: var(--color-accent);
  animation: float 15s ease-in-out infinite reverse;
}

.decoration--3 {
  top: 30%;
  left: 20%;
  width: 300px;
  height: 300px;
  background: var(--color-success);
  animation: float 18s ease-in-out infinite;
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -30px) scale(1.05); }
  50% { transform: translate(-20px, 20px) scale(0.95); }
  75% { transform: translate(20px, 10px) scale(1.02); }
}

.hero__content {
  position: relative;
  z-index: 10;
  text-align: center;
  padding: 0 24px;
  opacity: 0;
  transition: opacity 0.5s ease;
}

.hero__content--visible {
  opacity: 1;
}

/* Badge */
.hero__badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--color-border);
  border-radius: 30px;
  margin-bottom: 32px;
  opacity: 0;
}

.badge-dot {
  width: 8px;
  height: 8px;
  background: var(--color-success);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

.hero__title-group {
  margin-bottom: 48px;
}

.hero__subtitle {
  font-family: var(--font-display);
  font-size: clamp(16px, 2.5vw, 22px);
  font-weight: 500;
  color: var(--color-text-secondary);
  letter-spacing: 0.15em;
  text-transform: uppercase;
  margin-bottom: 16px;
  opacity: 0;
}

.hero__title {
  font-family: var(--font-display);
  font-size: clamp(52px, 14vw, 140px);
  font-weight: 700;
  letter-spacing: -0.04em;
  line-height: 1;
  margin: 0 0 24px;
  opacity: 0;
}

.title-char {
  display: inline-block;
  animation: charReveal 0.8s ease-out forwards;
  animation-delay: var(--char-delay);
  opacity: 0;
  transform: translateY(40px) rotateX(-90deg);
}

@keyframes charReveal {
  to {
    opacity: 1;
    transform: translateY(0) rotateX(0);
  }
}

.hero__desc {
  font-size: clamp(16px, 2vw, 20px);
  color: var(--color-text-muted);
  max-width: 500px;
  margin: 0 auto;
  opacity: 0;
}

.hero__stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  margin-bottom: 48px;
  opacity: 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-number {
  font-family: var(--font-display);
  font-size: clamp(28px, 5vw, 40px);
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-muted);
  letter-spacing: 0.02em;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: var(--color-border);
}

/* CTA Group */
.hero__cta-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  opacity: 0;
}

.hero__cta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 16px 32px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 30px;
  cursor: pointer;
  transition: all var(--transition-base);
  text-decoration: none;
}

.hero__cta--primary {
  color: #000;
  background: var(--color-text);
}

.hero__cta--primary:hover {
  transform: scale(1.05);
  box-shadow: 0 10px 40px rgba(255, 255, 255, 0.25);
}

.hero__cta--primary svg {
  transition: transform var(--transition-base);
}

.hero__cta--primary:hover svg {
  transform: translateY(4px);
}

.hero__cta--secondary {
  color: var(--color-text);
  background: transparent;
  border: 1px solid var(--color-border);
}

.hero__cta--secondary:hover {
  border-color: var(--color-text);
  background: rgba(255, 255, 255, 0.05);
}

.hero__scroll-hint {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  opacity: 0;
}

.scroll-mouse {
  width: 24px;
  height: 40px;
  border: 2px solid var(--color-text-muted);
  border-radius: 12px;
  display: flex;
  justify-content: center;
  padding-top: 8px;
}

.scroll-wheel {
  width: 4px;
  height: 8px;
  background: var(--color-text-muted);
  border-radius: 2px;
  animation: scrollWheel 2s ease-in-out infinite;
}

@keyframes scrollWheel {
  0%, 100% {
    opacity: 1;
    transform: translateY(0);
  }
  50% {
    opacity: 0.3;
    transform: translateY(8px);
  }
}

.scroll-text {
  font-size: 11px;
  color: var(--color-text-muted);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.hero__gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(to top, var(--color-bg) 0%, transparent 100%);
  pointer-events: none;
  z-index: 5;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero__stats {
    flex-direction: column;
    gap: 24px;
  }
  
  .stat-divider {
    width: 60px;
    height: 1px;
  }

  .hero__cta-group {
    flex-direction: column;
    width: 100%;
    max-width: 300px;
  }

  .hero__cta {
    width: 100%;
    justify-content: center;
  }
}
</style>
