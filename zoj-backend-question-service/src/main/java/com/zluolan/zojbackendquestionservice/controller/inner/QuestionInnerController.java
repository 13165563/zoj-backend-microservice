package com.zluolan.zojbackendquestionservice.controller.inner;

import com.zluolan.zojbackendmodel.entity.Question;
import com.zluolan.zojbackendquestionservice.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 题目内部服务接口
 * 供其他微服务调用
 */
@RestController
@RequestMapping("/inner/question")
public class QuestionInnerController {

    @Resource
    private QuestionService questionService;

    /**
     * 根据id获取题目
     *
     * @param questionId
     * @return
     */
    @GetMapping("/get/id")
    public Question getById(@RequestParam("questionId") long questionId) {
        return questionService.getById(questionId);
    }

    /**
     * 根据id列表获取题目列表
     *
     * @param idList
     * @return
     */
    @GetMapping("/get/ids")
    public List<Question> listByIds(@RequestParam("idList") Collection<Long> idList) {
        return questionService.listByIds(idList);
    }

    /**
     * 更新题目信息
     *
     * @param question
     * @return
     */
    @PostMapping("/update")
    public boolean updateById(@RequestBody Question question) {
        return questionService.updateById(question);
    }
}