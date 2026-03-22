package com.demo.wms.entity;

import com.demo.wms.enums.StockRecordType;

import java.time.LocalDateTime;

/**
 * 库存流水实体。
 * 每一次入库或出库都会生成一条记录，用来追踪库存变化过程。
 * <p>
 * 字段说明：
 * <p>
 * - id：库存记录编号。
 * <p>
 * - productId：对应的商品编号，用于关联具体商品。
 * <p>
 * - type：操作类型，表示本次记录属于入库还是出库。
 * <p>
 * - quantity：本次变更数量。
 * <p>
 * - beforeStock：变更前库存。
 * <p>
 * - afterStock：变更后库存。
 * <p>
 * - operator：执行本次操作的人员。
 * <p>
 * - operateTime：本次库存操作发生的时间。
 * <p>
 * - remark：备注信息，用于补充说明本次业务背景。
 */
public class StockRecord {
    private String id;
    private String productId;
    private StockRecordType type;
    private int quantity;
    private int beforeStock;
    private int afterStock;
    private String operator;
    private LocalDateTime operateTime;
    private String remark;

    public StockRecord() {
    }

    public StockRecord(String id, String productId, StockRecordType type, int quantity, int beforeStock,
                       int afterStock, String operator, LocalDateTime operateTime, String remark) {
        this.id = id;
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.beforeStock = beforeStock;
        this.afterStock = afterStock;
        this.operator = operator;
        this.operateTime = operateTime;
        this.remark = remark;
    }

    public StockRecord(StockRecord other) {
        this(other.id, other.productId, other.type, other.quantity, other.beforeStock,
                other.afterStock, other.operator, other.operateTime, other.remark);
    }

    public StockRecord copy() {
        return new StockRecord(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public StockRecordType getType() {
        return type;
    }

    public void setType(StockRecordType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getBeforeStock() {
        return beforeStock;
    }

    public void setBeforeStock(int beforeStock) {
        this.beforeStock = beforeStock;
    }

    public int getAfterStock() {
        return afterStock;
    }

    public void setAfterStock(int afterStock) {
        this.afterStock = afterStock;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public LocalDateTime getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(LocalDateTime operateTime) {
        this.operateTime = operateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
