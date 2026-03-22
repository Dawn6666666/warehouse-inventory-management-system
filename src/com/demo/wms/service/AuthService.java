package com.demo.wms.service;

import com.demo.wms.entity.User;

/**
 * 认证服务接口。
 * 负责登录校验，并在成功后返回当前登录用户信息。
 */
public interface AuthService {
    /**
     * 按用户名和密码执行登录校验。
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功后的用户副本
     */
    User login(String username, String password);
}
