import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('accessToken')
    if (token) {
      // 添加 Bearer 前缀
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    
    // 如果返回的状态码不是 200，说明接口有问题
    if (res.code && res.code !== 200) {
      console.error('API Error:', res.msg || 'Error')
      
      // Token 过期或无效
      if (res.code === 401 || res.code === 2001 || res.code === 2002) {
        // 清除本地存储
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        // 刷新页面，触发重新登录
        window.location.reload()
      }
      
      return Promise.reject(new Error(res.msg || 'Error'))
    }
    
    return res
  },
  (error) => {
    console.error('Response error:', error)
    
    // 处理 HTTP 错误状态
    if (error.response) {
      const status = error.response.status
      
      if (status === 401) {
        // 未授权，清除登录状态
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        window.location.reload()
      } else if (status === 403) {
        console.error('没有权限访问该资源')
      } else if (status === 500) {
        console.error('服务器内部错误')
      }
    }
    
    return Promise.reject(error)
  }
)

// 封装请求方法
export function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  return service(config) as Promise<T>
}

export default service
