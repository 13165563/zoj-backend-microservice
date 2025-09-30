import axios from 'axios'
import { JwtUtils } from '@/utils/jwt'

// 创建 axios 实例
const http = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || 'http://localhost:8100/api',
  timeout: 10000,
  withCredentials: false, // JWT认证不需要cookie
  // 配置JSON解析，避免大整数精度丢失
  transformResponse: [
    function (data) {
      try {
        // 在JSON解析之前，使用正则表达式将大整数ID字段替换为字符串
        // 匹配模式：19位数字（如1957010078758363138）
        const bigIntPattern = /"(\w*[Ii][Dd]\w*)":\s*(\d{19,})/g
        let processedData = data.replace(bigIntPattern, (match, fieldName, bigInt) => {
          return `"${fieldName}":"${bigInt}"`
        })

        // 也处理userId字段
        const userIdPattern = /"(\w*[Uu]ser[Ii]d\w*)":\s*(\d{19,})/g
        processedData = processedData.replace(userIdPattern, (match, fieldName, bigInt) => {
          return `"${fieldName}":"${bigInt}"`
        })

        // 现在安全地解析JSON
        const result = JSON.parse(processedData)
        return result
      } catch (e) {
        console.error('transformResponse 解析错误:', e)
        return data
      }
    },
  ],
})

// 请求拦截器
http.interceptors.request.use(
  (config) => {
    // 获取 JWT 令牌
    const token = JwtUtils.getToken()

    // 如果存在令牌，添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 响应拦截器
http.interceptors.response.use(
  (response) => {
    // 可以在这里统一处理响应数据
    return response.data
  },
  (error) => {
    // 处理 401 未授权错误
    if (error.response?.status === 401) {
      // 清除本地存储的令牌
      JwtUtils.removeToken()
      // 跳转到登录页面
      window.location.href = '/user/login'
    }

    // 统一处理错误
    console.error('请求错误:', error)
    return Promise.reject(error)
  },
)

export default http
