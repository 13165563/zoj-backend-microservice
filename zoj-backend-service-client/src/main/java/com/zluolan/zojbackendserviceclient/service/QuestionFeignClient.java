package com.zluolan.zojbackendserviceclient.service;

import com.zluolan.zojbackendmodel.entity.Question;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

/**
 * 题目服务Feign客户端
 */
@FeignClient(name = "zoj-backend-question-service", path = "/api/question/inner/question")
public interface QuestionFeignClient {

    /**
     * 根据id获取题目
     *
     * @param questionId
     * @return
     */
    @GetMapping("/get/id")
    Question getById(@RequestParam("questionId") long questionId);

    /**
     * 根据id列表获取题目列表
     *
     * @param idList
     * @return
     */
    @GetMapping("/get/ids")
    List<Question> listByIds(@RequestParam("idList") Collection<Long> idList);

    /**
     * 更新题目信息
     *
     * @param question
     * @return
     */
    @PostMapping("/update")
    boolean updateById(@RequestBody Question question);
}