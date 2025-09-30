package com.zluolan.zojbackendmodel.vo;

import cn.hutool.json.JSONUtil;
import com.zluolan.zojbackendmodel.dto.questionsubmit.JudgeInfo;
import com.zluolan.zojbackendmodel.entity.QuestionSubmit;
import com.zluolan.zojbackendmodel.enums.QuestionSubmitLanguageEnum;
import com.zluolan.zojbackendmodel.enums.QuestionSubmitStatusEnum;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目提交视图对象
 */
@Data
public class QuestionSubmitVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 代码字段（仅管理员可见）
     */
    private String code;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 编程语言文本描述
     */
    private String languageText;

    /**
     * 判题信息（json格式）
     */
    private JudgeInfo judgeInfo;

    /**
     * 判题信息文本描述
     */
    private String judgeInfoText;

    /**
     * 状态：0-待判题、1-判题中、2-成功、3-失败
     */
    private Integer status;

    /**
     * 状态文本描述
     */
    private String statusText;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 提交用户信息
     */
    private UserVO userVO;

    /**
     * 对应题目信息
     */
    private QuestionVO questionVO;

    /**
     * 包装类转对象
     *
     * @param questionSubmitVO
     * @return
     */
    public static QuestionSubmit voToObj(QuestionSubmitVO questionSubmitVO) {
        if (questionSubmitVO == null) {
            return null;
        }
        QuestionSubmit questionSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(questionSubmitVO, questionSubmit);
        JudgeInfo judgeInfo = questionSubmitVO.getJudgeInfo();
        if (judgeInfo != null) {
            questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        }
        return questionSubmit;
    }

    /**
     * 对象转包装类
     *
     * @param questionSubmit
     * @return
     */
    public static QuestionSubmitVO objToVo(QuestionSubmit questionSubmit) {
        if (questionSubmit == null) {
            return null;
        }
        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();
        BeanUtils.copyProperties(questionSubmit, questionSubmitVO);
        String judgeInfo = questionSubmit.getJudgeInfo();
        if (StringUtils.isNotBlank(judgeInfo)) {
            questionSubmitVO.setJudgeInfo(JSONUtil.toBean(judgeInfo, JudgeInfo.class));
        }
        // 转换状态文本描述
        Integer status = questionSubmit.getStatus();
        questionSubmitVO.setStatusText(QuestionSubmitStatusEnum.getEnumByValue(status).getText());
        // 转换编程语言文本描述
        String language = questionSubmit.getLanguage();
        questionSubmitVO.setLanguageText(QuestionSubmitLanguageEnum.getEnumByValue(language).getText());
        // 转换判题信息文本描述
        JudgeInfo judgeInfoObj = questionSubmitVO.getJudgeInfo();
        if (judgeInfoObj != null) {
            questionSubmitVO.setJudgeInfoText(JSONUtil.toJsonStr(judgeInfoObj));
        }
        return questionSubmitVO;
    }

    private static final long serialVersionUID = 1L;
}