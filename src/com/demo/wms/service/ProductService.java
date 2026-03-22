package com.demo.wms.service;

import com.demo.wms.entity.Product;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务接口。
 * 负责商品资料维护、查询、筛选和排序。
 */
public interface ProductService {
    Product addProduct(String name, String category, BigDecimal price, int alertStock);

    Product updateProduct(String productId, String name, String category, BigDecimal price, int alertStock);

    boolean removeOrDisableProduct(String productId);

    Product getProductById(String productId);

    List<Product> searchProductsByName(String keyword);

    List<Product> getProductsByCategory(String category);

    List<Product> getAllProducts();

    List<Product> getLowStockProducts();

    List<Product> sortProducts(String sortBy, boolean ascending);
}
