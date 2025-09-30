/**
 * 题目提交类型定义
 */

// 题目提交基本信息
export interface QuestionSubmit {
  id?: string // 修改为字符串类型，避免JavaScript大整数精度丢失
  language?: string
  code?: string
  judgeInfo?: string
  status?: number
  questionId?: string // 修改为字符串类型，避免JavaScript大整数精度丢失
  userId?: string // 修改为字符串类型，避免JavaScript大整数精度丢失
  createTime?: Date
  updateTime?: Date
  isDelete?: number
}

// 题目提交查询请求
export interface QuestionSubmitQueryRequest {
  id?: string // 修改为字符串类型，避免JavaScript大整数精度丢失
  language?: string
  status?: number
  questionId?: string // 修改为字符串类型，避免JavaScript大整数精度丢失
  userId?: string // 修改为字符串类型，避免JavaScript大整数精度丢失
  current?: number
  pageSize?: number
  sortField?: string
  sortOrder?: string
}

// 题目提交视图对象
export interface QuestionSubmitVO extends QuestionSubmit {
  question?: any // 题目信息
  user?: any // 用户信息
  statusText?: string
  languageText?: string
}
