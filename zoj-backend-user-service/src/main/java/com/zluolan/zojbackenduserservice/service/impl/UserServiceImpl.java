package com.zluolan.zojbackenduserservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zluolan.zojbackendcommon.common.ErrorCode;
import com.zluolan.zojbackendcommon.constant.CommonConstant;
import com.zluolan.zojbackendcommon.exception.BusinessException;
import com.zluolan.zojbackendmodel.entity.User;
import com.zluolan.zojbackendmodel.dto.user.UserQueryRequest;
import com.zluolan.zojbackendmodel.enums.UserRoleEnum;
import com.zluolan.zojbackendmodel.vo.LoginUserVO;
import com.zluolan.zojbackendmodel.vo.UserVO;
import com.zluolan.zojbackenduserservice.mapper.UserMapper;
import com.zluolan.zojbackenduserservice.service.UserService;
import com.zluolan.zojbackendcommon.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.zluolan.zojbackendcommon.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "yupi";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复注册");
            }

            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.getOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.warn("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 修复日志调用
        log.info("User logged in successfully, userId: {}", user.getId());
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        // 单机锁
        synchronized (unionId.intern()) {
            // 查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("unionId", unionId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setUnionId(unionId);
                user.setMpOpenId(mpOpenId);
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            // 记录用户的登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            return getLoginUserVO(user);
        }
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 从请求头获取用户信息
        String userIdStr = request.getHeader("X-User-Id");
        
        if (userIdStr == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        try {
            Long userId = Long.parseLong(userIdStr);
            User currentUser = this.getById(userId);
            if (currentUser == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            return currentUser;
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
    }

    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 从请求头获取用户信息
        String userIdStr = request.getHeader("X-User-Id");
        
        if (userIdStr == null) {
            return null;
        }
        
        try {
            Long userId = Long.parseLong(userIdStr);
            return this.getById(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 从请求头获取用户信息
        String userIdStr = request.getHeader("X-User-Id");
        
        if (userIdStr == null) {
            return false;
        }
        
        try {
            Long userId = Long.parseLong(userIdStr);
            User user = this.getById(userId);
            return isAdmin(user);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 从 Cookie 获取用户信息
     * 注意：这是一个简化的实现，实际项目中需要根据 Session 配置来解析
     */
    public User getLoginUserByCookie(String cookie) {
        if (StringUtils.isBlank(cookie)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        // 从 Cookie 中解析 Session ID
        // 这里需要根据实际的 Session 配置来解析
        // 由于 Spring Session Redis 的复杂性，这里提供一个简化的实现
        
        // 尝试从 Redis 中获取用户信息
        // 注意：这是一个简化的实现，实际项目中需要更复杂的 Session 解析逻辑
        try {
            // 这里应该根据实际的 Session 配置来解析 Cookie
            // 由于 Spring Session Redis 的复杂性，暂时返回 null 表示未登录
            // 在实际项目中，需要实现完整的 Session 解析逻辑
            return null;
        } catch (Exception e) {
            log.error("解析 Cookie 失败", e);
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Cookie 解析失败");
        }
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public User getUserByAccount(String userAccount) {
        if (StringUtils.isBlank(userAccount)) {
            return null;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        return this.getOne(queryWrapper);
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), 
                StringUtils.isNotBlank(sortOrder) && sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}