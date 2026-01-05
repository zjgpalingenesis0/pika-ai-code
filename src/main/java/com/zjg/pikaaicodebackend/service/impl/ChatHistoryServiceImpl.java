package com.zjg.pikaaicodebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zjg.pikaaicodebackend.exception_.BusinessException;
import com.zjg.pikaaicodebackend.exception_.ThrowUtils;
import com.zjg.pikaaicodebackend.mapper.ChatHistoryMapper;
import com.zjg.pikaaicodebackend.model.dto.chatHistory.ChatHistoryAddRequest;
import com.zjg.pikaaicodebackend.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.zjg.pikaaicodebackend.model.entity.App;
import com.zjg.pikaaicodebackend.model.entity.ChatHistory;
import com.zjg.pikaaicodebackend.model.entity.User;
import com.zjg.pikaaicodebackend.model.enums_.MessageTypeEnum;
import com.zjg.pikaaicodebackend.service.AppService;
import com.zjg.pikaaicodebackend.service.ChatHistoryService;
import com.zjg.pikaaicodebackend.service.UserService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

import static com.zjg.pikaaicodebackend.exception_.ErrorCode.*;

/**
 * 对话历史 服务层实现。
 *
 * @author wanfeng
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;

    @Resource
    private UserService userService;

    @Override
    public Long saveMessage(ChatHistoryAddRequest chatHistoryAddRequest, Long userId) {
        // 1. 校验参数
        ThrowUtils.throwIf(userId == null || userId <= 0, PARAMS_ERROR);
        ThrowUtils.throwIf(chatHistoryAddRequest == null, PARAMS_ERROR, "请求参数为空");
        ThrowUtils.throwIf(StrUtil.hasBlank(chatHistoryAddRequest.getMessage()), PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(chatHistoryAddRequest.getAppId() == null || chatHistoryAddRequest.getAppId() <= 0,
                PARAMS_ERROR, "应用id不合法");

        // 2. 校验消息类型
        String messageType = chatHistoryAddRequest.getMessageType();
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, PARAMS_ERROR, "消息类型不合法");

        // 3. 校验应用是否存在
        App app = appService.getById(chatHistoryAddRequest.getAppId());
        ThrowUtils.throwIf(app == null, NOT_FOUND_ERROR, "应用不存在");

        // 4. 创建对话历史记录
//        ChatHistory chatHistory = new ChatHistory();
//        chatHistory.setMessage(chatHistoryAddRequest.getMessage());
//        chatHistory.setMessageType(messageType);
//        chatHistory.setAppId(chatHistoryAddRequest.getAppId());
//        chatHistory.setUserId(userId);

        ChatHistory chatHistory = ChatHistory.builder()
                .appId(chatHistoryAddRequest.getAppId())
                .messageType(messageTypeEnum.getValue())
                .message(chatHistoryAddRequest.getMessage())
                .userId(userId)
                .build();

        // 5. 保存到数据库
        boolean result = this.save(chatHistory);
        ThrowUtils.throwIf(!result, OPERATION_ERROR, "保存对话历史失败");

        return chatHistory.getId();
    }

    @Override
    public Page<ChatHistory> getChatHistoryByAppId(Long appId, long current, long pageSize, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, PARAMS_ERROR, "应用id不合法");
        ThrowUtils.throwIf(current <= 0, PARAMS_ERROR, "页码不合法");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 100, PARAMS_ERROR, "每页大小不合法");

        // 2. 校验应用是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, NOT_FOUND_ERROR, "应用不存在");

        // 3. 权限校验：只有应用创建者和管理员可以查看
        if (!app.getUserId().equals(loginUser.getId()) || !userService.isAdmin(loginUser)) {
            throw new BusinessException(NO_AUTH_ERROR, "无权查看该应用的对话历史");
        }

        // 4. 查询对话历史，按时间倒序，取最新的记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("app_id", appId)
                .orderBy("create_time", false);

        Page<ChatHistory> chatHistoryPage = this.page(Page.of(current, pageSize), queryWrapper);
        return chatHistoryPage;
    }

    @Override
    public Page<ChatHistory> listChatHistoryByPage(Long appId, LocalDateTime lastCreateTime, int pageSize, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, PARAMS_ERROR);
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, NOT_FOUND_ERROR);
        //验证权限
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, NOT_FOUND_ERROR, "应用不存在");
        if (!app.getUserId().equals(loginUser.getId()) || !userService.isAdmin(loginUser)) {
            throw new BusinessException(NO_AUTH_ERROR, "权限不够");
        }

        // 3. 构建查询条件
        ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setAppId(appId);
        chatHistoryQueryRequest.setLastCreateTime(lastCreateTime);
        
        QueryWrapper queryWrapper = getQueryWrapper(chatHistoryQueryRequest);

        // 4. 分页查询
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        // 1. 判空
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }

        // 2. 获取查询参数
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();

        // 3. 构建查询条件
        queryWrapper = queryWrapper.eq("id", id)
                .like("message", message)
                .eq("message_type", messageType)
                .eq("app_id", appId)
                .eq("user_id", userId);
        //游标逻辑查询  -  只用create_time作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("create_time", lastCreateTime);
        }
        // 4. 排序条件（默认按时间倒序）
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            //默认按照创建时间降序排列
            queryWrapper.orderBy("create_time", false);
        }

        return queryWrapper;
    }

    @Override
    public boolean deleteChatHistoryByAppId(Long appId) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, PARAMS_ERROR, "应用id不合法");

        // 2. 构建删除条件（逻辑删除）
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("app_id", appId);

        // 3. 执行删除
        boolean result = this.remove(queryWrapper);
        ThrowUtils.throwIf(!result, OPERATION_ERROR, "删除对话历史失败");
        return result;
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            //直接构造查询条件,起始点为1而不是0，用于排除最新的用户信息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            //反转列表，确保按照时间正序
            historyList = historyList.reversed();
            //按照时间顺序添加到记忆中
            int loadCount = 0;
            chatMemory.clear();  //清理历史缓存，防止重复加载
            for (ChatHistory chatHistory : historyList) {
                if (MessageTypeEnum.USER.getValue().equals(chatHistory.getMessageType())) {
                    chatMemory.add(UserMessage.from(chatHistory.getMessage()));
                    loadCount++;
                }
                else if (MessageTypeEnum.AI.getValue().equals(chatHistory.getMessageType())) {
                    chatMemory.add(AiMessage.from(chatHistory.getMessage()));
                    loadCount++;
                }
            }
            return loadCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            return 0;
        }


    }

}
