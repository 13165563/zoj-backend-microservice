import http from '@/plugins/axios'
import type { BaseResponse, Page } from '@/api/common/type'
import type {
  Question,
  QuestionQueryRequest,
  QuestionVO,
  QuestionAddRequest,
  QuestionUpdateRequest,
} from './type'

/**
 * 题目相关 API 接口
 */

// 添加题目
export const addQuestion = (data: QuestionAddRequest) => {
  return http.post<BaseResponse<number>>('/question/add', data)
}

// 删除题目
export const deleteQuestion = (id: string) => {
  return http.post<BaseResponse<boolean>>('/question/delete', { id })
}

// 更新题目
export const updateQuestion = (data: QuestionUpdateRequest) => {
  return http.post<BaseResponse<boolean>>('/question/update', data)
}

// 根据id获取题目
export const getQuestionById = (id: string) => {
  return http.get<BaseResponse<Question>>(`/question/get?id=${id}`)
}

// 根据id获取题目VO
export const getQuestionVOById = (id: string) => {
  return http.get<BaseResponse<QuestionVO>>(`/question/get/vo?id=${id}`)
}

// 分页获取题目列表
export const listQuestionByPage = (data: QuestionQueryRequest) => {
  return http.post<BaseResponse<Page<Question>>>('/question/list/page', data)
}

// 分页获取题目VO列表
export const listQuestionVOByPage = (data: QuestionQueryRequest) => {
  return http.post<BaseResponse<Page<QuestionVO>>>('/question/list/page/vo', data)
}

// 分页获取当前用户创建的题目VO列表
export const listMyQuestionVOByPage = (data: QuestionQueryRequest) => {
  return http.post<BaseResponse<Page<QuestionVO>>>('/question/my/list/page/vo', data)
}
