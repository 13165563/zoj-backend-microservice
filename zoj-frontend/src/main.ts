import './assets/main.css'
import '@arco-design/web-vue/dist/arco.css'
import ArcoVue from '@arco-design/web-vue'
import ArcoVueIcon from '@arco-design/web-vue/es/icon'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

import App from './App.vue'
import router from './router'
import { useUserStore } from '@/stores/user'

const app = createApp(App)

// 创建 Pinia 实例并添加持久化插件
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)

// 初始化用户状态（只在有JWT令牌时尝试恢复）
const userStore = useUserStore()
// 尝试从token恢复用户信息
userStore.initUserFromToken()

app.use(router)
app.use(ArcoVue)
app.use(ArcoVueIcon)

app.mount('#app')
