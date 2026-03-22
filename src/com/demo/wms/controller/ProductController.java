package com.demo.wms.controller;

import com.demo.wms.entity.Product;
import com.demo.wms.exception.BizException;
import com.demo.wms.permission.PermissionPolicy;
import com.demo.wms.service.LogService;
import com.demo.wms.service.ProductService;
import com.demo.wms.util.InputUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * 商品管理控制器。
 * 负责商品模块的交互、输入采集和结果展示。
 */
public class ProductController {
    private final Scanner scanner;
    private final ProductService productService;
    private final LogService logService;

    public ProductController(Scanner scanner, ProductService productService, LogService logService) {
        this.scanner = scanner;
        this.productService = productService;
        this.logService = logService;
    }

    public void manageProducts(com.demo.wms.entity.User currentUser, PermissionPolicy permissionPolicy) {
        if (!permissionPolicy.canManageProduct()) {
            System.out.println("当前角色无权管理商品。");
            return;
        }

        while (true) {
            printMenu();
            int choice = InputUtil.readMenuChoice(scanner, "请选择：", 0, 5);
            switch (choice) {
                case 1:
                    addProduct(currentUser.getUsername());
                    break;
                case 2:
                    updateProduct(currentUser.getUsername());
                    break;
                case 3:
                    removeOrDisableProduct(currentUser.getUsername());
                    break;
                case 4:
                    queryProducts();
                    break;
                case 5:
                    sortProducts();
                    break;
                case 0:
                    return;
                default:
                    break;
            }
        }
    }

    private void addProduct(String operator) {
        try {
            String name = InputUtil.readRequiredString(scanner, "商品名称：");
            String category = InputUtil.readRequiredString(scanner, "商品分类：");
            BigDecimal price = InputUtil.readNonNegativeBigDecimal(scanner, "商品单价：");
            int alertStock = InputUtil.readNonNegativeInt(scanner, "预警库存：");
            Product product = productService.addProduct(name, category, price, alertStock);
            logService.info(operator, "PRODUCT", "ADD", "新增商品成功：" + product.getId());
            System.out.println("新增成功，商品编号：" + product.getId());
        } catch (BizException ex) {
            logService.warn(operator, "PRODUCT", "ADD", ex.getMessage());
            System.out.println("新增失败：" + ex.getMessage());
        }
    }

    private void updateProduct(String operator) {
        String productId = InputUtil.readRequiredString(scanner, "请输入商品编号：");
        Product existing = productService.getProductById(productId);
        if (existing == null) {
            System.out.println("商品不存在：" + productId);
            return;
        }

        try {
            System.out.println("当前商品信息：");
            printProducts(List.of(existing));

            // 控制层只处理“空输入沿用旧值”这类交互细节，字段合法性仍然由业务层统一校验。
            String name = InputUtil.readOptionalString(scanner, "新名称（直接回车表示不修改）：");
            String category = InputUtil.readOptionalString(scanner, "新分类（直接回车表示不修改）：");
            String priceText = InputUtil.readOptionalString(scanner, "新单价（直接回车表示不修改）：");
            String alertText = InputUtil.readOptionalString(scanner, "新预警库存（直接回车表示不修改）：");

            Product updated = productService.updateProduct(
                    productId,
                    name.isEmpty() ? existing.getName() : name,
                    category.isEmpty() ? existing.getCategory() : category,
                    priceText.isEmpty() ? existing.getPrice() : new BigDecimal(priceText),
                    alertText.isEmpty() ? existing.getAlertStock() : Integer.parseInt(alertText)
            );
            logService.info(operator, "PRODUCT", "UPDATE", "修改商品成功：" + updated.getId());
            System.out.println("修改成功。");
        } catch (NumberFormatException ex) {
            logService.warn(operator, "PRODUCT", "UPDATE", "输入格式错误");
            System.out.println("输入格式错误，请检查单价和预警库存。");
        } catch (BizException ex) {
            logService.warn(operator, "PRODUCT", "UPDATE", ex.getMessage());
            System.out.println("修改失败：" + ex.getMessage());
        }
    }

    private void removeOrDisableProduct(String operator) {
        String productId = InputUtil.readRequiredString(scanner, "请输入商品编号：");
        try {
            boolean removed = productService.removeOrDisableProduct(productId);
            if (removed) {
                logService.info(operator, "PRODUCT", "DELETE", "商品已物理删除：" + productId);
                System.out.println("删除成功，该商品无库存且无历史记录。");
            } else {
                // 这是商品删除策略里最关键的取舍：
                // 一旦商品参与过业务，优先停用，保留历史可追溯性。
                logService.info(operator, "PRODUCT", "DISABLE", "商品已停用：" + productId);
                System.out.println("商品已停用。存在库存或历史记录时，系统采用逻辑停用。");
            }
        } catch (BizException ex) {
            logService.warn(operator, "PRODUCT", "DELETE_OR_DISABLE", ex.getMessage());
            System.out.println("操作失败：" + ex.getMessage());
        }
    }

    private void queryProducts() {
        System.out.println();
        System.out.println("1. 按商品编号查询");
        System.out.println("2. 按商品名称查询");
        System.out.println("3. 按分类查询");
        System.out.println("4. 查看全部商品");
        System.out.println("0. 返回上级菜单");
        int choice = InputUtil.readMenuChoice(scanner, "请选择：", 0, 4);
        switch (choice) {
            case 1:
                // 精确查询适合按编号直接定位单个商品。
                Product product = productService.getProductById(InputUtil.readRequiredString(scanner, "商品编号："));
                if (product == null) {
                    System.out.println("未找到对应商品。");
                } else {
                    printProducts(List.of(product));
                }
                break;
            case 2:
                // 名称查询允许模糊匹配，更符合实际检索习惯。
                printProducts(productService.searchProductsByName(InputUtil.readRequiredString(scanner, "商品名称关键字：")));
                break;
            case 3:
                printProducts(productService.getProductsByCategory(InputUtil.readRequiredString(scanner, "商品分类：")));
                break;
            case 4:
                printProducts(productService.getAllProducts());
                break;
            case 0:
                break;
            default:
                break;
        }
    }

    private void sortProducts() {
        System.out.println();
        System.out.println("1. 按库存排序");
        System.out.println("2. 按单价排序");
        System.out.println("3. 按最后更新时间排序");
        System.out.println("0. 返回上级菜单");
        int choice = InputUtil.readMenuChoice(scanner, "请选择：", 0, 3);
        if (choice == 0) {
            return;
        }
        boolean ascending = chooseAscending();
        // 控制层只把排序意图翻译成简单标识，具体排序规则由业务层决定。
        String sortBy;
        switch (choice) {
            case 1:
                sortBy = "stock";
                break;
            case 2:
                sortBy = "price";
                break;
            case 3:
                sortBy = "time";
                break;
            default:
                sortBy = "id";
                break;
        }
        printProducts(productService.sortProducts(sortBy, ascending));
    }

    private boolean chooseAscending() {
        System.out.println("1. 升序");
        System.out.println("2. 降序");
        int choice = InputUtil.readMenuChoice(scanner, "请选择排序方向：", 1, 2);
        return choice == 1;
    }

    private void printProducts(List<Product> products) {
        System.out.println();
        if (products == null || products.isEmpty()) {
            System.out.println("暂无符合条件的商品。");
            return;
        }
        // 控制台程序没有图形界面，表格化输出更便于阅读和核对。
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
                    com.demo.wms.util.DateTimeUtil.format(product.getLastModified()));
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("======== 商品管理 ========");
        System.out.println("1. 新增商品");
        System.out.println("2. 修改商品");
        System.out.println("3. 删除/停用商品");
        System.out.println("4. 查询商品");
        System.out.println("5. 商品排序查看");
        System.out.println("0. 返回上级菜单");
    }
}
