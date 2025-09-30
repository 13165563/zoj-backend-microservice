<template>
  <div class="user-login-view">
    <div class="form-container">
      <h1>用户登录</h1>
      <a-form :model="loginForm" style="width: 100%; max-width: 400px" @submit="handleLogin">
        <a-form-item field="userAccount" label="账号" required>
          <a-input v-model="loginForm.userAccount" placeholder="请输入账号" />
        </a-form-item>
        <a-form-item field="userPassword" label="密码" required>
          <a-input-password v-model="loginForm.userPassword" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" style="width: 100%">
            JWT登录
          </a-button>
        </a-form-item>
        <a-form-item>
          <a-button type="outline" @click="handleSessionLogin" :loading="loading" style="width: 100%">
            Session登录
          </a-button>
        </a-form-item>
        <a-form-item>
          <a-button type="text" @click="goToRegister" style="width: 100%">
            还没有账号？点击注册
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/stores/user'
import { userLogin, userLoginJwt } from '@/api/user/userApi'

// 使用路由和用户store
const router = useRouter()
const userStore = useUserStore()

// 登录表单数据
const loginForm = reactive({
  userAccount: '',
  userPassword: '',
})

// 加载状态
const loading = ref(false)

// JWT登录处理（默认登录方式）
const handleLogin = async () => {
  if (!loginForm.userAccount || !loginForm.userPassword) {
    Message.error('请填写完整信息')
    return
  }

  // 防止重复提交
  if (loading.value) {
    return
  }

  loading.value = true
  try {
    const res = await userLoginJwt(loginForm)
    if (res.code === 0) {
      Message.success('JWT登录成功')

      // 获取用户数据和JWT令牌
      const { data } = res
      const { token, user } = data

      // 使用JWT登录方法存储令牌和用户信息
      userStore.login(user, token)

      // 处理登录后的跳转
      handleRedirect()
    } else {
      Message.error(res.message || 'JWT登录失败')
    }
  } catch (err) {
    Message.error('JWT登录失败，请稍后重试')
    console.error('JWT登录失败:', err)
  } finally {
    loading.value = false
  }
}

// Session登录处理
const handleSessionLogin = async () => {
  if (!loginForm.userAccount || !loginForm.userPassword) {
    Message.error('请填写完整信息')
    return
  }

  // 防止重复提交
  if (loading.value) {
    return
  }

  loading.value = true
  try {
    const res = await userLogin(loginForm)
    if (res.code === 0) {
      Message.success('Session登录成功')

      // 获取用户数据
      const { data } = res

      // 生成 JWT 令牌（根据后端实现调整）
      // 当前后端使用简化格式：userId:userRole:userAccount
      const token = `${data.id}:${data.userRole}:${data.userAccount}`

      // 使用新的login方法存储令牌和用户信息
      userStore.login(data, token)

      // 处理登录后的跳转
      handleRedirect()
    } else {
      Message.error(res.message || 'Session登录失败')
    }
  } catch (err) {
    Message.error('Session登录失败，请稍后重试')
    console.error('Session登录失败:', err)
  } finally {
    loading.value = false
  }
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/user/register')
}

// 处理登录成功后的跳转
const handleRedirect = () => {
  const redirect = router.currentRoute.value.query.redirect as string
  const isValidRedirect = redirect && redirect !== 'undefined' && redirect !== 'null'

  if (isValidRedirect) {
    router.push({
      path: redirect,
      replace: true,
    })
  } else {
    router.push({
      path: '/',
      replace: true,
    })
  }
}
</script>

<style scoped>
.user-login-view {
  width: 100%;
  display: flex;
  justify-content: center;
  background-color: #ffffff;
  padding: 20px 0;
}

.form-container {
  width: 100%;
  max-width: 400px;
  text-align: center;
}

.user-login-view h1 {
  margin-bottom: 32px;
  text-align: center;
}
</style>
