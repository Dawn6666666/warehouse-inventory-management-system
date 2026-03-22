package com.demo.wms.store;

import com.demo.wms.entity.Product;
import com.demo.wms.entity.StockRecord;
import com.demo.wms.entity.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统内存数据仓库。
 * 统一保存当前进程中的用户、商品和库存记录，并为 Service 层提供快照能力。
 */
public class WmsDataStore {
    // 用户按用户名登录，商品按编号定位，所以这里用 Map 做快速查找。
    private final Map<String, User> usersByUsername = new LinkedHashMap<>();
    private final Map<String, Product> productsById = new LinkedHashMap<>();
    // 进出库记录有天然时间顺序，所以总表用 List，便于遍历、排序和展示。
    private final List<StockRecord> stockRecords = new ArrayList<>();
    // 额外维护“商品编号 -> 记录列表”的索引，方便查某个商品的完整历史。
    private final Map<String, List<StockRecord>> stockRecordsByProductId = new LinkedHashMap<>();

    public Map<String, User> getUsersByUsername() {
        return usersByUsername;
    }

    public Map<String, Product> getProductsById() {
        return productsById;
    }

    public List<StockRecord> getStockRecords() {
        return stockRecords;
    }

    public Map<String, List<StockRecord>> getStockRecordsByProductId() {
        return stockRecordsByProductId;
    }

    public void replaceUsers(Map<String, User> users) {
        usersByUsername.clear();
        // 外部传入的数据先复制一份再接管，避免共享引用造成意外联动。
        usersByUsername.putAll(deepCopyUsers(users));
    }

    public void replaceProducts(Map<String, Product> products) {
        productsById.clear();
        productsById.putAll(deepCopyProducts(products));
    }

    public void replaceStockRecords(List<StockRecord> records) {
        stockRecords.clear();
        stockRecords.addAll(deepCopyRecords(records));
        // 记录总表被整体替换后，按商品维度的索引也必须同步重建。
        rebuildRecordIndex();
    }

    public void addStockRecord(StockRecord stockRecord) {
        StockRecord recordCopy = stockRecord.copy();
        stockRecords.add(recordCopy);
        // 新增记录时同步维护按商品编号分组的索引，避免每次查询都全表扫描。
        stockRecordsByProductId.computeIfAbsent(recordCopy.getProductId(), key -> new ArrayList<>())
                .add(recordCopy);
    }

    public boolean hasStockHistory(String productId) {
        List<StockRecord> records = stockRecordsByProductId.get(productId);
        return records != null && !records.isEmpty();
    }

    public Map<String, User> snapshotUsers() {
        // 快照返回深拷贝，服务层可以先改内存、失败时再回滚。
        return deepCopyUsers(usersByUsername);
    }

    public Map<String, Product> snapshotProducts() {
        // 商品数据也一样，深拷贝后再操作，能避免联调时把原始数据误改掉。
        return deepCopyProducts(productsById);
    }

    public List<StockRecord> snapshotStockRecords() {
        // 记录列表同样做值复制，确保“读快照”和“改原表”彼此独立。
        return deepCopyRecords(stockRecords);
    }

    private Map<String, User> deepCopyUsers(Map<String, User> source) {
        Map<String, User> copied = new LinkedHashMap<>();
        for (Map.Entry<String, User> entry : source.entrySet()) {
            copied.put(entry.getKey(), entry.getValue().copy());
        }
        return copied;
    }

    private Map<String, Product> deepCopyProducts(Map<String, Product> source) {
        Map<String, Product> copied = new LinkedHashMap<>();
        for (Map.Entry<String, Product> entry : source.entrySet()) {
            copied.put(entry.getKey(), entry.getValue().copy());
        }
        return copied;
    }

    private List<StockRecord> deepCopyRecords(List<StockRecord> source) {
        List<StockRecord> copied = new ArrayList<>();
        for (StockRecord record : source) {
            copied.add(record.copy());
        }
        return copied;
    }

    private void rebuildRecordIndex() {
        stockRecordsByProductId.clear();
        for (StockRecord record : stockRecords) {
            stockRecordsByProductId.computeIfAbsent(record.getProductId(), key -> new ArrayList<>())
                    .add(record);
        }
    }
}
