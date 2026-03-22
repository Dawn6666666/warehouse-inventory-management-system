package com.demo.wms.service.impl;

import com.demo.wms.entity.Product;
import com.demo.wms.entity.StockRecord;
import com.demo.wms.enums.ProductStatus;
import com.demo.wms.enums.StockRecordType;
import com.demo.wms.exception.BizException;
import com.demo.wms.exception.StockNotEnoughException;
import com.demo.wms.service.InventoryService;
import com.demo.wms.service.LogService;
import com.demo.wms.service.PersistenceService;
import com.demo.wms.store.WmsDataStore;
import com.demo.wms.util.DateTimeUtil;
import com.demo.wms.util.IdUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 库存服务实现。
 * 负责库存增减、库存记录生成，以及记录的查询和排序。
 */
public class InventoryServiceImpl implements InventoryService {
    private final WmsDataStore dataStore;
    private final PersistenceService persistenceService;
    private final LogService logService;

    public InventoryServiceImpl() {
        this(ServiceSupport.createStore());
    }

    public InventoryServiceImpl(WmsDataStore dataStore) {
        this(dataStore, new FilePersistenceServiceImpl(dataStore, new FileLogServiceImpl()), new FileLogServiceImpl());
    }

    public InventoryServiceImpl(WmsDataStore dataStore, PersistenceService persistenceService, LogService logService) {
        this.dataStore = dataStore;
        this.persistenceService = persistenceService;
        this.logService = logService;
    }

    @Override
    public StockRecord stockIn(String productId, int quantity, String operator, String remark) {
        return changeStock(productId, quantity, operator, remark, StockRecordType.IN);
    }

    @Override
    public StockRecord stockOut(String productId, int quantity, String operator, String remark) {
        return changeStock(productId, quantity, operator, remark, StockRecordType.OUT);
    }

    @Override
    public List<StockRecord> getAllRecords() {
        return copyRecords(dataStore.getStockRecords());
    }

    @Override
    public List<StockRecord> getRecordsByProductId(String productId) {
        List<StockRecord> source = dataStore.getStockRecordsByProductId().get(productId);
        return source == null ? new ArrayList<>() : copyRecords(source);
    }

    @Override
    public List<StockRecord> getRecordsByType(StockRecordType type) {
        List<StockRecord> result = new ArrayList<>();
        for (StockRecord record : dataStore.getStockRecords()) {
            if (record.getType() == type) {
                result.add(record.copy());
            }
        }
        return result;
    }

    @Override
    public List<StockRecord> getRecordsByOperator(String operator) {
        String normalized = operator == null ? "" : operator.trim().toLowerCase(Locale.ROOT);
        List<StockRecord> result = new ArrayList<>();
        for (StockRecord record : dataStore.getStockRecords()) {
            // 操作员查询支持包含匹配，方便按用户名关键字快速筛选记录。
            if (record.getOperator().toLowerCase(Locale.ROOT).contains(normalized)) {
                result.add(record.copy());
            }
        }
        return result;
    }

    @Override
    public List<StockRecord> sortRecordsByTime(boolean ascending) {
        List<StockRecord> result = getAllRecords();
        Comparator<StockRecord> comparator = Comparator.comparing(StockRecord::getOperateTime);
        result.sort(ascending ? comparator : comparator.reversed());
        return result;
    }

    @Override
    public List<StockRecord> sortRecordsByQuantity(boolean ascending) {
        List<StockRecord> result = getAllRecords();
        Comparator<StockRecord> comparator = Comparator.comparing(StockRecord::getQuantity);
        result.sort(ascending ? comparator : comparator.reversed());
        return result;
    }

    private StockRecord changeStock(String productId, int quantity, String operator, String remark, StockRecordType type) {
        // 入库和出库共用一套处理骨架，差别只体现在库存增减方向和记录类型上。
        Product product = dataStore.getProductsById().get(productId);
        if (product == null) {
            logService.warn(operator, "STOCK", type.name(), "操作失败，原因：商品不存在");
            throw new BizException("商品不存在：" + productId);
        }
        if (product.getStatus() == ProductStatus.DISABLED) {
            logService.warn(operator, "STOCK", type.name(), "操作失败，原因：商品已停用");
            throw new BizException("停用商品禁止继续进出库。");
        }
        if (quantity <= 0) {
            logService.warn(operator, "STOCK", type.name(), "操作失败，原因：数量必须大于 0");
            throw new BizException("数量必须大于 0。");
        }
        if (remark != null && remark.trim().length() > 50) {
            logService.warn(operator, "STOCK", type.name(), "操作失败，原因：备注长度超限");
            throw new BizException("备注长度不能超过 50。");
        }

        int beforeStock = product.getStock();
        int afterStock = type == StockRecordType.IN ? beforeStock + quantity : beforeStock - quantity;
        if (afterStock < 0) {
            logService.warn(operator, "STOCK", type.name(), "操作失败，原因：库存不足");
            throw new StockNotEnoughException("库存不足，当前库存：" + beforeStock);
        }

        // 先拍快照，再修改共享数据；这样一旦落盘失败，就能恢复到操作前状态。
        Map<String, Product> productSnapshot = dataStore.snapshotProducts();
        List<StockRecord> recordSnapshot = dataStore.snapshotStockRecords();

        product.setStock(afterStock);
        product.setLastModified(DateTimeUtil.now());
        // 记录里同时保存前后库存，后续查询时才能完整还原这次变更的影响。
        StockRecord record = new StockRecord(
                IdUtil.nextId(dataStore.getStockRecords().stream().map(StockRecord::getId).toList(), "R"),
                product.getId(),
                type,
                quantity,
                beforeStock,
                afterStock,
                operator,
                DateTimeUtil.now(),
                remark == null ? "" : remark.trim()
        );
        dataStore.addStockRecord(record);

        try {
            // 先保存商品主数据，再保存库存流水；
            // 两步都成功，当前业务才算真正完成。
            persistenceService.saveProducts();
            persistenceService.saveStockRecords();
        } catch (BizException ex) {
            // 只要任一步失败，就回滚内存，避免出现“界面显示成功、文件却没保存”的假成功。
            dataStore.replaceProducts(productSnapshot);
            dataStore.replaceStockRecords(recordSnapshot);
            logService.error(operator, "STOCK", type.name(), "数据保存失败，已回滚：" + ex.getMessage());
            throw ex;
        }

        String detail = String.format("商品 %s %s %d 件，库存 %d -> %d",
                product.getId(),
                type == StockRecordType.IN ? "入库" : "出库",
                quantity,
                beforeStock,
                afterStock);
        logService.info(operator, "STOCK", type.name(), detail);
        return record.copy();
    }

    private List<StockRecord> copyRecords(List<StockRecord> records) {
        List<StockRecord> result = new ArrayList<>();
        for (StockRecord record : records) {
            result.add(record.copy());
        }
        // 查询结果返回副本，避免调用方持有对内存主数据的直接引用。
        return result;
    }
}
