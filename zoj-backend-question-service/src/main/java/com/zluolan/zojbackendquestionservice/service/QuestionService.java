package com.zluolan.zojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zluolan.zojbackendmodel.dto.question.QuestionQueryRequest;
import com.zluolan.zojbackendmodel.entity.Question;
import com.zluolan.zojbackendmodel.vo.QuestionVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 题目服务接口
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验题目参数
     * @param question 题目信息
     * @param add 是否为添加操作
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取题目封装类
     * @param question 题目实体
     * @param request 请求信息
     * @return 题目封装类
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 获取题目分页封装类
     * @param questionPage 题目分页实体
     * @param request 请求信息
     * @return 题目分页封装类
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);

    /**
     * 获取查询条件
     * @param questionQueryRequest 查询请求参数
     * @return 查询条件构造器
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);
}