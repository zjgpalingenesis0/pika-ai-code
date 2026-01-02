package com.zjg.pikaaicodebackend.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zjg.pikaaicodebackend.core.AiCodeGeneratorFacade;
import com.zjg.pikaaicodebackend.exception_.BusinessException;
import com.zjg.pikaaicodebackend.exception_.ErrorCode;
import com.zjg.pikaaicodebackend.exception_.ThrowUtils;
import com.zjg.pikaaicodebackend.model.dto.app.AppQueryRequest;
import com.zjg.pikaaicodebackend.model.entity.App;
import com.zjg.pikaaicodebackend.mapper.AppMapper;
import com.zjg.pikaaicodebackend.model.entity.User;
import com.zjg.pikaaicodebackend.model.enums_.CodeGenTypeEnum;
import com.zjg.pikaaicodebackend.model.vo.AppVO;
import com.zjg.pikaaicodebackend.model.vo.UserVO;
import com.zjg.pikaaicodebackend.service.AppService;
import com.zjg.pikaaicodebackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zjg.pikaaicodebackend.exception_.ErrorCode.*;

/**
 * 应用 服务层实现。
 *
 * @author wanfeng
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

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
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        //参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() <= 0, NOT_FOUND_ERROR, "用户不存在");
        //查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        //仅本人可以生成代码
        if (!loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(NO_AUTH_ERROR, "权限不够");
        }
        //获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getByValue(codeGenType);
        ThrowUtils.throwIf(codeGenTypeEnum == null, SYSTEM_ERROR, "不支持的代码生成类型");
        //调用AI生成代码
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }

}

