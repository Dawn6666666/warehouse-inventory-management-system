package com.demo.wms.controller;

import com.demo.wms.entity.User;
import com.demo.wms.service.AuthService;
import com.demo.wms.util.InputUtil;

import java.util.Scanner;

/**
 * 登录控制器。
 * 只负责采集登录输入，并把认证过程委托给业务层。
 */
public class LoginController {
    private final Scanner scanner;
    private final AuthService authService;

    public LoginController(Scanner scanner, AuthService authService) {
        this.scanner = scanner;
        this.authService = authService;
    }

    public User login() {
        System.out.println();
        System.out.println("======== 登录系统 ========");
        String username = InputUtil.readRequiredString(scanner, "请输入用户名：");
        String password = InputUtil.readRequiredString(scanner, "请输入密码：");
        return authService.login(username, password);
    }
}
