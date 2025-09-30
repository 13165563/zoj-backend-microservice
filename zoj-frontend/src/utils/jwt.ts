/**
 * JWT工具类（前端版本）
 */
export class JwtUtils {
  private static readonly TOKEN_KEY = 'zoj-jwt-token';

  /**
   * 设置JWT令牌
   */
  static setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * 获取JWT令牌
   */
  static getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * 移除JWT令牌
   */
  static removeToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  /**
   * 检查令牌是否存在
   */
  static hasToken(): boolean {
    return this.getToken() !== null;
  }

  /**
   * 解析JWT令牌（不验证签名，仅用于前端显示）
   * 支持标准JWT格式和简化格式（userId:userRole:userAccount）
   */
  static parseToken(token: string): any {
    try {
      console.log('开始解析JWT token:', token)

      // 检查是否为简化格式（userId:userRole:userAccount）
      if (token.includes(':') && !token.includes('.')) {
        console.log('检测到简化格式token')
        const parts = token.split(':');
        if (parts.length === 3) {
          const result = {
            userId: parts[0],
            userRole: parts[1],
            userAccount: parts[2],
            exp: Math.floor(Date.now() / 1000) + 24 * 60 * 60 // 24小时后过期
          };
          console.log('简化格式解析结果:', result)
          return result;
        }
      }

      // 标准JWT格式
      if (token.includes('.')) {
        console.log('检测到标准JWT格式token')
        const parts = token.split('.');
        if (parts.length !== 3) {
          throw new Error('Invalid token format');
        }

        const payload = parts[1];
        console.log('JWT payload:', payload)
        const decoded = atob(payload);
        console.log('解码后的payload:', decoded)
        const result = JSON.parse(decoded);
        console.log('标准JWT解析结果:', result)
        return result;
      }

      console.log('未知的token格式')
      return null;
    } catch (error) {
      console.error('Token parsing error:', error);
      return null;
    }
  }

  /**
   * 检查令牌是否过期
   */
  static isTokenExpired(token: string): boolean {
    try {
      const payload = this.parseToken(token);
      if (!payload || !payload.exp) {
        return true;
      }

      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp < currentTime;
    } catch (error) {
      return true;
    }
  }

  /**
   * 检查令牌是否有效
   */
  static isTokenValid(token?: string): boolean {
    const tokenToCheck = token || this.getToken();
    if (!tokenToCheck) {
      return false;
    }

    return !this.isTokenExpired(tokenToCheck);
  }

  /**
   * 从令牌中获取用户信息
   */
  static getCurrentUser(): any {
    const token = this.getToken();
    if (!token) {
      return null;
    }

    return this.parseToken(token);
  }

  /**
   * 从令牌中获取用户ID
   */
  static getCurrentUserId(): string | null {
    const user = this.getCurrentUser();
    return user?.userId || null;
  }

  /**
   * 从令牌中获取用户账号
   */
  static getCurrentUserAccount(): string | null {
    const user = this.getCurrentUser();
    return user?.userAccount || null;
  }

  /**
   * 从令牌中获取用户角色
   */
  static getCurrentUserRole(): string | null {
    const user = this.getCurrentUser();
    return user?.userRole || null;
  }

  /**
   * 检查用户是否为管理员
   */
  static isAdmin(): boolean {
    const role = this.getCurrentUserRole();
    return role === 'admin';
  }

  /**
   * 检查用户是否已登录
   */
  static isLoggedIn(): boolean {
    return this.isTokenValid();
  }
}
