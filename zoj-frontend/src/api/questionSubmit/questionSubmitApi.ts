import http from '@/plugins/axios';
import type {
  BaseResponse,
  Page,
} from '@/api/common/type';
import type {
  QuestionSubmit,
  QuestionSubmitQueryRequest,
  QuestionSubmitVO,
} from './type';

/**
 * 题目提交相关 API 接口
 */

// 提交题目
export const doQuestionSubmit = (data: { questionId: string, language: string, code: string }) => {
  // 确保questionId是字符串类型
  const submitData = {
    ...data,
    questionId: String(data.questionId)
  };
  return http.post<BaseResponse<number>>('/question_submit/', submitData);
};

// 分页获取题目提交列表
export const listQuestionSubmitByPage = (data: QuestionSubmitQueryRequest) => {
  return http.post<BaseResponse<Page<QuestionSubmit>>>('/question_submit/list/page', data);
};

// 分页获取题目提交VO列表
export const listQuestionSubmitVOByPage = (data: QuestionSubmitQueryRequest) => {
  return http.post<BaseResponse<Page<QuestionSubmitVO>>>('/question_submit/list/page/vo', data);
};

// 根据ID获取题目提交详情
export const getQuestionSubmitVOById = (id: string) => {
  return http.get<BaseResponse<QuestionSubmitVO>>(`/question_submit/get/vo?id=${id}`);
};
