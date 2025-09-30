/**
 * 通用类型定义
 */

// 基础响应结构
export interface BaseResponse<T> {
  code: number;
  data: T;
  message: string;
}

// 分页响应结构
export interface Page<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
}

// 分页查询请求基础结构
export interface PageRequest {
  current?: number;
  pageSize?: number;
  sortField?: string;
  sortOrder?: string;
}

// 删除请求
export interface DeleteRequest {
  id: number;
}