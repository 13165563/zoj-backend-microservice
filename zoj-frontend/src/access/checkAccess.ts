import ACCESS_ENUM from "@/access/accessEnum";

/**
 * 检查用户是否具有指定权限
 * @param loginUser 当前登录用户
 * @param needAccess 需要的权限
 * @returns 是否具有权限
 */
const checkAccess = (loginUser: any, needAccess = ACCESS_ENUM.NOT_LOGIN) => {
  // 获取当前用户权限
  const userAccess = loginUser?.userRole ?? ACCESS_ENUM.NOT_LOGIN;
  
  // 如果不需要登录权限，则所有人都可以访问
  if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
    return true;
  }
  
  // 如果需要用户权限
  if (needAccess === ACCESS_ENUM.USER) {
    // 用户已登录即可访问
    if (userAccess === ACCESS_ENUM.NOT_LOGIN) {
      return false;
    }
  }
  
  // 如果需要管理员权限
  if (needAccess === ACCESS_ENUM.ADMIN) {
    return userAccess === ACCESS_ENUM.ADMIN;
  }
  
  return true;
};

export default checkAccess;