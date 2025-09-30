package com.zluolan.zojbackendquestionservice.controller;

import com.zluolan.zojbackendcommon.common.BaseResponse;
import com.zluolan.zojbackendcommon.common.ResultUtils;
import com.zluolan.zojbackendmodel.entity.Question;
import com.zluolan.zojbackendquestionservice.service.QuestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private QuestionService questionService;

    /**
     * 测试数据库连接和题目数据
     */
    @GetMapping("/questions")
    public BaseResponse<List<Question>> testQuestions() {
        List<Question> questions = questionService.list();
        return ResultUtils.success(questions);
    }

    /**
     * 测试特定题目ID
     */
    @GetMapping("/question")
    public BaseResponse<Question> testQuestion() {
        Question question = questionService.getById(1957010078758363100L);
        return ResultUtils.success(question);
    }

    /**
     * 测试获取第一个题目
     */
    @GetMapping("/first-question")
    public BaseResponse<Question> testFirstQuestion() {
        List<Question> questions = questionService.list();
        if (questions.isEmpty()) {
            return ResultUtils.success(null);
        }
        Question firstQuestion = questions.get(0);
        return ResultUtils.success(firstQuestion);
    }

    /**
     * 测试获取题目数量
     */
    @GetMapping("/count")
    public BaseResponse<Long> testQuestionCount() {
        long count = questionService.count();
        return ResultUtils.success(count);
    }
}
