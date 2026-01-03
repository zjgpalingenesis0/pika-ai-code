package com.zjg.pikaaicodebackend.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zjg.pikaaicodebackend.core.AiCodeGeneratorFacade;
import com.zjg.pikaaicodebackend.exception_.BusinessException;
import com.zjg.pikaaicodebackend.exception_.ErrorCode;
import com.zjg.pikaaicodebackend.exception_.ThrowUtils;
import com.zjg.pikaaicodebackend.model.dto.app.AppQueryRequest;
import com.zjg.pikaaicodebackend.model.dto.chatHistory.ChatHistoryAddRequest;
import com.zjg.pikaaicodebackend.model.entity.App;
import com.zjg.pikaaicodebackend.mapper.AppMapper;
import com.zjg.pikaaicodebackend.model.entity.User;
import com.zjg.pikaaicodebackend.model.enums_.CodeGenTypeEnum;
import com.zjg.pikaaicodebackend.model.enums_.MessageTypeEnum;
import com.zjg.pikaaicodebackend.model.vo.AppVO;
import com.zjg.pikaaicodebackend.model.vo.UserVO;
import com.zjg.pikaaicodebackend.service.AppService;
import com.zjg.pikaaicodebackend.service.ChatHistoryService;
import com.zjg.pikaaicodebackend.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zjg.pikaaicodebackend.constant.AppConstant.*;
import static com.zjg.pikaaicodebackend.exception_.ErrorCode.*;

/**
 * 应用 服务层实现。
 *
 * @author wanfeng
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Override
    public AppVO getAppVO(App app) {
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app, appVO);
        //关联用户查询信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }

        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollectionUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        //批量获取用户信息，避免N+1问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app ->{
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);

        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String appCover = appQueryRequest.getAppCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .like("app_name", appName)
                .like("app_cover", appCover)
                .like("init_prompt", initPrompt)
                .eq("priority", priority)
                .eq("user_id", userId)
                .eq("code_genType", codeGenType)
                .eq("deploy_key", deployKey)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, NOT_FOUND_ERROR, "用户不存在");

        //查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, NOT_FOUND_ERROR, "应用不存在");
        //验证用户是否有权限部署该应用，仅本人可以部署
        if (!loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(NO_AUTH_ERROR, "权限不够");
        }
        //检查是否有 部署标识
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        //获取代码生成类型,构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        //检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        //复制文件到部署目录
        String deployDirPath = CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        File deployDir = new File(deployDirPath);
        try {
            FileUtil.copyContent(sourceDir, deployDir, true);
        } catch (Exception e) {
            throw new BusinessException(SYSTEM_ERROR, "部署失败, " + e.getMessage());
        }
        //更新deployKey deploytime
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean result = this.updateById(updateApp);
        ThrowUtils.throwIf(!result, OPERATION_ERROR,"更新失败");
        //返回可访问的URL
        return String.format("%s/%s/", CODE_DEPLOY_HOST, deployKey);
    }

    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        //转为Long类型
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        try {
            //先删除对话历史
            chatHistoryService.deleteChatHistoryByAppId(appId);
        } catch (Exception e) {
            log.error("删除应用关联对话历史失败, {}", e.getMessage());
        }

        return super.removeById(id);
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        validateChatRequest(appId, message, loginUser);

        // 2. 查询应用信息并校验权限
        App app = getAppAndValidatePermission(appId, loginUser.getId());
        CodeGenTypeEnum codeGenTypeEnum = getCodeGenTypeEnum(app);

        // 3. 保存用户消息到对话历史
        saveUserMessage(app.getId(), message, loginUser.getId());

        // 4. 调用AI生成代码并处理响应
        return handleAiResponse(message, codeGenTypeEnum, appId, loginUser.getId());
    }

    /**
     * 校验聊天请求参数
     */
    private void validateChatRequest(Long appId, String message, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不合法");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() <= 0, NOT_FOUND_ERROR, "用户不存在");
    }

    /**
     * 获取应用并校验权限
     */
    private App getAppAndValidatePermission(Long appId, Long userId) {
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 仅本人可以生成代码
        if (!userId.equals(app.getUserId())) {
            throw new BusinessException(NO_AUTH_ERROR, "权限不够");
        }

        return app;
    }

    /**
     * 获取代码生成类型枚举
     */
    private CodeGenTypeEnum getCodeGenTypeEnum(App app) {
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getByValue(codeGenType);
        ThrowUtils.throwIf(codeGenTypeEnum == null, SYSTEM_ERROR, "不支持的代码生成类型");
        return codeGenTypeEnum;
    }

    /**
     * 保存用户消息到对话历史
     */
    private void saveUserMessage(Long appId, String message, Long userId) {
        ChatHistoryAddRequest userMessageRequest = ChatHistoryAddRequest.builder()
                .appId(appId)
                .message(message)
                .messageType(MessageTypeEnum.USER.getValue())
                .build();

        chatHistoryService.saveMessage(userMessageRequest, userId);
    }

    /**
     * 处理AI响应，包括流式返回和保存对话历史
     */
    private Flux<String> handleAiResponse(String message, CodeGenTypeEnum codeGenTypeEnum,
                                          Long appId, Long userId) {
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        StringBuilder responseBuilder = new StringBuilder();

        // 收集AI响应内容
        return contentFlux
                .doOnNext(responseBuilder::append)
                .doOnComplete(() -> {
                    // 流式响应完成，保存AI消息
                    String aiResponse = responseBuilder.toString();
                    if (StrUtil.isNotBlank(aiResponse)) {
                        saveAiMessage(appId, aiResponse, userId);
                    }
                })
                .doOnError(error -> {
                    // AI回复失败，记录错误信息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    saveAiErrorMessage(appId, errorMessage, userId);
                })
                .doOnCancel(() -> {
                    // 流被取消时也要保存已接收的内容
                    String partialResponse = responseBuilder.toString();
                    if (StrUtil.isNotBlank(partialResponse)) {
                        saveAiMessage(appId, partialResponse + " [流被中断]", userId);
                    }
                });
    }

    /**
     * 保存AI消息到对话历史
     */
    private void saveAiMessage(Long appId, String message, Long userId) {
        try {
            ChatHistoryAddRequest aiMessageRequest = ChatHistoryAddRequest.builder()
                    .appId(appId)
                    .message(message)
                    .messageType(MessageTypeEnum.AI.getValue())
                    .build();

            chatHistoryService.saveMessage(aiMessageRequest, userId);
        } catch (Exception e) {
            // 保存失败不影响主流程，只记录日志
            log.error("保存AI消息失败: " + e.getMessage());
        }
    }

    /**
     * 保存AI错误消息到对话历史
     */
    private void saveAiErrorMessage(Long appId, String errorMessage, Long userId) {
        try {
            ChatHistoryAddRequest errorRequest = ChatHistoryAddRequest.builder()
                    .appId(appId)
                    .message(errorMessage)
                    .messageType(MessageTypeEnum.AI.getValue())
                    .build();

            chatHistoryService.saveMessage(errorRequest, userId);
        } catch (Exception e) {
            // 保存失败不影响主流程
            System.err.println("保存错误消息失败: " + e.getMessage());
        }
    }


}

