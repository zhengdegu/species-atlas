import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getProfile, type LoginParams, type UserInfo, type LoginResult } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const user = ref<UserInfo | null>(null)
  
  // Token
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!accessToken.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const username = computed(() => user.value?.username || '')
  const nickname = computed(() => user.value?.nickname || user.value?.username || '')

  /**
   * 初始化：从 localStorage 恢复状态
   */
  function init() {
    const savedToken = localStorage.getItem('accessToken')
    const savedRefreshToken = localStorage.getItem('refreshToken')
    const savedUser = localStorage.getItem('user')

    if (savedToken) {
      accessToken.value = savedToken
      refreshToken.value = savedRefreshToken

      if (savedUser) {
        try {
          user.value = JSON.parse(savedUser)
        } catch (e) {
          console.error('Failed to parse user from localStorage')
        }
      }
    }
  }

  /**
   * 登录
   */
  async function doLogin(params: LoginParams): Promise<LoginResult> {
    const res = await login(params)
    const data = res.data

    // 保存状态
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    user.value = data.user

    // 保存到 localStorage
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('user', JSON.stringify(data.user))

    return data
  }

  /**
   * 登出
   */
  async function doLogout() {
    try {
      await logout()
    } catch (e) {
      // 即使请求失败也要清除本地状态
      console.error('Logout request failed:', e)
    } finally {
      clearAuth()
    }
  }

  /**
   * 清除认证状态
   */
  function clearAuth() {
    accessToken.value = null
    refreshToken.value = null
    user.value = null

    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  /**
   * 获取用户信息
   */
  async function fetchProfile() {
    try {
      const res = await getProfile()
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
    } catch (e) {
      console.error('Failed to fetch profile:', e)
    }
  }

  /**
   * 检查是否已登录
   */
  function checkAuth(): boolean {
    return !!localStorage.getItem('accessToken')
  }

  return {
    user,
    accessToken,
    refreshToken,
    isLoggedIn,
    isAdmin,
    username,
    nickname,
    init,
    doLogin,
    doLogout,
    clearAuth,
    fetchProfile,
    checkAuth
  }
})
