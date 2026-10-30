<script setup lang="ts">
import { speciesTypes } from '@/types/species'
import { useSpeciesStore } from '@/stores/species'

const store = useSpeciesStore()

function scrollToSection(type?: string) {
  if (type) {
    store.setType(type as any)
  }
  const section = document.getElementById('species-section')
  if (section) {
    section.scrollIntoView({ behavior: 'smooth' })
  }
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <footer class="footer">
    <div class="footer-glow"></div>
    
    <div class="container">
      <div class="footer-content">
        <!-- Logo 和简介 -->
        <div class="footer-brand">
          <div class="brand-logo" @click="scrollToTop">
            <span class="logo-icon">🌊</span>
            <span class="logo-text">物种图谱</span>
          </div>
          <p class="brand-desc">
            水生态物种溯源图谱是一个全面的水生物种数据库，
            收录了五大类水生物种的详细信息，包括形态特征、
            DNA序列和分类学信息。
          </p>
          <div class="brand-stats">
            <span>{{ store.stats.total }}+ 物种</span>
            <span>·</span>
            <span>5 大类别</span>
            <span>·</span>
            <span>100% 可溯源</span>
          </div>
        </div>

        <!-- 快速链接 -->
        <div class="footer-links">
          <h4 class="links-title">物种类别</h4>
          <ul class="links-list">
            <li v-for="type in speciesTypes" :key="type.name">
              <button @click="scrollToSection(type.name)">
                <span class="link-icon">{{ type.icon }}</span>
                {{ type.name }}
              </button>
            </li>
          </ul>
        </div>

        <!-- 关于 -->
        <div class="footer-about">
          <h4 class="links-title">关于项目</h4>
          <ul class="links-list">
            <li><button>数据来源</button></li>
            <li><button>技术架构</button></li>
            <li><button>API 文档</button></li>
            <li><button>联系我们</button></li>
          </ul>
        </div>

        <!-- 技术栈 -->
        <div class="footer-tech">
          <h4 class="links-title">技术支持</h4>
          <div class="tech-badges">
            <span class="tech-badge">Vue 3</span>
            <span class="tech-badge">TypeScript</span>
            <span class="tech-badge">Three.js</span>
            <span class="tech-badge">Tailwind CSS</span>
          </div>
        </div>
      </div>

      <!-- 底部版权 -->
      <div class="footer-bottom">
        <div class="copyright">
          <span>© 2024 水生态物种溯源图谱</span>
          <span class="separator">·</span>
          <span>保留所有权利</span>
        </div>
        <button class="back-to-top" @click="scrollToTop">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 19V5M5 12l7-7 7 7"/>
          </svg>
          回到顶部
        </button>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  position: relative;
  padding: 80px 0 40px;
  background: var(--color-bg);
  border-top: 1px solid var(--color-border);
  overflow: hidden;
}

.footer-glow {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 800px;
  height: 400px;
  background: radial-gradient(ellipse, rgba(41, 151, 255, 0.08) 0%, transparent 70%);
  pointer-events: none;
}

.footer-content {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 48px;
  margin-bottom: 64px;
}

/* Brand */
.footer-brand {
  max-width: 320px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: opacity var(--transition-fast);
}

.brand-logo:hover {
  opacity: 0.8;
}

.logo-icon {
  font-size: 32px;
}

.logo-text {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.02em;
}

.brand-desc {
  font-size: 14px;
  line-height: 1.7;
  color: var(--color-text-secondary);
  margin-bottom: 20px;
}

.brand-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-muted);
}

/* Links */
.links-title {
  font-family: var(--font-display);
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 20px;
  letter-spacing: 0.02em;
}

.links-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.links-list button {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--color-text-secondary);
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  transition: color var(--transition-fast);
}

.links-list button:hover {
  color: var(--color-text);
}

.link-icon {
  font-size: 16px;
}

/* Tech badges */
.tech-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tech-badge {
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-muted);
  background: var(--color-bg-tertiary);
  border: 1px solid var(--color-border);
  border-radius: 6px;
}

/* Bottom */
.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 32px;
  border-top: 1px solid var(--color-border);
}

.copyright {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-muted);
}

.separator {
  opacity: 0.5;
}

.back-to-top {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.back-to-top:hover {
  color: var(--color-text);
  background: var(--color-bg-card-hover);
  border-color: var(--color-primary);
}

/* 响应式 */
@media (max-width: 1024px) {
  .footer-content {
    grid-template-columns: 1fr 1fr;
  }

  .footer-brand {
    grid-column: span 2;
    max-width: 100%;
  }
}

@media (max-width: 600px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .footer-brand {
    grid-column: span 1;
  }

  .footer-bottom {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .copyright {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
