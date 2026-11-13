/**
 * 用户信息
 */
export interface User {
  id: number
  username: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  status: UserStatus
  role: UserRole
  lastLoginTime?: string
  createTime?: string
}

/**
 * 用户状态
 */
export type UserStatus = 'ACTIVE' | 'DISABLED' | 'LOCKED'

/**
 * 用户角色
 */
export type UserRole = 'ADMIN' | 'USER'

/**
 * 登录参数
 */
export interface LoginParams {
  username: string
  password: string
}

/**
 * 登录响应
 */
export interface LoginResult {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: User
}

/**
 * 注册参数
 */
export interface RegisterParams {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
}
