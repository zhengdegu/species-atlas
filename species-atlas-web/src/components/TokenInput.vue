<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

const emit = defineEmits<{
  authenticated: []
}>()

const token = ref('')
const visible = ref(true)
const error = ref('')
const loading = ref(false)
const showToken = ref(false)

// 检查是否已有 token
const hasToken = computed(() => !!localStorage.getItem('accessToken'))

onMounted(() => {
  // 如果已有 token，直接跳过
  if (hasToken.value) {
    visible.value = false
    emit('authenticated')
  }
})

async function handleSubmit() {
  if (!token.value.trim()) {
    error.value = '请输入访问密钥'
    return
  }

  loading.value = true
  error.value = ''

  try {
    // 保存 token
    localStorage.setItem('accessToken', token.value.trim())
    
    // 验证 token - 尝试获取一个接口
    const response = await fetch('/api/system/benthicAnimalDataSummary/list?pageNum=1&pageSize=1', {
      headers: {
        'Authorization': token.value.trim()
      }
    })

    const data = await response.json()

    if (data.code === 200) {
      visible.value = false
      emit('authenticated')
    } else if (data.code === 401) {
      error.value = '密钥无效或已过期，请重新输入'
      localStorage.removeItem('accessToken')
    } else {
      // 其他错误但可能 token 有效
      visible.value = false
      emit('authenticated')
    }
  } catch (e) {
    error.value = '验证失败，请检查网络连接'
    localStorage.removeItem('accessToken')
  } finally {
    loading.value = false
  }
}

function handleSkip() {
  visible.value = false
  emit('authenticated')
}

function clearToken() {
  localStorage.removeItem('accessToken')
  token.value = ''
  visible.value = true
}

// 暴露清除方法供外部调用
defineExpose({ clearToken, show: () => visible.value = true })
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="token-overlay">
        <div class="token-modal">
          <!-- 背景装饰 -->
          <div class="modal-glow"></div>
          
          <div class="modal-content">
            <!-- Logo -->
            <div class="modal-logo">
              <span class="logo-icon">🔐</span>
            </div>

            <!-- 标题 -->
            <h2 class="modal-title">访问验证</h2>
            <p class="modal-desc">
              请输入 API 访问密钥以加载物种数据
            </p>

            <!-- 表单 -->
            <form class="token-form" @submit.prevent="handleSubmit">
              <div class="input-group">
                <label class="input-label">访问密钥 (Token)</label>
                <div class="input-wrap">
                  <input
                    v-model="token"
                    :type="showToken ? 'text' : 'password'"
                    class="token-input"
                    placeholder="请输入您的 Token..."
                    autocomplete="off"
                  />
                  <button 
                    type="button" 
                    class="toggle-visibility"
                    @click="showToken = !showToken"
                  >
                    <svg v-if="showToken" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                    <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                  </button>
                </div>
                <p v-if="error" class="input-error">{{ error }}</p>
              </div>

              <div class="form-actions">
                <button 
                  type="submit" 
                  class="btn btn-primary"
                  :disabled="loading"
                >
                  <span v-if="loading" class="btn-loading"></span>
                  <span v-else>验证并进入</span>
                </button>
                <button 
                  type="button" 
                  class="btn btn-secondary"
                  @click="handleSkip"
                >
                  暂时跳过
                </button>
              </div>
            </form>

            <!-- 提示 -->
            <div class="modal-hint">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
              <span>Token 将保存在本地，下次访问无需重复输入</span>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.token-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(0, 0, 0, 0.9);
  backdrop-filter: blur(20px);
}

.token-modal {
  position: relative;
  width: 100%;
  max-width: 420px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 24px;
  overflow: hidden;
}

.modal-glow {
  position: absolute;
  top: -100px;
  left: 50%;
  transform: translateX(-50%);
  width: 300px;
  height: 300px;
  background: radial-gradient(ellipse, rgba(41, 151, 255, 0.2) 0%, transparent 70%);
  pointer-events: none;
}

.modal-content {
  position: relative;
  padding: 48px 40px;
  text-align: center;
}

.modal-logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 20px;
}

.logo-icon {
  font-size: 40px;
}

.modal-title {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 12px;
  letter-spacing: -0.02em;
}

.modal-desc {
  font-size: 15px;
  color: var(--color-text-secondary);
  margin-bottom: 32px;
}

/* Form */
.token-form {
  text-align: left;
}

.input-group {
  margin-bottom: 24px;
}

.input-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.token-input {
  width: 100%;
  padding: 14px 48px 14px 16px;
  font-size: 15px;
  font-family: var(--font-mono);
  color: var(--color-text);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  outline: none;
  transition: all var(--transition-fast);
}

.token-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(41, 151, 255, 0.15);
}

.token-input::placeholder {
  color: var(--color-text-muted);
  font-family: var(--font-text);
}

.toggle-visibility {
  position: absolute;
  right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: transparent;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  border-radius: 6px;
  transition: all var(--transition-fast);
}

.toggle-visibility:hover {
  color: var(--color-text);
  background: rgba(255, 255, 255, 0.1);
}

.input-error {
  margin-top: 8px;
  font-size: 13px;
  color: #ff6b6b;
}

/* Actions */
.form-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.btn {
  width: 100%;
  padding: 14px 24px;
  font-size: 15px;
  font-weight: 600;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  color: #000;
  background: var(--color-text);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 255, 255, 0.2);
}

.btn-secondary {
  color: var(--color-text-secondary);
  background: transparent;
  border: 1px solid var(--color-border);
}

.btn-secondary:hover {
  color: var(--color-text);
  border-color: var(--color-text-muted);
}

.btn-loading {
  width: 18px;
  height: 18px;
  border: 2px solid transparent;
  border-top-color: currentColor;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Hint */
.modal-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border-light);
  font-size: 12px;
  color: var(--color-text-muted);
}

/* Fade animation */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-enter-active .token-modal {
  animation: modalIn 0.3s ease-out;
}

.fade-leave-active .token-modal {
  animation: modalIn 0.2s ease-in reverse;
}

@keyframes modalIn {
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
@media (max-width: 480px) {
  .modal-content {
    padding: 32px 24px;
  }

  .modal-title {
    font-size: 24px;
  }
}
</style>
