import type { AppRouteRecordRaw } from "@/router/index";
import BasicLayout from "@/layouts/BasicLayout.vue";
import QuestionsView from "@/views/question/QuestionsView.vue";
import QuestionSubmitView from "@/views/question/QuestionSubmitView.vue";
import ViewQuestionView from "@/views/question/ViewQuestionView.vue";
import AddQuestionView from "@/views/question/AddQuestionView.vue";
import ManageQuestionView from "@/views/question/ManageQuestionView.vue";
import ACCESS_ENUM from "@/access/accessEnum";

export const questionRoutes: AppRouteRecordRaw[] = [
  {
    path: "/questions",
    name: "题目",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "浏览题目",
        component: QuestionsView,
      }
    ]
  },
  {
    path: "/question_submit",
    name: "题目提交",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "浏览题目提交",
        component: QuestionSubmitView,
        meta: {
          access: ACCESS_ENUM.USER,
          hideInMenu: true, // 与导航栏显示相关的路由保持一致
        },
      }
    ]
  },
  {
    path: "/view/question/:id",
    name: "做题",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "在线做题",
        component: ViewQuestionView,
        props: true,
        meta: {
          access: ACCESS_ENUM.USER,
          hideInMenu: true,
        },
      }
    ]
  },
  {
    path: "/add/question",
    name: "创建题目",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "创建题目页面",
        component: AddQuestionView,
        meta: {
          access: ACCESS_ENUM.USER,
          hideInMenu: true, // 隐藏在菜单中，通过管理题目页面访问
        },
      }
    ]
  },
  {
    path: "/update/question",
    name: "更新题目",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "更新题目页面",
        component: AddQuestionView,
        meta: {
          access: ACCESS_ENUM.USER,
          hideInMenu: true,
        },
      }
    ]
  },
  {
    path: "/manage/question/",
    name: "管理题目",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "管理题目页面",
        component: ManageQuestionView,
        meta: {
          access: ACCESS_ENUM.ADMIN,
          hideInMenu: true, // 管理页面默认隐藏，通过其他方式访问
        },
      }
    ]
  },
];