import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { LoginUserVO } from '@/api/user/type'
import { getLoginUser as getLoginUserApi } from '@/api/user/userApi'
import ACCESS_ENUM from '@/access/accessEnum'
import checkAccess from '@/access/checkAccess'
import { JwtUtils } from '@/utils/jwt'

export const useUserStore = defineStore(
  'user',
  () => {
    // 当前登录用户信息
    const loginUser = ref<LoginUserVO>({
      userName: '未登录',
      userRole: ACCESS_ENUM.NOT_LOGIN,
    })

    // 获取当前登录用户
    const getLoginUser = async () => {
      try {
        const res = await getLoginUserApi()
        if (res.code === 0) {
          loginUser.value = res.data
        } else {
          // 获取用户信息失败，设置为未登录状态
          loginUser.value = {
            userName: '未登录',
            userRole: ACCESS_ENUM.NOT_LOGIN,
          }
        }
      } catch (error) {
        // 请求异常，设置为未登录状态
        loginUser.value = {
          userName: '未登录',
          userRole: ACCESS_ENUM.NOT_LOGIN,
        }
      }
    }

    // 设置登录用户
    const setLoginUser = (user: LoginUserVO) => {
      loginUser.value = user
    }

    // 登录并设置JWT令牌
    const login = (user: LoginUserVO, token: string) => {
      // 存储JWT令牌
      JwtUtils.setToken(token)
      // 设置用户信息
      loginUser.value = user
    }

    // 用户是否已登录
    const isLoggedIn = computed(() => {
      return loginUser.value?.userRole !== ACCESS_ENUM.NOT_LOGIN
    })

    // 用户是否为管理员
    const isAdmin = computed(() => {
      return loginUser.value?.userRole === ACCESS_ENUM.ADMIN
    })

    // 检查用户是否有权限访问指定资源
    const hasAccess = (needAccess: string) => {
      return checkAccess(loginUser.value, needAccess)
    }

    // 用户登出
    const logout = () => {
      // 清除JWT令牌
      JwtUtils.removeToken()
      // 清除用户信息
      loginUser.value = {
        userName: '未登录',
        userRole: ACCESS_ENUM.NOT_LOGIN,
      }
    }

    // 检查用户登录状态是否有效（用于解决长时间后状态显示不正确的问题）
    const checkLoginStatus = async () => {
      // 首先检查JWT令牌是否存在且有效
      if (!JwtUtils.isTokenValid()) {
        logout()
        return false
      }

      // 暂时不调用后端API验证，直接从token恢复用户信息
      // 这样可以避免网关JWT过滤器配置问题
      const userInfo = JwtUtils.getCurrentUser()
      if (userInfo) {
        loginUser.value = {
          id: userInfo.userId,
          userAccount: userInfo.userAccount,
          userRole: userInfo.userRole,
          userName: userInfo.userAccount,
        } as LoginUserVO
        return true
      } else {
        logout()
        return false
      }
    }

    // 从JWT令牌恢复用户信息
    const restoreUserFromToken = () => {
      const token = JwtUtils.getToken()
      console.log('尝试从token恢复用户信息，token:', token)

      if (token && JwtUtils.isTokenValid()) {
        console.log('token有效，开始解析用户信息')
        const userInfo = JwtUtils.getCurrentUser()
        console.log('解析到的用户信息:', userInfo)

        if (userInfo) {
          loginUser.value = {
            id: userInfo.userId,
            userAccount: userInfo.userAccount,
            userRole: userInfo.userRole,
            userName: userInfo.userAccount, // 使用userAccount作为默认用户名
          } as LoginUserVO
          console.log('成功恢复用户信息:', loginUser.value)
          return true
        } else {
          console.log('解析用户信息失败')
        }
      } else {
        console.log('token无效或不存在，token:', token, 'isValid:', JwtUtils.isTokenValid())
      }
      return false
    }

    // 初始化时尝试从token恢复用户信息
    const initUserFromToken = () => {
      if (restoreUserFromToken()) {
        console.log('从token恢复用户信息成功:', loginUser.value)
      } else {
        console.log('无法从token恢复用户信息')
      }
    }

    // 立即尝试恢复用户信息
    initUserFromToken()

    return {
      loginUser,
      getLoginUser,
      setLoginUser,
      login,
      isLoggedIn,
      isAdmin,
      hasAccess,
      logout,
      checkLoginStatus,
      restoreUserFromToken,
      initUserFromToken,
    }
  },
  {
    // Pinia 持久化插件配置
    persist: {
      key: 'zoj-user-store',
      storage: localStorage,
      paths: ['loginUser'],
    },
  },
)
