package com.demo.wms.controller;

import com.demo.wms.entity.User;
import com.demo.wms.enums.Role;
import com.demo.wms.enums.UserStatus;
import com.demo.wms.exception.BizException;
import com.demo.wms.permission.PermissionPolicy;
import com.demo.wms.service.LogService;
import com.demo.wms.service.UserService;
import com.demo.wms.util.InputUtil;

import java.util.List;
import java.util.Scanner;

/**
 * 用户管理控制器。
 * 负责用户模块的菜单交互和结果反馈。
 */
public class UserController {
    private final Scanner scanner;
    private final UserService userService;
    private final LogService logService;

    public UserController(Scanner scanner, UserService userService, LogService logService) {
        this.scanner = scanner;
        this.userService = userService;
        this.logService = logService;
    }

    public void manageUsers(User currentUser, PermissionPolicy permissionPolicy) {
        if (!permissionPolicy.canManageUser()) {
            System.out.println("当前角色无权管理用户。");
            return;
        }

        while (true) {
            printMenu();
            int choice = InputUtil.readMenuChoice(scanner, "请选择：", 0, 5);
            switch (choice) {
                case 1:
                    addUser(currentUser);
                    break;
                case 2:
                    changePassword(currentUser);
                    break;
                case 3:
                    changeRole(currentUser);
                    break;
                case 4:
                    changeStatus(currentUser);
                    break;
                case 5:
                    printUsers(userService.getAllUsers());
                    break;
                case 0:
                    return;
                default:
                    break;
            }
        }
    }

    private void addUser(User currentUser) {
        try {
            String username = InputUtil.readRequiredString(scanner, "用户名：");
            String password = InputUtil.readRequiredString(scanner, "密码：");
            Role role = chooseRole();
            User user = userService.addUser(username, password, role);
            logService.info(currentUser.getUsername(), "USER", "ADD", "新增用户成功：" + user.getUsername());
            System.out.println("新增用户成功。");
        } catch (BizException ex) {
            logService.warn(currentUser.getUsername(), "USER", "ADD", ex.getMessage());
            System.out.println("新增用户失败：" + ex.getMessage());
        }
    }

    private void changePassword(User currentUser) {
        try {
            String username = InputUtil.readRequiredString(scanner, "目标用户名：");
            String newPassword = InputUtil.readRequiredString(scanner, "新密码：");
            userService.changePassword(username, newPassword);
            logService.info(currentUser.getUsername(), "USER", "CHANGE_PASSWORD", "修改密码成功：" + username);
            System.out.println("密码修改成功。");
        } catch (BizException ex) {
            logService.warn(currentUser.getUsername(), "USER", "CHANGE_PASSWORD", ex.getMessage());
            System.out.println("密码修改失败：" + ex.getMessage());
        }
    }

    private void changeRole(User currentUser) {
        String username = InputUtil.readRequiredString(scanner, "目标用户名：");
        if (currentUser.getUsername().equals(username)) {
            // 当前会话中的权限对象已经确定，不建议在同一会话内修改自己的角色。
            System.out.println("为避免当前会话权限与文件数据不一致，不允许修改当前登录账号自己的角色。");
            return;
        }
        try {
            Role role = chooseRole();
            userService.changeRole(username, role);
            logService.info(currentUser.getUsername(), "USER", "CHANGE_ROLE", "修改角色成功：" + username);
            System.out.println("角色修改成功。");
        } catch (BizException ex) {
            logService.warn(currentUser.getUsername(), "USER", "CHANGE_ROLE", ex.getMessage());
            System.out.println("角色修改失败：" + ex.getMessage());
        }
    }

    private void changeStatus(User currentUser) {
        try {
            String username = InputUtil.readRequiredString(scanner, "目标用户名：");
            UserStatus status = chooseStatus();
            // 状态修改额外传入当前操作者，便于业务层阻止“自己禁用自己”这类风险操作。
            userService.changeStatus(username, status, currentUser.getUsername());
            logService.info(currentUser.getUsername(), "USER", "CHANGE_STATUS",
                    "修改用户状态成功：" + username + " -> " + status.name());
            System.out.println("用户状态修改成功。");
        } catch (BizException ex) {
            logService.warn(currentUser.getUsername(), "USER", "CHANGE_STATUS", ex.getMessage());
            System.out.println("用户状态修改失败：" + ex.getMessage());
        }
    }

    private Role chooseRole() {
        System.out.println("1. 管理员");
        System.out.println("2. 操作员");
        int choice = InputUtil.readMenuChoice(scanner, "请选择角色：", 1, 2);
        return choice == 1 ? Role.ADMIN : Role.OPERATOR;
    }

    private UserStatus chooseStatus() {
        System.out.println("1. 启用");
        System.out.println("2. 禁用");
        int choice = InputUtil.readMenuChoice(scanner, "请选择状态：", 1, 2);
        return choice == 1 ? UserStatus.ACTIVE : UserStatus.DISABLED;
    }

    private void printUsers(List<User> users) {
        System.out.println();
        if (users == null || users.isEmpty()) {
            System.out.println("暂无用户数据。");
            return;
        }
        // 用户列表只展示最关键的身份字段，既便于演示，也方便排查权限问题。
        System.out.printf("%-8s %-12s %-10s %-8s%n", "编号", "用户名", "角色", "状态");
        for (User user : users) {
            System.out.printf("%-8s %-12s %-10s %-8s%n",
                    user.getId(),
                    user.getUsername(),
                    user.getRole().getDisplayName(),
                    user.getStatus().getDisplayName());
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("======== 用户管理 ========");
        System.out.println("1. 新增用户");
        System.out.println("2. 修改用户密码");
        System.out.println("3. 修改用户角色");
        System.out.println("4. 启用/禁用用户");
        System.out.println("5. 查看用户列表");
        System.out.println("0. 返回上级菜单");
    }
}
