package com.zluolan.zojbackendquestionservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zluolan.zojbackendcommon.common.ErrorCode;
import com.zluolan.zojbackendcommon.exception.BusinessException;
import com.zluolan.zojbackendcommon.exception.ThrowUtils;
import com.zluolan.zojbackendmodel.dto.questionsubmit.QuestionSubmitAddRequest;
import com.zluolan.zojbackendmodel.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.zluolan.zojbackendmodel.entity.Question;
import com.zluolan.zojbackendmodel.entity.QuestionSubmit;
import com.zluolan.zojbackendmodel.entity.User;
import com.zluolan.zojbackendmodel.vo.UserVO;
import com.zluolan.zojbackendmodel.enums.QuestionSubmitStatusEnum;
import com.zluolan.zojbackendmodel.vo.LoginUserVO;
import com.zluolan.zojbackendmodel.vo.QuestionSubmitVO;
import com.zluolan.zojbackendmodel.vo.QuestionVO;
import com.zluolan.zojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.zluolan.zojbackendquestionservice.service.QuestionSubmitService;
import com.zluolan.zojbackendcommon.utils.SqlUtils;
import com.zluolan.zojbackendserviceclient.service.QuestionFeignClient;
import com.zluolan.zojbackendserviceclient.service.UserFeignClient;
import com.zluolan.zojbackendquestionservice.mq.MyMessageProducer;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目提交服务实现类
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    private static final Logger log = LoggerFactory.getLogger(QuestionSubmitServiceImpl.class);

    @Resource
    private QuestionFeignClient questionFeignClient;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private MyMessageProducer myMessageProducer;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest 提交题目请求
     * @param loginUser                登录用户
     * @return 题目提交记录的 id
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        long questionId = questionSubmitAddRequest.getQuestionId();
        Question question = questionFeignClient.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已提交题目
        long userId = loginUser.getId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(questionSubmitAddRequest.getLanguage());
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        // 执行判题服务
        // 发送消息（只发送题目提交ID）
        myMessageProducer.sendMessage(String.valueOf(questionSubmit.getId()));
        return questionSubmit.getId();
    }

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest 查询请求参数
     * @return 查询条件构造器
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        // 排序
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals("asc"), sortField);
        return queryWrapper;
    }

    /**
     * 获取题目封装类
     *
     * @param questionSubmit 提交题目实体
     * @param loginUser      当前登录用户
     * @return 题目提交封装类
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 关联查询用户信息
        Long userId = questionSubmit.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userFeignClient.getById(userId);
        }
        UserVO userVO = userFeignClient.getUserVO(user);
        questionSubmitVO.setUserVO(userVO);
        // 关联查询题目信息
        Long questionId = questionSubmit.getQuestionId();
        Question question = null;
        if (questionId != null && questionId > 0) {
            question = questionFeignClient.getById(questionId);
        }
        QuestionVO questionVO = QuestionVO.objToVo(question);
        questionSubmitVO.setQuestionVO(questionVO);
        // 脱敏：仅本人和管理员能看见自己（提交 userId 和登录用户 id 相同）提交的代码
        log.debug("权限检查 - 登录用户ID: {}, 提交用户ID: {}, 登录用户: {}", 
                loginUser != null ? loginUser.getId() : "null", 
                questionSubmit.getUserId(), 
                loginUser);
        
        boolean isOwner = loginUser != null && loginUser.getId().equals(questionSubmit.getUserId());
        boolean isAdmin = false;
        if (loginUser != null) {
            try {
                isAdmin = userFeignClient.isAdmin(loginUser);
                log.debug("管理员检查结果: {}", isAdmin);
            } catch (Exception e) {
                log.error("检查管理员权限时出错", e);
                isAdmin = false;
            }
        }
        
        log.debug("权限判断 - 是否本人: {}, 是否管理员: {}, 登录用户ID: {}, 提交用户ID: {}", 
                isOwner, isAdmin, 
                loginUser != null ? loginUser.getId() : "null", 
                questionSubmit.getUserId());
        
        if (loginUser == null || (!isOwner && !isAdmin)) {
            log.debug("代码被脱敏 - 登录用户为空或权限不足");
            questionSubmitVO.setCode(null);
        } else {
            log.debug("代码保留 - 用户有权限查看");
        }
        return questionSubmitVO;
    }

    /**
     * 获取题目提交分页封装类
     *
     * @param questionSubmitPage 题目提交分页实体
     * @param loginUser          登录用户
     * @return 题目提交分页封装类
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollUtil.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        // 关联查询用户信息
        Set<Long> userIdSet = questionSubmitList.stream().map(QuestionSubmit::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userFeignClient.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 关联查询题目信息
        Set<Long> questionIdSet = questionSubmitList.stream().map(QuestionSubmit::getQuestionId).collect(Collectors.toSet());
        Map<Long, List<Question>> questionIdQuestionListMap = questionFeignClient.listByIds(questionIdSet).stream()
                .collect(Collectors.groupingBy(Question::getId));
        // 填充信息
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> {
            QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
            Long userId = questionSubmit.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionSubmitVO.setUserVO(userFeignClient.getUserVO(user));
            Long questionId = questionSubmit.getQuestionId();
            Question question = null;
            if (questionIdQuestionListMap.containsKey(questionId)) {
                question = questionIdQuestionListMap.get(questionId).get(0);
            }
            questionSubmitVO.setQuestionVO(QuestionVO.objToVo(question));
            // 脱敏：仅本人和管理员能看见自己（提交 userId 和登录用户 id 相同）提交的代码
            if (loginUser.getId() != questionSubmit.getUserId() && !userFeignClient.isAdmin(loginUser)) {
                questionSubmitVO.setCode(null);
            }
            return questionSubmitVO;
        }).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest 提交题目请求
     * @param request                  请求
     * @return 题目提交记录的 id
     */
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");
        
        if (userIdStr == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        try {
            Long userId = Long.parseLong(userIdStr);
            User user = new User();
            user.setId(userId);
            user.setUserRole(userRole);
            return doQuestionSubmit(questionSubmitAddRequest, user);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
    }
}