package com.zluolan.zojbackendjudgeservice.service;

import com.zluolan.zojbackendmodel.entity.Question;
import com.zluolan.zojbackendmodel.entity.QuestionSubmit;
import com.zluolan.zojbackendmodel.vo.QuestionSubmitVO;

/**
 * 判题服务
 */
public interface JudgeService {

    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    QuestionSubmitVO doJudge(long questionSubmitId);

    /**
     * 判题
     * @param questionSubmit
     * @param question
     * @return
     */
    QuestionSubmitVO doJudge(QuestionSubmit questionSubmit, Question question);
}