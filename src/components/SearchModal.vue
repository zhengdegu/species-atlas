<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useSpeciesStore } from '@/stores/species'
import { speciesTypes, type SpeciesType } from '@/types/species'
import { searchSpecies } from '@/api/species'
import type { Species } from '@/types/species'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  close: []
  select: [species: Species, type: SpeciesType]
}>()

const store = useSpeciesStore()
const inputRef = ref<HTMLInputElement>()
const query = ref('')
const loading = ref(false)
const results = ref<{ type: SpeciesType; species: Species }[]>([])
const selectedIndex = ref(0)

// 搜索防抖
let searchTimeout: ReturnType<typeof setTimeout>

async function performSearch() {
  if (!query.value.trim()) {
    results.value = []
    return
  }

  loading.value = true
  try {
    results.value = await searchSpecies(query.value.trim(), 10)
  } catch (error) {
    console.error('Search failed:', error)
    results.value = []
  } finally {
    loading.value = false
  }
}

function onInput() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(performSearch, 300)
  selectedIndex.value = 0
}

function selectResult(item: { type: SpeciesType; species: Species }) {
  emit('select', item.species, item.type)
  close()
}

function close() {
  query.value = ''
  results.value = []
  selectedIndex.value = 0
  emit('close')
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    selectedIndex.value = Math.min(selectedIndex.value + 1, results.value.length - 1)
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    selectedIndex.value = Math.max(selectedIndex.value - 1, 0)
  } else if (e.key === 'Enter' && results.value[selectedIndex.value]) {
    selectResult(results.value[selectedIndex.value])
  } else if (e.key === 'Escape') {
    close()
  }
}

function getTypeConfig(type: SpeciesType) {
  return speciesTypes.find(t => t.name === type)
}

// 当弹窗打开时聚焦输入框
watch(() => props.visible, async (visible) => {
  if (visible) {
    await nextTick()
    inputRef.value?.focus()
  }
})
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="search-overlay" @click.self="close">
        <div class="search-modal">
          <!-- 搜索输入框 -->
          <div class="search-input-wrap">
            <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="m21 21-4.3-4.3"/>
            </svg>
            <input
              ref="inputRef"
              v-model="query"
              type="text"
              class="search-input"
              placeholder="搜索物种名称、编号..."
              @input="onInput"
              @keydown="handleKeydown"
            />
            <div v-if="loading" class="search-loading">
              <div class="loading-spinner"></div>
            </div>
            <kbd class="search-kbd">ESC</kbd>
          </div>

          <!-- 搜索结果 -->
          <div v-if="results.length > 0" class="search-results">
            <div class="results-header">
              <span>搜索结果</span>
              <span class="results-count">{{ results.length }} 条</span>
            </div>
            <div class="results-list">
              <button
                v-for="(item, index) in results"
                :key="`${item.type}-${item.species.speciesCode}`"
                class="result-item"
                :class="{ active: selectedIndex === index }"
                @click="selectResult(item)"
                @mouseenter="selectedIndex = index"
              >
                <span 
                  class="item-badge"
                  :style="{ background: getTypeConfig(item.type)?.gradient }"
                >
                  {{ getTypeConfig(item.type)?.icon }}
                </span>
                <div class="item-info">
                  <span class="item-name">{{ item.species.chineseName }}</span>
                  <span class="item-code">{{ item.species.speciesCode }}</span>
                </div>
                <span class="item-type">{{ item.type }}</span>
              </button>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="query && !loading" class="search-empty">
            <div class="empty-icon">🔍</div>
            <p>未找到匹配的物种</p>
            <span>尝试更换关键词搜索</span>
          </div>

          <!-- 搜索提示 -->
          <div v-else class="search-hints">
            <div class="hints-title">快速搜索</div>
            <div class="hints-tags">
              <button 
                v-for="type in speciesTypes" 
                :key="type.name"
                class="hint-tag"
                @click="query = type.name; performSearch()"
              >
                {{ type.icon }} {{ type.name }}
              </button>
            </div>
            <div class="hints-shortcuts">
              <div class="shortcut">
                <kbd>↑</kbd><kbd>↓</kbd>
                <span>选择</span>
              </div>
              <div class="shortcut">
                <kbd>Enter</kbd>
                <span>确认</span>
              </div>
              <div class="shortcut">
                <kbd>ESC</kbd>
                <span>关闭</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.search-overlay {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 120px;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
}

.search-modal {
  width: 100%;
  max-width: 600px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5);
}

/* Input */
.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
}

.search-icon {
  flex-shrink: 0;
  color: var(--color-text-muted);
}

.search-input {
  flex: 1;
  font-size: 18px;
  color: var(--color-text);
  background: transparent;
  border: none;
  outline: none;
}

.search-input::placeholder {
  color: var(--color-text-muted);
}

.search-loading {
  flex-shrink: 0;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.search-kbd {
  flex-shrink: 0;
  padding: 4px 8px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--color-text-muted);
  background: var(--color-bg-tertiary);
  border: 1px solid var(--color-border);
  border-radius: 4px;
}

/* Results */
.search-results {
  max-height: 400px;
  overflow-y: auto;
}

.results-header {
  display: flex;
  justify-content: space-between;
  padding: 12px 20px;
  font-size: 12px;
  color: var(--color-text-muted);
  border-bottom: 1px solid var(--color-border-light);
}

.results-count {
  color: var(--color-primary);
}

.results-list {
  padding: 8px;
}

.result-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: transparent;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: background var(--transition-fast);
}

.result-item:hover,
.result-item.active {
  background: var(--color-bg-card);
}

.item-badge {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  border-radius: 10px;
}

.item-info {
  flex: 1;
  text-align: left;
}

.item-name {
  display: block;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 2px;
}

.item-code {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-text-muted);
}

.item-type {
  font-size: 12px;
  color: var(--color-text-secondary);
  padding: 4px 10px;
  background: var(--color-bg-tertiary);
  border-radius: 6px;
}

/* Empty */
.search-empty {
  padding: 48px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.search-empty p {
  font-size: 16px;
  color: var(--color-text);
  margin-bottom: 8px;
}

.search-empty span {
  font-size: 14px;
  color: var(--color-text-muted);
}

/* Hints */
.search-hints {
  padding: 20px;
}

.hints-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 16px;
}

.hints-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.hint-tag {
  padding: 8px 14px;
  font-size: 14px;
  color: var(--color-text-secondary);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.hint-tag:hover {
  color: var(--color-text);
  border-color: var(--color-primary);
  background: var(--color-bg-card-hover);
}

.hints-shortcuts {
  display: flex;
  justify-content: center;
  gap: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border-light);
}

.shortcut {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.shortcut kbd {
  padding: 3px 6px;
  font-family: var(--font-mono);
  font-size: 10px;
  background: var(--color-bg-tertiary);
  border: 1px solid var(--color-border);
  border-radius: 4px;
}

/* Animations */
.modal-enter-active {
  animation: modalIn 0.3s ease-out;
}

.modal-leave-active {
  animation: modalIn 0.2s ease-in reverse;
}

@keyframes modalIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-enter-active .search-modal {
  animation: slideIn 0.3s ease-out;
}

.modal-leave-active .search-modal {
  animation: slideIn 0.2s ease-in reverse;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>
