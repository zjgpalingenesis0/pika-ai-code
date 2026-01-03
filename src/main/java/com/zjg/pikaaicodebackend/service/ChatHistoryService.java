package com.zjg.pikaaicodebackend.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zjg.pikaaicodebackend.model.dto.chatHistory.ChatHistoryAddRequest;
import com.zjg.pikaaicodebackend.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.zjg.pikaaicodebackend.model.entity.ChatHistory;
import com.zjg.pikaaicodebackend.model.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层。
 *
 * @author wanfeng
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存对话消息
     * @param chatHistoryAddRequest 对话消息请求
     * @param userId 用户id
     * @return 消息id
     */
    Long saveMessage(ChatHistoryAddRequest chatHistoryAddRequest, Long userId);

    /**
     * 分页查询应用的对话历史（最新10条）
     * @param appId 应用id
     * @param current 当前页
     * @param pageSize 每页大小
     * @param loginUser 登录用户
     * @return 对话历史列表
     */
    Page<ChatHistory> getChatHistoryByAppId(Long appId, long current, long pageSize, User loginUser);

    /**
     * 分页查询对话历史  -  游标查询
     * @param appId
     * @param lastCreateTime
     * @param pageSize
     * @param loginUser
     * @return
     */
    Page<ChatHistory> listChatHistoryByPage(Long appId, LocalDateTime lastCreateTime, int pageSize, User loginUser);

    /**
     * 获取查询条件
     * @param chatHistoryQueryRequest 查询条件
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 删除应用的所有对话历史
     * @param appId 应用id
     * @return 是否成功
     */
    boolean deleteChatHistoryByAppId(Long appId);
}
