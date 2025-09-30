package com.zluolan.zojbackendmodel.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 */
@Data
public class QuestionAddRequest implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 题目答案
     */
    private String answer;

    /**
     * 判题用例
     */
    private List<QuestionJudgeCase> judgeCase;

    /**
     * 判题配置
     */
    private QuestionJudgeConfig judgeConfig;

    private static final long serialVersionUID = 1L;
}