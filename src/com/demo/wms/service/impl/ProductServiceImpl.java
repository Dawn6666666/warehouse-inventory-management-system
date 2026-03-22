package com.demo.wms.service.impl;

import com.demo.wms.entity.Product;
import com.demo.wms.enums.ProductStatus;
import com.demo.wms.exception.BizException;
import com.demo.wms.service.PersistenceService;
import com.demo.wms.service.ProductService;
import com.demo.wms.store.WmsDataStore;
import com.demo.wms.util.DateTimeUtil;
import com.demo.wms.util.IdUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 商品服务实现。
 * 负责商品资料维护、查询、排序以及删除/停用策略。
 */
public class ProductServiceImpl implements ProductService {
    private final WmsDataStore dataStore;
    private final PersistenceService persistenceService;

    public ProductServiceImpl() {
        this(ServiceSupport.createStore());
    }

    public ProductServiceImpl(WmsDataStore dataStore) {
        this(dataStore, new FilePersistenceServiceImpl(dataStore, new FileLogServiceImpl()));
    }

    public ProductServiceImpl(WmsDataStore dataStore, PersistenceService persistenceService) {
        this.dataStore = dataStore;
        this.persistenceService = persistenceService;
    }

    @Override
    public Product addProduct(String name, String category, BigDecimal price, int alertStock) {
        validateName(name);
        validateCategory(category);
        BigDecimal normalizedPrice = normalizePrice(price);
        validateAlertStock(alertStock);

        // 新增前先保存快照，后续如果落盘失败，可以恢复到操作前状态。
        Map<String, Product> snapshot = dataStore.snapshotProducts();
        Product product = new Product(
                IdUtil.nextId(dataStore.getProductsById().keySet(), "P"),
                name.trim(),
                category.trim(),
                normalizedPrice,
                0,
                alertStock,
                ProductStatus.ACTIVE,
                DateTimeUtil.now()
        );
        dataStore.getProductsById().put(product.getId(), product);
        try {
            persistenceService.saveProducts();
            return product.copy();
        } catch (BizException ex) {
            dataStore.replaceProducts(snapshot);
            throw ex;
        }
    }

    @Override
    public Product updateProduct(String productId, String name, String category, BigDecimal price, int alertStock) {
        Product product = requireProduct(productId);
        validateName(name);
        validateCategory(category);
        BigDecimal normalizedPrice = normalizePrice(price);
        validateAlertStock(alertStock);

        Map<String, Product> snapshot = dataStore.snapshotProducts();
        // 修改沿用原对象更新，再借助快照保证保存失败时可以恢复。
        product.setName(name.trim());
        product.setCategory(category.trim());
        product.setPrice(normalizedPrice);
        product.setAlertStock(alertStock);
        product.setLastModified(DateTimeUtil.now());
        try {
            persistenceService.saveProducts();
            return product.copy();
        } catch (BizException ex) {
            dataStore.replaceProducts(snapshot);
            throw ex;
        }
    }

    @Override
    public boolean removeOrDisableProduct(String productId) {
        Product product = requireProduct(productId);
        Map<String, Product> snapshot = dataStore.snapshotProducts();
        boolean removed;
        if (product.getStock() == 0 && !dataStore.hasStockHistory(product.getId())) {
            // 只有“无库存且无历史记录”的商品才允许物理删除。
            dataStore.getProductsById().remove(product.getId());
            removed = true;
        } else {
            // 一旦商品参与过业务，就优先停用，保留历史可追溯性。
            product.setStatus(ProductStatus.DISABLED);
            product.setLastModified(DateTimeUtil.now());
            removed = false;
        }
        try {
            persistenceService.saveProducts();
            return removed;
        } catch (BizException ex) {
            dataStore.replaceProducts(snapshot);
            throw ex;
        }
    }

    @Override
    public Product getProductById(String productId) {
        Product product = dataStore.getProductsById().get(productId);
        return product == null ? null : product.copy();
    }

    @Override
    public List<Product> searchProductsByName(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<Product> result = new ArrayList<>();
        for (Product product : dataStore.getProductsById().values()) {
            // 名称查询采用包含匹配，适合控制台场景下的模糊搜索。
            if (product.getName().toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                result.add(product.copy());
            }
        }
        return result;
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        String normalizedCategory = category == null ? "" : category.trim().toLowerCase(Locale.ROOT);
        List<Product> result = new ArrayList<>();
        for (Product product : dataStore.getProductsById().values()) {
            if (product.getCategory().toLowerCase(Locale.ROOT).equals(normalizedCategory)) {
                result.add(product.copy());
            }
        }
        return result;
    }

    @Override
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        for (Product product : dataStore.getProductsById().values()) {
            products.add(product.copy());
        }
        products.sort(Comparator.comparing(Product::getId));
        return products;
    }

    @Override
    public List<Product> getLowStockProducts() {
        List<Product> result = new ArrayList<>();
        for (Product product : dataStore.getProductsById().values()) {
            // 库存等于预警值时同样视为预警，方便尽早发现补货需求。
            if (product.getStock() <= product.getAlertStock()) {
                result.add(product.copy());
            }
        }
        result.sort(Comparator.comparing(Product::getStock));
        return result;
    }

    @Override
    public List<Product> sortProducts(String sortBy, boolean ascending) {
        List<Product> products = getAllProducts();
        Comparator<Product> comparator;
        // 排序字段由控制层以字符串形式传入，方便菜单层和业务层保持解耦。
        switch (sortBy == null ? "" : sortBy.toLowerCase(Locale.ROOT)) {
            case "stock":
                comparator = Comparator.comparing(Product::getStock);
                break;
            case "price":
                comparator = Comparator.comparing(Product::getPrice);
                break;
            case "time":
            case "lastmodified":
                comparator = Comparator.comparing(Product::getLastModified);
                break;
            default:
                comparator = Comparator.comparing(Product::getId);
                break;
        }
        products.sort(ascending ? comparator : comparator.reversed());
        return products;
    }

    private Product requireProduct(String productId) {
        Product product = dataStore.getProductsById().get(productId);
        if (product == null) {
            throw new BizException("商品不存在：" + productId);
        }
        return product;
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BizException("商品名称不能为空。");
        }
        if (name.trim().length() > 30) {
            throw new BizException("商品名称长度不能超过 30。");
        }
    }

    private void validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new BizException("商品分类不能为空。");
        }
        if (category.trim().length() > 20) {
            throw new BizException("商品分类长度不能超过 20。");
        }
    }

    private BigDecimal normalizePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("商品单价不能小于 0。");
        }
        if (price.scale() > 2) {
            throw new BizException("商品单价最多保留 2 位小数。");
        }
        // 金额统一保留两位小数，避免文件保存前后表现不一致。
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateAlertStock(int alertStock) {
        if (alertStock < 0) {
            throw new BizException("预警库存不能小于 0。");
        }
    }
}
