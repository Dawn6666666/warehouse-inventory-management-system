package com.demo.wms.service;

import com.demo.wms.entity.StockRecord;
import com.demo.wms.enums.StockRecordType;

import java.util.List;

/**
 * 库存服务接口。
 * 负责入库、出库以及库存流水的查询。
 */
public interface InventoryService {
    StockRecord stockIn(String productId, int quantity, String operator, String remark);

    StockRecord stockOut(String productId, int quantity, String operator, String remark);

    List<StockRecord> getAllRecords();

    List<StockRecord> getRecordsByProductId(String productId);

    List<StockRecord> getRecordsByType(StockRecordType type);

    List<StockRecord> getRecordsByOperator(String operator);

    List<StockRecord> sortRecordsByTime(boolean ascending);

    List<StockRecord> sortRecordsByQuantity(boolean ascending);
}
