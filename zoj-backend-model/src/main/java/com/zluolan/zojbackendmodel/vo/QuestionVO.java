package com.zluolan.zojbackendmodel.vo;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zluolan.zojbackendmodel.dto.question.QuestionJudgeCase;
import com.zluolan.zojbackendmodel.dto.question.QuestionJudgeConfig;
import com.zluolan.zojbackendmodel.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目封装类
 */
@Data
public class QuestionVO implements Serializable {

    /**
     * id
     */
    private Long id;

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
    @JsonIgnore
    private String answer;

    /**
     * 判题用例
     */
    @JsonIgnore
    private List<QuestionJudgeCase> judgeCase;

    /**
     * 判题配置
     */
    private QuestionJudgeConfig judgeConfig;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建题目人的信息
     */
    private UserVO userVO;

    /**
     * 包装类转对象
     *
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionVO, question);
        List<String> tagList = questionVO.getTags();
        if (tagList != null) {
            question.setTags(JSONUtil.toJsonStr(tagList));
        }
        QuestionJudgeConfig judgeConfig = questionVO.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        return question;
    }

    /**
     * 对象转包装类
     *
     * @param question
     * @return
     */
    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        String tags = question.getTags();
        if (StringUtils.isNotBlank(tags)) {
            questionVO.setTags(JSONUtil.toList(tags, String.class));
        }
        String judgeConfig = question.getJudgeConfig();
        if (StringUtils.isNotBlank(judgeConfig)) {
            questionVO.setJudgeConfig(JSONUtil.toBean(judgeConfig, QuestionJudgeConfig.class));
        }
        return questionVO;
    }

    private static final long serialVersionUID = 1L;
}