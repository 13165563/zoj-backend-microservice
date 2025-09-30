package com.zluolan.zojbackendjudgeservice.controller.inner;

import com.zluolan.zojbackendjudgeservice.service.JudgeService;
import com.zluolan.zojbackendmodel.entity.QuestionSubmit;
import com.zluolan.zojbackendmodel.vo.QuestionSubmitVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 内部判题服务接口
 */
@RestController
@RequestMapping("/inner")
public class JudgeInnerController {

    @Resource
    private JudgeService judgeService;

    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    @PostMapping("/do")
    public QuestionSubmitVO doJudge(@RequestParam("questionSubmitId") long questionSubmitId) {
        return judgeService.doJudge(questionSubmitId);
    }
}