package com.zluolan.zojbackendserviceclient.service;

import com.zluolan.zojbackendmodel.entity.Question;
import com.zluolan.zojbackendmodel.entity.QuestionSubmit;
import com.zluolan.zojbackendmodel.vo.QuestionSubmitVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 判题服务Feign客户端
 */
@FeignClient(name = "zoj-backend-judge-service", path = "/api/judge/inner")
public interface JudgeFeignClient {

    /**
     * 根据题目提交ID执行判题
     *
     * @param questionSubmitId 题目提交ID
     * @return 题目提交VO
     */
    @GetMapping("/do")
    QuestionSubmitVO doJudge(@RequestParam("questionSubmitId") long questionSubmitId);

    /**
     * 根据题目和提交信息执行判题
     *
     * @param questionSubmit 题目提交对象
     * @param question 题目对象
     * @return 题目提交VO
     */
    @PostMapping("/do/question")
    QuestionSubmitVO doJudge(@RequestBody QuestionSubmit questionSubmit, @RequestBody Question question);
}