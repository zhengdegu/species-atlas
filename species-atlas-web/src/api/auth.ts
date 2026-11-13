import { request } from './request'

// 登录参数
export interface LoginParams {
  username: string
  password: string
}

// 用户信息
export interface UserInfo {
  id: number
  username: string
  nickname: string
  email?: string
  phone?: string
  avatar?: string
  status: string
  role: string
  lastLoginTime?: string
  createTime?: string
}

// 登录响应
export interface LoginResult {
  accessToken: string
  refreshToken: string
  expiresIn: number
  mustChangePassword?: boolean
  user: UserInfo
}

// 注册参数
export interface RegisterParams {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
}

// 修改密码参数
export interface ChangePasswordParams {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

/**
 * 用户登录
 */
export function login(params: LoginParams): Promise<{ data: LoginResult }> {
  return request({
    url: '/auth/login',
    method: 'post',
    data: params
  })
}

/**
 * 用户注册
 */
export function register(params: RegisterParams): Promise<{ data: UserInfo }> {
  return request({
    url: '/auth/register',
    method: 'post',
    data: params
  })
}

/**
 * 刷新 Token
 */
export function refreshToken(refreshToken: string): Promise<{ data: LoginResult }> {
  return request({
    url: '/auth/refresh',
    method: 'post',
    data: { refreshToken }
  })
}

/**
 * 获取当前用户信息
 */
export function getProfile(): Promise<{ data: UserInfo }> {
  return request({
    url: '/auth/profile',
    method: 'get'
  })
}

/**
 * 修改密码
 */
export function changePassword(params: ChangePasswordParams): Promise<void> {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data: params
  })
}

/**
 * 退出登录
 */
export function logout(): Promise<void> {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
