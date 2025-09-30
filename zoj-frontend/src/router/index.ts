// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import ACCESS_ENUM from '@/access/accessEnum'
import checkAccess from '@/access/checkAccess'
import { JwtUtils } from '@/utils/jwt'

// Views
import UserLayout from '@/layouts/UserLayout.vue'
import BasicLayout from '@/layouts/BasicLayout.vue'
import UserLoginView from '@/views/user/UserLoginView.vue'
import UserRegisterView from '@/views/user/UserRegisterView.vue'
import ManageUserView from '@/views/user/ManageUserView.vue'
import QuestionsView from '@/views/question/QuestionsView.vue'
import QuestionSubmitView from '@/views/question/QuestionSubmitView.vue'
import ViewQuestionView from '@/views/question/ViewQuestionView.vue'
import AddQuestionView from '@/views/question/AddQuestionView.vue'
import ManageQuestionView from '@/views/question/ManageQuestionView.vue'
import NoAuthView from '@/views/NoAuthView.vue'
import AdminView from '@/views/AdminView.vue'

// 定义路由元信息接口
export interface RouteMeta {
  title?: string
  icon?: string
  hidden?: boolean
  alwaysShow?: boolean
  access?: string
  children?: RouteMeta[]
  hideInMenu?: boolean
}

// 修复后的 AppRouteRecordRaw 接口
export interface AppRouteRecordRaw extends Omit<RouteRecordRaw, 'meta' | 'children'> {
  meta?: RouteMeta
  children?: AppRouteRecordRaw[]
}

// 路由配置
export const constantRoutes: AppRouteRecordRaw[] = [
  {
    path: '/user',
    name: '用户',
    component: UserLayout,
    children: [
      {
        path: '/user/login',
        name: '用户登录',
        component: UserLoginView,
      },
      {
        path: '/user/register',
        name: '用户注册',
        component: UserRegisterView,
      },
    ],
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: '/questions',
    name: '浏览题目',
    component: QuestionsView,
  },
  {
    path: '/question_submit',
    name: '浏览题目提交',
    component: QuestionSubmitView,
  },
  {
    path: '/view/question/:id',
    name: '在线做题',
    component: ViewQuestionView,
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: '/add/question',
    name: '创建题目',
    component: AddQuestionView,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: '/update/question',
    name: '更新题目',
    component: AddQuestionView,
    meta: {
      access: ACCESS_ENUM.ADMIN,
      hideInMenu: true,
    },
  },
  {
    path: '/manage/question/',
    name: '管理题目',
    component: ManageQuestionView,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: '/',
    name: '主页',
    component: QuestionsView,
  },
  {
    path: '/noAuth',
    name: '无权限',
    component: NoAuthView,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: '/manage/user',
    name: '用户管理',
    component: ManageUserView,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes as RouteRecordRaw[],
})

// 添加路由守卫
router.beforeEach(async (to, from, next) => {
  console.log('路由守卫触发，当前路由:', to.path)
  const userStore = useUserStore()

  // 1. 处理特殊重定向（优先处理）
  if (to.path === '/login') {
    next('/user/login')
    return
  }

  // 2. 定义不需要认证的公开页面
  const publicPages = ['/user/login', '/user/register', '/questions', '/question_submit']
  const isPublicPage =
    publicPages.some((page) => to.path.startsWith(page)) ||
    to.path.startsWith('/view/question/') ||
    to.path === '/'

  // 3. 如果是公开页面，直接放行（不进行任何状态检查）
  if (isPublicPage) {
    console.log('公开页面，直接放行:', to.path)
    next()
    return
  }

  // 4. 对于需要认证的页面，进行状态检查和恢复
  try {
    // 如果用户已登录，检查权限
    if (userStore.isLoggedIn) {
      const needAccess = to.meta?.access as string
      if (needAccess && !userStore.hasAccess(needAccess)) {
        next('/noAuth')
        return
      }
      next()
    } else {
      // 未登录且需要认证，跳转到登录页
      console.log('未登录用户尝试访问需要认证的页面，跳转到登录页')
      next({
        path: '/user/login',
        query: { redirect: to.fullPath },
      })
    }
  } catch (error) {
    console.error('路由守卫错误:', error)
    // 出错时跳转到登录页
    next('/user/login')
  }
})

export default router
