package com.zluolan.zojbackendjudgeservice.controller;

import com.zluolan.zojbackendcommon.common.BaseResponse;
import com.zluolan.zojbackendcommon.common.ResultUtils;
import com.zluolan.zojbackendjudgeservice.service.JudgeService;
import com.zluolan.zojbackendmodel.vo.QuestionSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 判题服务接口
 */
@RestController
@RequestMapping("/judge")
@Slf4j
public class JudgeController {

    @Resource
    private JudgeService judgeService;

    /**
     * 根据题目提交ID执行判题
     *
     * @param questionSubmitId 题目提交ID
     * @return 题目提交VO
     */
    @GetMapping("/do")
    public BaseResponse<QuestionSubmitVO> doJudge(@RequestParam("questionSubmitId") long questionSubmitId) {
        QuestionSubmitVO questionSubmitVO = judgeService.doJudge(questionSubmitId);
        return ResultUtils.success(questionSubmitVO);
    }
}
