package com.xm.iipp.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.iipp.common.ErrorCode;
import com.xm.iipp.constant.CommonConstant;
import com.xm.iipp.exception.BusinessException;
import com.xm.iipp.exception.ThrowUtils;
import com.xm.iipp.mapper.QuestionBankQuestionMapper;
import com.xm.iipp.model.dto.questionBankQuestion.QuestionBankQuestionQueryRequest;
import com.xm.iipp.model.entity.Question;
import com.xm.iipp.model.entity.QuestionBank;
import com.xm.iipp.model.entity.QuestionBankQuestion;
import com.xm.iipp.model.entity.User;
import com.xm.iipp.model.vo.QuestionBankQuestionVO;
import com.xm.iipp.model.vo.UserVO;
import com.xm.iipp.service.QuestionBankQuestionService;
import com.xm.iipp.service.QuestionBankService;
import com.xm.iipp.service.QuestionService;
import com.xm.iipp.service.UserService;
import com.xm.iipp.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Questions List服务实现
 *
 * @author <a href="https://github.com/lixm">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Resource
    private UserService userService;

    @Resource
    private QuestionBankService questionBankService;

    @Resource
    @Lazy
    private QuestionService questionService;

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add                  对创建的数据进行校验
     */
    @Override
    public void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add) {
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.PARAMS_ERROR);

        //question and questionbank must be existed
        Long questionBankId = questionBankQuestion.getQuestionBankId();
        if (questionBankId != null) {
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "question bank is not exist");
        }
        Long questionId = questionBankQuestion.getQuestionId();
        if (questionId != null) {
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "question is not exist");
        }


        //no need to do validation
//        // todo 从对象中取值
//        String title = questionBankQuestion.getTitle();
//        // 创建数据时，参数不能为空
//        if (add) {
//            // todo 补充校验规则
//            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
//        }
//        // 修改数据时，有参数则校验
//        // todo 补充校验规则
//        if (StringUtils.isNotBlank(title)) {
//            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
//        }
    }

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = questionBankQuestionQueryRequest.getId();
        Long notId = questionBankQuestionQueryRequest.getNotId();
        String sortField = questionBankQuestionQueryRequest.getSortField();
        String sortOrder = questionBankQuestionQueryRequest.getSortOrder();
        Long userId = questionBankQuestionQueryRequest.getUserId();

        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        Long questionId = questionBankQuestionQueryRequest.getQuestionId();

        // todo 补充需要的查询条件

        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId), "questionBankId", questionBankId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取Questions List封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankQuestionVO questionBankQuestionVO = QuestionBankQuestionVO.objToVo(questionBankQuestion);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = questionBankQuestion.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionBankQuestionVO.setUser(userVO);

        // endregion

        return questionBankQuestionVO;
    }

    /**
     * 分页获取Questions List封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> questionBankQuestionList = questionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> questionBankQuestionVOPage = new Page<>(questionBankQuestionPage.getCurrent(), questionBankQuestionPage.getSize(), questionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(questionBankQuestionList)) {
            return questionBankQuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankQuestionVO> questionBankQuestionVOList = questionBankQuestionList.stream().map(questionBankQuestion -> {
            return QuestionBankQuestionVO.objToVo(questionBankQuestion);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionBankQuestionList.stream().map(QuestionBankQuestion::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        questionBankQuestionVOList.forEach(questionBankQuestionVO -> {
            Long userId = questionBankQuestionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionBankQuestionVO.setUser(userService.getUserVO(user));
        });
        // endregion

        questionBankQuestionVOPage.setRecords(questionBankQuestionVOList);
        return questionBankQuestionVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestionsToBank(List<Long> questionIdList, Long questionBankId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList), ErrorCode.PARAMS_ERROR, "题目列表为空");
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库非法");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 检查题目 id 是否存在
        List<Question> questionList = questionService.listByIds(questionIdList);
        // 合法的题目 id
        List<Long> validQuestionIdList = questionList.stream()
                .map(Question::getId)
                .collect(Collectors.toList());

        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionIdList), ErrorCode.PARAMS_ERROR, "合法的题目列表为空");

        // 检查哪些题目还不存在于题库中，避免重复插入
        LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .in(QuestionBankQuestion::getQuestionId, validQuestionIdList);
        List<QuestionBankQuestion> existQuestionList = this.list(lambdaQueryWrapper);
        // 已存在于题库中的题目 id
        Set<Long> existQuestionIdSet = existQuestionList.stream()
                .map(QuestionBankQuestion::getQuestionId)
                .collect(Collectors.toSet());
        // 已存在于题库中的题目 id，不需要再次添加
        validQuestionIdList = validQuestionIdList.stream().filter(questionId -> {
            return !existQuestionIdSet.contains(questionId);
        }).collect(Collectors.toList());
        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionIdList), ErrorCode.PARAMS_ERROR, "所有题目都已存在于题库中");

        // 检查题库 id 是否存在
        QuestionBank questionBank = questionBankService.getById(questionBankId);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        // 执行插入
        for (Long questionId : validQuestionIdList) {
            QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
            questionBankQuestion.setQuestionBankId(questionBankId);
            questionBankQuestion.setQuestionId(questionId);
            questionBankQuestion.setUserId(loginUser.getId());
            try {
                boolean result = this.save(questionBankQuestion);
                if (!result) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
                }
            } catch (DataIntegrityViolationException e) {
                log.error("数据库唯一键冲突或违反其他完整性约束，题目 id: {}, 题库 id: {}, 错误信息: {}",
                        questionId, questionBankId, e.getMessage());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目已存在于该题库，无法重复添加");
            } catch (DataAccessException e) {
                log.error("数据库连接问题、事务问题等导致操作失败，题目 id: {}, 题库 id: {}, 错误信息: {}",
                        questionId, questionBankId, e.getMessage());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
            } catch (Exception e) {
                // 捕获其他异常，做通用处理
                log.error("添加题目到题库时发生未知错误，题目 id: {}, 题库 id: {}, 错误信息: {}",
                        questionId, questionBankId, e.getMessage());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
            }

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveQuestionsFromBank(List<Long> questionIdList, Long questionBankId) {
        // 参数校验
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList), ErrorCode.PARAMS_ERROR, "题目列表为空");
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库非法");
        // 执行删除关联
        for (Long questionId : questionIdList) {
            // 构造查询
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .eq(QuestionBankQuestion::getQuestionId, questionId)
                    .eq(QuestionBankQuestion::getQuestionBankId, questionBankId);
            boolean result = this.remove(lambdaQueryWrapper);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "从题库移除题目失败");
            }
        }
    }


}
