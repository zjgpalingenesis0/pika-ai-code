package com.zjg.pikaaicodebackend.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zjg.pikaaicodebackend.model.dto.app.AppQueryRequest;
import com.zjg.pikaaicodebackend.model.entity.App;
import com.zjg.pikaaicodebackend.model.entity.User;
import com.zjg.pikaaicodebackend.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author wanfeng
 */
public interface AppService extends IService<App> {

    /**
     * 获取脱敏后应用信息
     * @param app 应用实体
     * @return 脱敏后应用信息
     */
    AppVO getAppVO(App app);

    /**
     * 获取脱敏后应用信息列表
     * @param appList 应用列表
     * @return 脱敏后应用信息列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取查询条件
     * @param appQueryRequest 查询条件
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 调用门面生成代码
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);
}
