<script setup lang="ts">
import { ref, onMounted } from 'vue'

const emit = defineEmits<{
  complete: []
}>()

const progress = ref(0)
const visible = ref(true)
const fadeOut = ref(false)

onMounted(() => {
  // 模拟加载进度
  const interval = setInterval(() => {
    progress.value += Math.random() * 15 + 5
    
    if (progress.value >= 100) {
      progress.value = 100
      clearInterval(interval)
      
      // 延迟后开始淡出
      setTimeout(() => {
        fadeOut.value = true
        
        // 动画完成后触发事件
        setTimeout(() => {
          visible.value = false
          emit('complete')
        }, 600)
      }, 300)
    }
  }, 100)
})
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="loading-screen" :class="{ 'fade-out': fadeOut }">
        <div class="loading-content">
          <!-- Logo 动画 -->
          <div class="loading-logo">
            <div class="logo-waves">
              <span class="wave wave-1"></span>
              <span class="wave wave-2"></span>
              <span class="wave wave-3"></span>
            </div>
            <span class="logo-icon">🌊</span>
          </div>

          <!-- 标题 -->
          <h1 class="loading-title">物种图谱</h1>
          <p class="loading-subtitle">Species Atlas</p>

          <!-- 进度条 -->
          <div class="loading-progress">
            <div class="progress-bar">
              <div 
                class="progress-fill" 
                :style="{ width: `${progress}%` }"
              ></div>
            </div>
            <span class="progress-text">{{ Math.floor(progress) }}%</span>
          </div>
        </div>

        <!-- 背景粒子效果 -->
        <div class="loading-bg">
          <div v-for="i in 20" :key="i" class="bg-particle" :style="{
            '--x': `${Math.random() * 100}%`,
            '--y': `${Math.random() * 100}%`,
            '--delay': `${Math.random() * 5}s`,
            '--duration': `${3 + Math.random() * 4}s`
          }"></div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.loading-screen {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.loading-screen.fade-out {
  opacity: 0;
  transform: scale(1.05);
}

.loading-content {
  position: relative;
  z-index: 10;
  text-align: center;
}

/* Logo */
.loading-logo {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-waves {
  position: absolute;
  inset: 0;
}

.wave {
  position: absolute;
  inset: 0;
  border: 2px solid var(--color-primary);
  border-radius: 50%;
  opacity: 0;
  animation: waveExpand 2s ease-out infinite;
}

.wave-1 { animation-delay: 0s; }
.wave-2 { animation-delay: 0.5s; }
.wave-3 { animation-delay: 1s; }

@keyframes waveExpand {
  0% {
    transform: scale(0.5);
    opacity: 0.8;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

.logo-icon {
  font-size: 64px;
  animation: logoFloat 3s ease-in-out infinite;
}

@keyframes logoFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* Title */
.loading-title {
  font-family: var(--font-display);
  font-size: 48px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.03em;
  margin-bottom: 8px;
}

.loading-subtitle {
  font-size: 14px;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.2em;
  margin-bottom: 48px;
}

/* Progress */
.loading-progress {
  width: 240px;
  margin: 0 auto;
}

.progress-bar {
  height: 4px;
  background: var(--color-bg-tertiary);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: var(--gradient-primary);
  border-radius: 2px;
  transition: width 0.1s ease-out;
}

.progress-text {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-text-muted);
}

/* Background particles */
.loading-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.bg-particle {
  position: absolute;
  left: var(--x);
  top: var(--y);
  width: 4px;
  height: 4px;
  background: var(--color-primary);
  border-radius: 50%;
  opacity: 0.3;
  animation: particleFloat var(--duration) ease-in-out infinite;
  animation-delay: var(--delay);
}

@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-100px) scale(0.5);
    opacity: 0.1;
  }
}

/* Fade transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
