package com.demo.wms.controller;

import com.demo.wms.entity.Product;
import com.demo.wms.entity.StockRecord;
import com.demo.wms.entity.User;
import com.demo.wms.enums.StockRecordType;
import com.demo.wms.exception.BizException;
import com.demo.wms.permission.PermissionPolicy;
import com.demo.wms.service.InventoryService;
import com.demo.wms.service.LogService;
import com.demo.wms.service.ProductService;
import com.demo.wms.util.DateTimeUtil;
import com.demo.wms.util.InputUtil;

import java.util.List;
import java.util.Scanner;

/**
 * 库存与记录控制器。
 * 负责库存操作、库存查询以及进出库记录展示。
 */
public class StockController {
    private final Scanner scanner;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final LogService logService;

    public StockController(Scanner scanner,
                           ProductService productService,
                           InventoryService inventoryService,
                           LogService logService) {
        this.scanner = scanner;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.logService = logService;
    }

    public void manageInventory(User currentUser, PermissionPolicy permissionPolicy) {
        if (!permissionPolicy.canViewInventory()) {
            System.out.println("当前角色无权查看库存。");
            return;
        }

        while (true) {
            printInventoryMenu();
            int choice = InputUtil.readMenuChoice(scanner, "请选择：", 0, 4);
            switch (choice) {
                case 1:
                    stockIn(currentUser, permissionPolicy);
                    break;
                case 2:
                    stockOut(currentUser, permissionPolicy);
                    break;
                case 3:
                    viewInventory();
                    break;
                case 4:
                    printProducts(productService.getLowStockProducts());
                    break;
                case 0:
                    return;
                default:
                    break;
            }
        }
    }

    public void viewRecords(User currentUser, PermissionPolicy permissionPolicy) {
        if (!permissionPolicy.canViewStockRecords()) {
            System.out.println("当前角色无权查看进出库记录。");
            return;
        }

        while (true) {
            printRecordMenu();
            int choice = InputUtil.readMenuChoice(scanner, "请选择：", 0, 5);
            switch (choice) {
                case 1:
                    printRecords(inventoryService.getAllRecords());
                    break;
                case 2:
                    // 按商品维度看流水，适合追踪某个商品的完整库存变化历史。
                    printRecords(inventoryService.getRecordsByProductId(
                            InputUtil.readRequiredString(scanner, "商品编号：")));
                    break;
                case 3:
                    filterByType();
                    break;
                case 4:
                    printRecords(inventoryService.getRecordsByOperator(
                            InputUtil.readRequiredString(scanner, "操作员用户名：")));
                    break;
                case 5:
                    sortRecords();
                    break;
                case 0:
                    return;
                default:
                    break;
            }
        }
    }

    private void stockIn(User currentUser, PermissionPolicy permissionPolicy) {
        if (!permissionPolicy.canStockIn()) {
            System.out.println("当前角色无权执行入库。");
            return;
        }
        operateStock(currentUser, true);
    }

    private void stockOut(User currentUser, PermissionPolicy permissionPolicy) {
        if (!permissionPolicy.canStockOut()) {
            System.out.println("当前角色无权执行出库。");
            return;
        }
        operateStock(currentUser, false);
    }

    private void operateStock(User currentUser, boolean stockIn) {
        try {
            String productId = InputUtil.readRequiredString(scanner, "商品编号：");
            int quantity = InputUtil.readPositiveInt(scanner, stockIn ? "入库数量：" : "出库数量：");
            String remark = InputUtil.readOptionalString(scanner, "备注（可为空）：");
            // 控制层不处理库存计算细节，只负责选择对应的业务入口。
            StockRecord record = stockIn
                    ? inventoryService.stockIn(productId, quantity, currentUser.getUsername(), remark)
                    : inventoryService.stockOut(productId, quantity, currentUser.getUsername(), remark);
            System.out.println((stockIn ? "入库" : "出库") + "成功，记录编号：" + record.getId());
        } catch (BizException ex) {
            System.out.println((stockIn ? "入库" : "出库") + "失败：" + ex.getMessage());
        }
    }

    private void viewInventory() {
        System.out.println();
        System.out.println("1. 查看全部库存");
        System.out.println("2. 按商品编号查询");
        System.out.println("3. 按商品名称查询");
        System.out.println("4. 按分类查询");
        System.out.println("5. 排序查看");
        System.out.println("0. 返回上级菜单");
        int choice = InputUtil.readMenuChoice(scanner, "请选择：", 0, 5);
        switch (choice) {
            case 1:
                printProducts(productService.getAllProducts());
                break;
            case 2:
                // 这里走精确匹配，便于演示编号作为主键的查询方式。
                Product product = productService.getProductById(InputUtil.readRequiredString(scanner, "商品编号："));
                if (product == null) {
                    System.out.println("未找到对应商品。");
                } else {
                    printProducts(List.of(product));
                }
                break;
            case 3:
                printProducts(productService.searchProductsByName(InputUtil.readRequiredString(scanner, "商品名称关键字：")));
                break;
            case 4:
                printProducts(productService.getProductsByCategory(InputUtil.readRequiredString(scanner, "商品分类：")));
                break;
            case 5:
                sortProducts();
                break;
            case 0:
                break;
            default:
                break;
        }
    }

    private void sortProducts() {
        System.out.println("1. 按库存排序");
        System.out.println("2. 按单价排序");
        System.out.println("3. 按最后更新时间排序");
        int fieldChoice = InputUtil.readMenuChoice(scanner, "请选择排序字段：", 1, 3);
        boolean ascending = chooseAscending();
        String sortBy = fieldChoice == 1 ? "stock" : fieldChoice == 2 ? "price" : "time";
        printProducts(productService.sortProducts(sortBy, ascending));
    }

    private void filterByType() {
        System.out.println("1. 入库记录");
        System.out.println("2. 出库记录");
        int choice = InputUtil.readMenuChoice(scanner, "请选择：", 1, 2);
        StockRecordType type = choice == 1 ? StockRecordType.IN : StockRecordType.OUT;
        printRecords(inventoryService.getRecordsByType(type));
    }

    private void sortRecords() {
        System.out.println("1. 按时间排序");
        System.out.println("2. 按数量排序");
        int choice = InputUtil.readMenuChoice(scanner, "请选择排序字段：", 1, 2);
        boolean ascending = chooseAscending();
        // 库存记录常见的两个观察维度就是“发生顺序”和“变更数量”。
        if (choice == 1) {
            printRecords(inventoryService.sortRecordsByTime(ascending));
        } else {
            printRecords(inventoryService.sortRecordsByQuantity(ascending));
        }
    }

    private boolean chooseAscending() {
        System.out.println("1. 升序");
        System.out.println("2. 降序");
        return InputUtil.readMenuChoice(scanner, "请选择排序方向：", 1, 2) == 1;
    }

    private void printProducts(List<Product> products) {
        System.out.println();
        if (products == null || products.isEmpty()) {
            System.out.println("暂无符合条件的商品。");
            return;
        }
        System.out.printf("%-8s %-12s %-10s %-10s %-8s %-8s %-8s %-20s%n",
                "编号", "名称", "分类", "单价", "库存", "预警", "状态", "最后修改时间");
        for (Product product : products) {
            System.out.printf("%-8s %-12s %-10s %-10s %-8d %-8d %-8s %-20s%n",
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getPrice().toPlainString(),
                    product.getStock(),
                    product.getAlertStock(),
                    product.getStatus().getDisplayName(),
                    DateTimeUtil.format(product.getLastModified()));
        }
    }

    private void printRecords(List<StockRecord> records) {
        System.out.println();
        if (records == null || records.isEmpty()) {
            System.out.println("暂无符合条件的库存记录。");
            return;
        }
        // 把“变更前/变更后”同时展示出来，更容易讲清库存流水的业务含义。
        System.out.printf("%-8s %-8s %-6s %-6s %-10s %-10s %-10s %-20s %-12s%n",
                "记录号", "商品号", "类型", "数量", "变更前", "变更后", "操作人", "操作时间", "备注");
        for (StockRecord record : records) {
            System.out.printf("%-8s %-8s %-6s %-6d %-10d %-10d %-10s %-20s %-12s%n",
                    record.getId(),
                    record.getProductId(),
                    record.getType().getDisplayName(),
                    record.getQuantity(),
                    record.getBeforeStock(),
                    record.getAfterStock(),
                    record.getOperator(),
                    DateTimeUtil.format(record.getOperateTime()),
                    record.getRemark());
        }
    }

    private void printInventoryMenu() {
        System.out.println();
        System.out.println("======== 库存管理 ========");
        System.out.println("1. 商品入库");
        System.out.println("2. 商品出库");
        System.out.println("3. 查看库存");
        System.out.println("4. 查看低库存预警");
        System.out.println("0. 返回上级菜单");
    }

    private void printRecordMenu() {
        System.out.println();
        System.out.println("======== 记录查询 ========");
        System.out.println("1. 查看全部记录");
        System.out.println("2. 按商品编号查询");
        System.out.println("3. 按操作类型筛选");
        System.out.println("4. 按操作员查询");
        System.out.println("5. 排序查看");
        System.out.println("0. 返回上级菜单");
    }
}
