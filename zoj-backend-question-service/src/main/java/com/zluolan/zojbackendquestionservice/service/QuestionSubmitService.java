package com.zluolan.zojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zluolan.zojbackendmodel.dto.questionsubmit.QuestionSubmitAddRequest;
import com.zluolan.zojbackendmodel.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.zluolan.zojbackendmodel.entity.QuestionSubmit;
import com.zluolan.zojbackendmodel.entity.User;
import com.zluolan.zojbackendmodel.vo.QuestionSubmitVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 题目提交服务接口
 */
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest 提交题目请求
     * @param loginUser                登录用户
     * @return 题目提交记录的 id
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest 查询请求参数
     * @return 查询条件构造器
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    /**
     * 获取题目封装类
     *
     * @param questionSubmit 提交题目实体
     * @param loginUser      当前登录用户
     * @return 题目提交封装类
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);

    /**
     * 获取题目提交分页封装类
     *
     * @param questionSubmitPage 题目提交分页实体
     * @param loginUser          登录用户
     * @return 题目提交分页封装类
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser);
}