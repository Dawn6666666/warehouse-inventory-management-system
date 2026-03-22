package com.demo.wms.controller;

import com.demo.wms.entity.User;
import com.demo.wms.enums.Role;
import com.demo.wms.exception.BizException;
import com.demo.wms.permission.AdminPermissionPolicy;
import com.demo.wms.permission.OperatorPermissionPolicy;
import com.demo.wms.permission.PermissionPolicy;
import com.demo.wms.service.LogService;
import com.demo.wms.service.PersistenceService;
import com.demo.wms.util.InputUtil;

import java.util.List;
import java.util.Scanner;

/**
 * 系统总控制器。
 * 负责登录前菜单、登录后主菜单以及各业务模块之间的流程跳转。
 */
public class MainController {
    private final Scanner scanner;
    private final LoginController loginController;
    private final ProductController productController;
    private final StockController stockController;
    private final UserController userController;
    private final LogService logService;
    private final PersistenceService persistenceService;

    public MainController(Scanner scanner,
                          LoginController loginController,
                          ProductController productController,
                          StockController stockController,
                          UserController userController,
                          LogService logService,
                          PersistenceService persistenceService) {
        this.scanner = scanner;
        this.loginController = loginController;
        this.productController = productController;
        this.stockController = stockController;
        this.userController = userController;
        this.logService = logService;
        this.persistenceService = persistenceService;
    }

    public void start() {
        while (true) {
            // 登录前首页只保留“进入系统”和“退出系统”两个入口，先把主流程控制住。
            printLoginMenu();
            int choice = InputUtil.readMenuChoice(scanner, "请选择：", 0, 1);
            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 0:
                    safeSaveBeforeExit();
                    logService.info("SYSTEM", "APP", "EXIT", "系统安全退出");
                    System.out.println("系统已退出，欢迎下次使用。");
                    return;
                default:
                    break;
            }
        }
    }

    private void handleLogin() {
        try {
            User currentUser = loginController.login();
            System.out.println("登录成功，欢迎你，" + currentUser.getUsername() + "（" + currentUser.getRole().getDisplayName() + "）");
            // 登录成功后，把当前登录用户作为后续菜单和权限判断的上下文。
            handleMainMenu(currentUser);
        } catch (BizException ex) {
            System.out.println("登录失败：" + ex.getMessage());
        }
    }

    private void handleMainMenu(User currentUser) {
        while (true) {
            PermissionPolicy permissionPolicy = resolvePermission(currentUser);
            // 主菜单分成“界面入口”和“权限能力”两层：
            // 角色决定能看到哪些菜单，权限策略决定真正允许做哪些事。
            if (currentUser.getRole() == Role.ADMIN) {
                printAdminMenu();
                int choice = InputUtil.readMenuChoice(scanner, "请选择：", 0, 5);
                if (processAdminChoice(choice, currentUser, permissionPolicy)) {
                    return;
                }
            } else {
                printOperatorMenu();
                int choice = InputUtil.readMenuChoice(scanner, "请选择：", 0, 2);
                if (processOperatorChoice(choice, currentUser, permissionPolicy)) {
                    return;
                }
            }
        }
    }

    private boolean processAdminChoice(int choice, User currentUser, PermissionPolicy permissionPolicy) {
        switch (choice) {
            case 1:
                productController.manageProducts(currentUser, permissionPolicy);
                return false;
            case 2:
                stockController.manageInventory(currentUser, permissionPolicy);
                return false;
            case 3:
                stockController.viewRecords(currentUser, permissionPolicy);
                return false;
            case 4:
                userController.manageUsers(currentUser, permissionPolicy);
                return false;
            case 5:
                showLogs(currentUser, permissionPolicy);
                return false;
            case 0:
                logService.info(currentUser.getUsername(), "AUTH", "LOGOUT", "退出登录");
                System.out.println("已退出当前账号。");
                return true;
            default:
                return false;
        }
    }

    private boolean processOperatorChoice(int choice, User currentUser, PermissionPolicy permissionPolicy) {
        switch (choice) {
            case 1:
                stockController.manageInventory(currentUser, permissionPolicy);
                return false;
            case 2:
                stockController.viewRecords(currentUser, permissionPolicy);
                return false;
            case 0:
                logService.info(currentUser.getUsername(), "AUTH", "LOGOUT", "退出登录");
                System.out.println("已退出当前账号。");
                return true;
            default:
                return false;
        }
    }

    private void showLogs(User currentUser, PermissionPolicy permissionPolicy) {
        if (!permissionPolicy.canViewLogs()) {
            System.out.println("当前角色无权查看日志。");
            return;
        }
        try {
            // 日志内容在这里不再二次解析，直接按文件中的可读文本逐行展示。
            List<String> logs = logService.readLogs();
            System.out.println();
            System.out.println("======== 操作日志 ========");
            if (logs.isEmpty()) {
                System.out.println("暂无日志记录。");
                return;
            }
            for (String log : logs) {
                if (!log.trim().isEmpty()) {
                    System.out.println(log);
                }
            }
        } catch (Exception ex) {
            logService.error(currentUser.getUsername(), "LOG", "VIEW", "读取日志失败：" + ex.getMessage());
            System.out.println("读取日志失败：" + ex.getMessage());
        }
    }

    private PermissionPolicy resolvePermission(User currentUser) {
        // 权限判断通过策略对象完成，便于按角色切换不同实现。
        if (currentUser.getRole() == Role.ADMIN) {
            return new AdminPermissionPolicy();
        }
        return new OperatorPermissionPolicy();
    }

    private void safeSaveBeforeExit() {
        try {
            // 退出前补做一次总保存，避免用户刚完成修改就退出导致数据未落盘。
            persistenceService.saveAll();
        } catch (BizException ex) {
            System.out.println("退出前最终保存失败：" + ex.getMessage());
        }
    }

    private void printLoginMenu() {
        System.out.println();
        System.out.println("======== 仓库进销存管理系统 ========");
        System.out.println("1. 登录系统");
        System.out.println("0. 退出系统");
    }

    private void printAdminMenu() {
        System.out.println();
        System.out.println("======== 管理员主菜单 ========");
        System.out.println("1. 商品管理");
        System.out.println("2. 库存管理");
        System.out.println("3. 记录查询");
        System.out.println("4. 用户管理");
        System.out.println("5. 日志查看");
        System.out.println("0. 退出登录");
    }

    private void printOperatorMenu() {
        System.out.println();
        System.out.println("======== 操作员主菜单 ========");
        System.out.println("1. 库存管理");
        System.out.println("2. 记录查询");
        System.out.println("0. 退出登录");
    }
}
