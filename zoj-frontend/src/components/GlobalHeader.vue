<template>
  <a-layout-header class="global-header">
    <div class="header-content">
      <!-- Logo 和标题 -->
      <div class="logo-container" @click="goHome">
        <div class="logo">
          <span class="logo-text">ZOJ</span>
        </div>
      </div>

      <!-- 导航菜单 -->
      <a-menu
        class="nav-menu"
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="handleMenuClick"
      >
        <a-menu-item v-for="route in visibleRoutes" :key="route.path" :data-path="route.path">
          {{ route.name }}
        </a-menu-item>
      </a-menu>

      <!-- 用户信息区域 -->
      <div class="user-actions">
        <div v-if="!userStore.isLoggedIn" class="login-actions">
          <a-button type="outline" @click="goToLogin">登录</a-button>
          <a-button type="primary" @click="goToRegister" style="margin-left: 10px">注册</a-button>
        </div>
        <div v-else class="user-info">
          <a-dropdown>
            <a-avatar class="user-avatar">
              {{ getUserInitial }}
            </a-avatar>
            <template #content>
              <a-doption @click="goToProfile"> <icon-user /> 个人中心 </a-doption>
              <a-doption @click="handleLogout"> <icon-export /> 退出登录 </a-doption>
            </template>
          </a-dropdown>
        </div>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { constantRoutes } from '@/router'
import checkAccess from '@/access/checkAccess'
import ACCESS_ENUM from '@/access/accessEnum'
import { userLogout as userLogoutApi } from '@/api/user/userApi'
import { Message } from '@arco-design/web-vue'
import { JwtUtils } from '@/utils/jwt'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 计算当前选中的菜单项
const selectedKeys = computed(() => {
  return [route.path]
})

// 计算有权限访问的路由
const visibleRoutes = computed(() => {
  return constantRoutes.filter((route) => {
    // 隐藏标记为隐藏的菜单项
    if (route.meta?.hideInMenu) {
      return false
    }

    // 根据权限过滤路由
    if (route.meta?.access) {
      return checkAccess(userStore.loginUser, route.meta.access)
    }

    // 默认显示
    return true
  })
})

// 获取用户名首字母
const getUserInitial = computed(() => {
  const userName = userStore.loginUser?.userName || '未知用户'
  if (userName === '未登录') {
    return '?'
  }
  return userName.charAt(0).toUpperCase() || '?'
})

// 监听用户登录状态变化
watch(
  () => userStore.loginUser,
  (newUser, oldUser) => {
    // 用户状态变化时，强制组件更新
    console.log('用户状态发生变化:', newUser, oldUser)
  },
  { deep: true },
)

// 监听路由变化，确保用户状态是最新的
watch(
  () => route.path,
  async () => {
    // 暂时禁用路由变化时的状态检查，避免频繁调用后端API
    // 如果需要检查登录状态，可以在用户主动操作时进行
    console.log('路由变化:', route.path, '用户登录状态:', userStore.isLoggedIn)
  },
)

// 处理菜单点击事件
const handleMenuClick = (key: string) => {
  router.push({ path: key })
}

// 跳转到首页
const goHome = () => {
  router.push('/')
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/user/login')
}

// 跳转到注册页
const goToRegister = () => {
  router.push('/user/register')
}

// 跳转到个人中心
const goToProfile = () => {
  // TODO: 实现跳转到个人中心功能
}

// 处理用户登出
const handleLogout = async () => {
  try {
    // JWT认证模式下，前端直接清除令牌即可
    userStore.logout()
    Message.success('登出成功')

    // 跳转到首页
    router.push('/')
  } catch (error: any) {
    // 即使出错，也执行登出操作
    userStore.logout()
    Message.success('登出成功')
    router.push('/')
  }
}

// 组件挂载时检查用户登录状态
onMounted(async () => {
  // 如果用户已登录且不在登录页面，验证登录状态是否仍然有效
  if (userStore.isLoggedIn && route.path !== '/user/login' && route.path !== '/user/register') {
    await userStore.checkLoginStatus()
  }
})
</script>

<style scoped>
.global-header {
  background-color: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0;
  height: 64px;
  line-height: 64px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
}

.logo-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  flex-shrink: 0;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #000000;
}

.logo-text {
  margin-left: 8px;
}

.nav-menu {
  flex: 1;
  margin: 0 20px;
  border-bottom: none;
  background-color: transparent;
  display: flex;
}

.nav-menu :deep(.arco-menu-inner) {
  display: flex;
  align-items: center;
}

.nav-menu :deep(.arco-menu-item) {
  color: #000000;
  font-size: 16px;
  padding: 0 20px;
  flex: 1;
  text-align: center;
  min-width: 100px;
}

.nav-menu :deep(.arco-menu-item:hover) {
  background-color: rgba(0, 0, 0, 0.05);
}

.nav-menu :deep(.arco-menu-selected) {
  background-color: rgba(0, 0, 0, 0.1);
  font-weight: bold;
}

.user-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.user-avatar {
  background-color: #165dff;
  cursor: pointer;
}

.login-actions :deep(.arco-btn) {
  color: #000000;
  border-color: #000000;
}

.login-actions :deep(.arco-btn-primary) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}
</style>
