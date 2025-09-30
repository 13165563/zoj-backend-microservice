/**
 * 题目类型定义
 */

// 判题信息
export interface JudgeInfo {
  message?: string
  time?: number
  memory?: number
}

// 题目基本信息
export interface Question {
  id?: string // 修改为只支持字符串类型，避免JavaScript大整数精度丢失
  title?: string
  content?: string
  tags?: string[]
  answer?: string
  submitNum?: number
  acceptedNum?: number
  judgeCase?: string
  judgeConfig?: string
  thumbNum?: number
  favourNum?: number
  userId?: string // 修改为只支持字符串类型，避免JavaScript大整数精度丢失
  createTime?: Date
  updateTime?: Date
  isDelete?: number
}

// 题目查询请求
export interface QuestionQueryRequest {
  id?: number
  title?: string
  content?: string
  tags?: string[]
  answer?: string
  userId?: number
  current?: number
  pageSize?: number
  sortField?: string
  sortOrder?: string
}

// 题目视图对象
export interface QuestionVO extends Question {
  judgeInfo?: JudgeInfo
  hasThumb?: boolean
  hasFavour?: boolean
}

// 题目添加或更新请求
export interface QuestionAddRequest extends Question {
  // 添加题目时需要的字段
}

export interface QuestionUpdateRequest extends Question {
  // 更新题目时需要的字段
  id: string | number
}

/**
 * 题目提交请求体
 */
export interface QuestionSubmitAddRequest {
  code: string
  language: string
  questionId: string // 改为string类型以避免大整数精度问题
}
