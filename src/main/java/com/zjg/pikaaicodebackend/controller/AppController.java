package com.zjg.pikaaicodebackend.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.zjg.pikaaicodebackend.annotation.AuthCheck;
import com.zjg.pikaaicodebackend.common_.BaseResponse;
import com.zjg.pikaaicodebackend.common_.DeleteRequest;
import com.zjg.pikaaicodebackend.common_.ResultUtils;
import com.zjg.pikaaicodebackend.exception_.BusinessException;
import com.zjg.pikaaicodebackend.exception_.ThrowUtils;
import com.zjg.pikaaicodebackend.model.dto.app.*;
import com.zjg.pikaaicodebackend.model.entity.App;
import com.zjg.pikaaicodebackend.model.entity.User;
import com.zjg.pikaaicodebackend.model.enums_.CodeGenTypeEnum;
import com.zjg.pikaaicodebackend.model.vo.AppVO;
import com.zjg.pikaaicodebackend.service.AppService;
import com.zjg.pikaaicodebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.zjg.pikaaicodebackend.constant.AppConstant.GOOD_APP_PRIORITY;
import static com.zjg.pikaaicodebackend.constant.UserConstant.ADMIN_ROLE;
import static com.zjg.pikaaicodebackend.exception_.ErrorCode.*;

/**
 * 应用 控制层。
 *
 * @author wanfeng
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 【用户】创建应用
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        //参数校验
        ThrowUtils.throwIf(appAddRequest == null, PARAMS_ERROR);
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), PARAMS_ERROR, "initPrompt不能为空");
        //获取当前用户
        User loginUser = userService.getCurrentUser(request);
        ThrowUtils.throwIf(loginUser == null, NOT_FOUND_ERROR, "用户不存在");
        //构造入库对象
        App app = new App();
        BeanUtils.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        //先暂时设置生成代码类型为  多文件形式
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        //设置应用名称为initPrompt前12位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        //在数据库中保存
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, OPERATION_ERROR, "创建应用失败");

        return ResultUtils.success(app.getId());
    }

    /**
     * 【用户】根据 id 修改自己的应用（目前只支持修改应用名称）
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        //参数校验
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, PARAMS_ERROR);
        //获取当前用户
        User loginUser = userService.getCurrentUser(request);
        ThrowUtils.throwIf(loginUser == null, NOT_FOUND_ERROR, "用户不存在");
        //获取原始应用信息
        App oldApp = appService.getById(appUpdateRequest.getId());
        ThrowUtils.throwIf(oldApp == null, NOT_FOUND_ERROR, "应用不存在");
        ThrowUtils.throwIf(!oldApp.getUserId().equals(loginUser.getId()), OPERATION_ERROR, "无权修改该应用");
        //设置修改后应用信息
        App app = new App();
        app.setId(appUpdateRequest.getId());
        app.setAppName(appUpdateRequest.getAppName());
        //修改应用信息
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, OPERATION_ERROR, "更新应用失败");

        return ResultUtils.success(true);
    }

    /**
     * 【用户】根据 id 删除自己的应用
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        //参数校验
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, PARAMS_ERROR);
        //获取登录用户信息
        User loginUser = userService.getCurrentUser(request);
        //找到对应应用信息
        App app = appService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(app == null, NOT_FOUND_ERROR, "应用不存在");
        //必须是创建者或者管理员才能删除
        if (!app.getUserId().equals(loginUser.getId()) || !userService.isAdmin(loginUser)) {
            throw new BusinessException(NO_AUTH_ERROR, "无权删除该应用");
        }

        //执行删除操作
        boolean result = appService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, OPERATION_ERROR, "删除应用失败");

        return ResultUtils.success(true);
    }

    /**
     * 【用户】根据 id 查看应用详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(Long id) {
        //参数校验
        ThrowUtils.throwIf(id == null || id <= 0, PARAMS_ERROR);
        //查询应用信息
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, NOT_FOUND_ERROR, "应用不存在");
        AppVO appVO = appService.getAppVO(app);
        return ResultUtils.success(appVO);
    }

    /**
     * 【用户】分页查询自己的应用列表（支持根据名称查询，每页最多 20 个）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<AppVO>> listAppVOByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        //参数校验
        ThrowUtils.throwIf(appQueryRequest == null, PARAMS_ERROR);
        //获取当前用户信息
        User loginUser = userService.getCurrentUser(request);
        //获取分页信息
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, PARAMS_ERROR, "每页最多20条");
        //只查询当前用户的应用
        appQueryRequest.setUserId(loginUser.getId());
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(current, pageSize), queryWrapper);
        //数据封装
        Page<AppVO> appVOPage = new Page<>(current, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);

        return ResultUtils.success(appVOPage);
    }

    /**
     * 【用户】分页查询精选的应用列表（支持根据名称查询，每页最多 20 个）
     */
    @PostMapping("/list/good/page/vo")
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
        //参数校验
        ThrowUtils.throwIf(appQueryRequest == null, PARAMS_ERROR);
        //获取分页参数
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, PARAMS_ERROR, "每页最多20条");
        //只查询精选的应用
        appQueryRequest.setPriority(GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        //分页查询
        Page<App> appPage = appService.page(Page.of(current, pageSize), queryWrapper);
        //数据封装
        Page<AppVO> appVOPage = new Page<>(current, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);

        return ResultUtils.success(appVOPage);
    }

    /**
     * 【管理员】根据 id 删除任意应用
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, PARAMS_ERROR);

        //判断是否存在
        Long appId = deleteRequest.getId();
        App oldApp = appService.getById(appId);
        ThrowUtils.throwIf(oldApp == null, NOT_FOUND_ERROR, "应用不存在");
        //执行删除
        boolean result = appService.removeById(appId);
        ThrowUtils.throwIf(!result, OPERATION_ERROR, "删除应用失败");

        return ResultUtils.success(true);
    }

    /**
     * 【管理员】根据 id 更新任意应用（支持更新应用名称、应用封面、优先级）
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppUpdateByAdminRequest appUpdateByAdminRequest) {
        ThrowUtils.throwIf(appUpdateByAdminRequest == null || appUpdateByAdminRequest.getId() == null, PARAMS_ERROR);

        //判断是否存在
        Long appId = appUpdateByAdminRequest.getId();
        App oldApp = appService.getById(appId);
        ThrowUtils.throwIf(oldApp == null, NOT_FOUND_ERROR, "应用不存在");
        //创建修改后应用对象
        App app = new App();
        BeanUtils.copyProperties(appUpdateByAdminRequest, app);
        //执行更新
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, OPERATION_ERROR, "更新应用失败");

        return ResultUtils.success(true);
    }

    /**
     * 【管理员】分页查询应用列表（支持根据除时间外的任何字段查询，每页数量不限）
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, PARAMS_ERROR);

        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();

        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);

        Page<App> appPage = appService.page(Page.of(current, pageSize), queryWrapper);
        Page<AppVO> appVOPage = new Page<>(current, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);

        return ResultUtils.success(appVOPage);
    }

    /**
     * 【管理员】根据 id 查看应用详情
     */
    @GetMapping("/admin/get")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<AppVO> getAppVOByIdByAdmin(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, PARAMS_ERROR);

        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, NOT_FOUND_ERROR, "应用不存在");

        AppVO appVO = appService.getAppVO(app);
        return ResultUtils.success(appVO);
    }

    /**
     * 应用聊天生成代码
     * @param appId
     * @param message
     * @param request
     * @return
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(Long appId, String message, HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(message), PARAMS_ERROR);

        User loginUser = userService.getCurrentUser(request);

        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
        //转换成ServerSentEvent格式
        return contentFlux.map(chunk -> {
           //将内容包装成JSON对象
            Map<String, String> wrapper = Map.of("d", chunk);
            String jsonData = JSONUtil.toJsonStr(wrapper);
            return ServerSentEvent.<String>builder()
                    .data(jsonData)
                    .build();
        }).concatWith(Mono.just(
                //发送结束事件
                ServerSentEvent.<String>builder()
                        .data("")
                        .event("done")
                        .build()
        ));
    }

    @PostMapping("deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, PARAMS_ERROR);
        Long appId = appDeployRequest.getId();
        ThrowUtils.throwIf(appId == null || appId <= 0, PARAMS_ERROR);

        User loginUser = userService.getCurrentUser(request);
        //调用服务部署应用
        String url = appService.deployApp(appId, loginUser);

        return ResultUtils.success(url);
    }

}
