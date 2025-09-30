package com.zluolan.zojbackenduserservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zluolan.zojbackendcommon.annotation.AuthCheck;
import com.zluolan.zojbackendcommon.common.BaseResponse;
import com.zluolan.zojbackendcommon.common.DeleteRequest;
import com.zluolan.zojbackendcommon.common.ErrorCode;
import com.zluolan.zojbackendcommon.common.ResultUtils;
import com.zluolan.zojbackendcommon.config.WxOpenConfig;
import com.zluolan.zojbackendcommon.constant.UserConstant;
import com.zluolan.zojbackendcommon.exception.BusinessException;
import com.zluolan.zojbackendcommon.exception.ThrowUtils;
import com.zluolan.zojbackendmodel.dto.user.UserAddRequest;
import com.zluolan.zojbackendmodel.dto.user.UserLoginRequest;
import com.zluolan.zojbackendmodel.dto.user.UserQueryRequest;
import com.zluolan.zojbackendmodel.dto.user.UserRegisterRequest;
import com.zluolan.zojbackendmodel.dto.user.UserUpdateMyRequest;
import com.zluolan.zojbackendmodel.dto.user.UserUpdateRequest;
import com.zluolan.zojbackendmodel.entity.User;
import com.zluolan.zojbackendmodel.vo.LoginUserVO;
import com.zluolan.zojbackendmodel.vo.UserVO;
import com.zluolan.zojbackenduserservice.service.UserService;
import com.zluolan.zojbackendcommon.util.SimpleJwtTool;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zluolan.zojbackenduserservice.service.impl.UserServiceImpl.SALT;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private WxMpService wxMpService;

    @Resource
    private WxOpenConfig wxOpenConfig;

    @Resource
    private SimpleJwtTool simpleJwtTool;

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        // 修复日志调用
        log.info("用户登录成功，用户ID: {}, 用户账号: {}", loginUserVO.getId(), loginUserVO.getUserAccount());
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户登录（微信开放平台）
     *
     * @param code
     * @param request
     * @return
     */
    @GetMapping("/login/wx_open")
    public BaseResponse<LoginUserVO> userLoginByWxOpen(@RequestParam("code") String code, HttpServletRequest request) {
        try {
            WxOAuth2AccessToken accessToken;
            try {
                accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
            } catch (Exception e) {
                log.error("微信开放平台登录失败", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
            }
            WxOAuth2UserInfo userInfo = wxMpService.getOAuth2Service().getUserInfo(accessToken, null);
            LoginUserVO user = userService.userLoginByMpOpen(userInfo, request);
            return ResultUtils.success(user);
        } catch (WxErrorException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "微信登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录（JWT方式）
     *
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/login/jwt")
    public BaseResponse<Map<String, Object>> userLoginJwt(@RequestBody UserLoginRequest userLoginRequest) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 验证用户账号和密码
        User user = userService.getUserByAccount(userAccount);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        if (!encryptPassword.equals(user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        
        // 生成JWT令牌
        String token = simpleJwtTool.generateToken(user.getId(), user.getUserAccount(), user.getUserRole());
        
        // 返回用户信息和令牌
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userService.getLoginUserVO(user));
        
        log.info("JWT登录成功，用户ID: {}, 用户账号: {}", user.getId(), user.getUserAccount());
        return ResultUtils.success(result);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        // 从请求头获取用户信息
        String userIdStr = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");
        String userAccount = request.getHeader("X-User-Account");
        
        if (userIdStr == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        try {
            Long userId = Long.parseLong(userIdStr);
            User user = userService.getById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            return ResultUtils.success(userService.getLoginUserVO(user));
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
    }

    /**
     * 验证JWT令牌并获取用户信息
     *
     * @param token
     * @return
     */
    @PostMapping("/validate/jwt")
    public BaseResponse<Map<String, Object>> validateJwtToken(@RequestParam("token") String token) {
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "令牌不能为空");
        }
        
        try {
            // 验证令牌
            if (!simpleJwtTool.validateToken(token)) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "令牌无效");
            }
            
            // 获取用户信息
            Long userId = simpleJwtTool.getUserIdFromToken(token);
            String userAccount = simpleJwtTool.getUserAccountFromToken(token);
            String userRole = simpleJwtTool.getUserRoleFromToken(token);
            
            User user = userService.getById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户不存在");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("userAccount", userAccount);
            result.put("userRole", userRole);
            result.put("user", userService.getLoginUserVO(user));
            
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "令牌验证失败");
        }
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> userResponse = getUserById(id, request);
        User user = userResponse.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}