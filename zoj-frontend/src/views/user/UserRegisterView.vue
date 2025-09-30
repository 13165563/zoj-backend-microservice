<template>
  <div class="user-register-view">
    <div class="form-container">
      <h1>用户注册</h1>
      <a-form 
        :model="registerForm" 
        style="width: 100%; max-width: 400px;"
        @submit="handleRegister"
      >
        <a-form-item field="userAccount" label="账号" required>
          <a-input 
            v-model="registerForm.userAccount" 
            placeholder="请输入账号" 
          />
        </a-form-item>
        <a-form-item field="userPassword" label="密码" required>
          <a-input-password
            v-model="registerForm.userPassword"
            placeholder="请输入密码"
          />
        </a-form-item>
        <a-form-item field="checkPassword" label="确认密码" required>
          <a-input-password
            v-model="registerForm.checkPassword"
            placeholder="请再次输入密码"
          />
        </a-form-item>
        <a-form-item>
          <a-button 
            type="primary" 
            html-type="submit" 
            :loading="loading"
            style="width: 100%"
          >
            注册
          </a-button>
        </a-form-item>
        <a-form-item>
          <a-button 
            type="text" 
            @click="goToLogin"
            style="width: 100%"
          >
            已有账号？点击登录
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { userRegister } from '@/api/user/userApi'

// 使用路由
const router = useRouter()

// 注册表单数据
const registerForm = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

// 加载状态
const loading = ref(false)

// 处理注册
const handleRegister = async (data: any) => {
  if (!registerForm.userAccount || !registerForm.userPassword || !registerForm.checkPassword) {
    Message.error('请填写完整信息')
    return
  }
  
  if (registerForm.userPassword !== registerForm.checkPassword) {
    Message.error('两次输入的密码不一致')
    return
  }
  
  loading.value = true
  try {
    const res = await userRegister({
      userAccount: registerForm.userAccount,
      userPassword: registerForm.userPassword,
      checkPassword: registerForm.checkPassword
    })
    if (res.code === 0) {
      Message.success('注册成功')
      // 跳转到登录页面
      router.push('/user/login')
    } else {
      Message.error(res.message || '注册失败')
    }
  } catch (err) {
    Message.error('注册失败，请稍后重试')
    console.error('注册失败:', err)
  } finally {
    loading.value = false
  }
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/user/login')
}
</script>

<style scoped>
.user-register-view {
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

.user-register-view h1 {
  margin-bottom: 32px;
  text-align: center;
}
</style>