package com.zluolan.zojbackendserviceclient.service;

import com.zluolan.zojbackendmodel.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 题目提交服务Feign客户端
 */
@FeignClient(name = "zoj-backend-question-service", path = "/api/question/inner/question_submit")
public interface QuestionSubmitFeignClient {

    /**
     * 根据id获取题目提交记录
     *
     * @param questionSubmitId
     * @return
     */
    @GetMapping("/get/id")
    QuestionSubmit getById(@RequestParam("questionSubmitId") long questionSubmitId);

    /**
     * 更新题目提交信息
     *
     * @param questionSubmit
     * @return
     */
    @PostMapping("/update")
    boolean updateById(@RequestBody QuestionSubmit questionSubmit);
}