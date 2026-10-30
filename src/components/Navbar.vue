<script setup lang="ts">
import { ref, onMounted, onUnmounted, inject } from 'vue'
import { speciesTypes, type SpeciesType } from '@/types/species'
import { useSpeciesStore } from '@/stores/species'

const emit = defineEmits<{
  openSearch: []
}>()

const store = useSpeciesStore()
const isScrolled = ref(false)
const mobileMenuOpen = ref(false)
const showSettings = ref(false)

// 注入清除 Token 的方法
const clearToken = inject<() => void>('clearToken')
const showTokenInput = inject<() => void>('showTokenInput')

function handleScroll() {
  isScrolled.value = window.scrollY > 50
}

function selectType(type: SpeciesType) {
  store.setType(type)
  mobileMenuOpen.value = false
  // 滚动到物种展示区域
  const section = document.getElementById('species-section')
  if (section) {
    section.scrollIntoView({ behavior: 'smooth' })
  }
}

function openSearch() {
  emit('openSearch')
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function handleLogout() {
  if (clearToken) {
    clearToken()
  }
  showSettings.value = false
}

function handleChangeToken() {
  if (showTokenInput) {
    showTokenInput()
  }
  showSettings.value = false
}

// 快捷键支持
function handleKeydown(e: KeyboardEvent) {
  // Cmd/Ctrl + K 打开搜索
  if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
    e.preventDefault()
    openSearch()
  }
}

// 点击外部关闭设置菜单
function handleClickOutside(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (!target.closest('.settings-dropdown') && !target.closest('.settings-btn')) {
    showSettings.value = false
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  window.addEventListener('keydown', handleKeydown)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('keydown', handleKeydown)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <nav 
    class="navbar"
    :class="{ 'navbar--scrolled': isScrolled }"
  >
    <div class="navbar__container">
      <!-- Logo -->
      <div class="navbar__logo" @click="scrollToTop">
        <span class="logo-icon">🌊</span>
        <span class="logo-text">物种图谱</span>
      </div>

      <!-- Navigation Links -->
      <div class="navbar__links">
        <button
          v-for="type in speciesTypes"
          :key="type.name"
          class="nav-link"
          :class="{ 'nav-link--active': store.currentType === type.name }"
          @click="selectType(type.name)"
        >
          {{ type.name }}
        </button>
      </div>

      <!-- Search Button -->
      <button class="navbar__search-btn" @click="openSearch">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/>
          <path d="m21 21-4.3-4.3"/>
        </svg>
        <span class="search-text">搜索</span>
        <kbd class="search-kbd">⌘K</kbd>
      </button>

      <!-- Settings Button -->
      <div class="navbar__settings">
        <button class="settings-btn" @click.stop="showSettings = !showSettings">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3"/>
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
          </svg>
        </button>
        
        <Transition name="dropdown">
          <div v-if="showSettings" class="settings-dropdown">
            <button class="dropdown-item" @click="handleChangeToken">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              更换密钥
            </button>
            <button class="dropdown-item dropdown-item--danger" @click="handleLogout">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
              退出登录
            </button>
          </div>
        </Transition>
      </div>

      <!-- Mobile Menu Button -->
      <button class="navbar__mobile-btn" @click="mobileMenuOpen = !mobileMenuOpen">
        <svg v-if="!mobileMenuOpen" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="3" y1="12" x2="21" y2="12"/>
          <line x1="3" y1="6" x2="21" y2="6"/>
          <line x1="3" y1="18" x2="21" y2="18"/>
        </svg>
        <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6 6 18M6 6l12 12"/>
        </svg>
      </button>
    </div>

    <!-- Mobile Menu -->
    <Transition name="slide">
      <div v-if="mobileMenuOpen" class="navbar__mobile-menu">
        <button
          v-for="type in speciesTypes"
          :key="type.name"
          class="mobile-link"
          :class="{ 'mobile-link--active': store.currentType === type.name }"
          @click="selectType(type.name)"
        >
          {{ speciesTypes.find(t => t.name === type.name)?.icon }}
          {{ type.name }}
        </button>
        <button class="mobile-search" @click="openSearch(); mobileMenuOpen = false">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.3-4.3"/>
          </svg>
          搜索物种
        </button>
      </div>
    </Transition>
  </nav>
</template>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 0 24px;
  height: 52px;
  display: flex;
  flex-direction: column;
  transition: all var(--transition-base);
}

.navbar--scrolled {
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(var(--blur-lg));
  -webkit-backdrop-filter: blur(var(--blur-lg));
  border-bottom: 1px solid var(--color-border);
}

.navbar__container {
  width: 100%;
  max-width: 1400px;
  height: 52px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar__logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  cursor: pointer;
  transition: opacity var(--transition-fast);
}

.navbar__logo:hover {
  opacity: 0.8;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-family: var(--font-display);
  letter-spacing: -0.02em;
}

.navbar__links {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-link {
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
  background: transparent;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.nav-link:hover {
  color: var(--color-text);
  background: rgba(255, 255, 255, 0.1);
}

.nav-link--active {
  color: var(--color-text);
  background: rgba(255, 255, 255, 0.15);
}

/* Search Button */
.navbar__search-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  font-size: 13px;
  color: var(--color-text-muted);
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.navbar__search-btn:hover {
  color: var(--color-text);
  background: rgba(255, 255, 255, 0.1);
  border-color: var(--color-text-muted);
}

.search-text {
  font-weight: 500;
}

.search-kbd {
  padding: 2px 6px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--color-text-muted);
  background: var(--color-bg-tertiary);
  border: 1px solid var(--color-border);
  border-radius: 4px;
}

/* Settings */
.navbar__settings {
  position: relative;
  margin-left: 8px;
}

.settings-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.settings-btn:hover {
  color: var(--color-text);
  background: rgba(255, 255, 255, 0.1);
}

.settings-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 160px;
  padding: 8px;
  background: rgba(30, 30, 30, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
  z-index: 100;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.dropdown-item:hover {
  color: var(--color-text);
  background: rgba(255, 255, 255, 0.1);
}

.dropdown-item--danger:hover {
  color: #ff6b6b;
  background: rgba(255, 107, 107, 0.1);
}

/* Dropdown animation */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
}

/* Mobile */
.navbar__mobile-btn {
  display: none;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: transparent;
  border: none;
  color: var(--color-text);
  cursor: pointer;
}

.navbar__mobile-menu {
  position: absolute;
  top: 52px;
  left: 0;
  right: 0;
  padding: 16px;
  background: rgba(0, 0, 0, 0.95);
  backdrop-filter: blur(var(--blur-lg));
  border-bottom: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-secondary);
  background: transparent;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.mobile-link:hover,
.mobile-link--active {
  color: var(--color-text);
  background: rgba(255, 255, 255, 0.1);
}

.mobile-search {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 16px;
  margin-top: 8px;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  cursor: pointer;
}

/* Slide animation */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 响应式 */
@media (max-width: 900px) {
  .navbar__links {
    display: none;
  }

  .navbar__search-btn .search-text,
  .navbar__search-btn .search-kbd {
    display: none;
  }

  .navbar__search-btn {
    padding: 8px;
    border-radius: 8px;
  }

  .navbar__mobile-btn {
    display: flex;
  }
}

@media (max-width: 480px) {
  .logo-text {
    display: none;
  }
}
</style>
