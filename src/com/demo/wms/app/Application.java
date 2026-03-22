package com.demo.wms.app;

import com.demo.wms.controller.LoginController;
import com.demo.wms.controller.MainController;
import com.demo.wms.controller.ProductController;
import com.demo.wms.controller.StockController;
import com.demo.wms.controller.UserController;
import com.demo.wms.service.AuthService;
import com.demo.wms.service.InventoryService;
import com.demo.wms.service.LogService;
import com.demo.wms.service.PersistenceService;
import com.demo.wms.service.ProductService;
import com.demo.wms.service.UserService;
import com.demo.wms.service.impl.AuthServiceImpl;
import com.demo.wms.service.impl.FileLogServiceImpl;
import com.demo.wms.service.impl.FilePersistenceServiceImpl;
import com.demo.wms.service.impl.InventoryServiceImpl;
import com.demo.wms.service.impl.ProductServiceImpl;
import com.demo.wms.service.impl.UserServiceImpl;
import com.demo.wms.store.WmsDataStore;

import java.util.Scanner;

/**
 * 程序入口。
 * 这里不写业务逻辑，只负责组装对象并启动整个控制台应用。
 */
public class Application {
    public static void main(String[] args) {
        // 先准备共享数据仓库，再把 Service 和 Controller 串起来。
        // 入口类的职责是完成依赖装配，而不是承载业务规则。
        WmsDataStore dataStore = new WmsDataStore();
        LogService logService = new FileLogServiceImpl();
        PersistenceService persistenceService = new FilePersistenceServiceImpl(dataStore, logService);

        // 程序启动时先准备目录和数据文件，再把历史数据恢复到内存。
        persistenceService.initialize();
        persistenceService.loadAll();

        AuthService authService = new AuthServiceImpl(dataStore, logService);
        ProductService productService = new ProductServiceImpl(dataStore, persistenceService);
        InventoryService inventoryService = new InventoryServiceImpl(dataStore, persistenceService, logService);
        UserService userService = new UserServiceImpl(dataStore, persistenceService);

        Scanner scanner = new Scanner(System.in);

        LoginController loginController = new LoginController(scanner, authService);
        ProductController productController = new ProductController(scanner, productService, logService);
        StockController stockController = new StockController(scanner, productService, inventoryService, logService);
        UserController userController = new UserController(scanner, userService, logService);

        MainController mainController = new MainController(
                scanner,
                loginController,
                productController,
                stockController,
                userController,
                logService,
                persistenceService
        );

        // 真正的业务流程从主控制器开始，main 方法只负责“搭台”，不负责写业务细节。
        mainController.start();
        scanner.close();
    }
}
