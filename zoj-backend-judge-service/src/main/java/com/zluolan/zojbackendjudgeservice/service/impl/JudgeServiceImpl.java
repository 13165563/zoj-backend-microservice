package com.zluolan.zojbackendjudgeservice.service.impl;

import cn.hutool.json.JSONUtil;
import com.zluolan.zojbackendcommon.common.ErrorCode;
import com.zluolan.zojbackendcommon.exception.BusinessException;
import com.zluolan.zojbackendjudgeservice.utils.ApiClientUtil;
import com.zluolan.zojbackendmodel.dto.question.JudgeCase;
import com.zluolan.zojbackendmodel.dto.questionsubmit.JudgeInfo;
import com.zluolan.zojbackendmodel.entity.Question;
import com.zluolan.zojbackendmodel.entity.QuestionSubmit;
import com.zluolan.zojbackendmodel.enums.JudgeInfoMessageEnum;
import com.zluolan.zojbackendmodel.enums.QuestionSubmitStatusEnum;
import com.zluolan.zojbackendmodel.vo.QuestionSubmitVO;
import com.zluolan.zojbackendjudgeservice.service.JudgeService;
import com.zluolan.zojbackendserviceclient.service.QuestionFeignClient;
import com.zluolan.zojbackendserviceclient.service.QuestionSubmitFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionFeignClient questionFeignClient;
    
    @Resource
    private QuestionSubmitFeignClient questionSubmitFeignClient;
    
    @Value("${codesandbox.remote.base-url}")
    private String codeSandboxBaseUrl;

    /**
     * 异步执行判题服务（通过ID）
     * @param questionSubmitId 题目提交ID
     * @return 题目提交VO
     */
    @Override
    @Async
    public QuestionSubmitVO doJudge(long questionSubmitId) {
        // 1. 传入题目的提交id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionSubmitFeignClient.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2. 如果题目提交状态不为等待中，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3. 更改判题（题目提交）的状态为"判题中"，防止重复执行
        QuestionSubmit updateQuestionSubmit = new QuestionSubmit();
        updateQuestionSubmit.setId(questionSubmitId);
        updateQuestionSubmit.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitFeignClient.updateById(updateQuestionSubmit);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        // 4. 调用沙箱，获取到执行结果
        return doJudge(questionSubmit, question);
    }
    
    /**
     * 判题服务（通过对象）
     * @param questionSubmit 题目提交对象
     * @param question 题目对象
     * @return 题目提交VO
     */
    @Override
    public QuestionSubmitVO doJudge(QuestionSubmit questionSubmit, Question question) {
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        
        // 5. 调用沙箱，获取到执行结果
        // 从题目中获取判题用例（JSON格式的字符串）
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());

        // 直接调用远程代码沙箱
        String responseStr = ApiClientUtil.executeCodeWithSimpleAuth(codeSandboxBaseUrl, code, language, inputList);
        
        // 解析响应
        com.alibaba.fastjson.JSONObject responseJson = com.alibaba.fastjson.JSON.parseObject(responseStr);
        List<String> rawOutputList = responseJson.getJSONArray("outputList").toJavaList(String.class);
        
        // 处理输出，去除换行符和多余空格
        List<String> outputList = rawOutputList.stream()
                .map(output -> output.trim().replaceAll("\\n", ""))
                .collect(Collectors.toList());
        
        // 解析时间和内存信息
        Long time = null;
        Long memory = null;
        String message = responseJson.getString("message");
        
        // 尝试从judgeInfo对象中获取时间和内存信息
        if (responseJson.containsKey("judgeInfo")) {
            com.alibaba.fastjson.JSONObject judgeInfo = responseJson.getJSONObject("judgeInfo");
            time = judgeInfo.getLong("time");
            memory = judgeInfo.getLong("memory");
        } else {
            // 如果没有judgeInfo，尝试从根级别获取
            time = responseJson.getLong("time");
            memory = responseJson.getLong("memory");
        }
        
        log.info("代码沙箱响应解析 - 时间: {}ms, 内存: {}KB, 消息: {}", time, memory, message);
        log.info("原始输出列表: {}", rawOutputList);
        log.info("处理后输出列表: {}", outputList);
        log.info("判题用例列表: {}", judgeCaseList);
        
        // 6. 根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(time);
        judgeInfo.setMemory(memory);
        
        // 简单的判题逻辑
        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;
        
        // 判断沙箱输出数量与输入数量是否相等
        if (outputList.size() != inputList.size()) {
            log.warn("输出数量不匹配 - 期望: {}, 实际: {}", inputList.size(), outputList.size());
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
        } else {
            // 依次判断每一项输出和预期输出是否相等
            for (int i = 0; i < judgeCaseList.size(); i++) {
                JudgeCase judgeCase = judgeCaseList.get(i);
                String expectedOutput = judgeCase.getOutput();
                String actualOutput = outputList.get(i);
                log.info("用例 {} - 期望输出: '{}', 实际输出: '{}'", i, expectedOutput, actualOutput);
                if (!expectedOutput.equals(actualOutput)) {
                    log.warn("用例 {} 输出不匹配", i);
                    judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                    break;
                }
            }
        }
        
        log.info("判题结果: {}", judgeInfoMessageEnum.getValue());
        judgeInfo.setMessage(judgeInfoMessageEnum.getValue());
        
        // 7. 修改数据库中的判题结果
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmit.getId());
        questionSubmitUpdate.setQuestionId(questionSubmit.getQuestionId());
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCESS.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        boolean update = questionSubmitFeignClient.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        
        // 8. 返回脱敏信息
        QuestionSubmit questionSubmitResult = questionSubmitFeignClient.getById(questionSubmit.getId());
        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();
        return questionSubmitVO;
    }
}