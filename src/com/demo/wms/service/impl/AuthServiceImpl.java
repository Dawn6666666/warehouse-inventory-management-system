package com.demo.wms.service.impl;

import com.demo.wms.entity.User;
import com.demo.wms.enums.UserStatus;
import com.demo.wms.exception.LoginException;
import com.demo.wms.service.AuthService;
import com.demo.wms.service.LogService;
import com.demo.wms.store.WmsDataStore;

/**
 * 认证服务实现。
 * 专门处理登录时的账号校验、状态校验和日志记录。
 */
public class AuthServiceImpl implements AuthService {

    private final WmsDataStore dataStore;
    private final LogService logService;

    public AuthServiceImpl() {
        this(ServiceSupport.createStore(), new FileLogServiceImpl());
    }

    public AuthServiceImpl(WmsDataStore dataStore, LogService logService) {
        this.dataStore = dataStore;
        this.logService = logService == null ? new FileLogServiceImpl() : logService;
    }

    @Override
    public User login(String username, String password) {
        // 登录校验采用“先校验输入，再校验账号存在，再校验密码和状态”的顺序，
        // 这样错误来源更清晰，也便于逐步记录日志。
        if (ServiceSupport.isBlank(username) || ServiceSupport.isBlank(password)) {
            logService.warn("guest", "AUTH", "LOGIN", "用户名或密码不能为空");
            throw new LoginException("用户名或密码不能为空");
        }
        // 先按用户名定位账号，再依次校验密码和账号状态。
        User user = dataStore.getUsersByUsername().get(username.trim());
        if (user == null) {
            logService.warn(username, "AUTH", "LOGIN", "用户名不存在");
            throw new LoginException("用户名不存在");
        }
        if (!password.equals(user.getPassword())) {
            logService.warn(username, "AUTH", "LOGIN", "密码错误");
            throw new LoginException("密码错误");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            logService.warn(username, "AUTH", "LOGIN", "账号被禁用");
            throw new LoginException("账号被禁用");
        }
        logService.info(username, "AUTH", "LOGIN", "SUCCESS");
        // 返回副本而不是原对象，避免控制层误改共享数据。
        return user.copy();
    }
}
