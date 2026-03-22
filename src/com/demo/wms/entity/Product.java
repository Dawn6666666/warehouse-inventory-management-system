package com.demo.wms.entity;

import com.demo.wms.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体。
 * 同时保存商品资料和当前库存，是库存业务中的核心对象。
 * <p>
 * 字段说明：
 * <p>
 * - id：商品编号，作为商品主键使用。
 * <p>
 * - name：商品名称，用于展示和检索。
 * <p>
 * - category：商品分类，用于分类查询和管理。
 * <p>
 * - price：商品单价。
 * <p>
 * - stock：当前库存数量。
 * <p>
 * - alertStock：预警库存阈值，库存小于等于该值时可视为低库存。
 * <p>
 * - status：商品状态，用于区分启用和停用。
 * <p>
 * - lastModified：最后修改时间，用于记录最近一次变更时间。
 */
public class Product {
    private String id;
    private String name;
    private String category;
    private BigDecimal price;
    private int stock;
    private int alertStock;
    private ProductStatus status;
    private LocalDateTime lastModified;

    public Product() {
    }

    public Product(String id, String name, String category, BigDecimal price, int stock,
                   int alertStock, ProductStatus status, LocalDateTime lastModified) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.alertStock = alertStock;
        this.status = status;
        this.lastModified = lastModified;
    }

    public Product(Product other) {
        this(other.id, other.name, other.category, other.price, other.stock,
                other.alertStock, other.status, other.lastModified);
    }

    public Product copy() {
        return new Product(this);
    }

    public boolean isActive() {
        return status == ProductStatus.ACTIVE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getAlertStock() {
        return alertStock;
    }

    public void setAlertStock(int alertStock) {
        this.alertStock = alertStock;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
}
