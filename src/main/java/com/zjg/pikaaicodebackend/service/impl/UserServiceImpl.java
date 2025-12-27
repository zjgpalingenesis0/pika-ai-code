package com.zjg.pikaaicodebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zjg.pikaaicodebackend.exception_.BusinessException;
import com.zjg.pikaaicodebackend.exception_.ThrowUtils;
import com.zjg.pikaaicodebackend.model.dto.user.UserQueryRequest;
import com.zjg.pikaaicodebackend.model.entity.User;
import com.zjg.pikaaicodebackend.mapper.UserMapper;
import com.zjg.pikaaicodebackend.model.vo.LoginUserVO;
import com.zjg.pikaaicodebackend.model.vo.UserVO;
import com.zjg.pikaaicodebackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

import static com.zjg.pikaaicodebackend.constant.UserConstant.USER_LOGIN_STATE;
import static com.zjg.pikaaicodebackend.exception_.ErrorCode.*;
import static com.zjg.pikaaicodebackend.model.enums_.UserRoleEnum.ADMIN;
import static javax.management.Query.eq;

/**
 * 用户 服务层实现。
 *
 * @author wanfeng
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(PARAMS_ERROR, "输入不能为空");
        }
        ThrowUtils.throwIf(userAccount.length() < 4, PARAMS_ERROR, "账号太短");
        ThrowUtils.throwIf(userPassword.length() < 6, PARAMS_ERROR, "密码太短");
        ThrowUtils.throwIf(checkPassword.length() < 6, PARAMS_ERROR, "检验码太短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), PARAMS_ERROR, "密码确认失败");
        //检查用户账号是否和数据库中已有的重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_account", userAccount);
        long result = this.mapper.selectCountByQuery(queryWrapper);
        ThrowUtils.throwIf(result > 0, PARAMS_ERROR, "账号重复，重新输入");
        //密码一定要加密
        String encryptPassword = getEncryptPassword(userPassword);
        //插入数据到数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
//        user.setUserName("bro1");
//        user.setUserRole(UserRoleEnum.ADMIN.getValue());

        boolean result2 = this.save(user);
        ThrowUtils.throwIf(!result2, SYSTEM_ERROR, "注册失败,数据库错误");

        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), PARAMS_ERROR, "登录信息不全");
        ThrowUtils.throwIf(userAccount.length() < 4, PARAMS_ERROR, "账号输入太短");
        ThrowUtils.throwIf(userPassword.length() < 6, PARAMS_ERROR, "密码太短");
        //对用户传递的密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        //查询数据库中的用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User loginUser = this.mapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(loginUser == null, SYSTEM_ERROR, "用户不存在");
        //保存用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, loginUser);


        return getLoginUserVO(loginUser);
    }

    @Override
    public User getCurrentUser(HttpServletRequest request) {
//        //获取用户登录态
//        Object obj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User currentUser = (User) obj;
//        //判断是否登录
//        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null,
//                NOT_LOGIN_ERROR, "用户未登录");
        //判断是否登录
        User currentUser = isLogin(request);
        //从数据库中查询
        User loginUser = this.getById(currentUser.getId());
        ThrowUtils.throwIf(loginUser == null, NOT_FOUND_ERROR, "用户不存在");
        //登录，返回用户
        return loginUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        //判断是否登录
        isLogin(request);
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }


    @Override
    public String getEncryptPassword(String userPassword) {
        ThrowUtils.throwIf(StrUtil.hasBlank(userPassword), PARAMS_ERROR, "密码为空");
        final String SALT = "aiCode";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public LoginUserVO getLoginUserVO(User loginUser) {
        //校验
        ThrowUtils.throwIf(loginUser == null, NOT_LOGIN_ERROR, "未登录");
        //复制信息到安全用户类
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(loginUser, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        //判断是否指定查询用户信息
        ThrowUtils.throwIf(user == null, PARAMS_ERROR);
        //复制信息到用户类
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        //校验
        ThrowUtils.throwIf(CollectionUtil.isEmpty(userList), PARAMS_ERROR);
        //循环处理
        return userList.stream()
                .map(this::getUserVO)
                .toList();
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        //判空
        ThrowUtils.throwIf(userQueryRequest == null, PARAMS_ERROR);
        //获取全部查询属性
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        Long id = userQueryRequest.getId();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        //添加所有条件
        return QueryWrapper.create()
                .like("user_account", userAccount)
                .like("user_name", userName)
                .like("user_profile", userProfile)
                .eq( "user_role", userRole)
                .orderBy(sortField, "ascend".equals(sortOrder))
                .eq("id", id);

    }

    @Override
    public User isLogin(HttpServletRequest request) {
        //获取登录态
        Object obj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) obj;
        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null, NOT_LOGIN_ERROR, "未登录");
        return currentUser;
    }


    @Override
    public boolean isAdmin(User loginUser) {

        return loginUser != null && ADMIN.getValue().equals(loginUser.getUserRole());
    }
}
