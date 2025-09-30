package com.zluolan.zojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zluolan.zojbackendcommon.annotation.AuthCheck;
import com.zluolan.zojbackendcommon.common.BaseResponse;
import com.zluolan.zojbackendcommon.common.ErrorCode;
import com.zluolan.zojbackendcommon.common.ResultUtils;
import com.zluolan.zojbackendcommon.constant.UserConstant;
import com.zluolan.zojbackendcommon.exception.BusinessException;
import com.zluolan.zojbackendmodel.dto.questionsubmit.QuestionSubmitAddRequest;
import com.zluolan.zojbackendmodel.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.zluolan.zojbackendmodel.entity.QuestionSubmit;
import com.zluolan.zojbackendmodel.entity.User;
import com.zluolan.zojbackendmodel.vo.LoginUserVO;
import com.zluolan.zojbackendmodel.vo.QuestionSubmitVO;
import com.zluolan.zojbackendquestionservice.service.QuestionSubmitService;
import com.zluolan.zojbackendserviceclient.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserFeignClient userFeignClient;
    
    /**
     * 从请求头获取用户ID
     */
    private Long getUserIdFromHeader(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
    }

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交记录的 id
     */
    @PostMapping("/")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                               HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能提交
        Long userId = getUserIdFromHeader(request);
        User user = new User();
        user.setId(userId);
        long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, user);
        return ResultUtils.success(questionSubmitId);
    }

    /**
     * 分页获取题目提交列表（仅管理员）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionSubmit>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 限制爬虫
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        return ResultUtils.success(questionSubmitPage);
    }

    /**
     * 分页获取题目提交封装列表
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitVOByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                           HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 限制爬虫
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户
        Long userId = getUserIdFromHeader(request);
        log.debug("从请求头获取的用户ID: {}", userId);
        
        User loginUser = null;
        if (userId != null) {
            // 从数据库获取完整的用户信息
            loginUser = userFeignClient.getById(userId);
            log.debug("从数据库获取的用户信息: {}", loginUser);
        }
        
        // 权限控制：普通用户只能查看自己的提交，管理员可以查看所有人的提交
        if (loginUser != null && !userFeignClient.isAdmin(loginUser)) {
            // 普通用户：只能查看自己的提交
            questionSubmitQueryRequest.setUserId(loginUser.getId());
            log.debug("普通用户权限控制 - 只查看自己的提交，用户ID: {}", loginUser.getId());
        } else if (loginUser != null) {
            log.debug("管理员权限 - 可以查看所有人的提交");
        } else {
            log.debug("未登录用户 - 无法查看提交记录");
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
    }

    /**
     * 根据ID获取题目提交详情
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionSubmitVO> getQuestionSubmitVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 获取提交记录
        QuestionSubmit questionSubmit = questionSubmitService.getById(id);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        
        // 获取当前登录用户
        Long userId = getUserIdFromHeader(request);
        log.debug("详情接口 - 从请求头获取的用户ID: {}", userId);
        
        User loginUser = null;
        if (userId != null) {
            // 从数据库获取完整的用户信息
            loginUser = userFeignClient.getById(userId);
            log.debug("详情接口 - 从数据库获取的用户信息: {}", loginUser);
        }
        
        QuestionSubmitVO questionSubmitVO = questionSubmitService.getQuestionSubmitVO(questionSubmit, loginUser);
        return ResultUtils.success(questionSubmitVO);
    }
}