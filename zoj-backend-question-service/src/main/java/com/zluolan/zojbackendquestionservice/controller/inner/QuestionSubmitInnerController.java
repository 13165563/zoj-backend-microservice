package com.zluolan.zojbackendquestionservice.controller.inner;

import com.zluolan.zojbackendmodel.entity.QuestionSubmit;
import com.zluolan.zojbackendquestionservice.service.QuestionSubmitService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 题目提交内部服务接口
 * 供其他微服务调用
 */
@RestController
@RequestMapping("/inner/question_submit")
public class QuestionSubmitInnerController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    /**
     * 根据id获取题目提交记录
     *
     * @param questionSubmitId
     * @return
     */
    @GetMapping("/get/id")
    public QuestionSubmit getById(@RequestParam("questionSubmitId") long questionSubmitId) {
        return questionSubmitService.getById(questionSubmitId);
    }

    /**
     * 更新题目提交信息
     *
     * @param questionSubmit
     * @return
     */
    @PostMapping("/update")
    public boolean updateById(@RequestBody QuestionSubmit questionSubmit) {
        return questionSubmitService.updateById(questionSubmit);
    }
}