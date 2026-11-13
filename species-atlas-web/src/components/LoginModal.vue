<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/auth'

const emit = defineEmits<{
  authenticated: []
}>()

const userStore = useUserStore()

const username = ref('')
const password = ref('')
const visible = ref(true)
const error = ref('')
const loading = ref(false)
const showPassword = ref(false)

// 修改密码模式
const showChangePassword = ref(false)
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const showNewPassword = ref(false)

// 检查是否已登录
onMounted(() => {
  userStore.init()
  if (userStore.isLoggedIn) {
    visible.value = false
    emit('authenticated')
  }
})

async function handleSubmit() {
  if (!username.value.trim()) {
    error.value = '请输入用户名'
    return
  }
  if (!password.value.trim()) {
    error.value = '请输入密码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const result = await userStore.doLogin({
      username: username.value.trim(),
      password: password.value
    })
    
    // 检查是否需要修改密码
    if (result.mustChangePassword) {
      showChangePassword.value = true
      oldPassword.value = password.value // 预填旧密码
      error.value = ''
    } else {
      visible.value = false
      emit('authenticated')
    }
  } catch (e: any) {
    error.value = e.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}

async function handleChangePassword() {
  if (!newPassword.value.trim()) {
    error.value = '请输入新密码'
    return
  }
  if (newPassword.value.length < 6) {
    error.value = '新密码长度至少6位'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    error.value = '两次输入的密码不一致'
    return
  }
  if (newPassword.value === oldPassword.value) {
    error.value = '新密码不能与旧密码相同'
    return
  }

  loading.value = true
  error.value = ''

  try {
    await changePassword({
      oldPassword: oldPassword.value,
      newPassword: newPassword.value,
      confirmPassword: confirmPassword.value
    })
    
    // 密码修改成功
    showChangePassword.value = false
    visible.value = false
    emit('authenticated')
  } catch (e: any) {
    error.value = e.message || '密码修改失败'
  } finally {
    loading.value = false
  }
}

function clearAuth() {
  userStore.clearAuth()
  username.value = ''
  password.value = ''
  oldPassword.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  showChangePassword.value = false
  visible.value = true
}

function show() {
  visible.value = true
}

// 暴露方法供外部调用
defineExpose({ clearToken: clearAuth, show })
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="login-overlay">
        <div class="login-modal">
          <!-- 背景装饰 -->
          <div class="modal-glow"></div>
          
          <div class="modal-content">
            <!-- Logo -->
            <div class="modal-logo">
              <span class="logo-icon">🌊</span>
            </div>

            <!-- 标题 -->
            <h2 class="modal-title">物种图谱</h2>
            <p class="modal-desc">
              {{ showChangePassword ? '首次登录，请修改初始密码' : '请登录以访问水生生物数据平台' }}
            </p>

            <!-- 登录表单 -->
            <form v-if="!showChangePassword" class="login-form" @submit.prevent="handleSubmit">
              <!-- 用户名 -->
              <div class="input-group">
                <label class="input-label">用户名</label>
                <div class="input-wrap">
                  <input
                    v-model="username"
                    type="text"
                    class="login-input"
                    placeholder="请输入用户名"
                    autocomplete="username"
                  />
                </div>
              </div>

              <!-- 密码 -->
              <div class="input-group">
                <label class="input-label">密码</label>
                <div class="input-wrap">
                  <input
                    v-model="password"
                    :type="showPassword ? 'text' : 'password'"
                    class="login-input"
                    placeholder="请输入密码"
                    autocomplete="current-password"
                  />
                  <button 
                    type="button" 
                    class="toggle-visibility"
                    @click="showPassword = !showPassword"
                  >
                    <svg v-if="showPassword" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                    <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                  </button>
                </div>
              </div>

              <!-- 错误提示 -->
              <p v-if="error" class="input-error">{{ error }}</p>

              <div class="form-actions">
                <button 
                  type="submit" 
                  class="btn btn-primary"
                  :disabled="loading"
                >
                  <span v-if="loading" class="btn-loading"></span>
                  <span v-else>登 录</span>
                </button>
              </div>
            </form>

            <!-- 修改密码表单 -->
            <form v-else class="login-form" @submit.prevent="handleChangePassword">
              <!-- 新密码 -->
              <div class="input-group">
                <label class="input-label">新密码</label>
                <div class="input-wrap">
                  <input
                    v-model="newPassword"
                    :type="showNewPassword ? 'text' : 'password'"
                    class="login-input"
                    placeholder="请输入新密码（至少6位）"
                    autocomplete="new-password"
                  />
                  <button 
                    type="button" 
                    class="toggle-visibility"
                    @click="showNewPassword = !showNewPassword"
                  >
                    <svg v-if="showNewPassword" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                    <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                  </button>
                </div>
              </div>

              <!-- 确认密码 -->
              <div class="input-group">
                <label class="input-label">确认新密码</label>
                <div class="input-wrap">
                  <input
                    v-model="confirmPassword"
                    type="password"
                    class="login-input"
                    placeholder="请再次输入新密码"
                    autocomplete="new-password"
                  />
                </div>
              </div>

              <!-- 错误提示 -->
              <p v-if="error" class="input-error">{{ error }}</p>

              <div class="form-actions">
                <button 
                  type="submit" 
                  class="btn btn-primary"
                  :disabled="loading"
                >
                  <span v-if="loading" class="btn-loading"></span>
                  <span v-else>确认修改</span>
                </button>
              </div>
            </form>

            <!-- 提示 -->
            <div v-if="!showChangePassword" class="modal-hint">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
              <span>默认账号: admin / admin123</span>
            </div>
            <div v-else class="modal-hint modal-hint-warning">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
              <span>请设置一个安全的新密码</span>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.login-overlay {
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

.login-modal {
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
.login-form {
  text-align: left;
}

.input-group {
  margin-bottom: 20px;
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

.login-input {
  width: 100%;
  padding: 14px 48px 14px 16px;
  font-size: 15px;
  color: var(--color-text);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  outline: none;
  transition: all var(--transition-fast);
}

.login-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(41, 151, 255, 0.15);
}

.login-input::placeholder {
  color: var(--color-text-muted);
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
  margin-top: 12px;
  padding: 10px 14px;
  font-size: 13px;
  color: #ff6b6b;
  background: rgba(255, 107, 107, 0.1);
  border-radius: 8px;
}

/* Actions */
.form-actions {
  margin-top: 24px;
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

.modal-hint-warning {
  color: #fbbf24;
}

.modal-hint-warning svg {
  stroke: #fbbf24;
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

.fade-enter-active .login-modal {
  animation: modalIn 0.3s ease-out;
}

.fade-leave-active .login-modal {
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
