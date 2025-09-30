/**
 * 用户类型定义
 */

// 用户基本信息
export interface User {
  id?: string // 修改为字符串类型，避免JavaScript大整数精度丢失
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
  createTime?: Date
  updateTime?: Date
}

// 登录用户信息
export interface LoginUser extends User {
  token?: string
}

// 用户登录请求
export interface UserLoginRequest {
  userAccount: string
  userPassword: string
}

// 用户注册请求
export interface UserRegisterRequest {
  userAccount: string
  userPassword: string
  checkPassword: string
}

// 用户查询请求
export interface UserQueryRequest {
  id?: string // 修改为字符串类型，避免JavaScript大整数精度丢失
  userName?: string
  userRole?: string
  current?: number
  pageSize?: number
  sortField?: string
  sortOrder?: string
  searchText?: string
}

// 用户视图对象
export interface UserVO extends User {
  // 视图对象可以与基础用户对象相同，或者根据需要添加额外字段
}

// 登录用户视图对象
export interface LoginUserVO extends User {
  token?: string
}

// 用户添加请求
export interface UserAddRequest {
  userAccount: string
  userName: string
  userPassword: string
  userRole: string
  userProfile?: string
}

// 用户更新请求
export interface UserUpdateRequest {
  id: string
  userAccount: string
  userName: string
  userPassword?: string
  userRole: string
  userProfile?: string
}

// 删除请求
export interface DeleteRequest {
  id: string
}
