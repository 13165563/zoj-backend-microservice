package com.zluolan.zojbackendmodel.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 题目判题配置
 */
@Data
public class JudgeConfig implements Serializable {

    /**
     * 时间限制（ms）
     */
    private Long timeLimit;

    /**
     * 内存限制（KB）
     */
    private Long memoryLimit;

    /**
     * 堆栈限制（KB）
     */
    private Long stackLimit;

    private static final long serialVersionUID = 1L;
}