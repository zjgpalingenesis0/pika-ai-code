package com.zjg.pikaaicodebackend.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.zjg.pikaaicodebackend.annotation.AuthCheck;
import com.zjg.pikaaicodebackend.common_.BaseResponse;
import com.zjg.pikaaicodebackend.common_.ResultUtils;
import com.zjg.pikaaicodebackend.exception_.ThrowUtils;
import com.zjg.pikaaicodebackend.model.dto.chatHistory.ChatHistoryAddRequest;
import com.zjg.pikaaicodebackend.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.zjg.pikaaicodebackend.model.entity.ChatHistory;
import com.zjg.pikaaicodebackend.model.entity.User;
import com.zjg.pikaaicodebackend.service.ChatHistoryService;
import com.zjg.pikaaicodebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.zjg.pikaaicodebackend.constant.UserConstant.ADMIN_ROLE;

import static com.zjg.pikaaicodebackend.exception_.ErrorCode.*;

/**
 * 对话历史 控制层。
 *
 * @author wanfeng
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 保存对话消息
     *
     * @param chatHistoryAddRequest 对话消息请求
     * @param request               HTTP请求
     * @return 消息id
     */
    @PostMapping("/add")
    public BaseResponse<Long> saveMessage(@RequestBody ChatHistoryAddRequest chatHistoryAddRequest,
                                          HttpServletRequest request) {
        // 1. 校验参数
        ThrowUtils.throwIf(chatHistoryAddRequest == null, PARAMS_ERROR, "请求参数为空");

        // 2. 获取登录用户
        User loginUser = userService.getCurrentUser(request);

        // 3. 调用服务保存消息
        Long messageId = chatHistoryService.saveMessage(chatHistoryAddRequest, loginUser.getId());
        ThrowUtils.throwIf(messageId == null || messageId <= 0, OPERATION_ERROR, "保存消息失败");

        return ResultUtils.success(messageId);
    }

    /**
     * 查询应用的对话历史（最新10条）
     * 仅应用创建者和管理员可见
     *
     * @param chatHistoryQueryRequest 查询请求（包含appId）
     * @param request                 HTTP请求
     * @return 对话历史列表
     */
    @PostMapping("/list/byApp")
    public BaseResponse<Page<ChatHistory>> getChatHistoryByAppId(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest,
                                                                   HttpServletRequest request) {
        // 1. 校验参数
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, PARAMS_ERROR, "请求参数为空");
        ThrowUtils.throwIf(chatHistoryQueryRequest.getAppId() == null || chatHistoryQueryRequest.getAppId() <= 0,
                PARAMS_ERROR, "应用id不合法");

        // 2. 获取登录用户
        User loginUser = userService.getCurrentUser(request);

        // 3. 获取分页参数，默认取最新10条
        long current = chatHistoryQueryRequest.getCurrent();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        if (current <= 0) {
            current = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }

        // 4. 查询对话历史
        Page<ChatHistory> chatHistoryPage = chatHistoryService.getChatHistoryByAppId(
                chatHistoryQueryRequest.getAppId(),
                current,
                pageSize,
                loginUser
        );

        return ResultUtils.success(chatHistoryPage);
    }

    /**
     * 分页查询所有对话历史
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param request
     * @return
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listChatHistoryByPage(@PathVariable Long appId,
                                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                                 @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                                 HttpServletRequest request) {
        User loginUser = userService.getCurrentUser(request);

        Page<ChatHistory> chatHistoryPage = chatHistoryService.listChatHistoryByPage(appId, lastCreateTime, pageSize, loginUser);

        return ResultUtils.success(chatHistoryPage);
    }

    /**
     * 管理员可以查询所有对话历史
     * @param chatHistoryQueryRequest
     * @return
     */
    @PostMapping("/admin/list/page/all")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPage(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, PARAMS_ERROR);

        int current = chatHistoryQueryRequest.getCurrent();
        int pageSize = chatHistoryQueryRequest.getPageSize();

        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> chatHistoryPage = chatHistoryService.page(Page.of(current, pageSize), queryWrapper);

        return ResultUtils.success(chatHistoryPage);
    }

    /**
     * 删除应用的所有对话历史（管理员）
     *
     * @param chatHistoryQueryRequest 查询请求（包含appId）
     * @return 是否成功
     */
    @PostMapping("/delete/byApp")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> deleteChatHistoryByAppId(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        // 1. 校验参数
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, PARAMS_ERROR, "请求参数为空");
        ThrowUtils.throwIf(chatHistoryQueryRequest.getAppId() == null || chatHistoryQueryRequest.getAppId() <= 0,
                PARAMS_ERROR, "应用id不合法");

        // 2. 删除对话历史
        boolean result = chatHistoryService.deleteChatHistoryByAppId(chatHistoryQueryRequest.getAppId());
        ThrowUtils.throwIf(!result, OPERATION_ERROR, "删除对话历史失败");

        return ResultUtils.success(true, "删除成功");
    }
}
